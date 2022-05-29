package emanondev.minigames.minigames.generic;

import emanondev.core.UtilsString;
import emanondev.core.YMLSection;
import emanondev.minigames.minigames.GameManager;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class MType<A extends MArena, O extends MOption> {

    private final String type;
    private final YMLSection section;
    private final Class<O> optionClazz;
    private final Class<A> arenaClazz;

    public MType(@NotNull String type, @NotNull Class<A> arenaClazz, @NotNull Class<O> optionClazz) {
        if (!UtilsString.isLowcasedValidID(type))
            throw new IllegalStateException("invalid id");
        this.type = type;
        this.arenaClazz = arenaClazz;
        this.optionClazz = optionClazz;
        this.section = GameManager.get().getSection(this);
    }

    public @NotNull YMLSection getSection() {
        return section;
    }

    public @NotNull String getType() {
        return type;
    }

    public @Nullable Location getLobbyLocation() {
        Location loc = section.getLocation("lobby", null);
        if (loc != null)
            return loc;
        return GameManager.get().getGlobalLobby();
    }

    public <T extends MArena> boolean matchType(@NotNull T arena) {
        return arenaClazz.isInstance(arena);
    }

    public <T extends MOption> boolean matchType(@NotNull T option) {
        return optionClazz.isInstance(option);
    }

    public abstract @NotNull MArenaBuilder getArenaBuilder(@NotNull UUID uuid, @NotNull String id);

    public abstract @NotNull MOption createDefaultOptions();

    public abstract @NotNull MGame<?, A, O> createGame(@NotNull String arenaId, @NotNull String optionId);
}
