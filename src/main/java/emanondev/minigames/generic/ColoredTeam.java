package emanondev.minigames.generic;


import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class ColoredTeam implements MTeam {

    private final HashSet<UUID> users = new HashSet<>();
    private final DyeColor color;
    private final AbstractMGame game;
    private final Team team;
    private final Objective objective;
    private final ChatColor chatColor;
    private Score score;

    public ColoredTeam(@NotNull AbstractMGame game, @NotNull DyeColor color) {
        this.color = color;
        this.chatColor = ChatColor.of(new java.awt.Color(color.getColor().getRed(), color.getColor().getGreen(), color.getColor()
                .getBlue()));
        this.game = game;
        this.team = game.getScoreboard().registerNewTeam(game.getId() + "_" + color.name().toLowerCase());
        team.setPrefix(chatColor.toString());
        this.objective = game.getObjective();
        //this.score = objective.getScore(getSingleChatColor()  + this.color.name().toLowerCase());
        //score.setScore(0);
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public org.bukkit.ChatColor getSingleChatColor() {
        return switch (color) {
            case RED -> org.bukkit.ChatColor.RED;
            case ORANGE, BROWN -> org.bukkit.ChatColor.GOLD;
            case BLUE -> org.bukkit.ChatColor.BLUE;
            case BLACK -> org.bukkit.ChatColor.BLACK;
            case GREEN -> org.bukkit.ChatColor.DARK_GREEN;
            case LIME -> org.bukkit.ChatColor.GREEN;
            case YELLOW -> org.bukkit.ChatColor.YELLOW;
            case GRAY -> org.bukkit.ChatColor.DARK_GRAY;
            case LIGHT_GRAY -> org.bukkit.ChatColor.GRAY;
            case WHITE -> org.bukkit.ChatColor.WHITE;
            case PURPLE -> org.bukkit.ChatColor.DARK_PURPLE;
            case MAGENTA, PINK -> org.bukkit.ChatColor.LIGHT_PURPLE;
            case CYAN -> org.bukkit.ChatColor.DARK_AQUA;
            case LIGHT_BLUE -> org.bukkit.ChatColor.AQUA;
        };
    }

    private String lastScoreName = null;

    private String getScoreName() {
        if (getGame().getMaxGamers() == 1)
            return getUsersAmount() == 1 ? getSingleChatColor() + Bukkit.getOfflinePlayer(getUsers().iterator().next()).getName()
                    : (getSingleChatColor() + this.color.name().toLowerCase());
        return getSingleChatColor() + this.color.name().toLowerCase();
    }

    @Override
    public void setScore(int value) {
        String scoreName = getScoreName();
        if (lastScoreName != null && !lastScoreName.equals(scoreName)) {
            objective.getScoreboard().resetScores(lastScoreName);
            lastScoreName = scoreName;
        } else if (lastScoreName == null)
            lastScoreName = scoreName;
        objective.getScore(scoreName).setScore(value);
    }

    @Override
    public void addScore(int val) {
        setScore(getScore()+val);
    }

    @Override
    public int getScore(){
        return lastScoreName==null?0:objective.getScore(lastScoreName).getScore();
    }

    @Override
    public void clearScore() {
        if (lastScoreName != null) {
            objective.getScoreboard().resetScores(lastScoreName);
            lastScoreName = null;
        }
    }

    @Override
    public AbstractMGame getGame() {
        return game;
    }

    @Override
    public Team getScoreboardTeam() {
        return team;
    }

    @Override
    public boolean removeUser(@NotNull OfflinePlayer user) {
        if (getGame().getMaxGamers() == 1) {
            String score = getScoreName();
            if (removeUser(user.getUniqueId())) {
                return true;
            }
            return false;
        }
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
        this.clearScore();
    }


    @Override
    public String getName() {
        return color.name().toLowerCase();
    }
}