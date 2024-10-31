package de.yetihafen.pommesmaker.main;

import de.yetihafen.pommesmaker.listeners.ActivateListener;
import de.yetihafen.pommesmaker.pommes.PommesMaker;
import de.yetihafen.pommesmaker.pommes.PommesMakerUI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main plugin;

    @Override
    public void onEnable() {
        plugin = this;
        PluginManager pm = Bukkit.getPluginManager();

        // Registering event listeners
        pm.registerEvents(new ActivateListener(), this);
        pm.registerEvents(new PommesMakerUI.InterfaceListener(), this);

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

    @Override
    public void onDisable() {
        // Optional: Cleanup or shutdown tasks can be added here
    }

    public static Main getPlugin() {
        return plugin;
    }
}
