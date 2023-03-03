package emanondev.minigames.command;

import emanondev.core.UtilsInventory;
import emanondev.core.UtilsString;
import emanondev.core.command.CoreCommand;
import emanondev.core.message.DMessage;
import emanondev.minigames.DropGroupManager;
import emanondev.minigames.Kit;
import emanondev.minigames.KitManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.generic.DropGroup;
import emanondev.minigames.generic.Perms;
import emanondev.minigames.generic.Registrable;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;

public class MiniDropGroupCommand extends CoreCommand {

    public MiniDropGroupCommand() {
        super("minidropgroup", Minigames.get(), Perms.COMMAND_MINIDROPGROUP, "Edit drop groups");
    }

    /*
    create <id>
    addhand <id> [weight]
    addchest <id> [weight]
    gui <id>
    list
    delete <id>
     */
    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, String @NotNull [] args) {
        if (args.length == 0) {
            help(sender, label, args);
            return;
        }
        switch (args[0].toLowerCase(Locale.ENGLISH)) {
            case "create" -> create(sender, label, args);
            case "addhand" -> addhand(sender, label, args);
            case "addchest" -> addchest(sender, label, args);
            case "gui" -> gui(sender, label, args);
            case "list" -> list(sender, label, args);
            case "delete" -> delete(sender, label, args);
            default -> help(sender, label, args);

        }
    }

    private static final int DEFAULT_WEIGHT = 10;

    private void help(CommandSender sender, String label, String[] args) {
        sendMsgList(sender, "minidropgroup.help", "%alias%", label);
    }

    private void delete(CommandSender sender, String label, String[] args) {
        if (args.length <= 1) {
            sendMsg(sender, "minidropgroup.error.delete_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        DropGroup group = DropGroupManager.get().get(id);
        if (group == null) {
            sendMsg(sender, "minidropgroup.error.id_not_found", "%id%", id, "%alias%", label);
            return;
        }
        DropGroupManager.get().delete(group);
        sendMsg(sender, "minidropgroup.success.delete", "%id%", id, "%alias%", label);

    }

    private void create(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        if (args.length <= 1) {
            sendMsg(player, "minidropgroup.error.create_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        DropGroup group = DropGroupManager.get().get(id);
        if (group != null) {
            sendMsg(player, "minidropgroup.error.id_already_used", "%id%", id, "%alias%", label);
            return;
        }
        try {
            DropGroupManager.get().register(args[1].toLowerCase(Locale.ENGLISH), new DropGroup(), player);
            sendMsg(player, "minidropgroup.success.create", "%id%", id, "%alias%", label);
        } catch (IllegalArgumentException e) {
            sendMsg(player, "minidropgroup.error.invalid_id", "%id%", id, "%alias%", label);
        }
    }

    private void addhand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        if (args.length <= 1) {
            sendMsg(player, "minidropgroup.error.addhand_params", "%alias%", label);
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (UtilsInventory.isAirOrNull(item)) {
            sendMsg(player, "minidropgroup.error.no_item_in_hand", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        DropGroup group = DropGroupManager.get().get(id);
        if (group == null) {
            sendMsg(player, "minidropgroup.error.id_not_found", "%alias%", label, "%id%", id);
            return;
        }
        int weight = DEFAULT_WEIGHT;
        if (args.length > 2) {
            try {
                weight = Integer.parseInt(args[2]);
            } catch (Exception e) {
                sendMsg(player, "minidropgroup.error.invalid_weight", "%alias%", label, "%weight%", args[2]);
                return;
            }
            if (weight == 0) {
                sendMsg(player, "minidropgroup.error.null_weight", "%alias%", label, "%weight%", args[2]);
                return;
            }
        }
        group.addWeight(item, weight);
        sendMsg(player, "minidropgroup.success.addhand", "%alias%", label, "%weight%", String.valueOf(weight), "%id%", id);
    }

    private void addchest(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        if (args.length <= 1) {
            sendMsg(player, "minidropgroup.error.addchest_params", "%alias%", label);
            return;
        }
        RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation(), player.getEyeLocation().getDirection(), 10, FluidCollisionMode.NEVER, true);
        Block block = result == null ? null : result.getHitBlock();
        if (block == null || !(block.getState() instanceof Container container)) {
            sendMsg(player, "minidropgroup.error.not_looking_to_container", "%alias%", label);
            return;
        }
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack item : container.getInventory().getStorageContents())
            if (!UtilsInventory.isAirOrNull(item))
                items.add(item);
        if (items.isEmpty()) {
            sendMsg(player, "minidropgroup.error.empty_container", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        DropGroup group = DropGroupManager.get().get(id);
        if (group == null) {
            sendMsg(player, "minidropgroup.error.id_not_found", "%alias%", label, "%id%", id);
            return;
        }
        int weight = DEFAULT_WEIGHT;
        if (args.length > 2) {
            try {
                weight = Integer.parseInt(args[2]);
            } catch (Exception e) {
                sendMsg(player, "minidropgroup.error.invalid_weight", "%alias%", label, "%weight%", args[2]);
                return;
            }
            if (weight == 0) {
                sendMsg(player, "minidropgroup.error.null_weight", "%alias%", label, "%weight%", args[2]);
                return;
            }
        }

        //mark the container
        for (double i = block.getX(); i <= block.getX() + 1; i += 0.2) {
            player.spawnParticle(Particle.COMPOSTER, i, block.getY(), block.getZ(), 1);
            player.spawnParticle(Particle.COMPOSTER, i, block.getY() + 1, block.getZ(), 1);
            player.spawnParticle(Particle.COMPOSTER, i, block.getY(), block.getZ() + 1, 1);
            player.spawnParticle(Particle.COMPOSTER, i, block.getY() + 1, block.getZ() + 1, 1);
        }
        for (double i = block.getY(); i <= block.getY() + 1; i += 0.2) {
            player.spawnParticle(Particle.COMPOSTER, block.getX(), i, block.getZ(), 1);
            player.spawnParticle(Particle.COMPOSTER, block.getX() + 1, i, block.getZ(), 1);
            player.spawnParticle(Particle.COMPOSTER, block.getX(), i, block.getZ() + 1, 1);
            player.spawnParticle(Particle.COMPOSTER, block.getX() + 1, i, block.getZ() + 1, 1);
        }
        for (double i = block.getZ(); i <= block.getZ() + 1; i += 0.2) {
            player.spawnParticle(Particle.COMPOSTER, block.getX(), block.getY(), i, 1);
            player.spawnParticle(Particle.COMPOSTER, block.getX() + 1, block.getY(), i, 1);
            player.spawnParticle(Particle.COMPOSTER, block.getX(), block.getY() + 1, i, 1);
            player.spawnParticle(Particle.COMPOSTER, block.getX() + 1, block.getY() + 1, i, 1);
        }


        for (ItemStack item : items)
            group.addWeight(item, weight);
        sendMsg(player, "minidropgroup.success.addchest", "%alias%", label, "%weight%", String.valueOf(weight), "%items%", String.valueOf(items.size()), "%id%", id);
    }

    private void list(CommandSender sender, String label, String[] args) {
        DMessage msg = new DMessage(getPlugin(), sender).appendLang("minidropgroup.success.list_prefix").newLine();
        boolean color = true;
        Color color1 = new Color(66, 233, 245);
        Color color2 = new Color(66, 179, 245);
        ArrayList<DropGroup> list = new ArrayList<>(DropGroupManager.get().getAll().values());
        list.sort(Comparator.comparing(Registrable::getId));
        for (DropGroup drop : list) {
            msg.appendHover(
                    new DMessage(getPlugin(), sender).appendLangList("minidropgroup.success.list_info",
                            UtilsString.merge(drop.getPlaceholders(), "%alias%", label)), new DMessage(getPlugin(), sender).append(color ? color1 : color2)
                            .appendRunCommand("/" + label + " gui " + drop.getId(), drop.getId())).append(" ");
            color = !color;
        }
        msg.send();
    }

    private void gui(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        if (args.length <= 1) {
            sendMsg(player, "minidropgroup.error.gui_params", "%alias%", label);
            return;
        }
        DropGroup group = DropGroupManager.get().get(args[1]);
        if (group == null) {
            sendMsg(player, "minidropgroup.error.id_not_found", "%alias%", label, "%id%", args[1]);
            return;
        }
        group.getEditorGui(player).open(player);
    }

    @Override
    public @Nullable List<String> onComplete(@NotNull CommandSender sender, @NotNull String label, String @NotNull [] args, @Nullable Location location) {
        return switch (args.length) {
            case 1 -> this.complete(args[0], List.of("create", "addhand", "addchest", "gui", "list", "delete"));
            case 2 -> switch (args[0].toLowerCase(Locale.ENGLISH)) {
                case "addhand", "addchest", "gui", "delete" -> this.complete(args[1], DropGroupManager.get().getAll().keySet());
                default -> Collections.emptyList();
            };
            case 3 -> switch (args[0].toLowerCase(Locale.ENGLISH)) {
                case "addhand", "addchest" -> this.complete(args[2], List.of("10", "50", "100"));
                default -> Collections.emptyList();
            };
            default -> Collections.emptyList();
        };
    }

    private void sendMsg(CommandSender target, String path, String... holders) {
        new DMessage(getPlugin(), target).appendLang(path, holders).send();
    }

    private void sendMsgList(CommandSender target, String path, String... holders) {
        new DMessage(getPlugin(), target).appendLangList(path, holders).send();
    }
}
