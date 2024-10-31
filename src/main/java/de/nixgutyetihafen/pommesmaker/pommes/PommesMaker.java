package de.nixgutyetihafen.pommesmaker.pommes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.EndPortalFrame;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import de.nixgutyetihafen.pommesmaker.main.Main;

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
    private static final int CYCLE_DURATION = 200;  // Adjustable cycle duration in ticks


    private PommesMaker(Block block) {
        if (!(block.getBlockData() instanceof EndPortalFrame)) {
            throw new IllegalArgumentException("Block is not an EndPortalFrame");
        }
        this.block = block;
        this.location = block.getLocation();
        this.above = location.clone().add(0, 1, 0);
        this.ui = new PommesMakerUI(this);
    }

    public void explode() {
        EndPortalFrame data = (EndPortalFrame) location.getBlock().getBlockData();
        data.setEye(false);
        block.setType(Material.AIR);
        location.getWorld().createExplosion(location, 1, true, false);
        block.setType(Material.END_PORTAL_FRAME);
        block.setBlockData(data);
        location.getWorld().spawnParticle(Particle.FLAME, above, 300, 0.5, 0.5, 0.5);

        // Check for alarm
        if ((System.currentTimeMillis() - 30 * 1000) > alarmSoundPlayedSince) {
            // Play sound
            location.getWorld().playSound(above, "serversound.alarm", 1, 1);
            alarmSoundPlayedSince = System.currentTimeMillis();
        }
        above.getBlock().setType(Material.FIRE);
        disable();
        this.status = Status.BROKEN;
    }

    public void enable() {
        if (status == Status.ON) return;
        if (status == Status.BROKEN) throw new IllegalStateException("PommesMaker is broken (can't be enabled)");

        EndPortalFrame data = (EndPortalFrame) block.getBlockData();
        data.setEye(true);
        block.setBlockData(data);
        this.status = Status.ON;

        progress = 0;

        // Initialize the inventory
        updateProgressUI();

        activeMakers.add(this);
    }


    public void disable() {
        progress = 0;
        updateProgressUI();

        EndPortalFrame data = (EndPortalFrame) block.getBlockData();
        data.setEye(false);
        block.setBlockData(data);
        activeMakers.remove(this);
        if (status != Status.BROKEN) {
            this.status = Status.OFF;
        }
    }

    public void tick() {
        progress++;
        if (progress >= CYCLE_DURATION) {
            finishCycle();
            return;
        }

        // Update the inventory UI for all viewers
        updateProgressUI();
        sendFurnaceAnimationPacket();
    }

    private void updateProgressUI() {
        getInventory().getViewers().forEach(viewer -> {
            if (viewer.getOpenInventory().getTopInventory().equals(getInventory())) {
                viewer.setWindowProperty(InventoryView.Property.COOK_TIME, (short) progress);
            }
        });
    }

    private boolean debugEnabled = false; // Default is false

    private void sendFurnaceAnimationPacket() {
        for (HumanEntity viewer : getInventory().getViewers()) {
            // Check if the viewer is a Player
            if (!(viewer instanceof Player)) {
                continue; // Skip if not a player
            }

            Player player = (Player) viewer; // Cast to Player

            if (!player.getOpenInventory().getTopInventory().equals(getInventory())) {
                continue; // Skip if not viewing the correct inventory
            }

            int windowId = player.getOpenInventory().getTopInventory().hashCode(); // Use hashCode() for a temporary workaround

            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.WINDOW_DATA);

            // Debugging
            if (Main.globalDebugMode) {
                Bukkit.getLogger().info("Preparing to send WINDOW_DATA packet with window ID: " + windowId);
                Bukkit.getLogger().info("Cook time set to: " + ((progress * 200) / CYCLE_DURATION));
            }

            try {
                // Prepare the packet
                packet.getIntegers().write(0, windowId); // Window ID
                packet.getIntegers().write(1, 0);        // Property ID (e.g., cook time)
                packet.getIntegers().write(2, (int) ((progress * 200) / CYCLE_DURATION)); // Cook time value

                // Log the expected structure
                if (Main.globalDebugMode) {
                    Bukkit.getLogger().info("Expected fields: " + packet.getIntegers().size() + " integers");
                }

                // Send the packet
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                if (Main.globalDebugMode) {
                    Bukkit.getLogger().info("Successfully sent furnace animation packet to: " + player.getName());
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("Failed to send packet to " + player.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }























    private ItemStack createProgressItem(int progress) {
        ItemStack progressItem = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE); // Change as needed
        ItemMeta meta = progressItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7Progress: " + progress + "/" + CYCLE_DURATION);
            progressItem.setItemMeta(meta);
        }
        return progressItem;
    }


    private void finishCycle() {
        Inventory fi = ui.getInv();
        ItemStack[] contents = fi.getContents();

        ItemStack smelting = contents[0];  // Slot 0: input item (potato)
        ItemStack result = contents[2];     // Slot 2: output item (pommes)

        int potatoes = (smelting != null) ? smelting.getAmount() : 0;
        int pommes = (result != null) ? result.getAmount() : 0;

        if (potatoes > 0 && pommes > 0) {
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

        progress = 0;
        updateProgressUI();
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
            if (maker.getLocation().equals(loc)) {
                for (ItemStack item : maker.getInventory().getContents()) {
                    if (item != null) loc.getWorld().dropItem(loc, item);
                }
                maker.disable();
                maker.delete();
            }
            i++;
        }
    }

    public static PommesMaker getFromLocation(Location loc) {
        for (PommesMaker maker : makers) {
            if (maker.getLocation().equals(loc)) {
                return maker;
            }
        }
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
