package emanondev.minigames.command;

import emanondev.core.UtilsString;
import emanondev.core.command.CoreCommand;
import emanondev.core.message.DMessage;
import emanondev.minigames.ArenaManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.generic.MArena;
import emanondev.minigames.generic.MSchemArena;
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

public class MiniArenaCommand extends CoreCommand {

    /* //TODO filter list with type
     * list
     * delete <id>
     */
    public MiniArenaCommand() {
        super("miniarena", Minigames.get(), Perms.COMMAND_MINIOPTION
                , "setup arena");
    }


    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            help(sender, label, args);
            return;
        }
        switch (args[0].toLowerCase(Locale.ENGLISH)) {
            case "list" -> list(sender, label, args);
            case "delete" -> delete(sender, label, args);
            case "paste" -> paste(sender, label, args);
            case "gui" -> gui(sender, label, args);
            default -> help(sender, label, args);
        }
    }

    private void paste(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        if (args.length <= 1) {
            sendMsg(sender, "miniarena.error.paste_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        MArena arena = ArenaManager.get().get(id);
        if (arena == null) {
            sendMsg(sender, "miniarena.error.id_not_found", "%id%", id, "%alias%", label);
            return;
        }
        if (!(arena instanceof MSchemArena schemArena)) {
            sendMsg(sender, "miniarena.error.no_schematic", "%id%", id, "%alias%", label);
            return;
        }
        schemArena.paste(player.getLocation());
        //WorldEditUtility.paste(player.getLocation(), schemArena.getSchematic(), true, getPlugin(), false, false, false);
        sendMsg(sender, "miniarena.success.paste", "%id%", id, "%alias%", label);
    }

    private void gui(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        if (args.length <= 1) {
            sendMsg(sender, "miniarena.error.gui_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        MArena arena = ArenaManager.get().get(id);
        if (arena == null) {
            sendMsg(sender, "miniarena.error.id_not_found", "%id%", id, "%alias%", label);
            return;
        }
        arena.getEditorGui(player, null).open(player);
    }

    @Override
    public @Nullable List<String> onComplete(@NotNull CommandSender sender, @NotNull String label, String @NotNull [] args, @Nullable Location location) {
        return switch (args.length) {
            case 1 -> this.complete(args[0], List.of("list", "delete", "paste", "gui"));
            case 2 -> switch (args[0].toLowerCase(Locale.ENGLISH)) {
                case "delete", "paste", "gui" -> this.complete(args[1], ArenaManager.get().getAll().keySet());
                default -> Collections.emptyList();
            };
            default -> Collections.emptyList();
        };
    }

    private void help(CommandSender sender, String label, String[] args) {
        sendMsgList(sender, "miniarena.help", "%alias%", label);
    }

    private void delete(CommandSender sender, String label, String[] args) {
        if (args.length <= 1) {
            sendMsg(sender, "miniarena.error.delete_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        MArena arena = ArenaManager.get().get(id);
        if (arena == null) {
            sendMsg(sender, "miniarena.error.id_not_found", "%id%", id, "%alias%", label);
            return;
        }
        //TODO is used???
        ArenaManager.get().delete(arena);
        sendMsg(sender, "miniarena.success.delete", "%id%", id, "%alias%", label);
    }

    private void list(CommandSender sender, String label, String[] args) {
        DMessage msg = new DMessage(getPlugin(), sender).appendLang("miniarena.success.list_prefix").newLine();
        boolean color = true;
        Color color1 = new Color(66, 233, 245);
        Color color2 = new Color(66, 179, 245);
        ArrayList<MArena> list = new ArrayList<>(ArenaManager.get().getAll().values());
        list.sort(Comparator.comparing(Registrable::getId));
        for (MArena arena : list) {
            msg.appendHover(
                    new DMessage(getPlugin(), sender).appendLangList("miniarena.success.list_info",
                            UtilsString.merge(arena.getPlaceholders(), "%alias%", label)), new DMessage(getPlugin(), sender).append(color ? color1 : color2)
                            .appendRunCommand("/" + label + " gui " + arena.getId(), arena.getId())).append(" ");
            color = !color;
        }
        msg.send();
    }

    private void sendMsg(CommandSender target, String path, String... holders) {
        new DMessage(getPlugin(), target).appendLang(path, holders).send();
    }

    private void sendMsgList(CommandSender target, String path, String... holders) {
        new DMessage(getPlugin(), target).appendLangList(path, holders).send();
    }
}