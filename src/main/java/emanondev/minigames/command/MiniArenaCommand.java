package emanondev.minigames.command;

import emanondev.core.UtilsString;
import emanondev.core.command.CoreCommand;
import emanondev.core.message.DMessage;
import emanondev.core.util.WorldEditUtility;
import emanondev.minigames.ArenaManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.generic.MArena;
import emanondev.minigames.generic.MSchemArena;
import emanondev.minigames.generic.Perms;
import emanondev.minigames.generic.Registrable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

        private SchemInfo(@NotNull String id,@NotNull BoundingBox box,@NotNull UUID user,@NotNull World world){
            this.id = id;
            this.box = box;
            this.user = user;
            this.world = world;
        }

        public void show(){
            Player player = Bukkit.getPlayer(user);
            if (player==null)
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
        if (args.length !=2 && args.length !=3) {
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
        if (w.getMaxHeight()<player.getLocation().getY()+schemArena.getSize().getBlockY()){
            sendDMessage(sender, "miniarena.error.too_high", "%id%", id, "%alias%", label);
            return;
        }
        if (w.getMinHeight()>player.getLocation().getY()){
            sendDMessage(sender, "miniarena.error.too_low", "%id%", id, "%alias%", label);
            return;
        }
        if (args.length==2 || !args[2].equalsIgnoreCase("-confirm")){
            new DMessage(getPlugin(),sender).appendRunCommand("/"+label+" "+args[0]+" "+args[1]+" -confirm",
                    getDMessage(sender, "miniarena.error.paste_need_confirmation", "%id%", id, "%alias%", label));
            return;
        }
        sendDMessage(sender, "miniarena.success.pasting", "%id%", id, "%alias%", label);
        schemArena.paste(player.getLocation()).whenComplete((s,th)->sendDMessage(sender, "miniarena.success.pasted", "%id%", id, "%alias%", label));
        UUID user = player.getUniqueId();
        Location loc = player.getLocation().getBlock().getLocation();
        BoundingBox box = BoundingBox.of(loc,loc.clone().add(schemArena.getSize().getBlockX(),schemArena.getSize().getBlockY(),schemArena.getSize().getBlockZ()));
        SchemInfo info = new SchemInfo(id, box, user,player.getWorld());

        pasted.put(info.user,info);
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
        if (info==null){
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
        if (schemArena.getSize().getBlockX()*schemArena.getSize().getBlockY()*schemArena.getSize().getBlockZ()!=info.box.getVolume()) { //TODO check that might need +1
            sendDMessage(sender, "miniarena.error.updating_different_size", "%id%", info.id, "%alias%", label);
            return;
        }
        if (args.length==1 || !args[1].equalsIgnoreCase("-confirm")){
            new DMessage(getPlugin(),sender).appendRunCommand("/"+label+" "+args[0]+" -confirm",
                    getDMessage(sender, "miniarena.error.update_need_confirmation", "%id%", info.id, "%alias%", label));
            return;
        }
        sendDMessage(sender, "miniarena.success.updating", "%id%", info.id, "%alias%", label);
        File file = schemArena.getSchematicFile();
        try {
            if (!file.renameTo(new File(file.getParentFile(),"old_"+file.getName())))
                throw new IllegalStateException();
            if (WorldEditUtility.save(file,WorldEditUtility.copy(info.world,info.box,false,true))) {
                pasted.remove(info.user);
                sendDMessage(sender, "miniarena.success.updated", "%id%", info.id, "%alias%", label);
            }else
                sendDMessage(sender, "miniarena.error.update_failed", "%id%", info.id, "%alias%", label);
        } catch (Exception e) {
            e.printStackTrace();
            sendDMessage(sender, "miniarena.success.updated", "%id%", info.id, "%alias%", label);
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
                    new DMessage(getPlugin(), sender).appendLangList("miniarena.success.list_info",
                            UtilsString.merge(arena.getPlaceholders(), "%alias%", label)), new DMessage(getPlugin(), sender).append(color ? color1 : color2)
                            .appendRunCommand("/" + label + " gui " + arena.getId(), arena.getId())).append(" ");
            color = !color;
        }
        msg.send();
    }
}