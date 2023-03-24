package emanondev.minigames.race.timedrace;

import emanondev.minigames.race.ARaceTeam;
import org.bukkit.DyeColor;
import org.jetbrains.annotations.NotNull;

public class TimeRaceTeam extends ARaceTeam {

    public TimeRaceTeam(@NotNull TimeRaceGame game, @NotNull DyeColor color) {
        super(game, color);
    }

    @Override
    public TimeRaceGame getGame() {
        return (TimeRaceGame) super.getGame();
    }
}