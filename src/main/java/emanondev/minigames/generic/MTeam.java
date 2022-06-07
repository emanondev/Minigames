package emanondev.minigames.generic;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public interface MTeam {
    boolean removeUser(@NotNull OfflinePlayer user);

    boolean removeUser(@NotNull UUID user);

    boolean addUser(@NotNull OfflinePlayer user);

    boolean addUser(@NotNull UUID user);

    boolean containsUser(@NotNull UUID user);

    boolean containsUser(@NotNull OfflinePlayer user);

    int getUsersAmount();

    @NotNull
    Set<UUID> getUsers();

    void clear();

    ChatColor getChatColor();

    String getName();
}
