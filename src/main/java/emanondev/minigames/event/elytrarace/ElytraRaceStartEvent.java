package emanondev.minigames.event.elytrarace;

import emanondev.minigames.event.GameStartEvent;
import emanondev.minigames.race.elytra.ElytraRaceGame;
import emanondev.minigames.race.running.RunRaceGame;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ElytraRaceStartEvent extends GameStartEvent<ElytraRaceGame> {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public ElytraRaceStartEvent(@NotNull ElytraRaceGame game) {
        super(game);
    }
}
