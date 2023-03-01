package emanondev.minigames.skywars;

import emanondev.core.ItemBuilder;
import emanondev.core.UtilsString;
import emanondev.core.VaultEconomyHandler;
import emanondev.minigames.ArenaManager;
import emanondev.minigames.MessageUtil;
import emanondev.minigames.Minigames;
import emanondev.minigames.OptionManager;
import emanondev.minigames.generic.MArena;
import emanondev.minigames.generic.MOption;
import emanondev.minigames.generic.MType;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
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
    public @NotNull MOption createDefaultOptions() {
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

    public void applyKillPoints(Player p) {
        double kp = getSection().loadDouble("game.kill_points", 2D);
        if (kp > 0) {
            new VaultEconomyHandler().addMoney(p, kp);
            MessageUtil.sendMessage(p, "generic.obtain_points", "%amount%", UtilsString.formatOptional2Digit(kp));
        }
    }

    public void applyWinPoints(Player p) {
        double win = getSection().loadDouble("game.win_points", 10D);
        if (win > 0) {
            new VaultEconomyHandler().addMoney(p, win);
            MessageUtil.sendMessage(p, "generic.obtain_points", "%amount%", UtilsString.formatOptional2Digit(win));
        }
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
}
