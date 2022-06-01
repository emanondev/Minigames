package emanondev.minigames.generic;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public interface MTeam {
    boolean removeUser(@NotNull Player user);

    boolean removeUser(@NotNull UUID user);

    boolean addUser(@NotNull Player user);

    boolean addUser(@NotNull UUID user);

    boolean containsUser(@NotNull UUID user);

    boolean containsUser(@NotNull Player user);

    int getUsersAmount();

    @NotNull
    Set<UUID> getUsers();

    void clear();
}
