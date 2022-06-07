package emanondev.minigames;

import emanondev.core.MessageBuilder;
import emanondev.core.UtilsMessages;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class MessageUtil {

    public static @NotNull List<String> getMultiMessage(@NotNull CommandSender player, @NotNull String path, String... holders) {
        List<String> msg = Minigames.get().getLanguageConfig(player).loadMultiMessage(path, null, true, player, holders);
        if (msg == null || msg.isEmpty())
            debug("No message list found at &e" + path + "&f on language file &e"
                    + Minigames.get().getLanguageConfig(player).getFileName());
        return msg == null ? Collections.emptyList() : msg;
    }

    public static @NotNull String getMessage(@NotNull CommandSender player, @NotNull String path, String... holders) {
        String msg = Minigames.get().getLanguageConfig(player).loadMessage(path, (String) null, true, player, holders);
        if (msg == null || msg.isEmpty())
            debug("No message list found at &e" + path + "&f on language file &e"
                    + Minigames.get().getLanguageConfig(player).getFileName());
        return msg == null ? "" : msg;
    }

    public static @NotNull String getString(@NotNull CommandSender player, @NotNull String path, String... holders) {
        String msg = Minigames.get().getLanguageConfig(player).loadMessage(path, (String) null, false, player, holders);
        if (msg == null || msg.isEmpty())
            debug("No message list found at &e" + path + "&f on language file &e"
                    + Minigames.get().getLanguageConfig(player).getFileName());
        return msg == null ? "" : msg;
    }

    public static void sendMessage(@NotNull CommandSender player, @NotNull String path, String... holders) {
        new MessageBuilder(Minigames.get(), player).addText(getMessage(player, path, holders)).send();
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
