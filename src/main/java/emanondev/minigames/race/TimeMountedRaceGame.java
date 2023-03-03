package emanondev.minigames.race;

import emanondev.minigames.MinigameTypes;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TimeMountedRaceGame extends ARaceGame<ARaceTeam<TimeMountedRaceGame>, TimeMountedRaceOption> {
    public TimeMountedRaceGame(Map<String, Object> map) {
        super(map);
    }

    @Override
    protected void onGamerReachRaceFinishArea(Player player) {

    }

    @Override
    protected void assignTeam(Player p) {

    }

    @Override
    public @NotNull TimeMountedRaceType getMinigameType() {
        return MinigameTypes.TIME_MOUNTED_RACE;
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