package emanondev.minigames.commands;

import emanondev.core.CoreCommand;
import emanondev.core.PermissionBuilder;
import emanondev.minigames.GameManager;
import emanondev.minigames.MessageUtil;
import emanondev.minigames.Minigames;
import emanondev.minigames.generic.MGame;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class LeaveCommand extends CoreCommand {
    public LeaveCommand() {
        super("leave", Minigames.get(), PermissionBuilder.ofCommand(Minigames.get(), "leave")
                        .setAccess(PermissionDefault.TRUE).buildAndRegister(Minigames.get()),
                "allow to quit the current game", List.of("quit", "exit"));
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        MGame game = GameManager.get().getGame(player);
        if (game == null) {
            MessageUtil.sendMessage(player, "leave.error.not_inside_game");
            return;
        }
        GameManager.get().quitGame(player);
        MessageUtil.sendMessage(player, "leave.success.message");

    }

    @Override
    public List<String> onComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @Nullable Location location) {
        return Collections.emptyList();
    }
}