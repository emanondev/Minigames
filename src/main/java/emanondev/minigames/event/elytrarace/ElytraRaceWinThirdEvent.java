package emanondev.minigames.event.elytrarace;

import emanondev.minigames.event.ARaceWinEvent;
import emanondev.minigames.games.race.ARaceTeam;
import emanondev.minigames.games.race.elytra.ElytraRaceGame;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ElytraRaceWinThirdEvent extends ARaceWinEvent<ElytraRaceGame> {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public ElytraRaceWinThirdEvent(@NotNull ARaceTeam<ElytraRaceGame> team, @NotNull Player lineCutter, @NotNull Set<Player> winners) {
        super(team, lineCutter, winners);
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
}
