package emanondev.minigames.games.deathmatch;

import emanondev.minigames.games.ColoredTeam;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DeathMatchTeam extends ColoredTeam {


    public DeathMatchTeam(@NotNull DeathMatchGame game, @NotNull DyeColor color) {
        super(game, color);
    }

    public boolean hasLost() {
        for (UUID user : this.getUsers()) {
            Player p = Bukkit.getPlayer(user);
            if (p != null && getGame().isGamer(p))
                return false;
        }
        return true;
    }    @Override
    public boolean addUser(@NotNull UUID user) {
        if (getGame().getOption().getTeamMaxSize() <= this.getUsersAmount())
            return false;
        return super.addUser(user);
    }



    @Override
    public DeathMatchGame getGame() {
        return (DeathMatchGame) super.getGame();
    }
}