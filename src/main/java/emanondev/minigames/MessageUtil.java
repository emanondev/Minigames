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

    public static void debug(String message) {
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "Debug " + message);
    }
}
