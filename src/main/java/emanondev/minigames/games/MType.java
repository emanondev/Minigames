package emanondev.minigames.games;

import emanondev.core.CorePlugin;
import emanondev.core.ItemBuilder;
import emanondev.core.UtilsString;
import emanondev.core.YMLSection;
import emanondev.core.message.SimpleMessage;
import emanondev.core.util.CorePluginLinked;
import emanondev.minigames.Configurations;
import emanondev.minigames.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class MType<A extends MArena, O extends MOption> implements CorePluginLinked {

    public final SimpleMessage GAME_START_MESSAGE;
    public final SimpleMessage GAME_END_MESSAGE;
    public final SimpleMessage PRESTART_COOLDOWN_BAR;
    public final SimpleMessage COLLECTINGPLAYERS_COOLDOWN_BAR;
    public final SimpleMessage END_COOLDOWN_BAR;
    public final SimpleMessage COLLECTINGPLAYERS_NO_COOLDOWN_BAR;
    public final SimpleMessage GAME_INTERRUPTED_MESSAGE;

    private final String type;
    private final YMLSection section;
    private final Class<O> optionClazz;
    private final Class<A> arenaClazz;
    private final CorePlugin plugin;

    public MType(@NotNull String type, @NotNull Class<A> arenaClazz, @NotNull Class<O> optionClazz, @NotNull CorePlugin plugin) {
        if (!UtilsString.isLowcasedValidID(type))
            throw new IllegalStateException("invalid id");
        this.type = type;
        this.arenaClazz = arenaClazz;
        this.optionClazz = optionClazz;
        this.section = GameManager.get().getSection(this);
        this.plugin = plugin;
        this.GAME_INTERRUPTED_MESSAGE = new SimpleMessage(getPlugin(), getType() + ".game.game_interrupted");
        this.COLLECTINGPLAYERS_COOLDOWN_BAR = new SimpleMessage(getPlugin(), getType() + ".game.collectingplayers_cooldown_bar");
        this.END_COOLDOWN_BAR = new SimpleMessage(getPlugin(), getType() + ".game.end_cooldown_bar");
        this.COLLECTINGPLAYERS_NO_COOLDOWN_BAR = new SimpleMessage(getPlugin(), getType() + ".game.collectingplayers_no_cooldown_bar");
        this.PRESTART_COOLDOWN_BAR = new SimpleMessage(getPlugin(), getType() + ".game.prestart_cooldown_bar");
        this.GAME_START_MESSAGE = new SimpleMessage(getPlugin(), getType() + ".game.game_start");
        this.GAME_END_MESSAGE = new SimpleMessage(getPlugin(), getType() + ".game.game_end");


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
    public abstract MArenaBuilder getArenaBuilder(@NotNull UUID uuid, @NotNull String id, @NotNull String label);

    @NotNull
    public abstract MOption createDefaultOptions();

    @NotNull
    public abstract MGame<?, A, O> createGame(@NotNull String arenaId, @NotNull String optionId);

    @NotNull
    public String getDisplayName() {
        return getSection().loadMessage("display.displayName", ChatColor.YELLOW + getType());
    }

    @NotNull
    public abstract ItemBuilder getGameSelectorBaseItem();


    public final @NotNull CorePlugin getPlugin() {
        return plugin;
    }

}
