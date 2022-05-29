package emanondev.minigames.minigames.skywars;

import emanondev.minigames.minigames.ArenaManager;
import emanondev.minigames.minigames.OptionManager;
import emanondev.minigames.minigames.generic.MArena;
import emanondev.minigames.minigames.generic.MOption;
import emanondev.minigames.minigames.generic.MType;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class SkyWarsType extends MType<SkyWarsArena, SkyWarsOption> {
    public SkyWarsType() {
        super("skywars", SkyWarsArena.class, SkyWarsOption.class);
        ConfigurationSerialization.registerClass(SkyWarsArena.class);
        ConfigurationSerialization.registerClass(SkyWarsOption.class);
        ConfigurationSerialization.registerClass(SkyWarsGame.class);
    }

    @Override
    @NotNull
    public SkyWarsArenaBuilder getArenaBuilder(@NotNull UUID uuid, @NotNull String id) {
        return new SkyWarsArenaBuilder(uuid, id);
    }

    @Override
    public @NotNull MOption createDefaultOptions() {
        return new SkyWarsOption();
    }

    @Override
    public @NotNull SkyWarsGame createGame(@NotNull String arenaId, @NotNull String optionId) {
        MArena arena = ArenaManager.get().getArena(arenaId);
        if (arena == null || !this.matchType(arena))
            throw new IllegalStateException();
        MOption option = OptionManager.get().getOption(optionId);
        if (option == null || !this.matchType(option))
            throw new IllegalStateException();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("option", optionId);
        map.put("arena", arenaId);
        return new SkyWarsGame(map);
    }
}
