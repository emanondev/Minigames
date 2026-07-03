package emanondev.minigames.event;

import emanondev.minigames.games.MGame;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

/**
 * Called when a group of Players win a Game.
 */
@Getter
public abstract class PlayersWinGameEvent<G extends MGame> extends GameEvent<G> {

    private final Set<Player> players;

    public PlayersWinGameEvent(@NotNull G game, @NotNull Set<Player> players) {
        super(game);
        this.players = Collections.unmodifiableSet(players);
    }

}
