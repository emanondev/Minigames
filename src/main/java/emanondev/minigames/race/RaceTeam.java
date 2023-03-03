package emanondev.minigames.race;

import org.bukkit.DyeColor;
import org.jetbrains.annotations.NotNull;

public class RaceTeam extends ARaceTeam {

    public RaceTeam(@NotNull RaceGame game, @NotNull DyeColor color) {
        super(game, color);
    }

    @Override
    public RaceGame getGame() {
        return (RaceGame) super.getGame();
    }
}
