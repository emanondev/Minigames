package emanondev.minigames;

import emanondev.core.UtilsMath;
import emanondev.core.YMLConfig;
import emanondev.core.YMLSection;
import emanondev.minigames.gamer.Gamer;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class GamerManager {
    private static GamerManager instance = null;
    private final HashMap<UUID, Gamer> gamers = new HashMap<>();


    public @NotNull Gamer getGamer(@NotNull UUID user) {
        if (gamers.containsKey(user))
            return gamers.get(user);
        Gamer gamer = new Gamer(user);
        gamers.put(gamer.getUniqueId(), gamer);
        return gamer;
    }

    public @NotNull Gamer getGamer(@NotNull OfflinePlayer user) {
        return getGamer(user.getUniqueId());
    }

    public GamerManager() {
        if (instance != null)
            throw new IllegalStateException();
        GamerManager.instance = this;
    }

    public static GamerManager get() {
        return instance;
    }


    private final HashMap<Integer, Long> levelupExp = new HashMap<>();

    private int maxLevel;

    public @Range(from = 1, to = Integer.MAX_VALUE) int getMaxLevel() {
        return maxLevel;
    }

    public @Range(from = 0, to = Long.MAX_VALUE) long getLevelUpExperience(@Range(from = 1, to = Integer.MAX_VALUE) int level) {
        if (level >= maxLevel)
            throw new IllegalArgumentException();
        return levelupExp.getOrDefault(level, Long.MAX_VALUE);
    }


    public void reload() {
        @NotNull YMLConfig conf = Minigames.get().getConfig("gamersConfig.yml");
        maxLevel = Math.max(1, conf.getInt("max_level"));
        levelupExp.clear();
        String formula = conf.getString("exp_formula");
        for (int i = 1; i < maxLevel; i++) {
            try {
                levelupExp.put(i, Math.max(1L, (long) UtilsMath.eval(formula.replace("%level%", String.valueOf(i)))));
            } catch (Exception e) {
                Minigames.get().logIssue("Invalid experience formula at &egamersConfig.yml &fon &eexp_formula &ffor level &e" +
                        i + " &f(&e" + formula + "&f)");
                levelupExp.put(i, Long.MAX_VALUE);
            }
        }
        gamers.values().forEach(Gamer::save);
    }

    public YMLSection getDatabaseSection(Gamer gamer) {
        return Minigames.get().getConfig("data" + File.separator + "gamers.yml").loadSection(gamer.getUniqueId().toString());
    }
}
