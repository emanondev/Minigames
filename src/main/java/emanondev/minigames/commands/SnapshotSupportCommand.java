package emanondev.minigames.commands;

import emanondev.core.CoreCommand;
import emanondev.core.PermissionBuilder;
import emanondev.core.PlayerSnapshot;
import emanondev.minigames.Minigames;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SnapshotSupportCommand extends CoreCommand {

    public SnapshotSupportCommand() {
        super("SnapshotSupport", Minigames.get(), PermissionBuilder.ofCommand(
                Minigames.get(), "SnapshotSupport").build());
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args) {
        Player p = (Player) sender;
        Minigames.get().getConfig("snapshots.yml").set(args[0], new PlayerSnapshot(p));
    }

    @Override
    public List<String> onComplete(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings, @Nullable Location location) {
        return null;
    }
}
