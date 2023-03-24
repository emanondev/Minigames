package emanondev.minigames.race.running;

import emanondev.core.message.DMessage;
import emanondev.minigames.MessageUtil;
import emanondev.minigames.MinigameTypes;
import emanondev.minigames.Minigames;
import emanondev.minigames.data.GameStat;
import emanondev.minigames.data.PlayerStat;
import emanondev.minigames.generic.ColoredTeam;
import emanondev.minigames.race.ARaceGame;
import emanondev.minigames.race.ARaceTeam;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@SerializableAs(value="RunRaceGame")
public class RunRaceGame extends ARaceGame<ARaceTeam<RunRaceGame>, RunRaceOption> {

    public RunRaceGame(@NotNull Map<String, Object> map) {
        super(map);
    }


    @Override
    public @NotNull RunRaceType getMinigameType() {
        return MinigameTypes.RUN_RACE;
    }

    @Override
    public void onEntityDeath(@NotNull EntityDeathEvent event) {
    }

    @Override
    public @NotNull PlayerStat getPlayedStat() {
        return PlayerStat.RUNRACE_PLAYED;
    }

    @Override
    public @NotNull PlayerStat getVictoryStat() {
        return PlayerStat.RUNRACE_VICTORY;
    }

    @Deprecated
    public void assignTeam(@NotNull Player player) {//TODO choose how to fill with options
        if (getTeam(player) != null)
            return;
        MessageUtil.debug(getId() + " assigning team to " + player.getName());
        List<ARaceTeam> teams = new ArrayList<>(getTeams());
        teams.sort(Comparator.comparingInt(ColoredTeam::getUsersAmount));
        for (ARaceTeam team : teams)
            if (team.addUser(player)) {
                new DMessage(Minigames.get(), player).appendLang(getMinigameType().getType() + ".game.assign_team",
                        "%color%", team.getColor().name());
                return;
            }
        throw new IllegalStateException("unable to add user to a party");
    }


    @Override
    public void checkGameEnd() {
        if (getGamers().size() <= 1)
            this.gameEnd();
    }

    public boolean canAddGamer(@NotNull Player player) {
        return getPhase() != Phase.PLAYING && super.canAddGamer(player);
    }
}
