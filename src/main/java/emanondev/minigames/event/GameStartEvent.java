package emanondev.minigames.event;

import emanondev.minigames.generic.MGame;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Called when a Game is Started.<br>
 * When a game starts may depends on game type, usually a game starts when enough players joined the game and start cooldown ends.
 */
public abstract class GameStartEvent<G extends MGame> extends GameEvent<G> {

    public GameStartEvent(@NotNull G game) {
        super(game);
    }

    public @NotNull Set<Player> getGamers() {
        return getGame().getGamers();
    }

}
