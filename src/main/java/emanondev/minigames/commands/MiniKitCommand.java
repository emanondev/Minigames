package emanondev.minigames.commands;

import emanondev.core.CoreCommand;
import emanondev.core.MessageBuilder;
import emanondev.core.PermissionBuilder;
import emanondev.core.PlayerSnapshot;
import emanondev.minigames.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MiniKitCommand extends CoreCommand {
    public MiniKitCommand() {
        super("Minikit", Minigames.get(), PermissionBuilder.ofCommand(Minigames.get(), "Minikit").buildAndRegister(Minigames.get()),
                "set minigame available kits");
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            onHelp(sender, label, args);
            return;
        }
        switch (args[0].toLowerCase()) {
            case "create" -> create(sender, label, args);
            case "delete" -> delete(sender, label, args);
            case "update" -> update(sender, label, args);
            case "list" -> list(sender, label, args);
            case "apply" -> apply(sender, label, args);
            case "test" -> {
                PlayerSnapshot snap = new PlayerSnapshot((Player) sender);
                snap.apply((Player) sender);
                GameManager.get().getSection(MinigameTypes.SKYWARS).set("default_kit", snap);
                GameManager.get().getSection(MinigameTypes.SKYWARS).save();
                Minigames.get().logTetraStar(ChatColor.DARK_RED, "exco " + snap.getExtraContents().size());
            }
            default -> onHelp(sender, label, args);
        }
    }

    private void list(CommandSender sender, String label, String[] args) {
        Set<String> kits = new TreeSet<>(KitManager.get().getKitsId());
        if (kits.isEmpty()) {
            new MessageBuilder(Minigames.get(), sender).addTextTranslation("minikit.success.list_no_kits", "").send();
            return;
        }

        MessageBuilder mBuilder = new MessageBuilder(Minigames.get(), sender);
        boolean color = true;
        for (String kit : kits) {
            if (color)
                mBuilder.addText(MessageUtil.getMessage(sender, "minikit.success.list_color_1"));
            else
                mBuilder.addText(MessageUtil.getMessage(sender, "minikit.success.list_color_2"));
            color = !color;
            mBuilder.addText(MessageUtil.getMessage(sender, "minikit.success.list_text",  "%id%", kit))
                    .addHover(MessageUtil.getMultiMessage(sender,"minikit.success.list_hover", "%id%",                            kit))
                    .addSuggestCommandConfigurable("minikit.success.list_suggest",  "%label%", label,
                            "%id%",
                            kit, "%player%", sender.getName());
        }
        mBuilder.send();
    }

    private void onHelp(CommandSender sender, String label, String[] args) {
        new MessageBuilder(Minigames.get(), sender)
                .addText(MessageUtil.getMessage(sender, "minikit.help.create_text", "%label%", label))
                .addHover(MessageUtil.getMultiMessage(sender, "minikit.help.create_hover", "%label%", label))
                .addSuggestCommand("/%label% create ", "%label%", label)
                .addText("\n")

                .addText(MessageUtil.getMessage(sender, "minikit.help.update_text", "%label%", label))
                .addHover(MessageUtil.getMultiMessage(sender, "minikit.help.update_hover", "%label%", label))
                .addSuggestCommand("/%label% update ", "%label%", label)
                .addText("\n")

                .addText(MessageUtil.getMessage(sender, "minikit.help.delete_text", "%label%", label))
                .addHover(MessageUtil.getMultiMessage(sender, "minikit.help.delete_hover", "%label%", label))
                .addSuggestCommand("/%label% delete ", "%label%", label)
                .addText("\n")

                .addText(MessageUtil.getMessage(sender, sender instanceof Player ? "minikit.help.apply_text" :
                        "minikit.help.apply_text_console", "%label%", label))
                .addHover(MessageUtil.getMultiMessage(sender, "minikit.help.apply_hover", "%label%", label))
                .addSuggestCommand("/%label% apply ", "%label%", label)
                .addText("\n")

                .addText(MessageUtil.getMessage(sender, "minikit.help.list_text", "%label%", label))
                .addHover(MessageUtil.getMultiMessage(sender, "minikit.help.list_hover", "%label%", label))
                .addSuggestCommand("/%label% list", "%label%", label)
                .send();
    }

    private void create(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        if (args.length != 2) {
            MessageUtil.sendMessage(player, "minikit.error.create_arguments_amount", "%label%", label);
            return;
        }
        if (KitManager.get().getKit(args[1]) != null) {
            MessageUtil.sendMessage(player, "minikit.error.already_used_id", "%id%", args[1].toLowerCase());
            return;
        }
        KitManager.get().createKit(args[1], player);
        MessageUtil.sendMessage(player, "minikit.success.create", "%id%", args[1].toLowerCase());
    }

    //apply <kit> [player]
    private void apply(CommandSender sender, String label, String[] args) {
        if (args.length != 2 && args.length != 3) {
            MessageUtil.sendMessage(sender, "minikit.error.apply_arguments_amount");
            return;
        }
        Player target = args.length == 3 ? this.readPlayer(sender, args[2]) : sender instanceof Player ? ((Player) sender) : null;
        if (target == null) {
            if (args.length == 3) {
                MessageUtil.sendMessage(sender, "minikit.error.apply_target_offline", "%player%", args[2]);
                return;
            }
            MessageUtil.sendMessage(sender, "minikit.error.apply_target_required", "%id%", args[1].toLowerCase());
            return;
        }
        Kit kit = KitManager.get().getKit(args[1]);
        if (kit == null) {
            MessageUtil.sendMessage(sender, "minikit.error.unexisting_id", "%id%", args[1].toLowerCase());
            return;
        }
        kit.apply(target);
        MessageUtil.sendMessage(sender, "minikit.success.apply", "%id%", args[1].toLowerCase()
                , "%player%", target.getName());
    }

    private void update(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        if (args.length != 2) {
            MessageUtil.sendMessage(player, "minikit.error.update_arguments_amount");
            return;
        }
        Kit kit = KitManager.get().getKit(args[1]);
        if (kit == null) {
            MessageUtil.sendMessage(player, "minikit.error.unexisting_id", "%id%", args[1].toLowerCase());
            return;
        }
        kit.updateSnapshot(new PlayerSnapshot(player)); //not optimized
        MessageUtil.sendMessage(player, "minikit.success.update", "%id%", args[1].toLowerCase());
    }

    //delete <id>
    private void delete(CommandSender sender, String label, String[] args) {
        if (args.length != 2) {
            MessageUtil.sendMessage(sender, "minikit.error.delete_arguments_amount");
            return;
        }
        if (KitManager.get().getKit(args[1]) == null) {
            MessageUtil.sendMessage(sender, "minikit.error.unexisting_id", "%id%", args[1].toLowerCase());
            return;
        }
        KitManager.get().deleteKit(args[1]);
        MessageUtil.sendMessage(sender, "minikit.success.delete", "%id%", args[1].toLowerCase());
    }

    @Override
    public List<String> onComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @Nullable Location location) {
        return switch (args.length) {
            case 1 -> this.complete(args[0], List.of("create", "update", "delete", "apply", "list"));
            case 2 -> switch (args[0].toLowerCase()) {
                case "update", "delete", "apply" -> this.complete(args[1], KitManager.get().getKitsId());
                default -> Collections.emptyList();
            };
            case 3 -> switch (args[0].toLowerCase()) {
                case "apply" -> this.completePlayerNames(sender, args[2]);
                default -> Collections.emptyList();
            };
            default -> Collections.emptyList();
        };
    }
}
