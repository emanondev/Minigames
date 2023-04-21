package emanondev.minigames.event;

import emanondev.minigames.games.MGame;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a Player joined a Game.
 */
public class PlayerJoinedGameEvent extends GameEvent {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final Player player;

    @SuppressWarnings("rawtypes")
    public PlayerJoinedGameEvent(@NotNull MGame game, @NotNull Player player) {
        super(game);
        this.player = player;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
}
