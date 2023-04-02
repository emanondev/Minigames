package emanondev.minigames.command;

import emanondev.core.UtilsString;
import emanondev.core.command.CoreCommand;
import emanondev.core.message.DMessage;
import emanondev.minigames.FillerManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.generic.DropsFiller;
import emanondev.minigames.generic.Perms;
import emanondev.minigames.generic.Registrable;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.*;

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
        sendDMessage(sender, "minidropsfiller.help", "%alias%", label);
    }

    private void delete(CommandSender sender, String label, String[] args) {
        if (args.length <= 1) {
            sendDMessage(sender, "minidropsfiller.error.delete_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        DropsFiller group = FillerManager.get().get(id);
        if (group == null) {
            sendDMessage(sender, "minidropsfiller.error.id_not_found", "%id%", id, "%alias%", label);
            return;
        }
        //TODO is used?
        FillerManager.get().delete(group);
        sendDMessage(sender, "minidropsfiller.success.delete", "%id%", id, "%alias%", label);
    }

    private void create(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        if (args.length <= 1) {
            sendDMessage(player, "minidropsfiller.error.create_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        DropsFiller group = FillerManager.get().get(id);
        if (group != null) {
            sendDMessage(player, "minidropsfiller.error.id_already_used", "%id%", id, "%alias%", label);
            return;
        }
        try {
            FillerManager.get().register(args[1].toLowerCase(Locale.ENGLISH), new DropsFiller(), player);
            sendDMessage(player, "minidropsfiller.success.create", "%id%", id, "%alias%", label);
        } catch (IllegalArgumentException e) {
            sendDMessage(player, "minidropsfiller.error.invalid_id", "%id%", id, "%alias%", label);
        }
    }

    private void list(CommandSender sender, String label, String[] args) {
        DMessage msg = new DMessage(getPlugin(), sender).appendLang("minidropsfiller.success.list_prefix").newLine();
        boolean color = true;
        Color color1 = new Color(66, 233, 245);
        Color color2 = new Color(66, 179, 245);
        ArrayList<DropsFiller> list = new ArrayList<>(FillerManager.get().getAll().values());
        list.sort(Comparator.comparing(Registrable::getId));
        for (DropsFiller filler : list) {
            msg.appendHover(
                    new DMessage(getPlugin(), sender).appendLang("minidropsfiller.success.list_info",
                            UtilsString.merge(filler.getPlaceholders(), "%alias%", label)), new DMessage(getPlugin(), sender).append(color ? color1 : color2)
                            .appendRunCommand("/" + label + " gui " + filler.getId(), filler.getId())).append(" ");
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
            sendDMessage(player, "minidropsfiller.error.gui_params", "%alias%", label);
            return;
        }
        DropsFiller group = FillerManager.get().get(args[1]);
        if (group == null) {
            sendDMessage(player, "minidropsfiller.error.id_not_found", "%alias%", label, "%id%", args[1]);
            return;
        }
        group.getEditorGui(player, null).open(player);
    }
}