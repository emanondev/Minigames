package emanondev.minigames.games.race;

import emanondev.core.CorePlugin;
import emanondev.core.message.SimpleMessage;
import emanondev.minigames.Minigames;
import emanondev.minigames.generic.MType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class ARaceType<O extends ARaceOption> extends MType<RaceArena, O> {

    public final SimpleMessage REACHED_CHECKPOINT = new SimpleMessage(Minigames.get(),
            getType() + ".game.reached_checkpoint");

    public ARaceType(@NotNull String type, @NotNull Class<RaceArena> arenaClazz, @NotNull Class<O> optionClazz, @NotNull CorePlugin plugin) {
        super(type, arenaClazz, optionClazz, plugin);
    }

    @Override
    public @NotNull RaceArenaBuilder getArenaBuilder(@NotNull UUID uuid, @NotNull String id, @NotNull String label) {
        return new RaceArenaBuilder(uuid, id, label);
    }
}
