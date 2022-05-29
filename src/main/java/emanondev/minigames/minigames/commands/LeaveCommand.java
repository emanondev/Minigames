package emanondev.minigames.minigames.commands;

import emanondev.core.CoreCommand;
import emanondev.core.CorePlugin;
import emanondev.core.MessageBuilder;
import emanondev.core.PermissionBuilder;
import emanondev.minigames.minigames.GameManager;
import emanondev.minigames.minigames.Minigames;
import emanondev.minigames.minigames.generic.MGame;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class LeaveCommand extends CoreCommand {
    public LeaveCommand() {
        super("leave", Minigames.get(), PermissionBuilder.ofCommand(Minigames.get(),"leave").buildAndRegister(Minigames.get()),
                "allow to quit the current game", List.of("quit","exit"));
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)){
            this.playerOnlyNotify(sender);
            return;
        }
        MGame game = GameManager.get().getGame(p);
        if (game==null){
            new MessageBuilder(Minigames.get(), p)
                    .addTextTranslation("leave.error.not_inside_game", "").send();
            return;
        }
        GameManager.get().quitGame(p);

    }

    @Override
    public List<String> onComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @Nullable Location location) {
        return Collections.emptyList();
    }
}
