package emanondev.minigames.command;

import emanondev.core.UtilsString;
import emanondev.core.command.CoreCommand;
import emanondev.core.message.DMessage;
import emanondev.minigames.FillerManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.generic.DropsFiller;
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

public class MiniDropsFillerCommand extends CoreCommand {

    /*
     * create <id>
     * gui <id>
     * list
     * delete <id>
     *
     */
    public MiniDropsFillerCommand() {
        super("minidropsfiller", Minigames.get(), Perms.COMMAND_MINIDROPSFILLER);
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
                case "gui", "delete" -> this.complete(args[1], FillerManager.get().getAll().keySet());
                default -> Collections.emptyList();
            };
            default -> Collections.emptyList();
        };
    }

    private void help(CommandSender sender, String label, String[] args) {
        sendMsgList(sender, "minidropsfiller.help", "%label%", label);
    }

    private void delete(CommandSender sender, String label, String[] args) {
        if (args.length <= 1) {
            sendMsg(sender, "minidropsfiller.error.delete_params", "%label%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        DropsFiller group = FillerManager.get().get(id);
        if (group == null) {
            sendMsg(sender, "minidropsfiller.error.id_not_found", "%id%", id, "%label%", label);
            return;
        }
        FillerManager.get().delete(group);
        sendMsg(sender, "minidropsfiller.success.delete", "%id%", id, "%label%", label);
    }

    private void create(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        if (args.length <= 1) {
            sendMsg(player, "minidropsfiller.error.create_params", "%label%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        DropsFiller group = FillerManager.get().get(id);
        if (group != null) {
            sendMsg(player, "minidropsfiller.error.id_already_used", "%id%", id, "%label%", label);
            return;
        }
        try {
            FillerManager.get().register(args[1].toLowerCase(Locale.ENGLISH), new DropsFiller(), player);
            sendMsg(player, "minidropsfiller.success.create", "%id%", id, "%label%", label);
        } catch (IllegalArgumentException e) {
            sendMsg(player, "minidropsfiller.error.invalid_id", "%id%", id, "%label%", label);
        }
    }

    private void list(CommandSender sender, String label, String[] args) {
        DMessage msg = new DMessage(getPlugin(), sender).appendLang("minidropsfiller.success.list_prefix").newLine();
        boolean color = true;
        Color color1 = new Color(66, 233, 245);
        Color color2 = new Color(66, 179, 245);
        for (DropsFiller drop : FillerManager.get().getAll().values()) {
            msg.appendHover(
                    new DMessage(getPlugin(), sender).appendLangList("minidropsfiller.success.list_info",
                            UtilsString.merge(drop.getPlaceholders(), "%label%", label)), new DMessage(getPlugin(), sender).append(color ? color1 : color2)
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
            sendMsg(player, "minidropsfiller.error.gui_params", "%label%", label);
            return;
        }
        DropsFiller group = FillerManager.get().get(args[1]);
        if (group == null) {
            sendMsg(player, "minidropsfiller.error.id_not_found", "%label%", label, "%id%", args[1]);
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