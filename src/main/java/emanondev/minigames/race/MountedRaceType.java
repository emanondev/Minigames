package emanondev.minigames.race;

import emanondev.core.ItemBuilder;
import emanondev.minigames.ArenaManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.OptionManager;
import emanondev.minigames.generic.MArena;
import emanondev.minigames.generic.MOption;
import emanondev.minigames.generic.MType;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class MountedRaceType extends MType<RaceArena, MountedRaceOption> {


    public MountedRaceType() {
        super("mountedrace", RaceArena.class, MountedRaceOption.class, Minigames.get());
        ConfigurationSerialization.registerClass(RaceArena.class);
        ConfigurationSerialization.registerClass(MountedRaceOption.class);
        ConfigurationSerialization.registerClass(MountedRaceGame.class);
    }

    @Override
    public @NotNull RaceArenaBuilder getArenaBuilder(@NotNull UUID uuid, @NotNull String id, @NotNull String label) {
        return new RaceArenaBuilder(uuid, id, label);
    }

    @Override
    public @NotNull MountedRaceOption createDefaultOptions() {
        return new MountedRaceOption();
    }

    @Override
    public @NotNull MountedRaceGame createGame(@NotNull String arenaId, @NotNull String optionId) {
        MArena arena = ArenaManager.get().get(arenaId);
        if (arena == null || !this.matchType(arena))
            throw new IllegalStateException();
        MOption option = OptionManager.get().get(optionId);
        if (option == null || !this.matchType(option))
            throw new IllegalStateException();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("option", optionId);
        map.put("arena", arenaId);
        return new MountedRaceGame(map);
    }

    @Override
    public @NotNull ItemBuilder getGameSelectorBaseItem() {
        return new ItemBuilder(getSection().getMaterial("display.gui.material", Material.LEATHER_BOOTS))
                .setGuiProperty().setCustomModelData(getSection()
                        .getInteger("display.gui.custommodel", null));
    }
}