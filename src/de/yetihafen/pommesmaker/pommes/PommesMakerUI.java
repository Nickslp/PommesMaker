package de.yetihafen.pommesmaker.pommes;

import de.yetihafen.pommesmaker.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Objects;

public class PommesMakerUI {

    private static final String INV_TITLE = "§6Pommes Maker";
    private static final HashMap<Inventory, PommesMaker> makerMapping = new HashMap<>();
    private final Inventory inv;
    private final PommesMaker maker;


    public PommesMakerUI(PommesMaker maker) {
        inv = Bukkit.createInventory(maker, InventoryType.FURNACE, INV_TITLE);
        this.maker = Objects.requireNonNull(maker);
        makerMapping.put(inv, maker);
    }

    public static class InterfaceListener implements Listener {

        @EventHandler
        public void triggerPommesMaker(InventoryClickEvent e) {

            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {

                if(!e.getView().getTitle().equals(INV_TITLE)) return;

                Inventory inv = e.getView().getTopInventory();

                PommesMaker maker = makerMapping.get(inv);

                ItemStack[] contents = inv.getContents();

                ItemStack smelting = contents[0];
                ItemStack fuel = contents[1];
                ItemStack result = contents[2];


                if(maker == null) return;
                if(fuel == null || fuel.getType() != Material.LAVA_BUCKET) {
                    if(maker.isActive()) maker.disable();
                    return;
                }

                if(smelting == null || smelting.getType() != Material.POTATO) {
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

    public Inventory getInv() {
        return inv;
    }
}
