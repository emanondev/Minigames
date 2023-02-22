package emanondev.minigames.command;

import emanondev.core.UtilsCommand;
import emanondev.core.UtilsString;
import emanondev.core.command.CoreCommandPlus;
import emanondev.minigames.MessageUtil;
import emanondev.minigames.MinigameTypes;
import emanondev.minigames.Minigames;
import emanondev.minigames.OptionManager;
import emanondev.minigames.generic.MOption;
import emanondev.minigames.generic.MType;
import emanondev.minigames.generic.Perms;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class MiniOptionCommand extends CoreCommandPlus {

    public MiniOptionCommand() {
        super("minioption", Minigames.get(), Perms.COMMAND_MINIOPTION
                , "setup options");
        addSubCommandHandler("create", (sender, label, args) -> {
                    if (!(sender instanceof Player player)) {
                        this.playerOnlyNotify(sender);
                        return;
                    }
                    if (args.length != 3) {
                        //arguments
                        MessageUtil.sendMessage(player, "minioption.error.create_arguments_amount", "%label%", label);
                        return;
                    }
                    MType type = MinigameTypes.get().getType(args[1]);
                    if (type == null) {
                        MessageUtil.sendMessage(player, "minioption.error.invalid_type", "%type%", args[1]);
                        return;
                    }
                    String id = args[2].toLowerCase();
                    if (!UtilsString.isLowcasedValidID(id)) {
                        MessageUtil.sendMessage(player, "minioption.error.invalid_id", "%id%", args[2]);
                        return;
                    }
                    if (OptionManager.get().get(id) != null) {
                        MessageUtil.sendMessage(player, "minioption.error.already_used_id", "%id%", args[2]);
                        return;
                    }
                    MOption option = type.createDefaultOptions();
                    OptionManager.get().register(id, option, player);
                    OptionManager.get().save(option);
                    option.openEditor(player);
                }, (sender, label, args) ->
                        args.length == 2 ? UtilsCommand.complete(args[1], MinigameTypes.get().getTypes(), MType::getType, (t) -> true)
                                : Collections.emptyList()
        );
        addSubCommandHandler("edit", (sender, label, args) -> {
                    if (!(sender instanceof Player player)) {
                        this.playerOnlyNotify(sender);
                        return;
                    }
                    if (args.length != 2) {
                        //arguments
                        MessageUtil.sendMessage(player, "minioption.error.edit_arguments_amount",
                                "%label%", label);
                        return;
                    }
                    String id = args[1].toLowerCase();
                    MOption options = OptionManager.get().get(id);
                    if (options == null) {
                        MessageUtil.sendMessage(player, "minioption.error.unexisting_id",
                                "%id%", args[1]);
                        return;
                    }
                    options.openEditor(player);
                }, (sender, label, args) ->
                        args.length == 2 ? UtilsCommand.complete(args[1], OptionManager.get().getAll().keySet())
                                : Collections.emptyList()

        );
    }
    /*

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {


        if (args.length == 0) {
            onHelp(sender, label, args);
            return;
        }
        switch (args[0].toLowerCase()) {
            case "create" -> create(sender, label, args);
            case "edit" -> edit(sender, label, args);
            case "list" -> list(sender, label, args);
            default -> onHelp(sender, label, args);
        }
    }*/


    private void list(CommandSender who, String label, String[] args) {
        who.sendMessage(ChatColor.GOLD + "Incomplete command"); //TODO
        for (MOption option : OptionManager.get().getAll().values()) {
            who.sendMessage(ChatColor.GOLD + option.getId());
        }
    }
/*
    private void edit(CommandSender sender, String label, String[] args) {

    }

    private void create(CommandSender sender, String label, String[] args) {
    }

    private void onHelp(CommandSender sender, String label, String[] args) {
        new MessageBuilder(Minigames.get(), sender)
                .addText(MessageUtil.getMessage(sender, "minioption.help.create_text", "%label%", label))
                .addHover(MessageUtil.getMultiMessage(sender, "minioption.help.create_hover", "%label%", label))
                .addSuggestCommand("/%label% create ", "%label%", label)
                .addText("\n")

                .addText(MessageUtil.getMessage(sender, "minioption.help.edit_text", "%label%", label))
                .addHover(MessageUtil.getMultiMessage(sender, "minioption.help.edit_hover", "%label%", label))
                .addSuggestCommand("/%label% edit ", "%label%", label)
                .addText("\n")

                .addText(MessageUtil.getMessage(sender, "minioption.help.list_text", "%label%", label))
                .addHover(MessageUtil.getMultiMessage(sender, "minioption.help.list_hover", "%label%", label))
                .addSuggestCommand("/%label% list", "%label%", label)
                .send();
    }

    @Override
    public List<String> onComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @Nullable Location location) {
        if (!(sender instanceof Player))
            return Collections.emptyList();
        return switch (args.length) {
            case 1 -> UtilsCommand.complete(args[0], List.of("create", "edit"));
            case 2 -> switch (args[0].toLowerCase()) {
                case "create" -> UtilsCommand.complete(args[1], MinigameTypes.get().getTypes(), MType::getType, (t) -> true);
                case "edit" -> UtilsCommand.complete(args[1], OptionManager.get().getAll().keySet());
                default -> Collections.emptyList();
            };
            default -> Collections.emptyList();
        };
    }*/
}