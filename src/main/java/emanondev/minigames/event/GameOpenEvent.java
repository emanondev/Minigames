package emanondev.minigames.event;

import emanondev.minigames.games.MGame;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a Game is Opened to Players.<br>
 * Which happens after Game is loaded and eventually when game finished is ready to host another round.
 */
public class GameOpenEvent extends GameEvent {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    @SuppressWarnings("rawtypes")
    public GameOpenEvent(@NotNull MGame game) {
        super(game);
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
}
