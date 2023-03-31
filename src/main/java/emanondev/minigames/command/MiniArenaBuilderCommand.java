package emanondev.minigames.command;

import emanondev.core.command.CoreCommand;
import emanondev.core.message.DMessage;
import emanondev.minigames.ArenaManager;
import emanondev.minigames.MinigameTypes;
import emanondev.minigames.Minigames;
import emanondev.minigames.generic.MArenaBuilder;
import emanondev.minigames.generic.MType;
import emanondev.minigames.generic.Perms;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MiniArenaBuilderCommand extends CoreCommand {

    private final static ArenaManager MAN = ArenaManager.get();

    public MiniArenaBuilderCommand() {
        super("miniarenabuilder", Minigames.get(), Perms.COMMAND_ARENABUILDER
                , "setup arenas", List.of("arenabuilder", "ab"));
    }

    @Override
    public void onExecute(@NotNull CommandSender commandSender, @NotNull String label, @NotNull String[] args) {
        if (!(commandSender instanceof Player sender)) {
            playerOnlyNotify(commandSender);
            return;
        }
        if (args.length == 0) {
            if (MAN.isBuilding(sender)) {
                MAN.getBuildingArena(sender).handleCommand(sender, label, args);
                return;
            }
            help(sender, label, args);
            return;
        }
        switch (args[0].toLowerCase(Locale.ENGLISH)) {
            case "create" -> create(sender, label, args);
            case "abort" -> abort(sender, label, args);
            default -> {
                if (MAN.isBuilding(sender))
                    MAN.getBuildingArena(sender).handleCommand(sender, label, args);
                else
                    help(sender, label, args);
            }
        }
    }

    private void help(@NotNull Player sender, @NotNull String label, @NotNull String[] args) {
        sendMsgList(sender, "miniarenabuilder.help", "%alias%", label);
    }

    private void create(@NotNull Player sender, @NotNull String label, @NotNull String[] args) {
        if (MAN.isBuilding(sender)) {
            sendMsg(sender, "miniarenabuilder.error.already_creating", "%alias%", label);
            return;
        }
        if (args.length != 3) {
            //arguments
            sendMsg(sender, "miniarenabuilder.error.create_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        if (MAN.get(id) != null) {
            sendMsg(sender, "miniarenabuilder.error.id_already_used", "%id%", id, "%alias%", label);
            return;
        }
        MArenaBuilder arenaBuilder = MAN.getBuildingArenaById(id);
        if (arenaBuilder != null) {
            sendMsg(sender, "miniarenabuilder.error.id_already_building", "%id%", id, "%alias%", label,
                    "%who%", Bukkit.getOfflinePlayer(arenaBuilder.getUser()).getName());
            return;
        }
        @SuppressWarnings("rawtypes")
        MType type = MinigameTypes.get().getType(args[2]);
        if (type == null) {
            sendMsg(sender, "miniarenabuilder.error.invalid_minigametype", "%type%", args[2], "%alias%", label);
            return;
        }
        if (!MAN.registerBuilder(sender.getUniqueId(), id, label, type))
            sendMsg(sender, "miniarenabuilder.error.invalid_id", "%id%", id, "%alias%", label);
    }

    private void abort(@NotNull Player sender, @NotNull String label, @NotNull String[] args) {
        if (MAN.isBuilding(sender)) {
            MAN.unregisterBuilder(sender.getUniqueId());
            sendMsg(sender, "miniarenabuilder.success.abort", "%alias%", label);
            return;
        }
        sendMsg(sender, "miniarenabuilder.error.not_creating", "%alias%", label);
    }


    //cmd create <id> <type>
    @Override
    public List<String> onComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @Nullable Location location) {
        if (!(sender instanceof Player who))
            return Collections.emptyList();
        ArenaManager arenaMan = ArenaManager.get();
        if (!arenaMan.isBuilding(who))
            return switch (args.length) {
                case 1 -> complete(args[0], List.of("create"));
                case 3 -> switch (args[0].toLowerCase(Locale.ENGLISH)) {
                    case "create" -> complete(args[2], MinigameTypes.get().getTypes(), MType::getType, (t) -> true);
                    default -> Collections.emptyList();
                };
                default -> Collections.emptyList();
            };
        return arenaMan.getBuildingArena(who).handleComplete(args);
    }


    private void sendMsg(CommandSender target, String path, String... holders) {
        new DMessage(getPlugin(), target).appendLang(path, holders).send();
    }

    private void sendMsgList(CommandSender target, String path, String... holders) {
        new DMessage(getPlugin(), target).appendLangList(path, holders).send();
    }
}
