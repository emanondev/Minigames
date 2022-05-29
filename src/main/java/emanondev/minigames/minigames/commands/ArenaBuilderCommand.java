package emanondev.minigames.minigames.commands;

import emanondev.core.CoreCommand;
import emanondev.core.PermissionBuilder;
import emanondev.minigames.minigames.ArenaManager;
import emanondev.minigames.minigames.Minigames;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ArenaBuilderCommand extends CoreCommand {


    public ArenaBuilderCommand() {
        super("arenabuilder", Minigames.get(), PermissionBuilder.ofCommand(Minigames.get(), "arenabuilder").buildAndRegister(Minigames.get())
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
