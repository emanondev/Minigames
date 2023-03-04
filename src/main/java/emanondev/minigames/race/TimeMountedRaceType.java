package emanondev.minigames.race;

import emanondev.core.ItemBuilder;
import emanondev.minigames.ArenaManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.OptionManager;
import emanondev.minigames.generic.MArena;
import emanondev.minigames.generic.MOption;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class TimeMountedRaceType extends ARaceType<TimeMountedRaceOption> {


    public TimeMountedRaceType() {
        super("timemountedrace", RaceArena.class, TimeMountedRaceOption.class, Minigames.get());
        ConfigurationSerialization.registerClass(RaceArena.class);
        ConfigurationSerialization.registerClass(TimeMountedRaceOption.class);
        ConfigurationSerialization.registerClass(TimeMountedRaceGame.class);
    }

    @Override
    public @NotNull TimeMountedRaceOption createDefaultOptions() {
        return new TimeMountedRaceOption();
    }

    @Override
    public @NotNull TimeMountedRaceGame createGame(@NotNull String arenaId, @NotNull String optionId) {
        MArena arena = ArenaManager.get().get(arenaId);
        if (arena == null || !this.matchType(arena))
            throw new IllegalStateException();
        MOption option = OptionManager.get().get(optionId);
        if (option == null || !this.matchType(option))
            throw new IllegalStateException();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("option", optionId);
        map.put("arena", arenaId);
        return new TimeMountedRaceGame(map);
    }

    @Override
    public @NotNull ItemBuilder getGameSelectorBaseItem() {
        return new ItemBuilder(getSection().getMaterial("display.gui.material", Material.LEATHER_BOOTS))
                .setGuiProperty().setCustomModelData(getSection()
                        .getInteger("display.gui.custommodel", null));
    }
}
