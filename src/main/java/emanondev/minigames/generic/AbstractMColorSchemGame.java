package emanondev.minigames.generic;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractMColorSchemGame<T extends ColoredTeam, A extends MSchemArena & MColorableTeamArena, O extends MOption> extends AbstractMGame<T, A, O> implements MSchemGame<T, A, O> {

    private final boolean isSquared; // count as squared even if the area is pretty small
    private final List<WorldBorder> borders = new ArrayList<>();
    private final WorldBorder spectatorBorder;
    private final List<BoundingBox> cacheArea = new ArrayList<>();
    private final HashMap<Player, Integer> bordersId = new HashMap<>();

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

    private void sendUpdateBorder(Player p) {
        Integer id = bordersId.get(p);
        if (id == null) {
            if (isSquared) {
                bordersId.put(p, 0);
                p.setWorldBorder(borders.get(0));
                return;
            }
            int closest = getClosestBorder(p.getLocation().toVector());
            bordersId.put(p, closest);
            p.setWorldBorder(borders.get(closest));
            return;
        }
        if (isSquared)
            return;
        int closest = getClosestBorder(p.getLocation().toVector());
        if (cacheArea.get(closest).contains(p.getLocation().toVector()))
            return;
        bordersId.put(p, closest);
        p.setWorldBorder(borders.get(closest));
        return;
    }

    private void sendSpectatorBorder(Player p) {
        p.setWorldBorder(spectatorBorder);
    }


    public AbstractMColorSchemGame(@NotNull Map<String, Object> map) {
        super(map);
        for (DyeColor color : getArena().getColors())
            teams.put(color, craftTeam(color));
        BoundingBox box = getBoundingBox();
        isSquared = box.getWidthX() == box.getWidthZ() || Math.max(box.getWidthX(), box.getWidthZ()) < 10;

        double margin = 0.25;
        double size = Math.max(box.getWidthX(), box.getWidthZ()) - 2 * margin; //sightly smaller
        if (!isSquared) {
            for (int i = 0; i < 4; i++) {
                WorldBorder wb = Bukkit.createWorldBorder();
                wb.setWarningDistance(3);
                wb.setSize(size);
                wb.setCenter((i < 2 ? box.getMinX() : box.getMaxX()) + size/2 + margin, (i % 2 == 0 ? box.getMinZ() : box.getMaxZ()) + size/2 + margin);
                borders.add(wb);
                cacheArea.add(new BoundingBox(i < 2 ? box.getMinX() : box.getMaxX()
                        , Integer.MIN_VALUE, i % 2 == 0 ? box.getMinZ() : box.getMaxZ()
                        , i >= 2 ? box.getMinX() : box.getMaxX()
                        , Integer.MAX_VALUE
                        , i % 2 != 0 ? box.getMinZ() : box.getMaxZ()));
            }
            WorldBorder wb = Bukkit.createWorldBorder();
            wb.setWarningDistance(3);
            wb.setSize(size);
            wb.setCenter(box.getCenterX() + size/2, box.getCenterZ() + size/2);
            spectatorBorder = wb;
        } else {

            WorldBorder wb = Bukkit.createWorldBorder();
            wb.setWarningDistance(3);
            wb.setSize(size);
            wb.setCenter(box.getMinX() + size/2, box.getMinZ() + size/2);
            borders.add(wb);
            spectatorBorder = wb;
        }
    }

    @Override
    public void gameRestart() {
        if (getPhase() != Phase.RESTART)
            throw new IllegalStateException();
        this.pasteSchematic();

        super.gameRestart();
    }

    protected abstract @NotNull T craftTeam(DyeColor color);

    @Override
    public @Nullable T getTeam(@NotNull Player player) {
        for (T team : teams.values()) {
            if (team.containsUser(player))
                return team;
        }
        return null;
    }

    private BoundingBox boxCache;

    @NotNull
    public BoundingBox getBoundingBox() {
        if (boxCache == null) {
            BlockVector3 dim = getSchematic().getDimensions();
            boxCache = new BoundingBox(getGameLocation().x, getGameLocation().y, getGameLocation().z,
                    getGameLocation().x + dim.getX() + 1, getGameLocation().y + dim.getY() + 1, getGameLocation().z + dim.getZ() + 1);
        }
        return boxCache.clone();
    }

    @Override
    public boolean containsLocation(@NotNull Location loc) {
        return loc.getWorld().getName().equals(this.getWorld().getName()) && (boxCache == null ? getBoundingBox().contains(loc.toVector()) : boxCache.contains(loc.toVector()));
    }

    private final Map<DyeColor, T> teams = new EnumMap<>(DyeColor.class);

    @Override
    public @NotNull Collection<T> getTeams() {
        return Collections.unmodifiableCollection(teams.values());
    }

    public void onPlayingPlayerMoveOutsideArena(PlayerMoveEvent event) {
        if (event.getTo().getY() < (boxCache == null ? getBoundingBox().getMinY() : boxCache.getMinY()))
            onPlayingPlayerFallOutsideArena(event);
        else {
            Location to = event.getFrom().clone();
            to.setY(event.getTo().getY());
            event.setTo(to);
        }
    }


    public void onPlayingPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getTo().getWorld() != event.getFrom().getWorld()) {
            onFakePlayingPlayerDeath(event.getPlayer(), null);
            return; //teleported away by something ?
        }
        if (!containsLocation(event.getTo())) {
            event.setCancelled(true);
        }
    }

    public void onPlayingPlayerMove(PlayerMoveEvent event) {
        sendUpdateBorder(event.getPlayer());
    }

    public void onPlayingPlayerAdded(Player player) {
        teleportToStartLocation(player);
        sendUpdateBorder(player);
    }

    protected abstract void onPlayingPlayerFallOutsideArena(PlayerMoveEvent event);

    protected void onSpectatorAdded(Player player) {
        super.onSpectatorAdded(player);
        player.teleport(getArena().getSpectatorsOffset().add(getGameLocation()));
        sendSpectatorBorder(player);
    }


    protected void onSpectatorRemoved(Player player) {
        super.onSpectatorRemoved(player);
        player.teleport(getArena().getSpectatorsOffset().add(getGameLocation()));
        player.setWorldBorder(player.getWorld().getWorldBorder());
    }

    protected void onPlayingPlayerRemoved(Player player) {
        player.setWorldBorder(player.getWorld().getWorldBorder());
    }

    @Override
    public void gameAbort() {
        switch (getPhase()){
            case PRE_START,PLAYING,END -> {
                for (Player player : getPlayingPlayers())
                    player.setWorldBorder(player.getWorld().getWorldBorder());
                for (Player player : getSpectators())
                    player.setWorldBorder(player.getWorld().getWorldBorder());
            }
        }
        super.gameAbort();
    }
}
