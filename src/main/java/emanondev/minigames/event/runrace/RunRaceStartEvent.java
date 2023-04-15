package emanondev.minigames.event.runrace;

import emanondev.minigames.event.GameStartEvent;
import emanondev.minigames.games.race.running.RunRaceGame;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RunRaceStartEvent extends GameStartEvent<RunRaceGame> {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public RunRaceStartEvent(@NotNull RunRaceGame game) {
        super(game);
    }
}
