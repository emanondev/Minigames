package emanondev.minigames.race.horse;

import emanondev.minigames.MinigameTypes;
import emanondev.minigames.data.PlayerStat;
import emanondev.minigames.race.ARaceTeam;
import emanondev.minigames.race.MountedRaceGame;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@SerializableAs(value="HorseRaceGame")
public class HorseRaceGame extends MountedRaceGame<ARaceTeam<HorseRaceGame>, HorseRaceOption> {

    public HorseRaceGame(@NotNull Map<String, Object> map) {
        super(map);
    }

    @Override
    public PlayerStat getPlayedStat() {
        return PlayerStat.HORSERACE_PLAYED;
    }

    @Override
    public PlayerStat getVictoryStat() {
        return PlayerStat.HORSERACE_VICTORY;
    }

    @Override
    public @NotNull HorseRaceType getMinigameType(){
        return MinigameTypes.HORSE_RACE;
    }



}
