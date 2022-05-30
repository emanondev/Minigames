package emanondev.minigames.commands;

import emanondev.core.CoreCommand;
import emanondev.core.MessageBuilder;
import emanondev.core.PermissionBuilder;
import emanondev.core.UtilsString;
import emanondev.minigames.*;
import emanondev.minigames.generic.MType;
import emanondev.minigames.generic.MArena;
import emanondev.minigames.generic.MGame;
import emanondev.minigames.generic.MOption;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
        if (args.length==0){
            onHelp(sender,label,args);
            return;
        }
        switch (args[0].toLowerCase()) {
            case "create" -> create(sender, label, args);
            case "edit" -> edit(sender, label, args);
            case "list" -> list(sender, label, args);
            default -> onHelp(sender,label,args);
        }
    }

    private void onHelp(CommandSender sender, String label, String[] args) {
        new MessageBuilder(Minigames.get(), sender)
                .addTextTranslation("minigame.help.create_text", "",
                        "%label%", label)
                .addHoverTranslation("minigame.help.create_hover", (List<String>) null,
                        "%label%", label)
                .addSuggestCommand("/%label% create ","%label%", label)
                .addText("\n")
                .addTextTranslation("minigame.help.edit_text", "",
                        "%label%", label)
                .addHoverTranslation("minigame.help.edit_hover", (List<String>) null,
                        "%label%", label)
                .addSuggestCommand("/%label% edit ","%label%", label)
                .addText("\n")
                .addTextTranslation("minigame.help.list_text", "",
                        "%label%", label)
                .addHoverTranslation("minigame.help.list_hover", (List<String>) null,
                        "%label%", label)
                .addSuggestCommand("/%label% list","%label%", label)
                .send();
    }

    private void list(CommandSender sender, String label, String[] args) {
        sender.sendMessage(ChatColor.GOLD +"Incomplete command"); //TODO
        for (MGame game : GameManager.get().getGames().values()){
            sender.sendMessage(game.getId()+" ("+game.getMinigameType().getType()+") Fase "+game.getPhase()+" Arena "+game.getArena().getId()+" Opzione "+game.getOption().getId());
        }
    }

    //create type arena option id
    private void create(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player who)) {
            this.playerOnlyNotify(sender);
            return;
        }

        if (args.length != 5) {
            new MessageBuilder(Minigames.get(), who)
                    .addTextTranslation("minigame.create.error.arguments_amount", "",
                            "%label%", label).send();
            return;
        }
        MType mType = MinigameTypes.get().getType(args[1]);
        if (mType == null) {
            new MessageBuilder(Minigames.get(), who)
                    .addTextTranslation("minigame.create.error.invalid_type", "", "%type%", args[1]).send();
            return;
        }
        MArena arena = ArenaManager.get().getArena(args[2]);
        if (arena == null) {
            new MessageBuilder(Minigames.get(), who)
                    .addTextTranslation("minigame.create.error.invalid_arena_name", "", "%arena%", args[2]).send();
            return;
        }
        if (!mType.matchType(arena)) {
            new MessageBuilder(Minigames.get(), who)
                    .addTextTranslation("minigame.create.error.invalid_arena_type", "", "%type%", args[1], "%arena%", args[2]).send();
            return;
        }
        MOption option = OptionManager.get().getOption(args[3]);
        if (option == null) {
            new MessageBuilder(Minigames.get(), who)
                    .addTextTranslation("minigame.create.error.invalid_option_name", "", "%option%", args[3]).send();
            return;
        }
        if (!mType.matchType(option)) {
            new MessageBuilder(Minigames.get(), who)
                    .addTextTranslation("minigame.create.error.invalid_option_type", "", "%type%", args[1], "%option%", args[3]).send();
            return;
        }
        String id = args[4].toLowerCase();
        if (!UtilsString.isLowcasedValidID(id)) {
            new MessageBuilder(Minigames.get(), who)
                    .addTextTranslation("minigame.create.error.invalid_id_name", "", "%id%", id).send();
            return;
        }
        if (GameManager.get().getPreMadeGameInstance(id) != null) {
            new MessageBuilder(Minigames.get(), who)
                    .addTextTranslation("minigame.create.error.already_used_id", "", "%id%", id).send();
            return;
        }
        MGame mGame = mType.createGame(arena.getId(), option.getId());
        GameManager.get().registerGame(id, mGame, who);
        GameManager.get().save(mGame);
    }

    private void edit(CommandSender sender, String s, String[] args) {
    }

    @Override
    public List<String> onComplete(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args, @Nullable Location location) {
        return switch (args.length) {
            case 1 -> this.complete(args[0], List.of("create", "edit","list"));
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
