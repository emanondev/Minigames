package emanondev.minigames.event;

import emanondev.minigames.gamer.Gamer;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class GamerExperienceGainEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    @Getter
    private final Gamer gamer;
    private long xp;
    @Getter
    private boolean cancelled = false;

    public GamerExperienceGainEvent(Gamer gamer, long xpGain) {
        this.gamer = gamer;
        this.xp = xpGain;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public @Range(from = 0, to = Long.MAX_VALUE) long getExperienceGain() {
        return xp;
    }

    public void setExperienceGain(@Range(from = 0, to = Long.MAX_VALUE) long value) {
        this.xp = value;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}