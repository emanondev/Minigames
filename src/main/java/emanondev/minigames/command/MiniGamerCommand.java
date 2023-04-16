package emanondev.minigames.command;

import emanondev.core.command.CoreCommand;
import emanondev.minigames.GamerManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.Perms;
import emanondev.minigames.gamer.Gamer;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MiniGamerCommand extends CoreCommand {


    public MiniGamerCommand() {
        super("minigamer", Minigames.get(), Perms.COMMAND_GAMER);
    }

    /*
    info [player]
    addxp <xp> [player]
    addlv <lv> [player]
    setlv <lv> [player]
    setxp <xp> [player]
    reset [player]
     */
    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            help(sender, label);
            return;
        }
        switch (args[0].toLowerCase(Locale.ENGLISH)) {
            case "info" -> info(sender, label, args);
            case "reset" -> reset(sender, label, args);
            case "addxp" -> addxp(sender, label, args);
            case "addlv" -> addlv(sender, label, args);
            case "setxp" -> setxp(sender, label, args);
            case "setlv" -> setlv(sender, label, args);
            default -> help(sender, label);
        }
    }

    private void info(CommandSender sender, String label, String[] args) {
        if (args.length == 1 && !(sender instanceof Player)) {
            sendDMessage(sender, "minigamer.error.info_params", "%label%", label);
            return;
        }
        OfflinePlayer target = args.length == 1 ? (Player) sender : readOfflinePlayer(args[1]);
        if (target == null) {
            sendDMessage(sender, "minigamer.error.never_joined_server", "%name%", args[1], "%label%", label);
            return;
        }
        Gamer gamer = GamerManager.get().getGamer(target);
        sendDMessage(sender, "minigamer.success.info", "%level%", String.valueOf(gamer.getLevel()), "%exp%",
                String.valueOf(gamer.getExperience()), "%levelupexp%", String.valueOf(gamer.getLevelUpExperience())
                , "%exptolevelup%", String.valueOf(gamer.getExperienceToLevelUp()), "%name%", target.getName(), "%label%", label);
    }

    private void reset(CommandSender sender, String label, String[] args) {
        if (args.length == 1 && !(sender instanceof Player)) {
            sendDMessage(sender, "minigamer.error.reset_params", "%label%", label);
            return;
        }
        OfflinePlayer target = args.length == 1 ? (Player) sender : readOfflinePlayer(args[1]);
        if (target == null) {
            sendDMessage(sender, "minigamer.error.never_joined_server", "%name%", args[1], "%label%", label);
            return;
        }
        Gamer gamer = GamerManager.get().getGamer(target);
        gamer.reset();
        sendDMessage(sender, "minigamer.success.reset", "%name%", target.getName(), "%label%", label);
    }

    //addxp <xp> [player]
    private void addxp(CommandSender sender, String label, String[] args) {
        if (args.length <= 1) {
            sendDMessage(sender, "minigamer.error.addxp_params", "%label%", label);
            return;
        }
        Integer amount = readInt(args[1]);
        if (amount == null) {
            sendDMessage(sender, "minigamer.error.invalid_number", "%amount%", args[1], "%label%", label);
            return;
        }
        if (amount <= 0) {
            sendDMessage(sender, "minigamer.error.invalid_amount_min_1", "%amount%", args[1], "%label%", label);
            return;
        }
        if (args.length == 2 && !(sender instanceof Player)) {
            sendDMessage(sender, "minigamer.error.addxp_params", "%label%", label);
            return;
        }
        OfflinePlayer target = args.length == 2 ? (Player) sender : readOfflinePlayer(args[2]);
        if (target == null) {
            sendDMessage(sender, "minigamer.error.never_joined_server", "%name%", args[2], "%label%", label);
            return;
        }
        Gamer gamer = GamerManager.get().getGamer(target);
        gamer.addExperience(amount);
        sendDMessage(sender, "minigamer.success.addxp", "%amount%", String.valueOf(amount), "%name%", target.getName(), "%label%", label);
    }

    private void addlv(CommandSender sender, String label, String[] args) {
        if (args.length <= 1) {
            sendDMessage(sender, "minigamer.error.addlv_params", "%label%", label);
            return;
        }
        Integer amount = readInt(args[1]);
        if (amount == null) {
            sendDMessage(sender, "minigamer.error.invalid_number", "%amount%", args[1], "%label%", label);
            return;
        }
        if (amount <= 0) {
            sendDMessage(sender, "minigamer.error.invalid_amount_min_1", "%amount%", args[1], "%label%", label);
            return;
        }
        if (args.length == 2 && !(sender instanceof Player)) {
            sendDMessage(sender, "minigamer.error.addlv_params", "%label%", label);
            return;
        }
        OfflinePlayer target = args.length == 2 ? (Player) sender : readOfflinePlayer(args[2]);
        if (target == null) {
            sendDMessage(sender, "minigamer.error.never_joined_server", "%name%", args[2], "%label%", label);
            return;
        }
        Gamer gamer = GamerManager.get().getGamer(target);
        gamer.setLevel(gamer.getLevel() + amount);
        sendDMessage(sender, "minigamer.success.addlv", "%amount%", String.valueOf(amount), "%name%", target.getName(), "%label%", label);

    }

    private void setxp(CommandSender sender, String label, String[] args) {
        if (args.length <= 1) {
            sendDMessage(sender, "minigamer.error.setxp_params", "%label%", label);
            return;
        }
        Integer amount = readInt(args[1]);
        if (amount == null) {
            sendDMessage(sender, "minigamer.error.invalid_number", "%amount%", args[1], "%label%", label);
            return;
        }
        if (amount < 0) {
            sendDMessage(sender, "minigamer.error.invalid_amount_min_0", "%amount%", args[1], "%label%", label);
            return;
        }
        if (args.length == 2 && !(sender instanceof Player)) {
            sendDMessage(sender, "minigamer.error.setxp_params", "%label%", label);
            return;
        }
        OfflinePlayer target = args.length == 2 ? (Player) sender : readOfflinePlayer(args[2]);
        if (target == null) {
            sendDMessage(sender, "minigamer.error.never_joined_server", "%name%", args[2], "%label%", label);
            return;
        }
        Gamer gamer = GamerManager.get().getGamer(target);
        gamer.setExperience(amount);
        sendDMessage(sender, "minigamer.success.setxp", "%amount%", String.valueOf(amount), "%name%", target.getName(), "%label%", label);
    }


    private void setlv(CommandSender sender, String label, String[] args) {
        if (args.length <= 1) {
            sendDMessage(sender, "minigamer.error.setlv_params", "%label%", label);
            return;
        }
        Integer amount = readInt(args[1]);
        if (amount == null) {
            sendDMessage(sender, "minigamer.error.invalid_number", "%amount%", args[1], "%label%", label);
            return;
        }
        if (amount <= 0) {
            sendDMessage(sender, "minigamer.error.invalid_amount_min_1", "%amount%", args[1], "%label%", label);
            return;
        }
        if (args.length == 2 && !(sender instanceof Player)) {
            sendDMessage(sender, "minigamer.error.setlv_params", "%label%", label);
            return;
        }
        OfflinePlayer target = args.length == 2 ? (Player) sender : readOfflinePlayer(args[2]);
        if (target == null) {
            sendDMessage(sender, "minigamer.error.never_joined_server", "%name%", args[2], "%label%", label);
            return;
        }
        Gamer gamer = GamerManager.get().getGamer(target);
        gamer.setLevel(amount);
        sendDMessage(sender, "minigamer.success.setlv", "%amount%", String.valueOf(amount), "%name%", target.getName(), "%label%", label);
    }


    private void help(CommandSender sender, String label) {
        sendDMessage(sender, "minigamer.help", "%label%", label);
    }

    /*
    info [player]
    addxp <xp> [player]
    addlv <lv> [player]
    setlv <lv> [player]
    setxp <xp> [player]
    reset [player]
     */
    @Override
    public @Nullable List<String> onComplete(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args, @Nullable Location location) {
        return switch (args.length) {
            case 1 -> complete(args[0], List.of("info", "addxp", "addlv", "setxp", "setlv", "reset"));
            case 2 -> switch (args[1].toLowerCase(Locale.ENGLISH)) {
                case "info", "reset" -> completePlayerNames(sender, args[1]);
                case "addxp", "addlv", "setxp", "setlv" -> List.of("1", "10", "100", "1000");
                default -> Collections.emptyList();
            };
            case 3 -> switch (args[1].toLowerCase(Locale.ENGLISH)) {
                case "addxp", "addlv", "setxp", "setlv" -> completePlayerNames(sender, args[2]);
                default -> Collections.emptyList();
            };
            default -> Collections.emptyList();
        };
    }
}
