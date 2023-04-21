package emanondev.minigames.event;

import emanondev.minigames.games.MGame;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a Game Ends.<br>
 */
public class GameEndEvent extends GameEvent {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    @SuppressWarnings("rawtypes")
    public GameEndEvent(@NotNull MGame game) {
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
