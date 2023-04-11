package emanondev.minigames.race.boat;

import emanondev.minigames.MinigameTypes;
import emanondev.minigames.data.PlayerStat;
import emanondev.minigames.event.boatrace.BoatRaceStartEvent;
import emanondev.minigames.event.boatrace.BoatRaceWinFirstEvent;
import emanondev.minigames.event.boatrace.BoatRaceWinSecondEvent;
import emanondev.minigames.event.boatrace.BoatRaceWinThirdEvent;
import emanondev.minigames.race.ARaceTeam;
import emanondev.minigames.race.ARaceType;
import emanondev.minigames.race.MountedRaceGame;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

@SerializableAs(value = "BoatRaceGame")
public class BoatRaceGame extends MountedRaceGame<ARaceTeam<BoatRaceGame>, BoatRaceOption> {

    public BoatRaceGame(@NotNull Map<String, Object> map) {
        super(map);
    }

    @Override
    protected void craftAndCallGameStartEvent() {
        Bukkit.getPluginManager().callEvent(new BoatRaceStartEvent(this));
    }

    @Override
    public @NotNull PlayerStat getPlayedStat() {
        return PlayerStat.BOATRACE_PLAYED;
    }

    @Override
    public @NotNull PlayerStat getVictoryStat() {
        return PlayerStat.BOATRACE_VICTORY;
    }

    @Override
    public @NotNull ARaceType<BoatRaceOption> getMinigameType() {
        return MinigameTypes.BOAT_RACE;
    }


    @Override
    protected void craftAndCallWinFirstEvent(@NotNull ARaceTeam<BoatRaceGame> team, @NotNull Player lineCutter, @NotNull Set<Player> winners) {
        Bukkit.getPluginManager().callEvent(new BoatRaceWinFirstEvent(team, lineCutter, winners));
    }

    @Override
    protected void craftAndCallWinSecondEvent(@NotNull ARaceTeam<BoatRaceGame> team, @NotNull Player lineCutter, @NotNull Set<Player> winners) {
        Bukkit.getPluginManager().callEvent(new BoatRaceWinSecondEvent(team, lineCutter, winners));
    }

    @Override
    protected void craftAndCallWinThirdEvent(@NotNull ARaceTeam<BoatRaceGame> team, @NotNull Player lineCutter, @NotNull Set<Player> winners) {
        Bukkit.getPluginManager().callEvent(new BoatRaceWinThirdEvent(team, lineCutter, winners));
    }

}
