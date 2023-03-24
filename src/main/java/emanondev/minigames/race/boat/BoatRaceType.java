package emanondev.minigames.race.boat;

import emanondev.core.ItemBuilder;
import emanondev.minigames.ArenaManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.OptionManager;
import emanondev.minigames.generic.MArena;
import emanondev.minigames.generic.MOption;
import emanondev.minigames.race.ARaceType;
import emanondev.minigames.race.RaceArena;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class BoatRaceType extends ARaceType<BoatRaceOption> {


    public BoatRaceType() {
        super("boatrace", RaceArena.class, BoatRaceOption.class, Minigames.get());
        ConfigurationSerialization.registerClass(RaceArena.class);
        ConfigurationSerialization.registerClass(BoatRaceOption.class);
        ConfigurationSerialization.registerClass(BoatRaceGame.class);
    }

    @Override
    public @NotNull BoatRaceOption createDefaultOptions() {
        return new BoatRaceOption();
    }

    @Override
    public @NotNull BoatRaceGame createGame(@NotNull String arenaId, @NotNull String optionId) {
        MArena arena = ArenaManager.get().get(arenaId);
        if (arena == null || !this.matchType(arena))
            throw new IllegalStateException();
        MOption option = OptionManager.get().get(optionId);
        if (option == null || !this.matchType(option))
            throw new IllegalStateException();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("option", optionId);
        map.put("arena", arenaId);
        return new BoatRaceGame(map);
    }

    @Override
    public @NotNull ItemBuilder getGameSelectorBaseItem() {
        return new ItemBuilder(getSection().getMaterial("display.gui.material", Material.OAK_BOAT))
                .setGuiProperty().setCustomModelData(getSection()
                        .getInteger("display.gui.custommodel", null));
    }
}