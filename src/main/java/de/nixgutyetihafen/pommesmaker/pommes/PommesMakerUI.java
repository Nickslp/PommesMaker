package de.nixgutyetihafen.pommesmaker.pommes;

import de.nixgutyetihafen.pommesmaker.main.Main;
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

    private static final String INV_TITLE = "§4Pommes Maker";
    private static final HashMap<Inventory, PommesMaker> makerMapping = new HashMap<>();
    private final Inventory inv;
    private final PommesMaker maker;

    public PommesMakerUI(PommesMaker maker) {
        this.maker = Objects.requireNonNull(maker, "PommesMaker cannot be null");
        this.inv = Bukkit.createInventory(maker, InventoryType.FURNACE, INV_TITLE);
        makerMapping.put(inv, maker);
    }

    public static class InterfaceListener implements Listener {

        @EventHandler
        public void triggerPommesMaker(InventoryClickEvent e) {
            // Execute the task later to allow for inventory updates
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                if (!e.getView().getTitle().equals(INV_TITLE)) return;

                Inventory inv = e.getView().getTopInventory();
                PommesMaker maker = makerMapping.get(inv);

                if (maker == null) return;

                ItemStack[] contents = inv.getContents();
                ItemStack smelting = contents[0];
                ItemStack fuel = contents[1];

                // Check for fuel and smelting conditions
                boolean hasFuel = fuel != null && fuel.getType() == Material.LAVA_BUCKET;
                boolean hasPotato = smelting != null && smelting.getType() == Material.POTATO;

                if (hasFuel && hasPotato) {
                    if (!maker.isActive()) {
                        maker.enable();
//                        e.getWhoClicked().sendMessage("§aPommesMaker enabled!"); // Feedback to player
                    }
                } else {
                    if (maker.isActive()) {
                        maker.disable();
//                        e.getWhoClicked().sendMessage("§cPommesMaker disabled!"); // Feedback to player
                    }
                }
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
