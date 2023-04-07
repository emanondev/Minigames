package emanondev.minigames.event.horserace;

import emanondev.minigames.event.ARaceWinEvent;
import emanondev.minigames.race.ARaceTeam;
import emanondev.minigames.race.horse.HorseRaceGame;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class HorseRaceWinThirdEvent extends ARaceWinEvent<HorseRaceGame> {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public HorseRaceWinThirdEvent(@NotNull ARaceTeam<HorseRaceGame> team, @NotNull Player lineCutter, @NotNull Set<Player> winners) {
        super(team, lineCutter, winners);
    }
}
