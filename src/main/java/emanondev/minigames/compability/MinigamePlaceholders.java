package emanondev.minigames.compability;

import emanondev.core.UtilsString;
import emanondev.core.message.DMessage;
import emanondev.minigames.GameManager;
import emanondev.minigames.GamerManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.data.GameStat;
import emanondev.minigames.data.PlayerStat;
import emanondev.minigames.gamer.Gamer;
import emanondev.minigames.games.MGame;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class MinigamePlaceholders extends PlaceholderExpansion {

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        String[] args = params.split("_");
        try {
            switch (args[0]) {
                //minigames_game_<what>_<game_id>
                case "game" -> {
                    String gameId = params.substring(args[0].length() + args[1].length() + 2);
                    @SuppressWarnings("rawtypes")
                    MGame game = GameManager.get().get(gameId);
                    if (game == null) {
                        Minigames.get().logIssue("Unable to parse placeholder %" + getIdentifier() + "_" + params + "% game " + gameId + " does not exist");
                        return "-error-";
                    }
                    return switch (args[1]) {
                        case "players" -> String.valueOf(game.getGamers().size());
                        case "spectators" -> String.valueOf(game.getSpectators());
                        case "maxplayers" -> String.valueOf(game.getMaxGamers());
                        case "phase" -> game.getPhase().getTranslatedName(player.isOnline() ? player.getPlayer() : null);
                        default -> {
                            Minigames.get().logIssue("Unable to parse placeholder %" + getIdentifier() + "_" + params + "% time unit " + args[1] + " doesn't match with existing ones");
                            yield "0";
                        }
                    };
                }
                //minigames_playerstats_<what>_<game_id>
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
                    @SuppressWarnings("rawtypes")
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
                //player_<what>
                case "player" -> {
                    Gamer gamer = GamerManager.get().getGamer(player.getUniqueId());
                    return switch (args[1]) {
                        case "xp", "exp", "experience" -> String.valueOf(gamer.getExperience());
                        case "lv", "level" -> String.valueOf(gamer.getLevel());
                        case "xptolevelup", "experiencetolevelup" -> String.valueOf(gamer.getExperienceToLevelUp());
                        case "xptoleveluppercent", "experiencetoleveluppercent" -> UtilsString.formatOptional2Digit(((double) gamer.getExperience()) / gamer.getLevelUpExperience() * 100); //TODO allow to choose format
                        case "levelupxp", "levelupexperience" -> String.valueOf(gamer.getLevelUpExperience());
                        case "xpbar" -> { //minigame_player_xpbar[_lenght][_symbol][_color1][_color2]
                            int lenght = 30;
                            if (args.length > 2) {
                                lenght = Integer.parseInt(args[2]);
                                if (lenght < 1)
                                    throw new IllegalArgumentException();
                            }
                            String symbol = "|";
                            if (args.length > 3)
                                symbol = args[3];
                            String color1 = "00FF00";
                            String color2 = "c4c4c4";
                            if (args.length > 4)
                                color1 = args[4];
                            if (args.length > 5)
                                color2 = args[5];
                            DMessage msg = new DMessage(Minigames.get(), gamer.getPlayer()).append("<#" + color1 + ">");
                            double percent = ((double) gamer.getExperience()) / gamer.getLevelUpExperience();
                            percent = percent * lenght;
                            for (int i = 0; i < lenght; i++) {
                                if (i >= percent && i - 1 < percent)
                                    msg.append("<#" + color2 + ">");
                                msg.append(symbol);
                            }
                            yield msg.toLegacy();
                        }
                        default -> throw new IllegalStateException();
                    };
                }
                default -> throw new IllegalStateException();

            }

        } catch (Exception e) {
            e.printStackTrace();
            Minigames.get().logIssue("Unable to parse placeholder %" + getIdentifier() + "_" + params + "% please check correct values");
            Minigames.get().logInfo("%minigames_playerstats_<time_unit>_<stat_id>");
            Minigames.get().logInfo("%minigames_gamestats_<time_unit>_<game_id>_stat_<stat_id>");
            Minigames.get().logInfo("Where time_unit could be: today, yesterday, currentweek, lastweek, currentmonth, lastmonth, last3months, total");
            Minigames.get().logInfo("%minigames_game_<what>_<game_id>");
            Minigames.get().logInfo("Where what could be: players, maxplayers, spectators, phase");
            Minigames.get().logInfo("%minigames_player_<what>");
            Minigames.get().logInfo("%minigames_player_xpbar[_lenght][_symbol][_color1][_color2]");
            Minigames.get().logInfo("%minigames_player_xptoleveluppercent[_format]");
            Minigames.get().logInfo("Where what could be: xp, lv, xptolevelup, levelupxp, xpbar, xptoleveluppercent");
            Minigames.get().logInfo("also lenght is the lenght of the bar in characters, symbol is the character");
            Minigames.get().logInfo("used on the bar, color1 and color2 are hexcodes like FF0000");
            Minigames.get().logInfo("finally format is the format to print the % value");
            return null;
        }
    }

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
}
