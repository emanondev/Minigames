package emanondev.minigames.data;

import emanondev.core.YMLConfig;
import emanondev.minigames.Minigames;
import emanondev.minigames.generic.MGame;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;

public class GameStat extends Stat<MGame> {

    private static final YMLConfig conf = Minigames.get().getConfig("data" + File.separator + "game_data.yml");
    private static final HashMap<String, GameStat> stats = new HashMap<>();

    private GameStat(@NotNull String id) {
        super(id);
    }

    public static GameStat getStat(@NotNull String id) {
        id = id.toLowerCase();
        if (stats.containsKey(id))
            return stats.get(id);
        GameStat stat = new GameStat(id);
        stats.put(id, stat);
        return stat;
    }

    @Override
    protected @NotNull YMLConfig getConfig() {
        return conf;
    }

    @Override
    protected @NotNull String getId(@NotNull MGame target) {
        return target.getId();
    }

    public final static GameStat PLAY_TIMES = getStat("PLAY_TIMES");
    public final static GameStat TOTAL_TIME_MS = getStat("TOTAL_TIME_MS");

}
