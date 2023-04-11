package emanondev.minigames.skywars;

import emanondev.core.ItemBuilder;
import emanondev.minigames.ArenaManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.OptionManager;
import emanondev.minigames.generic.MArena;
import emanondev.minigames.generic.MOption;
import emanondev.minigames.generic.MType;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class SkyWarsType extends MType<SkyWarsArena, SkyWarsOption> {
    public SkyWarsType() {
        super("skywars", SkyWarsArena.class, SkyWarsOption.class, Minigames.get());
        ConfigurationSerialization.registerClass(SkyWarsArena.class);
        ConfigurationSerialization.registerClass(SkyWarsOption.class);
        ConfigurationSerialization.registerClass(SkyWarsGame.class);
    }

    @Override
    @NotNull
    public SkyWarsArenaBuilder getArenaBuilder(@NotNull UUID uuid, @NotNull String id, @NotNull String label) {
        return new SkyWarsArenaBuilder(uuid, id, label);
    }

    @Override
    public @NotNull SkyWarsOption createDefaultOptions() {
        return new SkyWarsOption();
    }

    @Override
    public @NotNull SkyWarsGame createGame(@NotNull String arenaId, @NotNull String optionId) {
        MArena arena = ArenaManager.get().get(arenaId);
        if (arena == null || !this.matchType(arena))
            throw new IllegalStateException();
        MOption option = OptionManager.get().get(optionId);
        if (option == null || !this.matchType(option))
            throw new IllegalStateException();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("option", optionId);
        map.put("arena", arenaId);
        return new SkyWarsGame(map);
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
