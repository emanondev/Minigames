package emanondev.minigames.race;

import org.bukkit.DyeColor;
import org.jetbrains.annotations.NotNull;

public class MountedRaceTeam extends ARaceTeam {

    public MountedRaceTeam(@NotNull MountedRaceGame game, @NotNull DyeColor color) {
        super(game, color);
    }

    @Override
    public MountedRaceGame getGame() {
        return (MountedRaceGame) super.getGame();
    }
}
