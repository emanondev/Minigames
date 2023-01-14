package emanondev.minigames.command;

import emanondev.core.UtilsString;
import emanondev.core.command.CoreCommandPlus;
import emanondev.core.command.SubCommandListExecutor;
import emanondev.minigames.*;
import emanondev.minigames.generic.*;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static org.bukkit.ChatColor.*;

public class MiniGameCommand extends CoreCommandPlus {

    public MiniGameCommand() {
        super("MiniGame", Minigames.get(), Perms.COMMAND_MINIGAME, "Sets Games");
        addSubCommandHandler("create", this::create, (sender, label, args) -> onComplete(sender, label, args, null));
        addSubCommandHandler("edit", this::edit, (sender, label, args) -> onComplete(sender, label, args, null));
        addSubCommandHandler("list", new SubCommandListExecutor<MGame>(this, "list", () -> GameManager.get().getGames().values(),
                (Registrable::getId), this::list
        ), (sender, label, args) -> onComplete(sender, label, args, null));
    }

    private String list(MGame game, CommandSender sender) {
        StringBuilder text = new StringBuilder();
        text.append(GOLD + game.getId() + AQUA + " (" + YELLOW + game.getMinigameType().getType() + AQUA + ")").append("\n");
        text.append(AQUA + " Fase " + YELLOW + game.getPhase() + AQUA + " Arena " + YELLOW + game.getArena().getId() + AQUA +
                " Opzione " + YELLOW + game.getOption().getId()).append("\n");
        if (game instanceof AbstractMColorSchemGame boundGame) {
            BoundingBox bb = boundGame.getBoundingBox();
            text.append(AQUA + " Area: X da " + YELLOW + bb.getMinX() +
                    AQUA + " a " + YELLOW + bb.getMaxX() + AQUA + ", Z da " + YELLOW + bb.getMinZ() + AQUA + " a " + YELLOW +
                    bb.getMaxZ()).append("\n");
        }
        text.append(AQUA + " Gamers: " + game.getGamers().size() + AQUA + " Spect: " + game.getSpectators().size());

        return text.toString();
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
