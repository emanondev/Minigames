package emanondev.minigames.race;

import emanondev.core.message.DMessage;
import emanondev.minigames.MessageUtil;
import emanondev.minigames.MinigameTypes;
import emanondev.minigames.Minigames;
import emanondev.minigames.generic.ColoredTeam;
import emanondev.minigames.skywars.SkyWarsTeam;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RaceGame extends ARaceGame<ARaceTeam<RaceGame>,RaceOption>{

    public RaceGame(@NotNull Map<String, Object> map) {
        super(map);
    }


    @Override
    public @NotNull RaceType getMinigameType() {
        return MinigameTypes.RACE;
    }

    @Override
    public void onEntityDeath(@NotNull EntityDeathEvent event) {
    }

    //###
    @Override
    protected void onGamerReachRaceFinishArea(Player player) {
        //TODO won notify && celebrate

        gameEnd();
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
        if (getGamers().size()<=1)
            this.gameEnd();
    }

    @Override //TODO
    public boolean gameCanPreStart() {
        return getGamers().size() >= 1;
    }

    @Override //TODO
    public boolean gameCanStart() {
        int counter = 0;
        for (ARaceTeam<RaceGame> team : getTeams())
            if (getGamers(team).size() > 0)
                counter++;
        return counter >= 1;
    }

    /*
    @Override
    public boolean gameCanPreStart() {
        return getGamers().size() >= 2;
    }

    @Override
    public boolean gameCanStart() {
        int counter = 0;
        for (ARaceTeam<RaceGame> team : getTeams())
            if (getGamers(team).size() > 0)
                counter++;
        return counter >= 2;
    }*/
}