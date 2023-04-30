package emanondev.minigames.games;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public interface MTeam {

    @SuppressWarnings("rawtypes")
    MGame getGame();

    Team getScoreboardTeam();

    boolean removeUser(@NotNull OfflinePlayer user);

    boolean removeUser(@NotNull UUID user);

    boolean addUser(@NotNull OfflinePlayer user);

    boolean addUser(@NotNull UUID user);

    boolean containsUser(@NotNull UUID user);

    boolean containsUser(@NotNull OfflinePlayer user);

    int getUsersAmount();

    @NotNull
    Set<UUID> getUsers();

    void reset();

    ChatColor getChatColor();

    String getName();
}
