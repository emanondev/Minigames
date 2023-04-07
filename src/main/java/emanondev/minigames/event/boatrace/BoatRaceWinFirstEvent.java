package emanondev.minigames.event.boatrace;

import emanondev.minigames.event.ARaceWinEvent;
import emanondev.minigames.race.ARaceTeam;
import emanondev.minigames.race.boat.BoatRaceGame;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class BoatRaceWinFirstEvent extends ARaceWinEvent<BoatRaceGame> {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public BoatRaceWinFirstEvent(@NotNull ARaceTeam<BoatRaceGame> team, @NotNull Player lineCutter, @NotNull Set<Player> winners) {
        super(team, lineCutter, winners);
    }
}
