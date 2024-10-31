package de.nixgutyetihafen.pommesmaker.main;

import de.nixgutyetihafen.pommesmaker.commands.DebugTabCompleter;
import de.nixgutyetihafen.pommesmaker.listeners.ActivateListener;
import de.nixgutyetihafen.pommesmaker.pommes.PommesMaker;
import de.nixgutyetihafen.pommesmaker.pommes.PommesMakerUI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.PacketType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main extends JavaPlugin {

    private static Main plugin;
    private final Map<UUID, Integer> playerWindowIds = new HashMap<>(); // Store window IDs

    public static boolean globalDebugMode = false; // Make this public and static


    @Override
    public void onEnable() {
        plugin = this;
        PluginManager pm = Bukkit.getPluginManager();
        this.getCommand("setdebug").setExecutor(this); // Register the command
        this.getCommand("setdebug").setTabCompleter(new DebugTabCompleter()); // Register the tab completer

        // Registering event listeners
        pm.registerEvents(new ActivateListener(), this);
        pm.registerEvents(new PommesMakerUI.InterfaceListener(), this);

        // Register ProtocolLib packet listener
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Play.Server.OPEN_WINDOW) {
            @Override
            public void onPacketSending(PacketEvent event) {
                int windowId = event.getPacket().getIntegers().read(0);
                playerWindowIds.put(event.getPlayer().getUniqueId(), windowId); // Store the window ID
            }
        });

        // Particle effect task
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (PommesMaker m : PommesMaker.getActiveMakers()) {
                Location l = m.getLocation();
                l.getWorld().spawnParticle(Particle.DRIPPING_HONEY,
                        l.getBlockX() + 0.5,
                        l.getBlockY() + 0.3,
                        l.getBlockZ() + 0.5,
                        11, 0.2, 0F, 0.2);
            }
        }, 0, 4);

        // Smelting progress tick task
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (PommesMaker m : PommesMaker.getActiveMakers()) {
                m.tick();
            }
        }, 0, 1);
    }

    public boolean isGlobalDebugMode() {
        return globalDebugMode;
    }

    public void setGlobalDebugMode(boolean debugMode) {
        globalDebugMode = debugMode;
        Bukkit.getLogger().info("Global debug mode is now " + (debugMode ? "enabled" : "disabled"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("setdebug")) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("on")) {
                    setGlobalDebugMode(true);
                    sender.sendMessage("Global debug mode enabled.");
                } else if (args[0].equalsIgnoreCase("off")) {
                    setGlobalDebugMode(false);
                    sender.sendMessage("Global debug mode disabled.");
                } else {
                    sender.sendMessage("Usage: /setdebug <on|off>");
                }
            } else {
                sender.sendMessage("Usage: /setdebug <on|off>");
            }
            return true; // Command handled successfully
        }
        return false; // Command not recognized
    }



    @Override
    public void onDisable() {
        // Optional: Cleanup or shutdown tasks can be added here
    }

    public static Main getPlugin() {
        return plugin;
    }

    public Integer getWindowIdForPlayer(Player player) {
        return playerWindowIds.get(player.getUniqueId()); // Retrieve the window ID for the player
    }
}
