package emanondev.minigames;

import emanondev.core.UtilsMessages;
import emanondev.core.UtilsString;
import emanondev.core.message.MessageComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Deprecated
public class MessageUtil {

    public static @NotNull List<String> getMultiMessage(@NotNull CommandSender player, @NotNull String path, String... holders) {
        return Minigames.get().getLanguageConfig(player).getTrackMultiMessage(path, player, holders);
    }

    public static @NotNull String getMessage(@NotNull CommandSender player, @NotNull String path, String... holders) {
        return Minigames.get().getLanguageConfig(player).getTrackMessage(path, player, holders);
    }

    public static @NotNull String getString(@NotNull CommandSender player, @NotNull String path, String... holders) {
        return UtilsString.fix(Minigames.get().getLanguageConfig(player).getTrackString(path), player instanceof Player ? (Player) player : null, false, holders);
    }

    public static void sendMessage(@NotNull CommandSender player, @NotNull String path, String... holders) {
        new MessageComponent(Minigames.get(), player).append(getMessage(player, path, holders)).send();
    }

    public static void sendSubTitle(@NotNull Player player, @NotNull String path, String... holders) {
        player.sendTitle(" ", getMessage(player, path, holders), 0, 1, 0);
    }

    public static void clearTitle(@NotNull Player player) {
        player.sendTitle(" ", "", 0, 1, 0);
    }

    public static void debug(String message) {
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "Debug " + message);
    }

    public static void sendActionBarMessage(@NotNull Player player, @NotNull String path, String... holders) {
        UtilsMessages.sendActionbar(player, getMessage(player, path, holders));
    }

    public static void sendEmptyActionBarMessage(@NotNull Player player) {
        UtilsMessages.sendActionbar(player, "");
    }
}
