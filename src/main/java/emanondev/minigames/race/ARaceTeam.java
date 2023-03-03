package emanondev.minigames.race;

import emanondev.minigames.generic.ColoredTeam;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ARaceTeam<T extends ARaceGame> extends ColoredTeam {

    public ARaceTeam(@NotNull T game, @NotNull DyeColor color) {
        super(game, color);
    }

    @Override
    public boolean addUser(@NotNull UUID user) {
        if (getGame().getOption().getTeamMaxSize() <= this.getUsersAmount())
            return false;
        return super.addUser(user);
    }

    public boolean hasLost() {
        for (UUID user : this.getUsers()) {
            Player p = Bukkit.getPlayer(user);
            if (p != null && getGame().isGamer(p))
                return false;
        }
        return true;
    }

    @Override
    public T getGame() {
        return (T) super.getGame();
    }

}