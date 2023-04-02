package emanondev.minigames.command;

import emanondev.core.command.CoreCommand;
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
import java.util.HashSet;
import java.util.List;

public class FastJoinCommand extends CoreCommand {

    public FastJoinCommand() {
        super("fastjoin", Minigames.get(), Perms.COMMAND_FASTJOIN, "fast join a game");
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        if (GameManager.get().getCurrentGame(player) != null) {
            sendDMessage(player, "join.error.already_inside_a_game", "%alias%", label);
            return;
        }

        switch (args.length) {
            case 0 -> {
                @SuppressWarnings("rawtypes")
                List<MGame> available = new ArrayList<>(GameManager.get().getAll().values());
                available.removeIf((m) -> switch (m.getPhase()) {
                    case STOPPED, END, RESTART -> true;
                    default -> false;
                });
                Collections.shuffle(available);
                available.sort((m1, m2) -> (int) (1000 * ((double) m2.getGamers().size() / m2.getMaxGamers() - (double) m1.getGamers().size() / m1.getMaxGamers())));

                if (GameManager.get().joinGameAsGamer(player, available))
                    return;

                sendDMessage(player, "join.error.no_available_game", "%alias%", label);
            }
            case 1 -> {
                if (!args[0].contains(",")) {
                    @SuppressWarnings("rawtypes")
                    MType type = MinigameTypes.get().getType(args[0]);
                    if (type == null) {
                        sendDMessage(player, "join.error.invalid_type", "%type%", args[0], "%alias%", label);
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

                    sendDMessage(player, "join.error.no_available_game_of_type", "%type%", type.getType(), "%alias%", label);
                } else {
                    String[] typesRaw = args[0].split(",");
                    @SuppressWarnings({"rawtypes"})
                    HashSet<MType> types = new HashSet<>();
                    for (String typeRaw : typesRaw) {
                        @SuppressWarnings("rawtypes")
                        MType type = MinigameTypes.get().getType(typeRaw);
                        if (type == null) {
                            sendDMessage(player, "join.error.invalid_type", "%type%", typeRaw, "%alias%", label);
                            return;
                        }
                        types.add(type);
                    }
                    @SuppressWarnings({"rawtypes"})
                    List<MGame> available = new ArrayList<>(GameManager.get().getGameInstances(types).values());
                    available.removeIf((m) -> switch (m.getPhase()) {
                        case STOPPED, END, RESTART -> true;
                        default -> false;
                    });
                    Collections.shuffle(available);
                    available.sort((m1, m2) -> (100 * (m2.getGamers().size() / m2.getMaxGamers() - m1.getGamers().size() / m1.getMaxGamers())));

                    if (GameManager.get().joinGameAsGamer(player, available))
                        return;

                    sendDMessage(player, "join.error.no_available_game_of_type", "%type%", args[0], "%alias%", label);
                }
            }
            default -> sendDMessage(player, "join.error.fastjoin_params", "%alias%", label);
        }
    }

    @Override
    public List<String> onComplete(@NotNull CommandSender sender, @NotNull String s, String @NotNull [] args, @Nullable Location location) {
        if (args.length != 1)
            return Collections.emptyList();
        int index = args[0].lastIndexOf(",");
        if (index == -1)
            return this.complete(args[0], MinigameTypes.get().getTypes(), MType::getType);
        String param = args[0].substring(index + 1);
        String base = args[0].substring(0, index + 1);
        List<String> completes = this.complete(param, MinigameTypes.get().getTypes(), MType::getType, (m) -> true);
        for (int i = 0; i < completes.size(); i++)
            completes.set(i, base + completes.get(i));
        return completes;
    }

}
