package emanondev.minigames.event.horserace;

import emanondev.minigames.event.GameStartEvent;
import emanondev.minigames.games.race.horse.HorseRaceGame;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class HorseRaceStartEvent extends GameStartEvent<HorseRaceGame> {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public HorseRaceStartEvent(@NotNull HorseRaceGame game) {
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
