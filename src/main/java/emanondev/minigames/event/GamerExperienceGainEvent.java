package emanondev.minigames.event;

import emanondev.minigames.gamer.Gamer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class GamerExperienceGainEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    private long xp;
    private final Gamer gamer;
    private boolean cancelled = false;

    public GamerExperienceGainEvent(Gamer gamer, long xpGain) {
        this.gamer = gamer;
        this.xp = xpGain;
    }

    public @Range(from = 0, to = Long.MAX_VALUE) long getExperienceGain() {
        return xp;
    }

    public void setExperienceGain(@Range(from = 0, to = Long.MAX_VALUE) long value) {
        this.xp = value;
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