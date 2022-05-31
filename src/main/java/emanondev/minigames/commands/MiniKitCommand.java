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
                mBuilder.addTextTranslation("minikit.success.list_color_1", "");
            else
                mBuilder.addTextTranslation("minikit.success.list_color_2", "");

            mBuilder.addTextTranslation("minikit.success.list_text", "", "%id%", kit)
                    .addHoverTranslation("minikit.success.list_hover", new ArrayList<>(), "%id%",
                            kit)
                    .addSuggestCommandConfigurable("minikit.success.list_suggest", "", "%label%", label,
                            "%id%",
                            kit, "%player%", sender.getName());
        }
        mBuilder.send();
    }

    private void onHelp(CommandSender sender, String label, String[] args) {
        new MessageBuilder(Minigames.get(), sender)
                .addTextTranslation("minikit.help.create_text", "",
                        "%label%", label)
                .addHoverTranslation("minikit.help.create_hover", (List<String>) null,
                        "%label%", label)
                .addSuggestCommand("/%label% create ", "%label%", label)
                .addText("\n")
                .addTextTranslation("minikit.help.update_text", "",
                        "%label%", label)
                .addHoverTranslation("minikit.help.update_hover", (List<String>) null,
                        "%label%", label)
                .addSuggestCommand("/%label% update ", "%label%", label)
                .addText("\n")
                .addTextTranslation("minikit.help.delete_text", "",
                        "%label%", label)
                .addHoverTranslation("minikit.help.delete_hover", (List<String>) null,
                        "%label%", label)
                .addSuggestCommand("/%label% delete ", "%label%", label)
                .addText("\n")
                .addTextTranslation(sender instanceof Player ? "minikit.help.apply_text" : "minikit.help.apply_text_console", "",
                        "%label%", label)
                .addHoverTranslation("minikit.help.apply_hover", (List<String>) null,
                        "%label%", label)
                .addSuggestCommand("/%label% apply ", "%label%", label)
                .addText("\n")
                .addTextTranslation("minikit.help.list_text", "",
                        "%label%", label)
                .addHoverTranslation("minikit.help.list_hover", (List<String>) null,
                        "%label%", label)
                .addSuggestCommand("/%label% list", "%label%", label)
                .send();
    }

    private void create(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        if (args.length != 2) {
            new MessageBuilder(Minigames.get(), sender)
                    .addTextTranslation("minikit.error.create_arguments_amount", "", "%label%", label).send();
            return;
        }
        if (KitManager.get().getKit(args[1]) != null) {
            new MessageBuilder(Minigames.get(), sender)
                    .addTextTranslation("minikit.error.already_used_id", "", "%id%", args[1].toLowerCase()).send();
            return;
        }
        KitManager.get().createKit(args[1], player);
        new MessageBuilder(Minigames.get(), sender)
                .addTextTranslation("minikit.success.create", "", "%id%", args[1].toLowerCase()).send();
    }

    //apply <kit> [player]
    private void apply(CommandSender sender, String label, String[] args) {
        if (args.length != 2 && args.length != 3) {
            new MessageBuilder(Minigames.get(), sender)
                    .addTextTranslation("minikit.error.apply_arguments_amount", "").send();
            return;
        }
        Player target = args.length == 3 ? this.readPlayer(sender, args[2]) : sender instanceof Player ? ((Player) sender) : null;
        if (target == null) {
            if (args.length == 3) {
                new MessageBuilder(Minigames.get(), sender)
                        .addTextTranslation("minikit.error.apply_target_offline", "", "%player%", args[2]).send();
                return;
            }
            new MessageBuilder(Minigames.get(), sender)
                    .addTextTranslation("minikit.error.apply_target_required", "", "%id%", args[1].toLowerCase()).send();
            return;
        }
        Kit kit = KitManager.get().getKit(args[1]);
        if (kit == null) {
            new MessageBuilder(Minigames.get(), sender)
                    .addTextTranslation("minikit.error.unexisting_id", "", "%id%", args[1].toLowerCase()).send();
            return;
        }
        kit.apply(target);
        new MessageBuilder(Minigames.get(), sender)
                .addTextTranslation("minikit.success.apply", "", "%id%", args[1].toLowerCase()
                        , "%player%", target.getName()).send();
    }

    private void update(CommandSender sender, String label, String[] args) {
        if (args.length != 2) {
            new MessageBuilder(Minigames.get(), sender)
                    .addTextTranslation("minikit.error.update_arguments_amount", "").send();
            return;
        }
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        Kit kit = KitManager.get().getKit(args[1]);
        if (kit == null) {
            new MessageBuilder(Minigames.get(), sender)
                    .addTextTranslation("minikit.error.unexisting_id", "", "%id%", args[1].toLowerCase()).send();
            return;
        }
        kit.updateSnapshot(new PlayerSnapshot(player)); //not optimized
        new MessageBuilder(Minigames.get(), sender)
                .addTextTranslation("minikit.success.update", "", "%id%", args[1].toLowerCase()).send();
    }

    //delete <id>
    private void delete(CommandSender sender, String label, String[] args) {
        if (args.length != 2) {
            new MessageBuilder(Minigames.get(), sender)
                    .addTextTranslation("minikit.error.delete_arguments_amount", "").send();
            return;
        }
        if (KitManager.get().getKit(args[1]) == null) {
            new MessageBuilder(Minigames.get(), sender)
                    .addTextTranslation("minikit.error.unexisting_id", "", "%id%", args[1].toLowerCase()).send();
            return;
        }
        KitManager.get().deleteKit(args[1]);
        new MessageBuilder(Minigames.get(), sender)
                .addTextTranslation("minikit.success.delete", "", "%id%", args[1].toLowerCase()).send();
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
