package emanondev.minigames.minigames.generic;


import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class ColoredTeam implements MTeam {

    private final HashSet<UUID> users = new HashSet<>();
    private final DyeColor color;

    public ColoredTeam(@NotNull DyeColor color) {
        this.color = color;
    }

    @Override
    public boolean removeUser(@NotNull Player user) {
        return addUser(user.getUniqueId());
    }

    @Override
    public boolean removeUser(@NotNull UUID user) {
        return users.add(user);
    }

    @Override
    public boolean addUser(@NotNull Player user) {
        return addUser(user.getUniqueId());
    }

    @Override
    public boolean addUser(@NotNull UUID user) {
        return users.add(user);
    }

    @NotNull
    public DyeColor getColor() {
        return color;
    }

    @Override
    public boolean containsUser(@NotNull UUID user) {
        return users.contains(user);
    }

    @Override
    public boolean containsUser(@NotNull Player user) {
        return containsUser(user.getUniqueId());
    }

    @Override
    public int getUsersAmount() {
        return users.size();
    }

    @Override
    public @NotNull Set<UUID> getUsers() {
        return Collections.unmodifiableSet(users);
    }

    @Override
    public void clear() {
        users.clear();
    }
}