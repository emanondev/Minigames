package emanondev.minigames.event.boatrace;

import emanondev.minigames.event.GameStartEvent;
import emanondev.minigames.race.boat.BoatRaceGame;
import emanondev.minigames.race.running.RunRaceGame;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BoatRaceStartEvent extends GameStartEvent<BoatRaceGame> {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public BoatRaceStartEvent(@NotNull BoatRaceGame game) {
        super(game);
    }
}
