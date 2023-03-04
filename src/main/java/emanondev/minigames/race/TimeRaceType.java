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

public class TimeRaceType extends ARaceType<TimeRaceOption> {


    public TimeRaceType() {
        super("timerace", RaceArena.class, TimeRaceOption.class, Minigames.get());
        ConfigurationSerialization.registerClass(RaceArena.class);
        ConfigurationSerialization.registerClass(TimeRaceOption.class);
        ConfigurationSerialization.registerClass(TimeRaceGame.class);
    }

    @Override
    public @NotNull TimeRaceOption createDefaultOptions() {
        return new TimeRaceOption();
    }

    @Override
    public @NotNull TimeRaceGame createGame(@NotNull String arenaId, @NotNull String optionId) {
        MArena arena = ArenaManager.get().get(arenaId);
        if (arena == null || !this.matchType(arena))
            throw new IllegalStateException();
        MOption option = OptionManager.get().get(optionId);
        if (option == null || !this.matchType(option))
            throw new IllegalStateException();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("option", optionId);
        map.put("arena", arenaId);
        return new TimeRaceGame(map);
    }

    @Override
    public @NotNull ItemBuilder getGameSelectorBaseItem() {
        return new ItemBuilder(getSection().getMaterial("display.gui.material", Material.LEATHER_BOOTS))
                .setGuiProperty().setCustomModelData(getSection()
                        .getInteger("display.gui.custommodel", null));
    }
}
