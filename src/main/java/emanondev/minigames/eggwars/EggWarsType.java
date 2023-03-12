package emanondev.minigames.eggwars;

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
import org.bukkit.entity.Player;
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
        return new EggWarsArenaBuilder(uuid, id, label, Minigames.get());
    }

    @Override
    public @NotNull MOption createDefaultOptions() {
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
        return new ItemBuilder(Material.DRAGON_EGG).setGuiProperty();
    }

    public void applyKillPoints(Player p) {
        double kp = getSection().loadDouble("kill_points", 2D);
        if (kp > 0) {
            new VaultEconomyHandler().addMoney(p, kp);
            MessageUtil.sendMessage(p, "generic.obtain_points", "%amount%", UtilsString.formatOptional2Digit(kp));
        }
    }

    public void applyWinPoints(Player p) {
        double win = getSection().loadDouble("win_points", 10D);
        if (win > 0) {
            new VaultEconomyHandler().addMoney(p, win);
            MessageUtil.sendMessage(p, "generic.obtain_points", "%amount%", UtilsString.formatOptional2Digit(win));
        }
    }

    public double getSnowballPush() {
        return this.getSection().loadDouble("snowball_push", 0.5D);
    }
}
