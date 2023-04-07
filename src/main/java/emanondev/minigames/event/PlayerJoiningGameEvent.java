package emanondev.minigames.event;

import emanondev.minigames.generic.MGame;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a Player is joining a Game.
 */
public class PlayerJoiningGameEvent extends GameEvent implements Cancellable {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
    private final Player player;
    private boolean isCancelled = false;

    @SuppressWarnings("rawtypes")
    public PlayerJoiningGameEvent(@NotNull MGame game, @NotNull Player player) {
        super(game);
        this.player = player;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    /**
     * Note: if you cancel this event you should also handle a feedback for the player
     *
     * @param cancelled
     */
    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
}
