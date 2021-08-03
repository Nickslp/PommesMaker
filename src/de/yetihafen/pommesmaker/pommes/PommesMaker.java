package de.yetihafen.pommesmaker.pommes;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.EndPortalFrame;

import java.util.ArrayList;
import java.util.Random;


public class PommesMaker {

    private static final ArrayList<PommesMaker> makers = new ArrayList<>();
    private static final ArrayList<PommesMaker> activeMakers = new ArrayList<>();

    private final Location location;
    private final Location above;
    private final Block block;
    private Status status;
    private long activeSince = -1;

    private PommesMaker(Block block) {
        if(!(block.getBlockData() instanceof EndPortalFrame)) throw new IllegalArgumentException("Block is no EndPortalFrame");
        this.block = block;
        this.location = block.getLocation();
        this.above = location.clone().add(0,1,0);
    }

    private PommesMaker(Block b, Status status) {
        this(b);
        this.status = status;
    }

    public void explode() {
        EndPortalFrame data = (EndPortalFrame) location.getBlock().getBlockData();
        data.setEye(false);
        block.setType(Material.AIR);
        location.getWorld().createExplosion(location, 1, true,false);
        block.setType(Material.END_PORTAL_FRAME);
        block.setBlockData(data);
        location.getWorld().spawnParticle(Particle.FLAME, above,300, 0.5, 0.5, 0.5);
        location.getWorld().playSound(above,"serversound.alarm", 8,10);
        above.getBlock().setType(Material.FIRE);
        disable();
        this.status = Status.BROKEN;
    }

    public void enable() {
        if(status == Status.BROKEN) throw new IllegalStateException("PommesMaker is broken (can't be enabled)");
        EndPortalFrame data = (EndPortalFrame) block.getBlockData();
        data.setEye(true);
        block.setBlockData(data);
        this.status = Status.ON;
        activeSince = System.currentTimeMillis();
        activeMakers.add(this);
    }

    public void disable() {
        EndPortalFrame data = (EndPortalFrame) block.getBlockData();
        data.setEye(false);
        block.setBlockData(data);
        activeMakers.remove(this);
        activeSince = -1;
        if(status != Status.BROKEN)
            this.status = Status.OFF;
    }

    public void tick() {
        Random r = new Random();
        int res = r.nextInt(100) + 1;
        if(res < 2) explode();
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
        for(int i = 0; i < makers.size(); i++) {
            PommesMaker maker = makers.get(i);
            if(maker.getLocation().equals(loc)) {
                maker.disable();
                maker.delete();
            }
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

    public enum Status {
        OFF, ON, BROKEN
    }
}
