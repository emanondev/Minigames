package emanondev.minigames.command;

import emanondev.core.command.CoreCommand;
import emanondev.core.gui.FButton;
import emanondev.core.gui.PagedMapGui;
import emanondev.core.message.DMessage;
import emanondev.minigames.GameManager;
import emanondev.minigames.MinigameTypes;
import emanondev.minigames.Minigames;
import emanondev.minigames.Perms;
import emanondev.minigames.games.MGame;
import emanondev.minigames.games.MType;
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
    @SuppressWarnings("unchecked")
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
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
                ArrayList<MGame> values = new ArrayList<>(GameManager.get().getAll().values());
                values.removeIf((game) -> game.getPhase() == MGame.Phase.STOPPED);
                if (values.isEmpty()) {
                    sendDMessage(player, "join.error.no_available_game", "%alias%", label);
                    return;
                }
                PagedMapGui gui = new PagedMapGui(
                        new DMessage(getPlugin(), player).appendLang("join.gui.title").toLegacy()
                        , 6, player, null, Minigames.get(),
                        true);
                for (@SuppressWarnings("rawtypes") MGame game : values) {
                    FButton button = new FButton(gui, () -> game.getGameSelectorItem(player),
                            (e) -> GameManager.get().joinGameAsGamer(player, game));
                    if (gui.getButton(game.getJoinGuiSlot()) == null)
                        gui.setButton(game.getJoinGuiSlot(), button);
                    else
                        gui.addButton(button);
                }
                gui.open(player);
            }
            case 1 -> {
                @SuppressWarnings({"rawtypes"})
                MType type = MinigameTypes.get().getType(args[0]);
                if (type != null) {
                    @SuppressWarnings({"rawtypes"})
                    Collection<MGame> values = new ArrayList(GameManager.get().getGameInstances(type).values());
                    values.removeIf((game) -> game.getPhase() == MGame.Phase.STOPPED);
                    if (values.isEmpty()) {
                        sendDMessage(player, "join.error.no_available_game", "%alias%", label);
                        return;
                    }
                    PagedMapGui gui = new PagedMapGui(
                            new DMessage(getPlugin(), player).appendLang("join.gui.title").toLegacy()
                            , 6, player, null, Minigames.get(),
                            true);
                    for (@SuppressWarnings("rawtypes") MGame game : values) {
                        FButton button = new FButton(gui, () -> game.getGameSelectorItem(player),
                                (e) -> GameManager.get().joinGameAsGamer(player, game));
                        if (gui.getButton(game.getJoinTypeGuiSlot()) == null)
                            gui.setButton(game.getJoinTypeGuiSlot(), button);
                        else
                            gui.addButton(button);
                    }
                    gui.open(player);
                    return;
                }
                @SuppressWarnings("rawtypes")
                MGame game = GameManager.get().get(args[0]);
                if (game == null) {
                    sendDMessage(player, "join.error.invalid_game_or_minigame", "%name%", args[0], "%alias%", label);
                    return;
                }
                if (GameManager.get().joinGameAsGamer(player, game))
                    return;
                sendDMessage(player, "join.error.game_not_available", "%name%", game.getId(), "%alias%", label);
            }
            default -> sendDMessage(player, "join.error.join_params", "%alias%", label);
        }
    }

    @Override
    public List<String> onComplete(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args, @Nullable Location location) {
        return args.length == 1 ? this.complete(args[0], MinigameTypes.get().getTypesId()) : Collections.emptyList();
    }
}
