package emanondev.minigames.games.deathmatch;

import emanondev.core.ItemBuilder;
import emanondev.core.UtilsString;
import emanondev.core.VaultEconomyHandler;
import emanondev.core.message.DMessage;
import emanondev.minigames.ArenaManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.OptionManager;
import emanondev.minigames.games.MArena;
import emanondev.minigames.games.MOption;
import emanondev.minigames.games.MType;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class DeathMatchType extends MType<DeathMatchArena, DeathMatchOption> {
    public DeathMatchType() {
        super("deathmatch", DeathMatchArena.class, DeathMatchOption.class, Minigames.get());
        ConfigurationSerialization.registerClass(DeathMatchArena.class);
        ConfigurationSerialization.registerClass(DeathMatchOption.class);
        ConfigurationSerialization.registerClass(DeathMatchGame.class);
    }

    @Override
    @NotNull
    public DeathMatchArenaBuilder getArenaBuilder(@NotNull UUID uuid, @NotNull String id, @NotNull String label) {
        return new DeathMatchArenaBuilder(uuid, id, label);
    }

    @Override
    public @NotNull DeathMatchOption createDefaultOptions() {
        return new DeathMatchOption();
    }

    @Override
    public @NotNull DeathMatchGame createGame(@NotNull String arenaId, @NotNull String optionId) {
        MArena arena = ArenaManager.get().get(arenaId);
        if (arena == null || !this.matchType(arena))
            throw new IllegalStateException();
        MOption option = OptionManager.get().get(optionId);
        if (option == null || !this.matchType(option))
            throw new IllegalStateException();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("option", optionId);
        map.put("arena", arenaId);
        return new DeathMatchGame(map);
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
            new DMessage(getPlugin(), p).appendLang("generic.obtain_points", "%amount%", UtilsString.formatOptional2Digit(kp));
        }
    }

    public void applyWinPoints(Player p) {
        double win = getSection().loadDouble("game.win_points", 10D);
        if (win > 0) {
            new VaultEconomyHandler().addMoney(p, win);
            new DMessage(getPlugin(), p).appendLang("generic.obtain_points", "%amount%", UtilsString.formatOptional2Digit(win));
        }
    }
}
