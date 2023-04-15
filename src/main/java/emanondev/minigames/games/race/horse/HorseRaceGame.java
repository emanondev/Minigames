package emanondev.minigames.games.race.horse;

import emanondev.minigames.MinigameTypes;
import emanondev.minigames.data.PlayerStat;
import emanondev.minigames.event.horserace.HorseRaceStartEvent;
import emanondev.minigames.event.horserace.HorseRaceWinFirstEvent;
import emanondev.minigames.event.horserace.HorseRaceWinSecondEvent;
import emanondev.minigames.event.horserace.HorseRaceWinThirdEvent;
import emanondev.minigames.games.race.MountedRaceGame;
import emanondev.minigames.games.race.ARaceTeam;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

@SerializableAs(value = "HorseRaceGame")
public class HorseRaceGame extends MountedRaceGame<ARaceTeam<HorseRaceGame>, HorseRaceOption> {

    @Override
    protected void craftAndCallGameStartEvent() {
        Bukkit.getPluginManager().callEvent(new HorseRaceStartEvent(this));
    }

    public HorseRaceGame(@NotNull Map<String, Object> map) {
        super(map);
    }

    @Override
    public @NotNull PlayerStat getPlayedStat() {
        return PlayerStat.HORSERACE_PLAYED;
    }

    @Override
    public @NotNull PlayerStat getVictoryStat() {
        return PlayerStat.HORSERACE_VICTORY;
    }

    @Override
    public @NotNull HorseRaceType getMinigameType() {
        return MinigameTypes.HORSE_RACE;
    }

    @Override
    protected void craftAndCallWinFirstEvent(@NotNull ARaceTeam<HorseRaceGame> team, @NotNull Player lineCutter, @NotNull Set<Player> winners) {
        Bukkit.getPluginManager().callEvent(new HorseRaceWinFirstEvent(team, lineCutter, winners));
    }

    @Override
    protected void craftAndCallWinSecondEvent(@NotNull ARaceTeam<HorseRaceGame> team, @NotNull Player lineCutter, @NotNull Set<Player> winners) {
        Bukkit.getPluginManager().callEvent(new HorseRaceWinSecondEvent(team, lineCutter, winners));
    }

    @Override
    protected void craftAndCallWinThirdEvent(@NotNull ARaceTeam<HorseRaceGame> team, @NotNull Player lineCutter, @NotNull Set<Player> winners) {
        Bukkit.getPluginManager().callEvent(new HorseRaceWinThirdEvent(team, lineCutter, winners));
    }

}
