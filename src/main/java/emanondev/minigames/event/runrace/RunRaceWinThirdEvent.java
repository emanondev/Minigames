package emanondev.minigames.event.runrace;

import emanondev.minigames.event.ARaceWinEvent;
import emanondev.minigames.games.race.ARaceTeam;
import emanondev.minigames.games.race.running.RunRaceGame;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class RunRaceWinThirdEvent extends ARaceWinEvent<RunRaceGame> {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public RunRaceWinThirdEvent(@NotNull ARaceTeam<RunRaceGame> team, @NotNull Player lineCutter, @NotNull Set<Player> winners) {
        super(team, lineCutter, winners);
    }
}
