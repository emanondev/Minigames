package emanondev.minigames.event;

import emanondev.minigames.generic.MGame;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a Game is Loaded.<br>
 * Which happens on server start and when a new game is created and enabled.
 */
public class GameLoadEvent extends GameEvent {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public GameLoadEvent(@NotNull MGame game) {
        super(game);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
}
