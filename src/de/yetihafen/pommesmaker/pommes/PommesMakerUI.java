package de.yetihafen.pommesmaker.pommes;

import de.yetihafen.pommesmaker.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.FurnaceInventory;

import java.util.HashMap;
import java.util.Objects;

public class PommesMakerUI {

    private static final String INV_TITLE = "§6Pommes Maker";
    private static final HashMap<FurnaceInventory, PommesMaker> makerMapping = new HashMap<>();
    private final FurnaceInventory inv;
    private final PommesMaker maker;


    public PommesMakerUI(PommesMaker maker) {
        inv = (FurnaceInventory) Bukkit.createInventory(maker, InventoryType.FURNACE, INV_TITLE);
        this.maker = Objects.requireNonNull(maker);
        makerMapping.put(inv, maker);
    }

    public static class InterfaceListener implements Listener {

        @EventHandler
        public void triggerPommesMaker(InventoryClickEvent e) {

            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {

                if(!e.getView().getTitle().equals(INV_TITLE)) return;


                FurnaceInventory inv = (FurnaceInventory) e.getView().getTopInventory();
                PommesMaker maker = makerMapping.get(inv);
                if(maker == null) return;
                if(inv.getFuel() == null || inv.getFuel().getType() != Material.LAVA_BUCKET) {
                    if(maker.isActive()) maker.disable();
                    return;
                }

                if(inv.getSmelting() == null || inv.getSmelting().getType() != Material.POTATO) {
                    if(maker.isActive()) maker.disable();
                    return;
                }
                maker.enable();
            }, 1);

        }
    }

    public PommesMaker getMaker() {
        return maker;
    }

    public FurnaceInventory getInv() {
        return inv;
    }
}
