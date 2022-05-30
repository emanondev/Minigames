package emanondev.minigames.commands;

import emanondev.core.CoreCommand;
import emanondev.core.MessageBuilder;
import emanondev.core.PermissionBuilder;
import emanondev.core.UtilsCommand;
import emanondev.minigames.GameManager;
import emanondev.minigames.MinigameTypes;
import emanondev.minigames.Minigames;
import emanondev.minigames.generic.MGame;
import emanondev.minigames.generic.MType;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FastJoinCommand extends CoreCommand {

    public FastJoinCommand() {
        super("fastjoin", Minigames.get(), PermissionBuilder.ofCommand(Minigames.get(), "fastjoin")
                .setAccess(PermissionDefault.TRUE).buildAndRegister(Minigames.get()), "fast join a game");
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            this.playerOnlyNotify(sender);
            return;
        }
        if (GameManager.get().getGame(p) != null) {
            new MessageBuilder(Minigames.get(), p)
                    .addTextTranslation("fastjoin.error.already_inside_a_game", "").send();
            return;
        }

        switch (args.length) {
            case 0 -> {
                List<MGame> available = new ArrayList<>(GameManager.get().getGames().values());
                available.removeIf((m) -> switch (m.getPhase()) {
                    case STOPPED, END, RESTART -> true;
                    default -> false;
                });
                Collections.shuffle(available);
                available.sort((m1, m2) -> (int) (1000 * ((double) m2.getCollectedPlayers().size() / m2.getMaxPlayers() - (double) m1.getCollectedPlayers().size() / m1.getMaxPlayers())));

                if (GameManager.get().joinGameAsPlayer(p, available))
                    return;

                new MessageBuilder(Minigames.get(), p)
                        .addTextTranslation("fastjoin.error.no_available_game", "").send();
            }
            case 1 -> {
                MType type = MinigameTypes.get().getType(args[0]);
                if (type == null) {
                    new MessageBuilder(Minigames.get(), p)
                            .addTextTranslation("fastjoin.error.invalid_type", ""
                                    , "%type%", args[0]).send();
                    return;
                }
                List<MGame> available = new ArrayList<>(GameManager.get().getPreMadeGameInstances(type).values());
                available.removeIf((m) -> switch (m.getPhase()) {
                    case STOPPED, END, RESTART -> true;
                    default -> false;
                });
                Collections.shuffle(available);
                available.sort((m1, m2) -> (100 * (m2.getCollectedPlayers().size() / m2.getMaxPlayers() - m1.getCollectedPlayers().size() / m1.getMaxPlayers())));

                if (GameManager.get().joinGameAsPlayer(p, available))
                    return;

                new MessageBuilder(Minigames.get(), p)
                        .addTextTranslation("fastjoin.error.no_available_game_of_type", ""
                                , "%type%", type.getType()).send();
            }
            default -> {
                new MessageBuilder(Minigames.get(), p)
                        .addTextTranslation("fastjoin.error.arguments_amount", ""
                                , "%label%", label).send();
            }

        }
    }

    @Override
    public List<String> onComplete(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args, @Nullable Location location) {
        return switch (args.length) {
            case 1 -> UtilsCommand.complete(args[0], MinigameTypes.get().getTypes(), (m) -> m.getType(), (m) -> true);
            default -> Collections.emptyList();
        };
    }
}
