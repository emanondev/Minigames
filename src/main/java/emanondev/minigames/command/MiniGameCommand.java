package emanondev.minigames.command;

import emanondev.core.UtilsString;
import emanondev.core.command.CoreCommandPlus;
import emanondev.core.message.DMessage;
import emanondev.minigames.*;
import emanondev.minigames.generic.*;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.*;

public class MiniGameCommand extends CoreCommandPlus {

    /*
    create <id> <type> <arena> <option>
    tp <id>
    stop <id>
    reset <id>
    list [type] <- filtro opzionale
    delete
    setspawn
    unsetspawn
     */
    public MiniGameCommand() {
        super("MiniGame", Minigames.get(), Perms.COMMAND_MINIGAME, "Sets Games");
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> onComplete(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args, @Nullable Location location) {
        return switch (args.length) {
            case 1 -> this.complete(args[0], List.of("create", "list", "delete", "tp"));
            case 2 -> switch (args[0].toLowerCase()) {
                case "list" -> this.complete(args[1], MinigameTypes.get().getTypesId());
                case "delete", "tp", "stop", "reset" -> this.complete(args[1], GameManager.get().getAll().keySet());
                default -> Collections.emptyList();
            };
            case 3 -> switch (args[0].toLowerCase()) {
                case "create" -> this.complete(args[2], MinigameTypes.get().getTypesId());
                case "delete" -> this.complete(args[2], GameManager.get().getAll().keySet());
                default -> Collections.emptyList();
            };
            case 4 -> switch (args[0].toLowerCase()) {
                case "create" -> {
                    @SuppressWarnings("rawtypes")
                    MType mtype = MinigameTypes.get().getType(args[2]);
                    yield mtype == null ? Collections.emptyList() :
                            this.complete(args[3], ArenaManager.get().getCompatibleArenas(mtype).keySet());
                }
                default -> Collections.emptyList();
            };
            case 5 -> switch (args[0].toLowerCase()) {
                case "create" -> {
                    @SuppressWarnings("rawtypes")
                    MType mtype = MinigameTypes.get().getType(args[2]);
                    yield mtype == null ? Collections.emptyList() :
                            this.complete(args[4], OptionManager.get().getCompatibles(mtype).keySet());
                }
                default -> Collections.emptyList();
            };
            default -> Collections.emptyList();
        };
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            help(sender, label, args);
            return;
        }
        switch (args[0].toLowerCase(Locale.ENGLISH)) {
            case "create" -> create(sender, label, args);
            case "tp" -> tp(sender, label, args);
            case "stop" -> stop(sender, label, args);
            case "reset" -> reset(sender, label, args);
            case "list" -> list(sender, label, args);
            case "delete" -> delete(sender, label, args);
            case "setspawn" -> setspawn(sender, label, args);
            case "unsetspawn" -> unsetspawn(sender, label, args);
            default -> help(sender, label, args);
        }
    }

    private void unsetspawn(CommandSender sender, String label, String[] args) {
        C.setRespawnLocation(null);
        sendDMessage(sender, "minigame.success.unsetspawn", "%alias%", label);
    }

    private void setspawn(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        C.setRespawnLocation(player.getLocation());
        sendDMessage(sender, "minigame.success.setspawn", "%alias%", label);
    }

    private void tp(CommandSender sender, String label, String[] args) {
        sendDMessage(sender, "not_implemented");
    }

    private void stop(CommandSender sender, String label, String[] args) {
        sendDMessage(sender, "not_implemented");
    }

    private void reset(CommandSender sender, String label, String[] args) {
        sendDMessage(sender, "not_implemented");
    }

    private void delete(CommandSender sender, String label, String[] args) {
        if (args.length <= 1) {
            sendDMessage(sender, "minigame.error.delete_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        @SuppressWarnings("rawtypes")
        MGame group = GameManager.get().get(id);
        if (group == null) {
            sendDMessage(sender, "minigame.error.id_not_found", "%id%", id, "%alias%", label);
            return;
        }
        GameManager.get().delete(group);
        sendDMessage(sender, "minigame.success.delete", "%id%", id, "%alias%", label);
    }

    //minigame list [type]
    private void list(CommandSender sender, String label, String[] args) {
        @SuppressWarnings("rawtypes")
        MType type = null;
        if (args.length > 1) {
            type = MinigameTypes.get().getType(args[1]);
            if (type == null) {
                sendDMessage(sender, "minigame.error.invalid_minigametype", "%type%", args[1], "%alias%", label);
                return;
            }
        }

        DMessage msg = new DMessage(getPlugin(), sender).appendLang("minigame.success.list_prefix").newLine();
        boolean color = true;
        Color color1 = new Color(66, 233, 245);
        Color color2 = new Color(66, 179, 245);
        @SuppressWarnings("rawtypes")
        ArrayList<MGame> list = new ArrayList<>(GameManager.get().getAll().values());
        list.sort(Comparator.comparing(Registrable::getId));
        for (@SuppressWarnings("rawtypes") MGame game : list) {
            if (type == null || game.getMinigameType().equals(type)) {
                msg.appendHover(
                        new DMessage(getPlugin(), sender).appendLang("minigame.success.list_info",
                                UtilsString.merge(game.getPlaceholders(), "%alias%", label)), new DMessage(getPlugin(), sender).append(color ? color1 : color2)
                                .appendRunCommand("/" + label + " tp " + game.getId(), game.getId())).append(" ");
                color = !color;
            }
        }
        msg.send();
    }

    private void help(CommandSender sender, String label, String[] args) {
        sendDMessage(sender, "minigame.help", "%alias%", label);
    }


    //create id type arena option
    @SuppressWarnings("unchecked")
    private void create(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }

        if (args.length != 5) {
            sendDMessage(player, "minigame.error.create_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase();
        if (!UtilsString.isLowcasedValidID(id)) {
            sendDMessage(player, "minigame.error.invalid_id", "%id%", id, "%alias%", label);
            return;
        }
        if (GameManager.get().get(id) != null) {
            sendDMessage(player, "minigame.error.id_already_used", "%id%", id, "%alias%", label);
            return;
        }
        @SuppressWarnings("rawtypes")
        MType mType = MinigameTypes.get().getType(args[2]);
        if (mType == null) {
            sendDMessage(player, "minigame.error.invalid_minigametype", "%type%", args[2], "%alias%", label);
            return;
        }
        MArena arena = ArenaManager.get().get(args[3]);
        if (arena == null) {
            sendDMessage(player, "minigame.error.arena_not_found", "%arena%", args[3], "%alias%", label);
            return;
        }
        if (!mType.matchType(arena)) {
            sendDMessage(player, "minigame.error.invalid_arena_type", "%type%", args[2], "%arena%", args[3], "%alias%", label);
            return;
        }
        MOption option = OptionManager.get().get(args[4]);
        if (option == null) {
            sendDMessage(player, "minigame.error.option_not_found", "%option%", args[4], "%alias%", label);
            return;
        }
        if (!mType.matchType(option)) {
            sendDMessage(player, "minigame.error.invalid_option_type", "%type%", args[2], "%option%", args[4], "%alias%", label);
            return;
        }
        @SuppressWarnings("rawtypes")
        MGame mGame = mType.createGame(arena.getId(), option.getId());
        GameManager.get().register(id, mGame, player);
        mGame.initialize();
        sendDMessage(player, "minigame.success.create", UtilsString.merge(mGame.getPlaceholders(), "%alias%", label));
    }
}
