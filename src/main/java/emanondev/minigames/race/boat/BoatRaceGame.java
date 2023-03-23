package emanondev.minigames.race.boat;

import emanondev.minigames.MinigameTypes;
import emanondev.minigames.data.PlayerStat;
import emanondev.minigames.race.ARaceTeam;
import emanondev.minigames.race.ARaceType;
import emanondev.minigames.race.MountedRaceGame;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@SerializableAs(value="BoatRaceGame")
public class BoatRaceGame extends MountedRaceGame<ARaceTeam<BoatRaceGame>, BoatRaceOption> {

    public BoatRaceGame(@NotNull Map<String, Object> map) {
        super(map);
    }


    @Override
    public PlayerStat getPlayedStat() {
        return PlayerStat.BOATRACE_PLAYED;
    }

    @Override
    public PlayerStat getVictoryStat() {
        return PlayerStat.BOATRACE_VICTORY;
    }

    @Override
    public @NotNull ARaceType<BoatRaceOption> getMinigameType() {
        return MinigameTypes.BOAT_RACE;
    }

}
