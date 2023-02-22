package emanondev.minigames.command;

import emanondev.core.command.CoreCommand;
import emanondev.core.gui.PagedListFGui;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JoinCommand extends CoreCommand {

    public JoinCommand() {
        super("join", Minigames.get(), Perms.COMMAND_JOIN, " join a game");
    }

    //join [game_id]
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
                //TODO open a gui
                ArrayList<MGame> values = new ArrayList<>(GameManager.get().getAll().values());
                values.removeIf((game) -> game.getPhase() == MGame.Phase.STOPPED);
                if (values.isEmpty()) {
                    sendMsg(player, "join.error.no_available_game", "%label%", label);
                    return;
                }
                PagedListFGui<MGame> gui = new PagedListFGui<>(
                        new DMessage(getPlugin(), player).appendLang("join.gui.title").toLegacy()
                        , 6, player, null, Minigames.get(),
                        false,
                        (e, game) ->
                                GameManager.get().joinGameAsGamer(player, game)
                        ,
                        (game) -> game.getGameSelectorItem(player));
                gui.addElements(values);
                gui.open(player);
            }
            case 1 -> {
                MType type = MinigameTypes.get().getType(args[0]);
                if (type != null) {
                    Collection<MGame> values = new ArrayList(GameManager.get().getGameInstances(type).values());
                    values.removeIf((game) -> game.getPhase() == MGame.Phase.STOPPED);
                    if (values.isEmpty()) {
                        sendMsg(player, "join.error.no_available_game", "%label%", label);
                        return;
                    }
                    PagedListFGui<MGame> gui = new PagedListFGui<>(
                            new DMessage(getPlugin(), player).appendLang("join.gui.title").toLegacy()
                            , 6, player, null, Minigames.get(),
                            true,
                            (e, game) ->
                                    GameManager.get().joinGameAsGamer(player, game)
                            ,
                            (game) -> game.getGameSelectorItem(player));
                    gui.addElements(values);
                    gui.open(player);
                    return;
                }
                //
                MGame game = GameManager.get().get(args[0]);
                if (game == null) {
                    sendMsg(player, "join.error.invalid_game_or_minigame", "%name%", game.getId(), "%label%", label);
                    return;
                }
                if (GameManager.get().joinGameAsGamer(player, game))
                    return;
                sendMsg(player, "join.error.game_not_available", "%name%", game.getId(), "%label%", label);
            }
            default -> sendMsg(player, "join.error.join_params", "%label%", label);
        }
    }

    @Override
    public List<String> onComplete(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args, @Nullable Location location) {
        return args.length == 1 ? this.complete(args[0], MinigameTypes.get().getTypesId()) : Collections.emptyList();
    }

    private void sendMsg(CommandSender target, String path, String... holders) {
        new DMessage(getPlugin(), target).appendLang(path, holders).send();
    }

    private void sendMsgList(CommandSender target, String path, String... holders) {
        new DMessage(getPlugin(), target).appendLangList(path, holders).send();
    }
}
