package de.yetihafen.pommesmaker.pommes;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.EndPortalFrame;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
public class PommesMaker implements InventoryHolder {

    private static final ArrayList<PommesMaker> makers = new ArrayList<>();
    private static final ArrayList<PommesMaker> activeMakers = new ArrayList<>();
    private final Location location;
    private final Location above;
    private final Block block;
    private Status status;
    private long alarmSoundPlayedSince = -1;
    private final PommesMakerUI ui;
    private int progress = 0;

    private PommesMaker(Block block) {
        if(!(block.getBlockData() instanceof EndPortalFrame)) throw new IllegalArgumentException("Block is no EndPortalFrame");
        this.block = block;
        this.location = block.getLocation();
        this.above = location.clone().add(0,1,0);
        this.ui = new PommesMakerUI(this);
    }

    public void explode() {
        EndPortalFrame data = (EndPortalFrame) location.getBlock().getBlockData();
        data.setEye(false);
        block.setType(Material.AIR);
        location.getWorld().createExplosion(location, 1, true,false);
        block.setType(Material.END_PORTAL_FRAME);
        block.setBlockData(data);
        location.getWorld().spawnParticle(Particle.FLAME, above,300, 0.5, 0.5, 0.5);

        // check for alarm
        if((System.currentTimeMillis() - 30 * 1000) > alarmSoundPlayedSince) {
            // play sound
            location.getWorld().playSound(above,"serversound.alarm", 1,1);
            alarmSoundPlayedSince = System.currentTimeMillis();
        }
        above.getBlock().setType(Material.FIRE);
        disable();
        this.status = Status.BROKEN;
    }

    public void enable() {
        if(status == Status.ON) return;
        if(status == Status.BROKEN) throw new IllegalStateException("PommesMaker is broken (can't be enabled)");
        EndPortalFrame data = (EndPortalFrame) block.getBlockData();
        data.setEye(true);
        block.setBlockData(data);
        this.status = Status.ON;
        activeMakers.add(this);
    }

    public void disable() {
        progress = 0;
        // getInventory().getViewers().forEach(v -> v.setWindowProperty(InventoryView.Property.COOK_TIME, progress));
        EndPortalFrame data = (EndPortalFrame) block.getBlockData();
        data.setEye(false);
        block.setBlockData(data);
        activeMakers.remove(this);
        if(status != Status.BROKEN)
            this.status = Status.OFF;
    }

    public void tick() {
        progress++;
        if (progress > 200) progress = 0;
        if (progress == 200) {
            finishCycle();
            return;
        }
        //getInventory().getViewers().forEach(v -> v.setWindowProperty(InventoryView.Property.COOK_TIME, progress));

    }

    private void finishCycle() {

        Inventory fi = ui.getInv();

        ItemStack[] contents = fi.getContents();

        ItemStack smelting = contents[0];
        ItemStack result = contents[2];

        int potatoes = smelting == null ? 0 : smelting.getAmount();
        int pommes = result == null ? 0 : result.getAmount();

        if(potatoes > 0 && pommes > 0) {
            smelting.setAmount(smelting.getAmount() - 1);
            result.setAmount(result.getAmount() + 1);
        } else if (potatoes > 0) {
            ItemStack item = new ItemStack(Material.BAKED_POTATO);
            ItemMeta imeta = item.getItemMeta();
            imeta.setDisplayName("§6Pommes");
            item.setItemMeta(imeta);
            contents[2] = item;
            smelting.setAmount(smelting.getAmount() - 1);
        }
        fi.setContents(contents);


    }

    public void repair() {
        this.status = Status.OFF;
        activeMakers.remove(this);
    }

    private void delete() {
        disable();
        makers.remove(this);
    }

    public static void deleteAt(Location loc) {

        int i = 0;
        while (i < makers.size()) {
            PommesMaker maker = makers.get(i);
            if(maker.getLocation().equals(loc)) {
                for(ItemStack item : maker.getInventory().getContents())
                    if(item != null) loc.getWorld().dropItem(loc, item);
                maker.disable();
                maker.delete();
            }
            i++;
        }
    }

    public static PommesMaker getFromLocation(Location loc) {
        for (PommesMaker maker : makers)
            if (maker.getLocation().equals(loc))
                return maker;
        try {
            PommesMaker maker = new PommesMaker(loc.getBlock());
            makers.add(maker);
            return maker;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isActive() {
        return status == Status.ON;
    }

    public Location getLocation() {
        return block.getLocation();
    }

    public Status getStatus() {
        return status;
    }

    public Block getBlock() {
        return block;
    }

    public static ArrayList<PommesMaker> getActiveMakers() {
        return activeMakers;
    }

    public static ArrayList<PommesMaker> getMakers() {
        return makers;
    }

    public PommesMakerUI getUi() {
        return ui;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return ui.getInv();
    }

    public enum Status {
        OFF, ON, BROKEN
    }
}
