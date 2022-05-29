package emanondev.minigames.minigames.generic;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
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

    private final Map<DyeColor, T> teams = new EnumMap<>(DyeColor.class);

    @Override
    public @NotNull Collection<T> getTeams() {
        return Collections.unmodifiableCollection(teams.values());
    }

    @Override
    public boolean containsLocation(@NotNull Location loc) {
        return loc.getWorld().getName().equals(this.getWorld().getName()) && getBoundingBox().contains(loc.toVector());
    }

    public void onPlayingPlayerMoveOutsideArena(PlayerMoveEvent event) {
        if (event.getTo().getY() < getBoundingBox().getMinY())
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
