package emanondev.minigames.command;

import emanondev.core.UtilsString;
import emanondev.core.command.CoreCommand;
import emanondev.core.message.DMessage;
import emanondev.minigames.Kit;
import emanondev.minigames.KitManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.Perms;
import emanondev.minigames.games.Registrable;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.*;

public class MiniKitCommand extends CoreCommand {
    /*
    create <id>
    update <id>
    apply <id> [player]
    list
    price <id> <amount>
    delete <id>
     */
    public MiniKitCommand() {
        super("minikit", Minigames.get(), Perms.COMMAND_MINIKIT,
                "set minigame available kits");
    }


    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            help(sender, label, args);
            return;
        }
        switch (args[0].toLowerCase(Locale.ENGLISH)) {
            case "create" -> create(sender, label, args);
            case "update" -> update(sender, label, args);
            case "apply" -> apply(sender, label, args);
            case "price" -> price(sender, label, args);
            case "gui" -> gui(sender, label, args);
            case "list" -> list(sender, label, args);
            case "delete" -> delete(sender, label, args);
            default -> help(sender, label, args);
        }
    }

    @Override
    public @Nullable List<String> onComplete(@NotNull CommandSender sender, @NotNull String label, String @NotNull [] args, @Nullable Location location) {
        return switch (args.length) {
            case 1 -> this.complete(args[0], List.of("create", "update", "apply", "list", "delete", "price", "gui"));
            case 2 -> switch (args[0].toLowerCase(Locale.ENGLISH)) {
                case "gui", "update", "apply", "delete", "price" -> this.complete(args[1], KitManager.get().getAll().keySet());
                default -> Collections.emptyList();
            };
            default -> Collections.emptyList();
        };
    }

    private void help(CommandSender sender, String label, String[] args) {
        sendDMessage(sender, "minikit.help", "%alias%", label);
    }

    private void create(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        if (args.length <= 1) {
            sendDMessage(player, "minikit.error.create_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        Kit group = KitManager.get().get(id);
        if (group != null) {
            sendDMessage(player, "minikit.error.id_already_used", "%id%", id, "%alias%", label);
            return;
        }
        try {
            KitManager.get().register(args[1].toLowerCase(Locale.ENGLISH), Kit.fromPlayer(player), player);
            sendDMessage(player, "minikit.success.create", "%id%", id, "%alias%", label);
        } catch (IllegalArgumentException e) {
            sendDMessage(player, "minikit.error.invalid_id", "%id%", id, "%alias%", label);
        }
    }

    private void update(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        if (args.length <= 1) {
            sendDMessage(sender, "minikit.error.update_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        Kit kit = KitManager.get().get(id);
        if (kit == null) {
            sendDMessage(sender, "minikit.error.id_not_found", "%id%", id, "%alias%", label);
            return;
        }
        kit.updateSnapshot(player);
        sendDMessage(sender, "minikit.success.update", "%id%", id, "%alias%", label);

    }

    private void apply(CommandSender sender, String label, String[] args) {
        if (args.length != 2 && args.length != 3) {
            sendDMessage(sender, "minikit.error.apply_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        Kit kit = KitManager.get().get(id);
        if (kit == null) {
            sendDMessage(sender, "minikit.error.id_not_found", "%id%", id, "%alias%", label);
            return;
        }
        Player target = args.length == 3 ? this.readPlayer(sender, args[2]) : sender instanceof Player ? ((Player) sender) : null;
        if (target == null) {
            if (args.length == 3) {
                sendDMessage(sender, "minikit.error.apply_target_offline", "%player%", args[2], "%alias%", label);
                return;
            }
            sendDMessage(sender, "minikit.error.apply_target_required", "%id%", args[1].toLowerCase(), "%alias%", label);
            return;
        }
        kit.apply(target);
        sendDMessage(sender, "minikit.success.apply", "%id%", id, "%alias%", label);
    }

    private void price(CommandSender sender, String label, String[] args) {
        if (args.length != 3) {
            sendDMessage(sender, "minikit.error.price_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        Kit kit = KitManager.get().get(id);
        if (kit == null) {
            sendDMessage(sender, "minikit.error.id_not_found", "%id%", id, "%alias%", label);
            return;
        }
        Integer price = this.readInt(args[2]);
        if (price == null || price < 0) {
            sendDMessage(sender, "minikit.error.invalid_price", "%price%", args[2], "%alias%", label);
            return;
        }
        kit.setPrice(price);
        sendDMessage(sender, "minikit.success.price", "%id%", id, "%alias%", label);
    }

    private void gui(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        if (args.length <= 1) {
            sendDMessage(player, "minikit.error.gui_params", "%alias%", label);
            return;
        }
        Kit group = KitManager.get().get(args[1]);
        if (group == null) {
            sendDMessage(player, "minikit.error.id_not_found", "%alias%", label, "%id%", args[1]);
            return;
        }
        group.getEditorGui(player).open(player);
    }

    private void list(CommandSender sender, String label, String[] args) {
        DMessage msg = new DMessage(getPlugin(), sender).appendLang("minikit.success.list_prefix").newLine();
        boolean color = true;
        Color color1 = new Color(66, 233, 245);
        Color color2 = new Color(66, 179, 245);
        ArrayList<Kit> list = new ArrayList<>(KitManager.get().getAll().values());
        list.sort(Comparator.comparing(Registrable::getId));
        for (Kit kit : list) {
            msg.appendHover(
                    new DMessage(getPlugin(), sender).appendLang("minikit.success.list_info",
                            UtilsString.merge(kit.getPlaceholders(), "%alias%", label)), new DMessage(getPlugin(), sender).append(color ? color1 : color2)
                            .appendRunCommand("/" + label + " apply " + kit.getId(), kit.getId())).append(" ");
            color = !color;
        }
        msg.send();
    }

    private void delete(CommandSender sender, String label, String[] args) {
        if (args.length <= 1) {
            sendDMessage(sender, "minikit.error.delete_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        Kit kit = KitManager.get().get(id);
        if (kit == null) {
            sendDMessage(sender, "minikit.error.id_not_found", "%id%", id, "%alias%", label);
            return;
        }
        //TODO is used?
        KitManager.get().delete(kit);
        sendDMessage(sender, "minikit.success.delete", "%id%", id, "%alias%", label);
    }
}
