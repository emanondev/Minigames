package emanondev.minigames.event.skywars;

import emanondev.minigames.event.PlayersWinGameEvent;
import emanondev.minigames.games.skywars.SkyWarsGame;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class SkyWarsWinEvent extends PlayersWinGameEvent<SkyWarsGame> {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public SkyWarsWinEvent(@NotNull SkyWarsGame game, @NotNull Set<Player> players) {
        super(game, players);
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

}
