package emanondev.minigames.event.deathmatch;

import emanondev.minigames.event.PlayersWinGameEvent;
import emanondev.minigames.games.deathmatch.DeathMatchGame;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class DeathMatchWinEvent extends PlayersWinGameEvent<DeathMatchGame> {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public DeathMatchWinEvent(@NotNull DeathMatchGame game, @NotNull Set<Player> players) {
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
