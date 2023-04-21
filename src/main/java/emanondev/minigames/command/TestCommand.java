package emanondev.minigames.command;

import emanondev.core.command.CoreCommand;
import emanondev.minigames.Minigames;
import emanondev.minigames.games.eggwars.ShopsMenu;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TestCommand extends CoreCommand {


    public TestCommand() {
        super("test2", Minigames.get(), new Permission("aaa.bbb"));
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String s, String @NotNull [] args) {
        Player p = (Player) sender;
        new ShopsMenu(p).open(p);
    }

    @Override
    public @Nullable List<String> onComplete(@NotNull CommandSender sender, @NotNull String s, String @NotNull [] args, @Nullable Location location) {
        return null;
    }
}
