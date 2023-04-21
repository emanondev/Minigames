package emanondev.minigames.event;

import emanondev.minigames.gamer.Gamer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class GamerLevelUpEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final int newLevel;
    private final Gamer gamer;
    private boolean cancelled = false;

    public GamerLevelUpEvent(Gamer gamer, @Range(from = 2, to = Integer.MAX_VALUE) int i) {
        this.gamer = gamer;
        this.newLevel = i;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public @Range(from = 2, to = Integer.MAX_VALUE) int getNewLevel() {
        return newLevel;
    }

    public Gamer getGamer() {
        return gamer;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
