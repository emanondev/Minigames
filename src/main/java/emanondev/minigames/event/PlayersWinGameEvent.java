package emanondev.minigames.event;

import emanondev.minigames.generic.MGame;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

/**
 * Called when a group of Players win a Game.
 */
public class PlayersWinGameEvent extends GameEvent {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final Set<Player> players;

    public PlayersWinGameEvent(@NotNull MGame game, @NotNull Set<Player> players) {
        super(game);
        this.players = Collections.unmodifiableSet(players);
    }

    public Set<Player> getPlayers() {
        return players;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
}
