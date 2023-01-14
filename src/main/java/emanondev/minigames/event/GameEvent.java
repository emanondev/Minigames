package emanondev.minigames.event;

import emanondev.minigames.generic.MGame;
import emanondev.minigames.generic.MType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class GameEvent extends Event {

    private final MGame game;

    public GameEvent(@NotNull MGame game) {
        this.game = game;
    }

    public MGame getGame() {
        return game;
    }

    public MType getGameType() {
        return game.getMinigameType();
    }
}
