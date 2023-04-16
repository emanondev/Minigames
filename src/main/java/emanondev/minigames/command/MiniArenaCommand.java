package emanondev.minigames.command;

import emanondev.core.UtilsString;
import emanondev.core.command.CoreCommand;
import emanondev.core.message.DMessage;
import emanondev.core.util.WorldEditUtility;
import emanondev.minigames.ArenaManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.Perms;
import emanondev.minigames.games.MArena;
import emanondev.minigames.games.MSchemArena;
import emanondev.minigames.games.Registrable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;

public class MiniArenaCommand extends CoreCommand {

    /* //TODO filter list with type
     * list
     * delete <id>
     * paste
     * update
     */
    public MiniArenaCommand() {
        super("miniarena", Minigames.get(), Perms.COMMAND_MINIOPTION
                , "setup arena");
        task = new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                tick++;
                for (UUID uuid : pasted.keySet()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p == null || !p.isValid())
                        return;
                    if (!Objects.equals(p.getWorld(), pasted.get(uuid).world))
                        return;
                    spawnParticleBoxFaces(p, tick, pasted.get(uuid).box);
                }
            }
        }.runTaskTimer(getPlugin(), 20L, 20L);
    }

    //from MArenaBuilder START
    private void spawnParticleBoxFaces(Player p, int tick, BoundingBox box) {
        markFaces(p, tick, box.getMin(), box.getMax());
    }

    private static final int RATEO = 5;
    private static final int RADIUS = 100;

    private void markFaces(Player p, int val, Vector min, Vector max) {
        Location l = p.getLocation();
        int xMin = Math.max(l.getBlockX() - RADIUS, min.getBlockX()), xMax = Math.min(l.getBlockX() + RADIUS, max.getBlockX() + 1);
        int zMin = Math.max(l.getBlockZ() - RADIUS, min.getBlockZ()), zMax = Math.min(l.getBlockZ() + RADIUS, max.getBlockZ() + 1);
        for (int x = xMin; x <= xMax; x++)
            for (int z = zMin; z <= zMax; z++) {
                if (Math.abs(x + min.getBlockY() + z) % RATEO == val % RATEO)
                    spawnParticle(p, x, min.getBlockY(), z);
                if (Math.abs(x + max.getBlockY() + 1 + z) % RATEO == val % RATEO)
                    spawnParticle(p, x, max.getBlockY() + 1, z);
            }
        for (int x = xMin; x <= xMax; x++)
            for (int y = min.getBlockY(); y <= max.getBlockY() + 1; y++) {
                if (Math.abs(x + y + min.getBlockZ()) % RATEO == val % RATEO)
                    spawnParticle(p, x, y, min.getBlockZ());
                if (Math.abs(x + y + max.getBlockZ() + 1) % RATEO == val % RATEO)
                    spawnParticle(p, x, y, max.getBlockZ() + 1);
            }
        for (int z = zMin; z <= zMax; z++)
            for (int y = min.getBlockY(); y <= max.getBlockY() + 1; y++) {
                if (Math.abs(min.getBlockX() + y + z) % RATEO == val % RATEO)
                    spawnParticle(p, min.getBlockX(), y, z);
                if (Math.abs(max.getBlockX() + 1 + y + z) % RATEO == val % RATEO)
                    spawnParticle(p, max.getBlockX() + 1, y, z);
            }
    }

    private void spawnParticle(Player p, double x, double y, double z) {
        p.spawnParticle(Particle.WAX_OFF, x, y, z, 0, 0, 0, 0, 0, null);
    }
    //from MArenaBuilder END

    private BukkitTask task;

    public void reload() {
        //restart task?
    }


    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            help(sender, label, args);
            return;
        }
        switch (args[0].toLowerCase(Locale.ENGLISH)) {
            case "list" -> list(sender, label, args);
            case "delete" -> delete(sender, label, args);
            case "paste" -> paste(sender, label, args);
            case "update" -> update(sender, label, args);
            case "gui" -> gui(sender, label, args);
            default -> help(sender, label, args);
        }
    }

    private final HashMap<UUID, SchemInfo> pasted = new HashMap<>();

    private static class SchemInfo {
        private final BoundingBox box;
        private final String id;
        private final UUID user;
        private final World world;

        private SchemInfo(@NotNull String id, @NotNull BoundingBox box, @NotNull UUID user, @NotNull World world) {
            this.id = id;
            this.box = box;
            this.user = user;
            this.world = world;
        }

        public void show() {
            Player player = Bukkit.getPlayer(user);
            if (player == null)
                return;

            //TODO spawn particles
        }

    }

    //miniarena paste <id> [-confirm]
    private void paste(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        if (args.length != 2 && args.length != 3) {
            sendDMessage(sender, "miniarena.error.paste_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        MArena arena = ArenaManager.get().get(id);
        if (arena == null) {
            sendDMessage(sender, "miniarena.error.id_not_found", "%id%", id, "%alias%", label);
            return;
        }
        if (!(arena instanceof MSchemArena schemArena)) {
            sendDMessage(sender, "miniarena.error.no_schematic", "%id%", id, "%alias%", label);
            return;
        }
        World w = player.getWorld();
        if (w.getMaxHeight() < player.getLocation().getY() + schemArena.getSize().getBlockY()) {
            sendDMessage(sender, "miniarena.error.too_high", "%id%", id, "%alias%", label);
            return;
        }
        if (w.getMinHeight() > player.getLocation().getY()) {
            sendDMessage(sender, "miniarena.error.too_low", "%id%", id, "%alias%", label);
            return;
        }
        if (args.length == 2 || !args[2].equalsIgnoreCase("-confirm")) {
            new DMessage(getPlugin(), sender).appendRunCommand("/" + label + " " + args[0] + " " + args[1] + " -confirm",
                    getDMessage(sender, "miniarena.error.paste_need_confirmation", "%id%", id, "%alias%", label)).send();
            return;
        }
        sendDMessage(sender, "miniarena.success.pasting", "%id%", id, "%alias%", label);
        schemArena.paste(player.getLocation()).whenComplete((s, th) -> sendDMessage(sender, "miniarena.success.pasted", "%id%", id, "%alias%", label));
        UUID user = player.getUniqueId();
        Location loc = player.getLocation().getBlock().getLocation();
        BoundingBox box = BoundingBox.of(loc, loc.clone().add(schemArena.getSize().getBlockX() - 1, schemArena.getSize().getBlockY() - 1, schemArena.getSize().getBlockZ() - 1));
        SchemInfo info = new SchemInfo(id, box, user, player.getWorld());
        pasted.put(info.user, info);
    }

    //miniarena update [-confirm]
    private void update(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        if (args.length != 1 && args.length != 2) {
            sendDMessage(sender, "miniarena.error.update_params", "%alias%", label);
            return;
        }
        SchemInfo info = pasted.get(player.getUniqueId());
        if (info == null) {
            sendDMessage(sender, "miniarena.error.nothing_pasted", "%alias%", label);
            return;
        }

        //no arena

        MArena arena = ArenaManager.get().get(info.id);
        if (arena == null) {
            sendDMessage(sender, "miniarena.error.updating_id_not_found", "%id%", info.id, "%alias%", label);
            return;
        }
        if (!(arena instanceof MSchemArena schemArena)) {
            sendDMessage(sender, "miniarena.error.updating_no_schematic", "%id%", info.id, "%alias%", label);
            return;
        }
        if ((schemArena.getSize().getBlockX() - 1) * (schemArena.getSize().getBlockY() - 1) * (schemArena.getSize().getBlockZ() - 1) != info.box.getVolume()) { //TODO check that might need +1
            sendDMessage(sender, "miniarena.error.updating_different_size", "%id%", info.id, "%alias%", label);
            return;
        }
        if (args.length == 1 || !args[1].equalsIgnoreCase("-confirm")) {
            new DMessage(getPlugin(), sender).appendRunCommand("/" + label + " " + args[0] + " -confirm",
                    getDMessage(sender, "miniarena.error.update_need_confirmation", "%id%", info.id, "%alias%", label)).send();
            return;
        }
        sendDMessage(sender, "miniarena.success.updating", "%id%", info.id, "%alias%", label);
        File file = schemArena.getSchematicFile();
        try {
            if (!file.renameTo(new File(file.getParentFile(), "old_" + file.getName())))
                throw new IllegalStateException();
            if (WorldEditUtility.save(file, WorldEditUtility.copy(info.world, info.box, false, true))) {
                pasted.remove(info.user);
                sendDMessage(sender, "miniarena.success.updated", "%id%", info.id, "%alias%", label);
                schemArena.invalidateCache();
            } else
                sendDMessage(sender, "miniarena.error.update_failed", "%id%", info.id, "%alias%", label);
        } catch (Exception e) {
            e.printStackTrace();
            sendDMessage(sender, "miniarena.error.update_failed", "%id%", info.id, "%alias%", label);
        }
    }

    private void gui(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            this.playerOnlyNotify(sender);
            return;
        }
        if (args.length <= 1) {
            sendDMessage(sender, "miniarena.error.gui_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        MArena arena = ArenaManager.get().get(id);
        if (arena == null) {
            sendDMessage(sender, "miniarena.error.id_not_found", "%id%", id, "%alias%", label);
            return;
        }
        arena.getEditorGui(player, null).open(player);
    }

    @Override
    public @Nullable List<String> onComplete(@NotNull CommandSender sender, @NotNull String label, String @NotNull [] args, @Nullable Location location) {
        return switch (args.length) {
            case 1 -> this.complete(args[0], List.of("list", "delete", "paste", "gui"));
            case 2 -> switch (args[0].toLowerCase(Locale.ENGLISH)) {
                case "delete", "paste", "gui" -> this.complete(args[1], ArenaManager.get().getAll().keySet());
                default -> Collections.emptyList();
            };
            default -> Collections.emptyList();
        };
    }

    private void help(CommandSender sender, String label, String[] args) {
        sendDMessage(sender, "miniarena.help", "%alias%", label);
    }

    private void delete(CommandSender sender, String label, String[] args) {
        if (args.length <= 1) {
            sendDMessage(sender, "miniarena.error.delete_params", "%alias%", label);
            return;
        }
        String id = args[1].toLowerCase(Locale.ENGLISH);
        MArena arena = ArenaManager.get().get(id);
        if (arena == null) {
            sendDMessage(sender, "miniarena.error.id_not_found", "%id%", id, "%alias%", label);
            return;
        }
        //TODO is used???
        ArenaManager.get().delete(arena);
        sendDMessage(sender, "miniarena.success.delete", "%id%", id, "%alias%", label);
    }

    private void list(CommandSender sender, String label, String[] args) {
        DMessage msg = new DMessage(getPlugin(), sender).appendLang("miniarena.success.list_prefix").newLine();
        boolean color = true;
        Color color1 = new Color(66, 233, 245);
        Color color2 = new Color(66, 179, 245);
        ArrayList<MArena> list = new ArrayList<>(ArenaManager.get().getAll().values());
        list.sort(Comparator.comparing(Registrable::getId));
        for (MArena arena : list) {
            msg.appendHover(
                    new DMessage(getPlugin(), sender).appendLang("miniarena.success.list_info",
                            UtilsString.merge(arena.getPlaceholders(), "%alias%", label)), new DMessage(getPlugin(), sender).append(color ? color1 : color2)
                            .appendRunCommand("/" + label + " gui " + arena.getId(), arena.getId())).append(" ");
            color = !color;
        }
        msg.send();
    }
}