package emanondev.minigames.games.race.running;

import emanondev.core.message.DMessage;
import emanondev.minigames.MessageUtil;
import emanondev.minigames.MinigameTypes;
import emanondev.minigames.Minigames;
import emanondev.minigames.data.PlayerStat;
import emanondev.minigames.event.runrace.RunRaceStartEvent;
import emanondev.minigames.event.runrace.RunRaceWinFirstEvent;
import emanondev.minigames.event.runrace.RunRaceWinSecondEvent;
import emanondev.minigames.event.runrace.RunRaceWinThirdEvent;
import emanondev.minigames.games.race.ARaceGame;
import emanondev.minigames.games.race.ARaceTeam;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

@SerializableAs(value = "RunRaceGame")
public class RunRaceGame extends ARaceGame<ARaceTeam<RunRaceGame>, RunRaceOption> {

    public RunRaceGame(@NotNull Map<String, Object> map) {
        super(map);
    }

    @Override
    protected void craftAndCallGameStartEvent() {
        Bukkit.getPluginManager().callEvent(new RunRaceStartEvent(this));
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
        for (@SuppressWarnings("rawtypes") ARaceTeam team : getTeams())
            if (team.getUsersAmount() < getOption().getTeamMaxSize() && team.addUser(player)) {
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

    @Override
    protected void craftAndCallWinFirstEvent(@NotNull ARaceTeam<RunRaceGame> team, @NotNull Player lineCutter, @NotNull Set<Player> winners) {
        Bukkit.getPluginManager().callEvent(new RunRaceWinFirstEvent(team, lineCutter, winners));
    }

    @Override
    protected void craftAndCallWinSecondEvent(@NotNull ARaceTeam<RunRaceGame> team, @NotNull Player lineCutter, @NotNull Set<Player> winners) {
        Bukkit.getPluginManager().callEvent(new RunRaceWinSecondEvent(team, lineCutter, winners));
    }

    @Override
    protected void craftAndCallWinThirdEvent(@NotNull ARaceTeam<RunRaceGame> team, @NotNull Player lineCutter, @NotNull Set<Player> winners) {
        Bukkit.getPluginManager().callEvent(new RunRaceWinThirdEvent(team, lineCutter, winners));
    }
}
