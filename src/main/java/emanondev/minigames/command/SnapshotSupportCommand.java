package emanondev.minigames.command;

import emanondev.core.CoreCommand;
import emanondev.core.PermissionBuilder;
import emanondev.core.packetentity.PacketItem;
import emanondev.minigames.Minigames;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
        PacketItem item = Minigames.get().getPacketManager().getPacketItem(p.getLocation());
        item.setGravity(false);
        item.setItemStack(new ItemStack(Material.DIAMOND),true);
        item.setCustomName("hola!");
        item.setCustomNameVisible(true);
        item.spawn(List.of(p));

        /*;
        p.setAllowFlight(true);
        p.setFlying(true);


        /*if (args[0].equals("load")) {
            if (args.length > 1)
                switch (args[1]) {
                    case "spect" -> Configurations.applyGameSpectatorSnapshot(p);
                    case "pre" -> Configurations.applyGamePreStartSnapshot(p);
                    case "game" -> Configurations.applyGameEmptyStartSnapshot(p);
                    case "collect" -> Configurations.applyGameCollectingPlayersSnapshot(p);
                    case "end" -> Configurations.applyGameEndSnapshot(p);
                }
            else
                Configurations.applyGameSpectatorSnapshot(p);

            return;
        }
        Minigames.get().getConfig("snapshots.yml").set(args[0], new PlayerSnapshot(p));*/
    }

    @Override
    public List<String> onComplete(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings, @Nullable Location location) {
        return null;
    }
}
