package emanondev.minigames.command;

import emanondev.core.UtilsString;
import emanondev.core.command.CoreCommand;
import emanondev.core.message.DMessage;
import emanondev.minigames.Kit;
import emanondev.minigames.KitManager;
import emanondev.minigames.Minigames;
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
            case "list" -> list(sender, label, args);
            case "delete" -> delete(sender, label, args);
            default -> help(sender, label, args);
        }
    }

    @Override
    public @Nullable List<String> onComplete(@NotNull CommandSender sender, @NotNull String label, String @NotNull [] args, @Nullable Location location) {
        return switch (args.length) {
            case 1 -> this.complete(args[0], List.of("create", "update", "apply", "list", "delete", "price"));
            case 2 -> switch (args[0].toLowerCase(Locale.ENGLISH)) {
                case "update", "apply", "delete", "price" -> this.complete(args[1], KitManager.get().getAll().keySet());
                default -> Collections.emptyList();
            };
            default -> Collections.emptyList();
        };
    }

    private void price(CommandSender sender, String label, String[] args) {
        if (args.length != 3) {
            sendMsg(sender, "minikit.error.price_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        Kit kit = KitManager.get().get(id);
        if (kit == null) {
            sendMsg(sender, "minikit.error.id_not_found", "%id%", id, "%alias%", label);
            return;
        }
        Integer price = this.readInt(args[2]);
        if (price == null || price < 0) {
            sendMsg(sender, "minikit.error.invalid_price", "%price%", args[2], "%alias%", label);
            return;
        }
        kit.setPrice(price);
        sendMsg(sender, "minikit.success.price", "%id%", id, "%alias%", label);
    }

    private void apply(CommandSender sender, String label, String[] args) {
        if (args.length != 2 && args.length != 3) {
            sendMsg(sender, "minikit.error.apply_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        Kit kit = KitManager.get().get(id);
        if (kit == null) {
            sendMsg(sender, "minikit.error.id_not_found", "%id%", id, "%alias%", label);
            return;
        }
        Player target = args.length == 3 ? this.readPlayer(sender, args[2]) : sender instanceof Player ? ((Player) sender) : null;
        if (target == null) {
            if (args.length == 3) {
                sendMsg(sender, "minikit.error.apply_target_offline", "%player%", args[2], "%alias%", label);
                return;
            }
            sendMsg(sender, "minikit.error.apply_target_required", "%id%", args[1].toLowerCase(), "%alias%", label);
            return;
        }
        kit.apply(target);
        sendMsg(sender, "minikit.success.apply", "%id%", id, "%alias%", label);
    }

    private void update(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        if (args.length <= 1) {
            sendMsg(sender, "minikit.error.update_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        Kit kit = KitManager.get().get(id);
        if (kit == null) {
            sendMsg(sender, "minikit.error.id_not_found", "%id%", id, "%alias%", label);
            return;
        }
        kit.updateSnapshot(player);
        sendMsg(sender, "minikit.success.update", "%id%", id, "%alias%", label);

    }

    private void help(CommandSender sender, String label, String[] args) {
        sendMsgList(sender, "minikit.help", "%alias%", label);
    }

    private void delete(CommandSender sender, String label, String[] args) {
        if (args.length <= 1) {
            sendMsg(sender, "minikit.error.delete_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        Kit kit = KitManager.get().get(id);
        if (kit == null) {
            sendMsg(sender, "minikit.error.id_not_found", "%id%", id, "%alias%", label);
            return;
        }
        KitManager.get().delete(kit);
        sendMsg(sender, "minikit.success.delete", "%id%", id, "%alias%", label);
    }

    private void create(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        if (args.length <= 1) {
            sendMsg(player, "minikit.error.create_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        Kit group = KitManager.get().get(id);
        if (group != null) {
            sendMsg(player, "minikit.error.id_already_used", "%id%", id, "%alias%", label);
            return;
        }
        try {
            KitManager.get().register(args[1].toLowerCase(Locale.ENGLISH), Kit.fromPlayer(player), player);
            sendMsg(player, "minikit.success.create", "%id%", id, "%alias%", label);
        } catch (IllegalArgumentException e) {
            sendMsg(player, "minikit.error.invalid_id", "%id%", id, "%alias%", label);
        }
    }

    private void list(CommandSender sender, String label, String[] args) {
        DMessage msg = new DMessage(getPlugin(), sender).appendLang("minikit.success.list_prefix").newLine();
        boolean color = true;
        Color color1 = new Color(66, 233, 245);
        Color color2 = new Color(66, 179, 245);
        for (Kit kit : KitManager.get().getAll().values()) {
            msg.appendHover(
                    new DMessage(getPlugin(), sender).appendLangList("minikit.success.list_info",
                            UtilsString.merge(kit.getPlaceholders(), "%alias%", label)), new DMessage(getPlugin(), sender).append(color ? color1 : color2)
                            .appendRunCommand("/" + label + " apply " + kit.getId(), kit.getId())).append(" ");
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
