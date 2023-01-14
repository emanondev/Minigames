package emanondev.minigames.command;

import emanondev.core.command.CoreCommand;
import emanondev.minigames.ArenaManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.generic.Perms;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ArenaBuilderCommand extends CoreCommand {


    public ArenaBuilderCommand() {
        super("arenabuilder", Minigames.get(), Perms.COMMAND_ARENABUILDER
                , "setup arenas");
    }

    @Override
    public void onExecute(@NotNull CommandSender commandSender, @NotNull String label, @NotNull String[] args) {
        ArenaManager.get().onArenaBuilderExecute((Player) commandSender, label, args);
    }

    @Override
    public List<String> onComplete(@NotNull CommandSender commandSender, @NotNull String label, @NotNull String[] args, @Nullable Location location) {
        return ArenaManager.get().onArenaBuilderComplete((Player) commandSender, label, args);
    }
}
