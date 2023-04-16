package emanondev.minigames.games.race.elytra;

import emanondev.core.ItemBuilder;
import emanondev.minigames.ArenaManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.OptionManager;
import emanondev.minigames.games.MArena;
import emanondev.minigames.games.MOption;
import emanondev.minigames.games.race.ARaceType;
import emanondev.minigames.games.race.RaceArena;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class ElytraRaceType extends ARaceType<ElytraRaceOption> {


    public ElytraRaceType() {
        super("elytrarace", RaceArena.class, ElytraRaceOption.class, Minigames.get());
        ConfigurationSerialization.registerClass(RaceArena.class);
        ConfigurationSerialization.registerClass(ElytraRaceOption.class);
        ConfigurationSerialization.registerClass(ElytraRaceGame.class);
    }

    @Override
    public @NotNull ElytraRaceOption createDefaultOptions() {
        return new ElytraRaceOption();
    }

    @Override
    public @NotNull ElytraRaceGame createGame(@NotNull String arenaId, @NotNull String optionId) {
        MArena arena = ArenaManager.get().get(arenaId);
        if (arena == null || !this.matchType(arena))
            throw new IllegalStateException();
        MOption option = OptionManager.get().get(optionId);
        if (option == null || !this.matchType(option))
            throw new IllegalStateException();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("option", optionId);
        map.put("arena", arenaId);
        return new ElytraRaceGame(map);
    }

    @Override
    public @NotNull ItemBuilder getGameSelectorBaseItem() {
        return new ItemBuilder(getSection().getMaterial("display.gui.material", Material.ELYTRA))
                .setGuiProperty().setCustomModelData(getSection()
                        .getInteger("display.gui.custommodel", null));
    }
}
