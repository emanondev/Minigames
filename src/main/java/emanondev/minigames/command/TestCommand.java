package emanondev.minigames.command;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import emanondev.core.command.CoreCommand;
import emanondev.minigames.Minigames;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TestCommand extends CoreCommand {


    public TestCommand() {
        super("test2", Minigames.get(), new Permission("aaa.bbb"));
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String s, String @NotNull [] args) {
        Player p = (Player) sender;
        BoundingBox box = BoundingBox.of(p.getLocation(), p.getLocation());
        box.expand(10, 10, 10);

        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (counter >= 30)
                    this.cancel();
                counter++;
                spawnParticleBoxFaces(p, counter, Particle.FLAME, box);
            }
        }.runTaskTimer(getPlugin(), 20L, 20L);

    }

    @Override
    public @Nullable List<String> onComplete(@NotNull CommandSender sender, @NotNull String s, String @NotNull [] args, @Nullable Location location) {
        return null;
    }


    protected void spawnParticle(Player p, Particle particle, double x, double y, double z) {
        spawnParticle(p, particle, x, y, z, 1, null);
    }

    protected void spawnParticle(Player p, Particle particle, double x, double y, double z, int count) {
        spawnParticle(p, particle, x, y, z, count, null);
    }

    protected void spawnParticle(Player p, Particle particle, double x, double y, double z, Object data) {
        spawnParticle(p, particle, x, y, z, 0, data);
    }

    protected void spawnParticle(Player p, Particle particle, double x, double y, double z, int count, Object data) {
        p.spawnParticle(particle, x, y, z, count, 0, 0, 0, 0, data);
    }


    protected void spawnParticleCircle(Player p, Particle particle, double x, double y, double z, double radius, boolean rotateHalf) {
        spawnParticleCircle(p, particle, x, y, z, radius, rotateHalf, null);
    }

    protected void spawnParticleCircle(Player p, Particle particle, double x, double y, double z, double radius, boolean rotateHalf, Object data) {
        Location l = p.getLocation();
        if (x > l.getBlockX() - RADIUS && x < l.getBlockX() + RADIUS && z > l.getBlockZ() - RADIUS && z < l.getBlockZ() + RADIUS)
            for (int i = 0; i < 8; i++) {
                double degree = ((rotateHalf ? 0 : 0.5) + i) * Math.PI / 4;
                double xOffset = x + radius * Math.sin(degree);
                double zOffset = z + radius * Math.cos(degree);
                spawnParticle(p, particle, xOffset, y + 0.05, zOffset, 1, data);
            }
    }

    protected void spawnParticleBoxEdges(Player p, Particle particle, BoundingBox box) {
        spawnParticleBoxEdges(p, particle, box, null);
    }


    protected void spawnParticleBoxFaces(Player p, int tick, Particle particle, BoundingBox box) {
        spawnParticleBoxFaces(p, tick, particle, box, null);
    }

    protected boolean spawnParticleWorldEditRegionEdges(Player p, Particle particle) {
        return spawnParticleWorldEditRegionEdges(p, particle, null);
    }


    protected void spawnParticleBoxEdges(Player p, Particle particle, BoundingBox box, Object data) {
        markEdges(p, particle, box.getMin(), box.getMax().add(new Vector(-1, -1, -1)), data);
    }


    protected void spawnParticleBoxFaces(Player p, int tick, Particle particle, BoundingBox box, Object data) {
        markFaces(p, tick, particle, box.getMin(), box.getMax().add(new Vector(-1, -1, -1)), data);
    }

    protected boolean spawnParticleWorldEditRegionEdges(Player p, Particle particle, Object data) {
        try {
            Region sel = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(p))
                    .getSelection(BukkitAdapter.adapt(p.getWorld()));
            markEdges(p, particle, new Vector(sel.getMinimumPoint().getX(), sel.getMinimumPoint().getY(),
                    sel.getMinimumPoint().getZ()), new Vector(sel.getMaximumPoint().getX(), sel.getMaximumPoint().getY(),
                    sel.getMaximumPoint().getZ()), data);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void markEdges(Player p, Particle particle, Vector min, Vector max, Object data) {
        Location l = p.getLocation();
        int xMin = Math.max(l.getBlockX() - RADIUS, min.getBlockX()), xMax = Math.min(l.getBlockX() + RADIUS, max.getBlockX());
        int zMin = Math.max(l.getBlockZ() - RADIUS, min.getBlockZ()), zMax = Math.min(l.getBlockZ() + RADIUS, max.getBlockZ());
        for (int i = xMin; i <= xMax; i++) {
            spawnParticle(p, particle, i, min.getY(), min.getZ(), 1, data);
            spawnParticle(p, particle, i, max.getY() + 1, min.getZ(), 1, data);
            spawnParticle(p, particle, i, min.getY(), max.getZ() + 1, 1, data);
            spawnParticle(p, particle, i, max.getY() + 1, max.getZ() + 1, 1, data);
        }
        for (int i = min.getBlockY(); i <= max.getBlockY(); i++) {
            spawnParticle(p, particle, min.getX(), i, min.getZ(), 1, data);
            spawnParticle(p, particle, max.getX() + 1, i, min.getZ(), 1, data);
            spawnParticle(p, particle, min.getX(), i, max.getZ() + 1, 1, data);
            spawnParticle(p, particle, max.getX() + 1, i, max.getZ() + 1, 1, data);
        }
        for (int i = zMin; i <= zMax; i++) {
            spawnParticle(p, particle, min.getX(), min.getY(), i, 1, data);
            spawnParticle(p, particle, max.getX() + 1, min.getY(), i, 1, data);
            spawnParticle(p, particle, min.getX(), max.getY() + 1, i, 1, data);
            spawnParticle(p, particle, max.getX() + 1, max.getY() + 1, i, 1, data);
        }
    }

    private static final int RATEO = 7;
    private static final int RADIUS = 100;

    private void markFaces(Player p, int val, Particle particle, Vector min, Vector max, Object data) {
        Location l = p.getLocation();
        int xMin = Math.max(l.getBlockX() - RADIUS, min.getBlockX()), xMax = Math.min(l.getBlockX() + RADIUS, max.getBlockX());
        int zMin = Math.max(l.getBlockZ() - RADIUS, min.getBlockZ()), zMax = Math.min(l.getBlockZ() + RADIUS, max.getBlockZ());
        //logInfo("X: "+xMin+" "+xMax+"   Z: "+zMin+" "+zMax);
        for (int x = xMin; x <= xMax; x++)
            for (int z = zMin; z <= zMax; z++) {
                if (Math.abs(x + min.getBlockY() + z) % RATEO == val % RATEO)
                    spawnParticle(p, particle, x, min.getBlockY(), z, data);
                if (Math.abs(x + max.getBlockY() + 1 + z) % RATEO == val % RATEO)
                    spawnParticle(p, particle, x, max.getBlockY() + 1, z, data);
            }
        for (int x = xMin; x <= xMax; x++)
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                if (Math.abs(x + y + min.getBlockZ()) % RATEO == val % RATEO)
                    spawnParticle(p, particle, x, y, min.getBlockZ(), data);
                if (Math.abs(x + y + max.getBlockZ() + 1) % RATEO == val % RATEO)
                    spawnParticle(p, particle, x, y, max.getBlockZ() + 1, data);
            }
        for (int z = zMin; z <= zMax; z++)
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                if (Math.abs(min.getBlockX() + y + z) % RATEO == val % RATEO)
                    spawnParticle(p, particle, min.getBlockX(), y, z, data);
                if (Math.abs(max.getBlockX() + 1 + y + z) % RATEO == val % RATEO)
                    spawnParticle(p, particle, max.getBlockX() + 1, y, z, data);
            }
    }
}
