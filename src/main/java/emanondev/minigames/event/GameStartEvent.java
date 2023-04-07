package emanondev.minigames.event;

import emanondev.minigames.generic.MGame;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a Game is Started.<br>
 * When a game starts may depends on game type, usually a game starts when enough players joined the game and start cooldown ends.
 */
public class GameStartEvent extends GameEvent {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @SuppressWarnings("rawtypes")
    public GameStartEvent(@NotNull MGame game) {
        super(game);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
}
