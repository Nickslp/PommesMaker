package de.yetihafen.pommesmaker.main;

import de.yetihafen.pommesmaker.listeners.ActivateListener;
import de.yetihafen.pommesmaker.pommes.PommesMaker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class Main extends JavaPlugin {


    ActivateListener l;

    @Override
    public void onEnable() {
        PluginManager pm = Bukkit.getPluginManager();
        l = new ActivateListener();
        pm.registerEvents(l, this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for(PommesMaker m : PommesMaker.getActiveMakers()) {
                Location l = m.getLocation();
                l.getWorld().spawnParticle(Particle.DRIPPING_HONEY, l.getBlockX() + 0.5, l.getBlockY() + 0.3, l.getBlockZ() + 0.5, 11, 0.2, 0F, 0.2);
            }
        }, 0, 0);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for(int i = 0; i < PommesMaker.getActiveMakers().size(); i++) {
                PommesMaker m = PommesMaker.getActiveMakers().get(i);
                m.tick();
            }

        }, 0, 20 * 5);
    }

    @Override
    public void onDisable() {

    }
}
