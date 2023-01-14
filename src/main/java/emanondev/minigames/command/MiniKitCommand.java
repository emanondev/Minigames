package emanondev.minigames.command;

import emanondev.core.PlayerSnapshot;
import emanondev.core.command.CoreCommandPlus;
import emanondev.core.command.SubCommandListExecutor;
import emanondev.minigames.Kit;
import emanondev.minigames.KitManager;
import emanondev.minigames.MessageUtil;
import emanondev.minigames.Minigames;
import emanondev.minigames.generic.Perms;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MiniKitCommand extends CoreCommandPlus {
    public MiniKitCommand() {
        super("minikit", Minigames.get(), Perms.COMMAND_MINIKIT,
                "set minigame available kits");
        this.addSubCommandHandler("list", new SubCommandListExecutor<>(this,
                "list", () -> {
            ArrayList<Kit> list = new ArrayList<>(KitManager.get().getKits().values());
            list.sort(Comparator.comparing(Kit::getId));
            return list;
        }
                , Kit::getId, ((kit, sender) -> kit.getId())), null);

        this.addSubCommandHandler("create", (sender, label, args) -> {
            if (!(sender instanceof Player player)) {
                this.playerOnlyNotify(sender);
                return;
            }
            if (args.length != 2) {
                MessageUtil.sendMessage(player, getPathLang("error.create_arguments_amount"), "%label%", label);
                return;
            }
            if (KitManager.get().getKit(args[1]) != null) {
                MessageUtil.sendMessage(player, getPathLang("error.already_used_id"), "%id%", args[1].toLowerCase());
                return;
            }
            KitManager.get().createKit(args[1], player);
            MessageUtil.sendMessage(player, getPathLang("success.create"), "%id%", args[1].toLowerCase());
        }, null);


        this.addSubCommandHandler("apply", (sender, label, args) -> {
            if (args.length != 2 && args.length != 3) {
                MessageUtil.sendMessage(sender, getPathLang("error.apply_arguments_amount"));
                return;
            }
            Player target = args.length == 3 ? this.readPlayer(sender, args[2]) : sender instanceof Player ? ((Player) sender) : null;
            if (target == null) {
                if (args.length == 3) {
                    MessageUtil.sendMessage(sender, getPathLang("error.apply_target_offline"), "%player%", args[2]);
                    return;
                }
                MessageUtil.sendMessage(sender, getPathLang("error.apply_target_required"), "%id%", args[1].toLowerCase());
                return;
            }
            Kit kit = KitManager.get().getKit(args[1]);
            if (kit == null) {
                MessageUtil.sendMessage(sender, getPathLang("error.unexisting_id"), "%id%", args[1].toLowerCase());
                return;
            }
            kit.apply(target);
            MessageUtil.sendMessage(sender, getPathLang("success.apply"), "%id%", args[1].toLowerCase()
                    , "%player%", target.getName());
        }, (sender, label, args) -> args.length == 2 ? this.complete(args[1], KitManager.get().getKitsId()) :
                (args.length == 3 ? this.completePlayerNames(sender, args[2]) : Collections.emptyList()));


        this.addSubCommandHandler("update", (sender, label, args) -> {
            if (!(sender instanceof Player player)) {
                this.playerOnlyNotify(sender);
                return;
            }
            if (args.length != 2) {
                MessageUtil.sendMessage(player, getPathLang("error.update_arguments_amount"));
                return;
            }
            Kit kit = KitManager.get().getKit(args[1]);
            if (kit == null) {
                MessageUtil.sendMessage(player, getPathLang("error.unexisting_id"), "%id%", args[1].toLowerCase());
                return;
            }
            kit.updateSnapshot(new PlayerSnapshot(player)); //not optimized
            MessageUtil.sendMessage(player, getPathLang("success.update"), "%id%", args[1].toLowerCase());
        }, (sender, label, args) -> args.length == 2 ? this.complete(args[1], KitManager.get().getKitsId()) : Collections.emptyList());
        this.addSubCommandHandler("delete", (sender, label, args) -> {
            if (args.length != 2) {
                MessageUtil.sendMessage(sender, getPathLang("error.delete_arguments_amount"));
                return;
            }
            if (KitManager.get().getKit(args[1]) == null) {
                MessageUtil.sendMessage(sender, getPathLang("error.unexisting_id"), "%id%", args[1].toLowerCase());
                return;
            }
            KitManager.get().deleteKit(args[1]);
            MessageUtil.sendMessage(sender, getPathLang("success.delete"), "%id%", args[1].toLowerCase());
        }, (sender, label, args) -> args.length == 2 ? this.complete(args[1], KitManager.get().getKitsId()) : Collections.emptyList());
    }
}
