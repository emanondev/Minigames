package emanondev.minigames.event;

import emanondev.minigames.generic.MGame;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a Game Ends.<br>
 */
public class GameEndEvent extends GameEvent {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @SuppressWarnings("rawtypes")
    public GameEndEvent(@NotNull MGame game) {
        super(game);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
}
