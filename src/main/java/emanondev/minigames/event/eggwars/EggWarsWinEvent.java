package emanondev.minigames.event.eggwars;

import emanondev.minigames.event.PlayersWinGameEvent;
import emanondev.minigames.games.eggwars.EggWarsGame;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class EggWarsWinEvent extends PlayersWinGameEvent<EggWarsGame> {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public EggWarsWinEvent(@NotNull EggWarsGame game, @NotNull Set<Player> players) {
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
