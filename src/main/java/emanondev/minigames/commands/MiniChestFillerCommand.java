package emanondev.minigames.commands;

import emanondev.core.CoreCommand;
import emanondev.core.PermissionBuilder;
import emanondev.minigames.Minigames;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MiniChestFillerCommand extends CoreCommand {
    public MiniChestFillerCommand() {
        super("minichestfiller", Minigames.get(), PermissionBuilder.ofCommand(Minigames.get(), "minichestfiller")
                .buildAndRegister(Minigames.get()), "sets chest filler options");
    }

    @Override
    public void onExecute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {

    }

    @Override
    public List<String> onComplete(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings, @Nullable Location location) {
        return null;
    }
}
