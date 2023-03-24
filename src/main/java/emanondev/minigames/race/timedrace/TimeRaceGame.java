package emanondev.minigames.race.timedrace;

import emanondev.minigames.MinigameTypes;
import emanondev.minigames.data.PlayerStat;
import emanondev.minigames.race.ARaceGame;
import emanondev.minigames.race.ARaceTeam;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class TimeRaceGame extends ARaceGame<ARaceTeam<TimeRaceGame>, TimeRaceOption> {
    public TimeRaceGame(Map<String, Object> map) {
        super(map);
    }

    //TODO
    @Override
    protected void onGamerReachRaceFinishArea(Player player) {

    }

    @Override
    protected void assignTeam(Player p) {

    }

    @Override
    public void checkGameEnd() {
    }

    @Override
    public boolean gameCanPreStart() {
        return true;
    }

    @Override
    public boolean gameCanStart() {
        return true;
    }

    @Override
    public void onEntityDeath(@NotNull EntityDeathEvent event) {

    }
}
