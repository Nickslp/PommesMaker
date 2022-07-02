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

        pm.registerEvents(new ActivateListener(), this);
        pm.registerEvents(new PommesMakerUI.InterfaceListener(), this);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for(PommesMaker m : PommesMaker.getActiveMakers()) {
                Location l = m.getLocation();
                l.getWorld().spawnParticle(Particle.DRIPPING_HONEY, l.getBlockX() + 0.5, l.getBlockY() + 0.3, l.getBlockZ() + 0.5, 11, 0.2, 0F, 0.2);
            }
        }, 0, 4);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for(int i = 0; i < PommesMaker.getActiveMakers().size(); i++) {
                PommesMaker m = PommesMaker.getActiveMakers().get(i);
                m.tick();
            }

        }, 0, 0);
    }

    @Override
    public void onDisable() {

    }

    public static Main getPlugin() {
        return plugin;
    }
}
