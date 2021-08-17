package de.yetihafen.pommesmaker.listeners;

import de.yetihafen.pommesmaker.main.Main;
import de.yetihafen.pommesmaker.pommes.PommesMaker;
import de.yetihafen.pommesmaker.pommes.PommesMakerUI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.FurnaceInventory;

import java.util.HashMap;
import java.util.Map;


public class ActivateListener implements Listener {

    Map<Location, Long> activeExplosions = new HashMap<>();
    int expTriggerCount = 0;

    @EventHandler
    public void detectWater(BlockPhysicsEvent e) {
        // return in different trigger case
        if(e.getBlock().getType() != Material.END_PORTAL_FRAME) return;
        if(e.getSourceBlock().getType() != Material.WATER) return;

        Block frame = e.getBlock();
        Block above = frame.getWorld().getBlockAt(frame.getLocation().clone().add(0D,1D,0D));

        if(above.getType() != Material.WATER) return;

        // clear cached locations
        if(expTriggerCount > 20)
            activeExplosions.clear();


        // check if location is already exploding
        if(activeExplosions.containsKey(above.getLocation())) {
            long started = activeExplosions.get(above.getLocation());

            if(System.currentTimeMillis() - 5000 < started) {
                // explosion happened less than 5 sec ago
                return;
            }
        }

        // create explosion
        PommesMaker.getFromLocation(frame.getLocation()).explode();
        expTriggerCount++;

        // save location
        activeExplosions.put(above.getLocation(), System.currentTimeMillis());
    }


    @EventHandler
    public void usePommesMaker(PlayerInteractEvent e) {
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(e.getHand() == EquipmentSlot.OFF_HAND) return;
        if(e.getClickedBlock().getType() != Material.END_PORTAL_FRAME) return;

        e.setCancelled(true);

        PommesMaker maker = PommesMaker.getFromLocation(e.getClickedBlock().getLocation());



        if(maker.getStatus() == PommesMaker.Status.BROKEN) return;

        e.getPlayer().openInventory(maker.getUi().getInv());
    }




    @EventHandler
    public void onBottleThrow(ProjectileHitEvent e){
        Projectile projectile = e.getEntity();
        if(projectile instanceof ThrownPotion){
            if(e.getHitBlock().getType().equals(Material.END_PORTAL_FRAME)){
                Block under = e.getHitBlock();
                Location loc = under.getLocation();
                PommesMaker.getFromLocation(loc).explode();
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if(e.getBlock().getType() != Material.END_PORTAL_FRAME) return;
        PommesMaker.deleteAt(e.getBlock().getLocation());
    }

    public Map<Location, Long> getActiveExplosions() {
        return activeExplosions;
    }
}
