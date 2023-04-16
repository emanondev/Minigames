package emanondev.minigames.games;


import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class ColoredTeam implements MTeam {

    private final HashSet<UUID> users = new HashSet<>();
    private final DyeColor color;
    @SuppressWarnings("rawtypes")
    private final AbstractMGame game;
    private final Team team;
    private final ChatColor chatColor;

    public ColoredTeam(@SuppressWarnings("rawtypes") @NotNull AbstractMGame game, @NotNull DyeColor color) {
        this.color = color;
        this.chatColor = ChatColor.of(new java.awt.Color(color.getColor().getRed(), color.getColor().getGreen(), color.getColor()
                .getBlue()));
        this.game = game;
        this.team = game.getScoreboard().registerNewTeam(game.getId() + "_" + color.name().toLowerCase(Locale.ENGLISH));
        team.setPrefix(chatColor.toString() + "⬛ " + ChatColor.WHITE); //smaller ■
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public AbstractMGame getGame() {
        return game;
    }

    @Override
    public Team getScoreboardTeam() {
        return team;
    }

    @Override
    public boolean removeUser(@NotNull OfflinePlayer user) {
        return removeUser(user.getUniqueId());
    }

    @Override
    public boolean removeUser(@NotNull UUID user) {
        if (users.remove(user)) {
            team.removeEntry(Bukkit.getOfflinePlayer(user).getName());
            return true;
        }
        return false;
    }

    @Override
    public boolean addUser(@NotNull OfflinePlayer user) {
        return addUser(user.getUniqueId());
    }

    @Override
    public boolean addUser(@NotNull UUID user) {
        if (users.add(user)) {
            team.addEntry(Bukkit.getOfflinePlayer(user).getName());
            return true;
        }
        return false;
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
    public boolean containsUser(@NotNull OfflinePlayer user) {
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


    @Override
    public String getName() {
        if (game.getOption().getTeamMaxSize() == 1 && users.size() == 1)
            for (UUID user : users)
                return Bukkit.getOfflinePlayer(user).getName();
        return color.name().toLowerCase(Locale.ENGLISH);
    }
}