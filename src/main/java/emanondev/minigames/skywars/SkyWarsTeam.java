package emanondev.minigames.skywars;

import emanondev.minigames.generic.ColoredTeam;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SkyWarsTeam extends ColoredTeam {

    private final SkyWarsGame game;

    public SkyWarsTeam(@NotNull SkyWarsGame game, @NotNull DyeColor color) {
        super(game,color);
        this.game = game;
    }

    @Override
    public boolean addUser(@NotNull UUID user) {
        if (game.getOption().getTeamMaxSize() <= this.getUsersAmount())
            return false;
        return super.addUser(user);
    }

    @Override
    public void clear() {
        super.clear();
    }

    public boolean hasLost() {
        for (UUID user : this.getUsers()) {
            Player p = Bukkit.getPlayer(user);
            if (p != null && game.isGamer(p))
                return false;
        }
        return true;
    }
}