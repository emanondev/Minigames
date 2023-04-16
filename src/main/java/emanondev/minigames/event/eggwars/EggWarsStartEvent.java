package emanondev.minigames.event.eggwars;

import emanondev.minigames.event.GameStartEvent;
import emanondev.minigames.games.eggwars.EggWarsGame;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class EggWarsStartEvent extends GameStartEvent<EggWarsGame> {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public EggWarsStartEvent(@NotNull EggWarsGame game) {
        super(game);
    }
}
