package de.yetihafen.pommesmaker.pommes;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.FurnaceInventory;

public class PommesMakerUI {

    private final FurnaceInventory inv;
    private PommesMaker maker;


    public PommesMakerUI(PommesMaker maker) {
        inv = (FurnaceInventory) Bukkit.createInventory(maker, InventoryType.FURNACE, "§6Pommes Maker");
        this.maker = maker;
    }

    public FurnaceInventory getInv() {
        return inv;
    }
}
