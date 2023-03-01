package emanondev.minigames;

import emanondev.core.message.MessageComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MessageUtil {

    @Deprecated
    public static @NotNull List<String> getMultiMessage(@NotNull CommandSender player, @NotNull String path, String... holders) {
        return Minigames.get().getLanguageConfig(player).getTrackMultiMessage(path, player, holders);
    }

    @Deprecated
    public static @NotNull String getMessage(@NotNull CommandSender player, @NotNull String path, String... holders) {
        return Minigames.get().getLanguageConfig(player).getTrackMessage(path, player, holders);
    }

    @Deprecated
    public static void sendMessage(@NotNull CommandSender player, @NotNull String path, String... holders) {
        new MessageComponent(Minigames.get(), player).append(getMessage(player, path, holders)).send();
    }

    public static void debug(String message) {
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "Debug " + message);
    }
}
