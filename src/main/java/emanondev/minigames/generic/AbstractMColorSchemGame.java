package emanondev.minigames.generic;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public abstract class AbstractMColorSchemGame<T extends ColoredTeam, A extends MSchemArena & MColorableTeamArena, O extends MOption> extends AbstractMGame<T, A, O> implements MSchemGame<T, A, O> {

    public AbstractMColorSchemGame(@NotNull Map<String, Object> map) {
        super(map);
        for (DyeColor color : getArena().getColors())
            teams.put(color, craftTeam(color));
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
            BlockVector3 min = getSchematic().getMinimumPoint();
            BlockVector3 max = getSchematic().getMaximumPoint();
            boxCache = new BoundingBox(getGameLocation().x + min.getX(), getGameLocation().y + min.getY(), getGameLocation().z + min.getZ(),
                    getGameLocation().x + max.getX() + 1, getGameLocation().y + max.getY() + 1, getGameLocation().z + max.getZ() + 1);
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
        if (event.getTo().getY() < (boxCache == null ? getBoundingBox().getMinY():boxCache.getMinY()))
            onPlayingPlayerFallOutsideArena(event);
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

    protected abstract void onPlayingPlayerFallOutsideArena(PlayerMoveEvent event);


}
