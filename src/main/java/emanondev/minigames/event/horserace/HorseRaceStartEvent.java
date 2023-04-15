package emanondev.minigames.event.horserace;

import emanondev.minigames.event.GameStartEvent;
import emanondev.minigames.games.race.horse.HorseRaceGame;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class HorseRaceStartEvent extends GameStartEvent<HorseRaceGame> {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public HorseRaceStartEvent(@NotNull HorseRaceGame game) {
        super(game);
    }
}
