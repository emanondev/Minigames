package emanondev.minigames.event;

import emanondev.minigames.games.MGame;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a Player quit a Game.
 * This is called when a player quit a game but also when it quit the server.
 */
public class PlayerQuitGameEvent extends GameEvent {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    private final Player player;

    @SuppressWarnings("rawtypes")
    public PlayerQuitGameEvent(@NotNull MGame game, @NotNull Player player) {
        super(game);
        this.player = player;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
}
