package emanondev.minigames.skywars;

import emanondev.core.UtilsString;
import emanondev.core.VaultEconomyHandler;
import emanondev.minigames.ArenaManager;
import emanondev.minigames.MessageUtil;
import emanondev.minigames.OptionManager;
import emanondev.minigames.generic.MArena;
import emanondev.minigames.generic.MOption;
import emanondev.minigames.generic.MType;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
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

    public void applyKillPoints(Player p) {
        double kp = getSection().loadDouble("kill_points", 2D);
        if (kp > 0) {
            new VaultEconomyHandler().addMoney(p, kp);
            MessageUtil.sendMessage(p,"generic.obtain_points","%amount%", UtilsString.formatOptional2Digit(kp));
        }
    }

    public void applyWinPoints(Player p) {
        double win = getSection().loadDouble("win_points", 10D);
        if (win > 0) {
            new VaultEconomyHandler().addMoney(p, win);
            MessageUtil.sendMessage(p,"generic.obtain_points","%amount%", UtilsString.formatOptional2Digit(win));
        }
    }
}
