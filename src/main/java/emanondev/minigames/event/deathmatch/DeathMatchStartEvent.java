package emanondev.minigames.event.deathmatch;

import emanondev.minigames.event.GameStartEvent;
import emanondev.minigames.games.deathmatch.DeathMatchGame;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DeathMatchStartEvent extends GameStartEvent<DeathMatchGame> {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public DeathMatchStartEvent(@NotNull DeathMatchGame game) {
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
