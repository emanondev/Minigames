package emanondev.minigames.compability;

import emanondev.minigames.GameManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.data.GameStat;
import emanondev.minigames.data.PlayerStat;
import emanondev.minigames.generic.MGame;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class MinigamePlaceholders extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "minigames";
    }

    @Override
    public @NotNull String getAuthor() {
        return "emanon";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        String[] args = params.split("_");
        try {
            switch (args[0]) {
                //minigames_playerstats_<what>_<game_id>
                case "game" -> {
                    String gameId = params.substring(args[0].length() + args[1].length() + 2);
                    MGame game = GameManager.get().get(gameId);
                    if (game == null) {
                        Minigames.get().logIssue("Unable to parse placeholder %" + getIdentifier() + "_" + params + "% game " + gameId + " does not exist");
                        return "-error-";
                    }
                    return switch (args[1]) {
                        case "players" -> String.valueOf(game.getGamers().size());
                        case "spectators" -> String.valueOf(game.getMaxGamers());
                        case "maxplayers" -> String.valueOf(game.getSpectators());
                        case "phase" -> game.getPhase().name(); //TODO translate that
                        default -> {
                            Minigames.get().logIssue("Unable to parse placeholder %" + getIdentifier() + "_" + params + "% time unit " + args[1] + " doesn't match with existing ones");
                            yield "0";
                        }
                    };
                }
                case "playerstats" -> {
                    String statId = params.substring(args[0].length() + args[1].length() + 2);
                    return switch (args[1]) {
                        case "today" -> String.valueOf(PlayerStat.getStat(statId).getToday(player));
                        case "yesterday" -> String.valueOf(PlayerStat.getStat(statId).getYesterday(player));
                        case "currentweek" -> String.valueOf(PlayerStat.getStat(statId).getCurrentWeek(player));
                        case "lastweek" -> String.valueOf(PlayerStat.getStat(statId).getLastWeek(player));
                        case "currentmonth" -> String.valueOf(PlayerStat.getStat(statId).getCurrentMonth(player));
                        case "lastmonth" -> String.valueOf(PlayerStat.getStat(statId).getLastMonth(player));
                        case "last3months" -> String.valueOf(PlayerStat.getStat(statId).getLastThreeMonths(player));
                        case "total" -> String.valueOf(PlayerStat.getStat(statId).getTotal(player));
                        default -> {
                            Minigames.get().logIssue("Unable to parse placeholder %" + getIdentifier() + "_" + params + "% time unit " + args[1] + " doesn't match with existing ones");
                            yield "0";
                        }
                    };
                }
                //minigames_gamestats_<game_id>_stat_<stat_id>
                case "gamestats" -> {
                    String[] ids = params.substring(args[0].length() + 1).split("_stat_");
                    MGame game = GameManager.get().get(ids[0]);
                    if (game == null) {
                        Minigames.get().logIssue("Unable to parse placeholder %" + getIdentifier() + "_" + params + "% game " + ids[0] + " does not exist");
                        return "0";
                    }
                    return switch (args[1]) {
                        case "today" -> String.valueOf(GameStat.getStat(ids[1]).getToday(game));
                        case "yesterday" -> String.valueOf(GameStat.getStat(ids[1]).getYesterday(game));
                        case "currentweek" -> String.valueOf(GameStat.getStat(ids[1]).getCurrentWeek(game));
                        case "lastweek" -> String.valueOf(GameStat.getStat(ids[1]).getLastWeek(game));
                        case "currentmonth" -> String.valueOf(GameStat.getStat(ids[1]).getCurrentMonth(game));
                        case "lastmonth" -> String.valueOf(GameStat.getStat(ids[1]).getLastMonth(game));
                        case "last3months" -> String.valueOf(GameStat.getStat(ids[1]).getLastThreeMonths(game));
                        case "total" -> String.valueOf(GameStat.getStat(ids[1]).getTotal(game));
                        default -> {
                            Minigames.get().logIssue("Unable to parse placeholder %" + getIdentifier() + "_" + params + "% time unit " + args[1] + " doesn't match with existing ones");
                            yield "0";
                        }
                    };
                }
                default -> {
                    throw new IllegalStateException();
                }

            }

        } catch (Exception e) {
            Minigames.get().logIssue("Unable to parse placeholder %" + getIdentifier() + "_" + params + "% please check correct values");
            Minigames.get().logInfo("%minigames_playerstats_<time_unit>_<stat_id>");
            Minigames.get().logInfo("%minigames_gamestats_<time_unit>_<game_id>_stat_<stat_id>");
            Minigames.get().logInfo("Where time_unit could be: today, yesterday, currentweek, lastweek, currentmonth, lastmonth, last3months, total");
            Minigames.get().logInfo("%minigames_game_<what>_<game_id>");
            Minigames.get().logInfo("Where what could be: players, maxplayers, spectators, phase");
            return null;
        }
    }
}
