package emanondev.minigames.games.eggwars;

import emanondev.core.ItemBuilder;
import emanondev.minigames.ArenaManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.OptionManager;
import emanondev.minigames.games.MArena;
import emanondev.minigames.games.MOption;
import emanondev.minigames.games.MType;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class EggWarsType extends MType<EggWarsArena, EggWarsOption> {
    public EggWarsType() {
        super("eggwars", EggWarsArena.class, EggWarsOption.class, Minigames.get());
        ConfigurationSerialization.registerClass(EggWarsArena.class);
        ConfigurationSerialization.registerClass(EggWarsOption.class);
        ConfigurationSerialization.registerClass(EggWarsGame.class);
    }

    @Override
    @NotNull
    public EggWarsArenaBuilder getArenaBuilder(@NotNull UUID uuid, @NotNull String id, @NotNull String label) {
        return new EggWarsArenaBuilder(uuid, id, label);
    }

    @Override
    public @NotNull EggWarsOption createDefaultOptions() {
        return new EggWarsOption();
    }

    @Override
    public @NotNull EggWarsGame createGame(@NotNull String arenaId, @NotNull String optionId) {
        MArena arena = ArenaManager.get().get(arenaId);
        if (arena == null || !this.matchType(arena))
            throw new IllegalStateException();
        MOption option = OptionManager.get().get(optionId);
        if (option == null || !this.matchType(option))
            throw new IllegalStateException();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("option", optionId);
        map.put("arena", arenaId);
        return new EggWarsGame(map);
    }

    @Override
    public @NotNull ItemBuilder getGameSelectorBaseItem() {
        return new ItemBuilder(getSection().getMaterial("display.gui.material", Material.BOW))
                .setGuiProperty().setCustomModelData(getSection()
                        .getInteger("display.gui.custommodel", null));
    }

    public double getSnowballPush() {
        return this.getSection().loadDouble("game.snowball_push", 0.5D);
    }

    public double getSnowballVerticalPush() {
        return this.getSection().loadDouble("game.snowball_vertical_push", 0.3D);
    }

    public ItemStack getKillRewardItem() { //TODO description
        return new ItemBuilder(Material.MAGMA_CREAM).setGuiProperty().addEnchantment(Enchantment.DURABILITY, 1).build();
    }

    public double getKillPoints() {
        return getSection().loadDouble("game.kill_points", 2D);
    }

    public double getWinPoints() {
        return getSection().loadDouble("game.win_points", 5D);
    }

    public int getKillExp() {
        return getSection().loadInteger("game.kill_exp", 2);
    }

    public int getWinExp() {
        return getSection().loadInteger("game.win_exp", 5);
    }
}
