package emanondev.minigames.event.deathmatch;

import emanondev.minigames.event.GameEvent;
import emanondev.minigames.games.deathmatch.DeathMatchGame;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeathMatchDeathEvent extends GameEvent<DeathMatchGame> {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final Player dead;
    private final Player killer;

    public DeathMatchDeathEvent(@NotNull DeathMatchGame game, Player loser) {
        this(game, loser, null);
    }

    public DeathMatchDeathEvent(@NotNull DeathMatchGame game, @NotNull Player loser, @Nullable Player killer) {
        super(game);
        this.dead = loser;
        this.killer = killer;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public @NotNull Player getDead() {
        return dead;
    }

    public @Nullable Player getKiller() {
        return killer;
    }
}
