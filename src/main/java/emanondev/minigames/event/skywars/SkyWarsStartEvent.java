package emanondev.minigames.event.skywars;

import emanondev.minigames.event.GameStartEvent;
import emanondev.minigames.skywars.SkyWarsGame;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SkyWarsStartEvent extends GameStartEvent<SkyWarsGame> {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public SkyWarsStartEvent(@NotNull SkyWarsGame game) {
        super(game);
    }
}
