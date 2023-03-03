package emanondev.minigames.race;

import org.bukkit.DyeColor;
import org.jetbrains.annotations.NotNull;

public class TimeMountedRaceTeam extends ARaceTeam {

    public TimeMountedRaceTeam(@NotNull TimeMountedRaceGame game, @NotNull DyeColor color) {
        super(game, color);
    }

    @Override
    public TimeMountedRaceGame getGame() {
        return (TimeMountedRaceGame) super.getGame();
    }
}
