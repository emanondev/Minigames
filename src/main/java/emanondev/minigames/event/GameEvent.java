package emanondev.minigames.event;

import emanondev.minigames.games.MGame;
import lombok.Getter;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class GameEvent<G extends MGame> extends Event {

    private final G game;

    public GameEvent(@NotNull G game) {
        this.game = game;
    }

}
