package emanondev.minigames.event.eggwars;

import emanondev.minigames.event.GameEvent;
import emanondev.minigames.games.eggwars.EggWarsGame;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EggWarsDeathEvent extends GameEvent<EggWarsGame> {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final Player dead;
    private final Player killer;

    public EggWarsDeathEvent(@NotNull EggWarsGame game, Player loser) {
        this(game, loser, null);
    }

    public EggWarsDeathEvent(@NotNull EggWarsGame game, @NotNull Player loser, @Nullable Player killer) {
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
