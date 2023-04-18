package emanondev.minigames.data;

import emanondev.core.YMLConfig;
import emanondev.minigames.Minigames;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

public class PlayerStat extends Stat<UUID> {

    private static final YMLConfig conf = Minigames.get().getConfig("data" + File.separator + "user_data.yml");
    private static final HashMap<String, PlayerStat> stats = new HashMap<>();

    public static final PlayerStat DEATHMATCH_DEATHS = getStat("DEATHMATCH_DEATHS");
    public static final PlayerStat DEATHMATCH_KILLS = getStat("DEATHMATCH_KILLS");
    public static final PlayerStat DEATHMATCH_VICTORY = getStat("DEATHMATCH_VICTORY");
    public static final PlayerStat DEATHMATCH_PLAYED = getStat("DEATHMATCH_PLAYED");

    public static final PlayerStat SKYWARS_DEATHS = getStat("DEATHMATCH_DEATHS");
    public static final PlayerStat SKYWARS_KILLS = getStat("SKYWARS_KILLS");
    public static final PlayerStat SKYWARS_VICTORY = getStat("SKYWARS_VICTORY");
    public static final PlayerStat SKYWARS_PLAYED = getStat("SKYWARS_PLAYED");

    public static final PlayerStat EGGWARS_DEATHS = getStat("DEATHMATCH_DEATHS");
    public static final PlayerStat EGGWARS_KILLS = getStat("EGGWARS_KILLS");
    public static final PlayerStat EGGWARS_VICTORY = getStat("EGGWARS_VICTORY");
    public static final PlayerStat EGGWARS_PLAYED = getStat("EGGWARS_PLAYED");

    public static final PlayerStat ELYTRARACE_PLAYED = getStat("ELYTRARACE_PLAYED");
    public static final PlayerStat ELYTRARACE_VICTORY = getStat("ELYTRARACE_VICTORY");
    public static final PlayerStat ELYTRARACE_VICTORY_FIRST = getStat("ELYTRARACE_VICTORY_FIRST");
    public static final PlayerStat ELYTRARACE_VICTORY_SECOND = getStat("ELYTRARACE_VICTORY_SECOND");
    public static final PlayerStat ELYTRARACE_VICTORY_THIRD = getStat("ELYTRARACE_VICTORY_THIRD");

    public static final PlayerStat HORSERACE_PLAYED = getStat("HORSERACE_PLAYED");
    public static final PlayerStat HORSERACE_VICTORY = getStat("HORSERACE_VICTORY");
    public static final PlayerStat HORSERACE_VICTORY_FIRST = getStat("HORSERACE_VICTORY_FIRST");
    public static final PlayerStat HORSERACE_VICTORY_SECOND = getStat("HORSERACE_VICTORY_SECOND");
    public static final PlayerStat HORSERACE_VICTORY_THIRD = getStat("HORSERACE_VICTORY_THIRD");

    public static final PlayerStat RUNRACE_PLAYED = getStat("RUNRACE_PLAYED");
    public static final PlayerStat RUNRACE_VICTORY = getStat("RUNRACE_VICTORY");
    public static final PlayerStat RUNRACE_VICTORY_FIRST = getStat("RUNRACE_VICTORY_FIRST");
    public static final PlayerStat RUNRACE_VICTORY_SECOND = getStat("RUNRACE_VICTORY_SECOND");
    public static final PlayerStat RUNRACE_VICTORY_THIRD = getStat("RUNRACE_VICTORY_THIRD");

    public static final PlayerStat BOATRACE_PLAYED = getStat("BOATRACE_PLAYED");
    public static final PlayerStat BOATRACE_VICTORY = getStat("BOATRACE_VICTORY");
    public static final PlayerStat BOATRACE_VICTORY_FIRST = getStat("BOATRACE_VICTORY_FIRST");
    public static final PlayerStat BOATRACE_VICTORY_SECOND = getStat("BOATRACE_VICTORY_SECOND");
    public static final PlayerStat BOATRACE_VICTORY_THIRD = getStat("BOATRACE_VICTORY_THIRD");

    public static final PlayerStat GAME_PLAYED = getStat("GAME_PLAYED");


    private PlayerStat(@NotNull String id) {
        super(id);
    }

    public static PlayerStat getStat(@NotNull String id) {
        id = id.toLowerCase();
        if (stats.containsKey(id))
            return stats.get(id);
        PlayerStat stat = new PlayerStat(id);
        stats.put(id, stat);
        return stat;
    }

    @Override
    protected @NotNull YMLConfig getConfig() {
        return conf;
    }

    @Override
    protected @NotNull String getId(@NotNull UUID target) {
        return target.toString();
    }

    public void add(@NotNull OfflinePlayer target, int amount) {
        add(target.getUniqueId(), amount);
    }

    public void add(@NotNull OfflinePlayer target, @NotNull Calendar day, int amount) {
        add(target.getUniqueId(), day, amount);
    }

    public int getTotal(@NotNull OfflinePlayer target) {
        return getTotal(target.getUniqueId());
    }

    public int getToday(@NotNull OfflinePlayer target) {
        return getToday(target.getUniqueId());
    }

    public int getYesterday(@NotNull OfflinePlayer target) {
        return getYesterday(target.getUniqueId());
    }

    public int getCurrentWeek(@NotNull OfflinePlayer target) {
        return getCurrentWeek(target.getUniqueId());
    }

    public int getLastWeek(@NotNull OfflinePlayer target) {
        return getLastWeek(target.getUniqueId());
    }

    public int getCurrentMonth(@NotNull OfflinePlayer target) {
        return getCurrentMonth(target.getUniqueId());
    }

    public int getLastMonth(@NotNull OfflinePlayer target) {
        return getLastMonth(target.getUniqueId());
    }

    public int getLastThreeMonths(@NotNull OfflinePlayer target) {
        return getLastThreeMonths(target.getUniqueId());
    }
}
