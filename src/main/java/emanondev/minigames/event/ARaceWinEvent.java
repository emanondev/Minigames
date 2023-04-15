package emanondev.minigames.event;

import emanondev.minigames.games.race.ARaceGame;
import emanondev.minigames.games.race.ARaceTeam;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public class ARaceWinEvent<G extends ARaceGame> extends GameEvent<G> {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    private final Player lineCutter;
    private final Set<Player> winners;

    public ARaceWinEvent(@NotNull ARaceTeam<G> team, @NotNull Player lineCutter, @NotNull Set<Player> winners) {
        super(team.getGame());
        this.lineCutter = lineCutter;
        if (!winners.contains(lineCutter))
            throw new IllegalArgumentException("winner doesn't contain linecutter");
        this.winners = Collections.unmodifiableSet(winners);
    }

    public Player getLineCutter() {
        return lineCutter;
    }

    public Set<Player> getWinners() {
        return winners;
    }
}
