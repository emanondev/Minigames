package emanondev.minigames.command;

import emanondev.core.UtilsString;
import emanondev.core.command.CoreCommand;
import emanondev.core.message.DMessage;
import emanondev.minigames.MinigameTypes;
import emanondev.minigames.Minigames;
import emanondev.minigames.OptionManager;
import emanondev.minigames.generic.MOption;
import emanondev.minigames.generic.MType;
import emanondev.minigames.generic.Perms;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MiniOptionCommand extends CoreCommand {

    /*
     * create <id> <type>
     * gui <id>
     * list
     * delete <id>
     */
    public MiniOptionCommand() {
        super("minioption", Minigames.get(), Perms.COMMAND_MINIOPTION
                , "setup options");
    }


    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            help(sender, label, args);
            return;
        }
        switch (args[0].toLowerCase(Locale.ENGLISH)) {
            case "create" -> create(sender, label, args);
            case "gui" -> gui(sender, label, args);
            case "list" -> list(sender, label, args);
            case "delete" -> delete(sender, label, args);
            default -> help(sender, label, args);
        }
    }

    @Override
    public @Nullable List<String> onComplete(@NotNull CommandSender sender, @NotNull String label, String @NotNull [] args, @Nullable Location location) {
        return switch (args.length) {
            case 1 -> this.complete(args[0], List.of("create", "gui", "list", "delete"));
            case 2 -> switch (args[0].toLowerCase(Locale.ENGLISH)) {
                case "gui", "delete" -> this.complete(args[1], OptionManager.get().getAll().keySet());
                default -> Collections.emptyList();
            };
            case 3 -> switch (args[0].toLowerCase(Locale.ENGLISH)) {
                case "create" -> this.complete(args[2], MinigameTypes.get().getTypesId());
                default -> Collections.emptyList();
            };
            default -> Collections.emptyList();
        };
    }

    private void help(CommandSender sender, String label, String[] args) {
        sendMsgList(sender, "minioption.help", "%alias%", label);
    }

    private void delete(CommandSender sender, String label, String[] args) {
        if (args.length <= 1) {
            sendMsg(sender, "minioption.error.delete_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        MOption group = OptionManager.get().get(id);
        if (group == null) {
            sendMsg(sender, "minioption.error.id_not_found", "%id%", id, "%alias%", label);
            return;
        }
        //TODO is used???
        OptionManager.get().delete(group);
        sendMsg(sender, "minioption.success.delete", "%id%", id, "%alias%", label);
    }

    private void create(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        if (args.length != 3) {
            sendMsg(player, "minioption.error.create_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        MOption group = OptionManager.get().get(id);
        if (group != null) {
            sendMsg(player, "minioption.error.id_already_used", "%id%", id, "%alias%", label);
            return;
        }
        @SuppressWarnings("rawtypes")
        MType type = MinigameTypes.get().getType(args[2]);
        if (type == null) {
            sendMsg(player, "minioption.error.invalid_minigametype", "%type%", args[2], "%alias%", label);
            return;
        }
        try {
            OptionManager.get().register(id, type.createDefaultOptions(), player);
            sendMsg(player, "minioption.success.create", "%id%", id, "%type%", type.getType(), "%alias%", label);
        } catch (IllegalArgumentException e) {
            sendMsg(player, "minioption.error.invalid_id", "%id%", id, "%alias%", label);
        }
    }

    private void list(CommandSender sender, String label, String[] args) {
        DMessage msg = new DMessage(getPlugin(), sender).appendLang("minioption.success.list_prefix").newLine();
        boolean color = true;
        Color color1 = new Color(66, 233, 245);
        Color color2 = new Color(66, 179, 245);
        for (MOption drop : OptionManager.get().getAll().values()) {
            msg.appendHover(
                    new DMessage(getPlugin(), sender).appendLangList("minioption.success.list_info",
                            UtilsString.merge(drop.getPlaceholders(), "%alias%", label)), new DMessage(getPlugin(), sender).append(color ? color1 : color2)
                            .appendRunCommand("/" + label + " gui " + drop.getId(), drop.getId())).append(" ");
            color = !color;
        }
        msg.send();
    }

    private void gui(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        if (args.length <= 1) {
            sendMsg(player, "minioption.error.gui_params", "%alias%", label);
            return;
        }
        MOption group = OptionManager.get().get(args[1]);
        if (group == null) {
            sendMsg(player, "minioption.error.id_not_found", "%alias%", label, "%id%", args[1]);
            return;
        }
        group.getEditorGui(player, null).open(player);
    }

    private void sendMsg(CommandSender target, String path, String... holders) {
        new DMessage(getPlugin(), target).appendLang(path, holders).send();
    }

    private void sendMsgList(CommandSender target, String path, String... holders) {
        new DMessage(getPlugin(), target).appendLangList(path, holders).send();
    }
}