package emanondev.minigames.command;

import emanondev.core.command.CoreCommand;
import emanondev.minigames.GameManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.Perms;
import emanondev.minigames.games.MGame;
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
        @SuppressWarnings("rawtypes")
        MGame game = GameManager.get().getCurrentGame(player);
        if (game == null) {
            sendDMessage(player, "leave.error.not_inside_game", "%alias%", label);
            return;
        }
        GameManager.get().quitGame(player);
        sendDMessage(player, "leave.success.leave", "%name%", game.getId(), "%alias%", label);

    }

    @Override
    public List<String> onComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @Nullable Location location) {
        return Collections.emptyList();
    }
}
