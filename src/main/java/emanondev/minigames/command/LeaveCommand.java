package emanondev.minigames.command;

import emanondev.core.command.CoreCommand;
import emanondev.core.message.DMessage;
import emanondev.minigames.GameManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.generic.MGame;
import emanondev.minigames.generic.Perms;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class LeaveCommand extends CoreCommand {
    public LeaveCommand() {
        super("leave", Minigames.get(), Perms.COMMAND_LEAVE,
                "allow to quit the current game", List.of("quit", "exit"));
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        MGame game = GameManager.get().getCurrentGame(player);
        if (game == null) {
            sendMsg(player, "leave.error.not_inside_game", "%label%", label);
            return;
        }
        GameManager.get().quitGame(player);
        sendMsg(player, "leave.success.leave", "%name%", game.getId(), "%label%", label);

    }

    @Override
    public List<String> onComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @Nullable Location location) {
        return Collections.emptyList();
    }

    private void sendMsg(CommandSender target, String path, String... holders) {
        new DMessage(getPlugin(), target).appendLang(path, holders).send();
    }

    private void sendMsgList(CommandSender target, String path, String... holders) {
        new DMessage(getPlugin(), target).appendLangList(path, holders).send();
    }
}
