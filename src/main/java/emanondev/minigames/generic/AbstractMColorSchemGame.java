package emanondev.minigames.generic;

import com.sk89q.worldedit.math.BlockVector3;
import emanondev.core.util.WorldEditUtility;
import emanondev.minigames.Minigames;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractMColorSchemGame<T extends ColoredTeam, A extends MSchemArena & MColorableTeamArena, O extends MOption> extends AbstractMGame<T, A, O> implements MSchemGame<T, A, O> {

    private final boolean isSquared; // count as squared even if the area is pretty small
    private final List<WorldBorder> borders = new ArrayList<>();
    private final List<BoundingBox> cacheArea = new ArrayList<>();
    private final HashMap<Player, Integer> bordersId = new HashMap<>();
    private final BoundingBox hiddenBorderArea;
    private final Map<DyeColor, T> teams = new EnumMap<>(DyeColor.class);
    private final HashSet<Chunk> clearedEntitiesBefore = new HashSet<>();

    private BoundingBox boxCache;

    private static final double MARGIN_WORLD_BORDER_RESTRICTION = 0.25;
    private static final int SEE_WORLD_BORDER_DISTANCE = 6;
    private static final int CLEAR_MARGIN = 128;

    private int getClosestBorder(Vector loc) {
        BoundingBox box = getBoundingBox();
        double d0 = loc.distanceSquared(new Vector(box.getMinX(), loc.getY(), box.getMinZ()));
        double d1 = loc.distanceSquared(new Vector(box.getMinX(), loc.getY(), box.getMaxZ()));
        double d2 = loc.distanceSquared(new Vector(box.getMaxX(), loc.getY(), box.getMinZ()));
        double d3 = loc.distanceSquared(new Vector(box.getMaxX(), loc.getY(), box.getMaxZ()));
        if (d0 < d1 && d0 < d2 && d0 < d3)
            return 0;
        if (d1 < d2 && d1 < d3)
            return 1;
        if (d2 < d3)
            return 2;
        return 3;
    }

    private void sendUpdateBorder(Player player) {
        Integer id = bordersId.get(player);
        if (hiddenBorderArea.contains(player.getLocation().toVector()))
            if (id == null)
                return;
            else {
                player.setWorldBorder(null);
                bordersId.remove(player);
                return;
            }
        if (id == null) {
            if (isSquared) {
                bordersId.put(player, 0);
                player.setWorldBorder(borders.get(0));
                return;
            }
            int closest = getClosestBorder(player.getLocation().toVector());
            bordersId.put(player, closest);
            player.setWorldBorder(borders.get(closest));
            return;
        }
        if (isSquared)
            return;
        int closest = getClosestBorder(player.getLocation().toVector());
        if (cacheArea.get(closest).contains(player.getLocation().toVector()))
            return;
        bordersId.put(player, closest);
        player.setWorldBorder(borders.get(closest));
    }

    public AbstractMColorSchemGame(@NotNull Map<String, Object> map) {
        super(map);
        for (DyeColor color : getArena().getColors()) {
            teams.put(color, craftTeam(color));
            getScoreboard().registerNewTeam(color.name());
        }

        BoundingBox box = getBoundingBox();
        isSquared = box.getWidthX() == box.getWidthZ() || Math.max(box.getWidthX(), box.getWidthZ()) < 10;

        double size = Math.max(box.getWidthX(), box.getWidthZ()) - 2 * MARGIN_WORLD_BORDER_RESTRICTION; //sightly smaller
        if (!isSquared) {
            for (int i = 0; i < 4; i++) {
                WorldBorder wb = Bukkit.createWorldBorder();
                wb.setWarningDistance(1);
                wb.setSize(size);
                wb.setCenter((i < 2 ? box.getMinX() : box.getMaxX()) + size / 2 + MARGIN_WORLD_BORDER_RESTRICTION, (i % 2 == 0 ? box.getMinZ() : box.getMaxZ()) + size / 2 + MARGIN_WORLD_BORDER_RESTRICTION);
                borders.add(wb);
                cacheArea.add(new BoundingBox(i < 2 ? box.getMinX() : box.getMaxX()
                        , Integer.MIN_VALUE, i % 2 == 0 ? box.getMinZ() : box.getMaxZ()
                        , i >= 2 ? box.getMinX() : box.getMaxX()
                        , Integer.MAX_VALUE
                        , i % 2 != 0 ? box.getMinZ() : box.getMaxZ()));
                //MessageUtil.debug(getId() + " box " + wb.getCenter().getX() + " " + wb.getCenter().getZ() + " size " + wb.getSize());

            }
            WorldBorder wb = Bukkit.createWorldBorder();
            wb.setWarningDistance(1);
            wb.setSize(size);
            wb.setCenter(box.getCenterX() + size / 2, box.getCenterZ() + size / 2);
        } else {
            WorldBorder wb = Bukkit.createWorldBorder();
            wb.setWarningDistance(1);
            wb.setSize(size);
            wb.setCenter(box.getMinX() + size / 2, box.getMinZ() + size / 2);
            borders.add(wb);
        }
        hiddenBorderArea = box.clone().expand(-SEE_WORLD_BORDER_DISTANCE, 999, -SEE_WORLD_BORDER_DISTANCE);
    }

    @Override
    public void gameInitialize() {
        super.gameInitialize();
        BoundingBox box = getBoundingBox();
        BoundingBox above = new BoundingBox(box.getMinX(), box.getMaxY(), box.getMinZ(),
                box.getMaxX(), getWorld().getMaxHeight(), box.getMaxZ());
        BoundingBox below = new BoundingBox(box.getMinX(), getWorld().getMinHeight(), box.getMinZ(),
                box.getMaxX(), box.getMinY() - 1, box.getMaxZ());
        above.expand(CLEAR_MARGIN, 0, CLEAR_MARGIN);
        below.expand(CLEAR_MARGIN, 0, CLEAR_MARGIN);
        WorldEditUtility.pasteAir(above, getWorld(), true, Minigames.get());
        WorldEditUtility.pasteAir(below, getWorld(), true, Minigames.get());
        BoundingBox side1 = new BoundingBox(box.getMinX(), box.getMinY(), box.getMinZ() - 1,
                box.getMaxX(), box.getMaxY(), box.getMinZ() - CLEAR_MARGIN);
        BoundingBox side2 = new BoundingBox(box.getMinX(), box.getMinY(), box.getMaxZ(),
                box.getMaxX(), box.getMaxY(), box.getMaxZ() + CLEAR_MARGIN);
        side1.expand(CLEAR_MARGIN, 0, 0);
        side2.expand(CLEAR_MARGIN, 0, 0);
        WorldEditUtility.pasteAir(side1, getWorld(), true, Minigames.get());
        WorldEditUtility.pasteAir(side2, getWorld(), true, Minigames.get());
        BoundingBox side3 = new BoundingBox(box.getMinX() - 1, box.getMinY(), box.getMinZ(),
                box.getMinX() - CLEAR_MARGIN, box.getMaxY(), box.getMaxZ());
        BoundingBox side4 = new BoundingBox(box.getMaxX(), box.getMinY(), box.getMinZ(),
                box.getMaxX() + CLEAR_MARGIN, box.getMaxY(), box.getMaxZ());
        WorldEditUtility.pasteAir(side3, getWorld(), true, Minigames.get());
        WorldEditUtility.pasteAir(side4, getWorld(), true, Minigames.get());
    }

    @Override
    public void gameRestart() {
        if (getPhase() != Phase.RESTART)
            throw new IllegalStateException();
        this.pasteSchematic();
        super.gameRestart();
        this.clearedEntitiesBefore.clear();
        World w = getWorld();
        getBoundingBox(); //force load
        for (Chunk chunk : w.getLoadedChunks())
            if (this.overlaps(chunk) && chunk.isEntitiesLoaded()) {
                clearedEntitiesBefore.add(chunk);
                for (Entity e : chunk.getEntities())
                    if (!(e instanceof Player))
                        e.remove();
            }
    }

    protected abstract @NotNull T craftTeam(@NotNull DyeColor color);

    @Override
    public @Nullable T getTeam(@NotNull UUID player) {
        for (T team : teams.values()) {
            if (team.containsUser(player))
                return team;
        }
        return null;
    }

    @NotNull
    @Contract("-> new")
    public BoundingBox getBoundingBox() {
        if (boxCache == null) {
            BlockVector3 dim = getSchematic().getDimensions();
            boxCache = new BoundingBox(getGameLocation().x, getGameLocation().y, getGameLocation().z,
                    getGameLocation().x + dim.getX(), getGameLocation().y + dim.getY(), getGameLocation().z + dim.getZ());
        }
        return boxCache.clone();
    }

    @Override
    public boolean containsLocation(@NotNull Location loc) {
        return Objects.equals(loc.getWorld(), this.getWorld()) && (boxCache == null ? getBoundingBox().contains(loc.toVector()) : boxCache.contains(loc.toVector()));
    }

    @Override
    public @NotNull Collection<T> getTeams() {
        return Collections.unmodifiableCollection(teams.values());
    }

    public void onGamerMoveOutsideArena(@NotNull PlayerMoveEvent event) {
        if (event.getTo().getY() < (boxCache == null ? getBoundingBox().getMinY() : boxCache.getMinY()))
            onGamerFallOutsideArena(event);
        else {
            Location to = event.getFrom().clone();
            to.setY(event.getTo().getY());
            event.setTo(to);
        }
    }


    public void onGamerTeleport(@NotNull PlayerTeleportEvent event) {
        if (Objects.equals(event.getTo().getWorld(), event.getFrom().getWorld())) {
            onFakeGamerDeath(event.getPlayer(), null, false);
            return; //teleported away by something ?
        }
        if (!containsLocation(event.getTo()))
            event.setCancelled(true);
    }

    public void onGamerMoveInsideArena(@NotNull PlayerMoveEvent event) {
        super.onGamerMoveInsideArena(event);
        sendUpdateBorder(event.getPlayer());
    }

    @Override
    public void onSpectatorMove(@NotNull PlayerMoveEvent event) {
        if (event.getTo().getZ() > getBoundingBox().getMaxZ() || event.getTo().getZ() < getBoundingBox().getMinZ()
                || event.getTo().getX() > getBoundingBox().getMaxX() || event.getTo().getX() < getBoundingBox().getMinX())
            event.setCancelled(true);
        else
            sendUpdateBorder(event.getPlayer());
    }

    public void onGamerAdded(@NotNull Player player) {
        super.onGamerAdded(player);
        sendUpdateBorder(player); //unrequired maybe
    }

    protected abstract void onGamerFallOutsideArena(@NotNull PlayerMoveEvent event);

    public void teleportResetLocation(@NotNull Player player) {
        switch (getPhase()) {
            case PRE_START, PLAYING -> player.teleport(getArena().getSpawnOffset(getTeam(player).getColor())
                    .add(getGameLocation()));
            case END, COLLECTING_PLAYERS -> player.teleport(getArena().getSpectatorsOffset().add(getGameLocation()));
            default -> new IllegalStateException().printStackTrace();
        }
    }

    @Override
    public void onSpectatorAdded(@NotNull Player player) {
        super.onSpectatorAdded(player);
        //TODO may work differently player.teleport(getArena().getSpectatorsOffset().add(getGameLocation()));
        sendUpdateBorder(player);
    }

    @Override
    public void onSpectatorRemoved(@NotNull Player player) {
        player.setWorldBorder(null);
        super.onSpectatorRemoved(player);
    }

    @Override
    public void onGamerRemoved(@NotNull Player player) {
        player.setWorldBorder(null);
        super.onGamerRemoved(player);
    }

    @Override
    public void gameAbort() {
        switch (getPhase()) {
            case PRE_START, PLAYING, END -> {
                for (Player player : getGamers())
                    player.setWorldBorder(null);
                for (Player player : getSpectators())
                    player.setWorldBorder(null);
            }
        }
        teams.values().forEach(ColoredTeam::clear);
        super.gameAbort();
    }

    public boolean overlaps(@NotNull Chunk chunk) {
        return Objects.equals(getWorld(), chunk.getWorld()) && getBoundingBox().overlaps(
                new BoundingBox(chunk.getX() * 16, chunk.getWorld().getMinHeight(), chunk.getZ() * 16
                        , (chunk.getX() + 1) * 16, chunk.getWorld().getMaxHeight(), (chunk.getZ() + 1) * 16));
    }

    @Override
    public void onChunkEntitiesLoad(@NotNull Chunk chunk) {
        if (!clearedEntitiesBefore.contains(chunk)) {
            for (Entity e : chunk.getEntities())
                if (!(e instanceof Player))
                    e.remove();
            clearedEntitiesBefore.add(chunk);
        }
    }
}
