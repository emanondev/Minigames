package emanondev.minigames.generic;

import emanondev.core.UtilsString;
import emanondev.core.YMLSection;
import emanondev.minigames.Configurations;
import emanondev.minigames.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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

    @NotNull
    public YMLSection getSection() {
        return section;
    }

    @NotNull
    public String getType() {
        return type;
    }

    public void applyDefaultPlayerSnapshot(@NotNull Player player) {
        Configurations.applyGameEmptyStartSnapshot(player);
    }

    public <T extends MArena> boolean matchType(@NotNull T arena) {
        return arenaClazz.isInstance(arena);
    }

    public <T extends MOption> boolean matchType(@NotNull T option) {
        return optionClazz.isInstance(option);
    }

    @NotNull
    public abstract MArenaBuilder getArenaBuilder(@NotNull UUID uuid, @NotNull String id);

    @NotNull
    public abstract MOption createDefaultOptions();

    @NotNull
    public abstract MGame<?, A, O> createGame(@NotNull String arenaId, @NotNull String optionId);

    @NotNull
    public String getDisplayName() {
        return getSection().loadMessage("displayName", ChatColor.YELLOW + getType());
    }
}
