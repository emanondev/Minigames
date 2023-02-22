package emanondev.minigames.command;

import emanondev.core.command.CoreCommand;
import emanondev.core.message.DMessage;
import emanondev.minigames.GameManager;
import emanondev.minigames.MinigameTypes;
import emanondev.minigames.Minigames;
import emanondev.minigames.generic.MGame;
import emanondev.minigames.generic.MType;
import emanondev.minigames.generic.Perms;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FastJoinCommand extends CoreCommand {

    public FastJoinCommand() {
        super("fastjoin", Minigames.get(), Perms.COMMAND_FASTJOIN, "fast join a game");
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        if (GameManager.get().getCurrentGame(player) != null) {
            sendMsg(player, "join.error.already_inside_a_game", "%label%", label);
            return;
        }

        switch (args.length) {
            case 0 -> {
                List<MGame> available = new ArrayList<>(GameManager.get().getAll().values());
                available.removeIf((m) -> switch (m.getPhase()) {
                    case STOPPED, END, RESTART -> true;
                    default -> false;
                });
                Collections.shuffle(available);
                available.sort((m1, m2) -> (int) (1000 * ((double) m2.getGamers().size() / m2.getMaxGamers() - (double) m1.getGamers().size() / m1.getMaxGamers())));

                if (GameManager.get().joinGameAsGamer(player, available))
                    return;

                sendMsg(player, "join.error.no_available_game", "%label%", label);
            }
            case 1 -> {
                MType type = MinigameTypes.get().getType(args[0]);
                if (type == null) {
                    sendMsg(player, "join.error.invalid_type", "%type%", args[0], "%label%", label);
                    return;
                }
                @SuppressWarnings({"rawtypes", "unchecked"})
                List<MGame> available = new ArrayList<>(GameManager.get().getGameInstances(type).values());
                available.removeIf((m) -> switch (m.getPhase()) {
                    case STOPPED, END, RESTART -> true;
                    default -> false;
                });
                Collections.shuffle(available);
                available.sort((m1, m2) -> (100 * (m2.getGamers().size() / m2.getMaxGamers() - m1.getGamers().size() / m1.getMaxGamers())));

                if (GameManager.get().joinGameAsGamer(player, available))
                    return;

                sendMsg(player, "join.error.no_available_game_of_type", "%type%", type.getType(), "%label%", label);
            }
            default -> sendMsg(player, "join.error.fastjoin_params", "%label%", label);
        }
    }

    @Override
    public List<String> onComplete(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args, @Nullable Location location) {
        return args.length == 1 ? this.complete(args[0], MinigameTypes.get().getTypes(), MType::getType, (m) -> true) : Collections.emptyList();
    }

    private void sendMsg(CommandSender target, String path, String... holders) {
        new DMessage(getPlugin(), target).appendLang(path, holders).send();
    }

    private void sendMsgList(CommandSender target, String path, String... holders) {
        new DMessage(getPlugin(), target).appendLangList(path, holders).send();
    }

}
