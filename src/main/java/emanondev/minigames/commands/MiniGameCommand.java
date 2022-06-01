package emanondev.minigames.commands;

import emanondev.core.CoreCommand;
import emanondev.core.MessageBuilder;
import emanondev.core.PermissionBuilder;
import emanondev.core.UtilsString;
import emanondev.minigames.*;
import emanondev.minigames.generic.MArena;
import emanondev.minigames.generic.MGame;
import emanondev.minigames.generic.MOption;
import emanondev.minigames.generic.MType;
import emanondev.minigames.skywars.SkyWarsGame;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class MiniGameCommand extends CoreCommand {

    public MiniGameCommand() {
        super("MiniGame", Minigames.get(), PermissionBuilder.ofCommand(Minigames.get(), "MiniGame")
                .buildAndRegister(Minigames.get()), "Sets Games");
    }

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
    }

    private void onHelp(CommandSender sender, String label, String[] args) {
        new MessageBuilder(Minigames.get(), sender)
                .addText(MessageUtil.getMessage(sender, "minigame.help.create_text", "%label%", label))
                .addHover(MessageUtil.getMultiMessage(sender, "minigame.help.create_hover", "%label%", label))
                .addSuggestCommand("/%label% create ", "%label%", label)
                .addText("\n")

                .addText(MessageUtil.getMessage(sender, "minigame.help.edit_text", "%label%", label))
                .addHover(MessageUtil.getMultiMessage(sender, "minigame.help.edit_hover", "%label%", label))
                .addSuggestCommand("/%label% edit ", "%label%", label)
                .addText("\n")

                .addText(MessageUtil.getMessage(sender, "minigame.help.list_text", "%label%", label))
                .addHover(MessageUtil.getMultiMessage(sender, "minigame.help.list_hover", "%label%", label))
                .addSuggestCommand("/%label% list", "%label%", label)
                .send();
    }

    private void list(CommandSender sender, String label, String[] args) {
        sender.sendMessage(ChatColor.GOLD + "Incomplete command"); //TODO
        for (MGame game : GameManager.get().getGames().values()) {
            BoundingBox bb = ((SkyWarsGame) game).getBoundingBox();
            sender.sendMessage(game.getId() + " (" + game.getMinigameType().getType() + ") Fase " + game.getPhase() + " Arena " + game.getArena().getId() + " Opzione " + game.getOption().getId());
            sender.sendMessage(game.getId() + " Area: " + bb.getMinX() + ":" + bb.getMaxX() + "  " + bb.getMinZ() + ":" + bb.getMaxZ());
            sender.sendMessage(game.getId() + " PP: " + game.getPlayingPlayers().size() + " SP: " + game.getSpectators().size() + " CP: " + game.getCollectedPlayers().size());
        }
    }

    //create type arena option id
    private void create(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }

        if (args.length != 5) {
            MessageUtil.sendMessage(player, "minigame.create.error.arguments_amount",
                    "%label%", label);
            return;
        }
        MType mType = MinigameTypes.get().getType(args[1]);
        if (mType == null) {
            MessageUtil.sendMessage(player, "minigame.create.error.invalid_type", "%type%", args[1]);
            return;
        }
        MArena arena = ArenaManager.get().getArena(args[2]);
        if (arena == null) {
            MessageUtil.sendMessage(player, "minigame.create.error.invalid_arena_name", "%arena%", args[2]);
            return;
        }
        if (!mType.matchType(arena)) {
            MessageUtil.sendMessage(player, "minigame.create.error.invalid_arena_type", "%type%", args[1], "%arena%", args[2]);
            return;
        }
        MOption option = OptionManager.get().getOption(args[3]);
        if (option == null) {
            MessageUtil.sendMessage(player, "minigame.create.error.invalid_option_name", "%option%", args[3]);
            return;
        }
        if (!mType.matchType(option)) {
            MessageUtil.sendMessage(player, "minigame.create.error.invalid_option_type", "%type%", args[1], "%option%", args[3]);
            return;
        }
        String id = args[4].toLowerCase();
        if (!UtilsString.isLowcasedValidID(id)) {
            MessageUtil.sendMessage(player, "minigame.create.error.invalid_id_name", "%id%", id);
            return;
        }
        if (GameManager.get().getPreMadeGameInstance(id) != null) {
            MessageUtil.sendMessage(player, "minigame.create.error.already_used_id", "%id%", id);
            return;
        }
        MGame mGame = mType.createGame(arena.getId(), option.getId());
        GameManager.get().registerGame(id, mGame, player);
        GameManager.get().save(mGame);
    }

    private void edit(CommandSender sender, String s, String[] args) {
    }

    @Override
    public List<String> onComplete(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args, @Nullable Location location) {
        return switch (args.length) {
            case 1 -> this.complete(args[0], List.of("create", "edit", "list"));
            case 2 -> switch (args[0].toLowerCase()) {
                case "create" -> this.complete(args[1], MinigameTypes.get().getTypesId());
                case "edit" -> this.complete(args[1], GameManager.get().getGames().keySet());
                default -> Collections.emptyList();
            };
            case 3 -> switch (args[0].toLowerCase()) {
                case "create" -> {
                    MType mtype = MinigameTypes.get().getType(args[1]);
                    yield mtype == null ? Collections.emptyList() :
                            this.complete(args[2], ArenaManager.get().getCompatibleArenas(mtype).keySet());
                }
                default -> Collections.emptyList();
            };
            case 4 -> switch (args[0].toLowerCase()) {
                case "create" -> {
                    MType mtype = MinigameTypes.get().getType(args[1]);
                    yield mtype == null ? Collections.emptyList() :
                            this.complete(args[3], OptionManager.get().getCompatibleOptions(mtype).keySet());
                }
                default -> Collections.emptyList();
            };
            default -> Collections.emptyList();
        };
    }
}
