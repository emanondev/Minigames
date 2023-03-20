package emanondev.minigames.generic;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import emanondev.core.CorePlugin;
import emanondev.core.UtilsString;
import emanondev.core.message.DMessage;
import emanondev.minigames.Minigames;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public abstract class MArenaBuilder {

    private final UUID builder;
    private final BukkitTask timerRunnable;
    private final String id;
    private final BossBar bar = Bukkit.createBossBar(" ", BarColor.RED, BarStyle.SOLID);
    private final String label;
    private final CorePlugin plugin;
    private int phase = 1;

    public String getLabel() {
        return label;
    }

    public CorePlugin getPlugin() {
        return plugin;
    }

    public MArenaBuilder(@NotNull UUID user, @NotNull String id, @NotNull String label, @NotNull CorePlugin plugin) {
        this.builder = user;
        if (!UtilsString.isLowcasedValidID(id))
            throw new IllegalArgumentException();
        this.id = id.toLowerCase();
        this.label = label;
        this.plugin = plugin;
        timerRunnable = Bukkit.getScheduler().runTaskTimer(Minigames.get(), () -> {
            Player p = getBuilder();
            if (p != null && p.isOnline() && p.isValid()) {
                //TODO message repeat, bossbar, actionbar
                bar.addPlayer(p);
                onTimerCall();
            }
        }, 10, 5);
        setPhaseRaw(1);
    }

    protected int getPhase() {
        return phase;
    }

    protected void setPhaseRaw(int phase) {
        this.phase = phase;
        bar.setTitle(getCurrentBossBarMessage().toLegacy());
        onPhaseStart();
        Player p = getBuilder();
        if (p != null) {
            bar.addPlayer(p);
            getRepeatedMessage().send();
        }
    }

    protected abstract void onPhaseStart();

    public @NotNull String getId() {
        return id;
    }

    public @NotNull UUID getUser() {
        return builder;
    }

    public @Nullable Player getBuilder() {
        return Bukkit.getPlayer(builder);
    }

    public boolean isBuilder(@NotNull OfflinePlayer player) {
        return isBuilder(player.getUniqueId());
    }

    public boolean isBuilder(@NotNull UUID uuid) {
        return builder.equals(uuid);
    }

    public abstract @NotNull DMessage getCurrentBossBarMessage();

    public abstract @NotNull DMessage getRepeatedMessage();

    public abstract void handleCommand(Player sender, String label, @NotNull String[] args);

    public abstract List<String> handleComplete(@NotNull String[] args);

    public void abort() {
        timerRunnable.cancel();
        bar.removeAll();
    }

    public abstract MArena build();

    /**
     * Timer Runnable call
     * Note: this funcion is called 4 times each second (every 5 game ticks)
     */
    public abstract void onTimerCall(/*int timerTick*/);


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

    private static final int RATEO = 5;
    private static final int RADIUS = 100;

    private void markFaces(Player p, int val, Particle particle, Vector min, Vector max, Object data) {
        Location l = p.getLocation();
        int xMin = Math.max(l.getBlockX() - RADIUS, min.getBlockX()), xMax = Math.min(l.getBlockX() + RADIUS, max.getBlockX());
        int zMin = Math.max(l.getBlockZ() - RADIUS, min.getBlockZ()), zMax = Math.min(l.getBlockZ() + RADIUS, max.getBlockZ());
        for (int x = xMin; x <= xMax; x++)
            for (int z = zMin; z <= zMax; z++) {
                if (Math.abs(x + min.getY() + z) % RATEO == val)
                    spawnParticle(p, particle, x, min.getY(), z, data);
                if (Math.abs(x + max.getY() + 1 + z) % RATEO == val)
                    spawnParticle(p, particle, x, max.getY() + 1, z, data);
            }
        for (int x = xMin; x <= xMax; x++)
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                if (Math.abs(x + y + min.getZ()) % RATEO == val)
                    spawnParticle(p, particle, x, y, min.getZ(), data);
                if (Math.abs(x + y + max.getZ() + 1) % RATEO == val)
                    spawnParticle(p, particle, x, y, max.getZ() + 1, data);
            }
        for (int z = zMin; z <= zMax; z++)
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                if (Math.abs(min.getX() + y + z) % RATEO == val)
                    spawnParticle(p, particle, min.getX(), y, z, data);
                if (Math.abs(max.getX() + 1 + y + z) % RATEO == val)
                    spawnParticle(p, particle, max.getX() + 1, y, z, data);
            }
    }

    protected void sendMsg(CommandSender target, String path, String... holders) {
        new DMessage(getPlugin(), target).appendLang(path, holders).send();
    }

    protected void sendMsgList(CommandSender target, String path, String... holders) {
        new DMessage(getPlugin(), target).appendLangList(path, holders).send();
    }
}
