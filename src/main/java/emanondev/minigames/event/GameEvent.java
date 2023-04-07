package emanondev.minigames.event;

import emanondev.minigames.generic.MGame;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class GameEvent<G extends MGame> extends Event {

    private final G game;

    public GameEvent(@NotNull G game) {
        this.game = game;
    }

    public G getGame() {
        return game;
    }
}
