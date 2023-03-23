package emanondev.minigames.race;

import emanondev.core.message.DMessage;
import emanondev.minigames.MessageUtil;
import emanondev.minigames.MinigameTypes;
import emanondev.minigames.Minigames;
import emanondev.minigames.data.GameStat;
import emanondev.minigames.data.PlayerStat;
import emanondev.minigames.generic.ColoredTeam;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public abstract class MountedRaceGame<T extends ARaceTeam, O extends MountedRaceOption> extends ARaceGame<T ,O> {

    public MountedRaceGame(@NotNull Map<String, Object> map) {
        super(map);
        //this.played = played;
        //this.victory = victory;
    }

    public abstract PlayerStat getPlayedStat();
    public abstract PlayerStat getVictoryStat();

    @Override
    public void onEntityDeath(@NotNull EntityDeathEvent event) {
        //TODO check if is mounted
        for (Entity passenger : event.getEntity().getPassengers())
            if (passenger instanceof Player player && isGamer(player))
                this.onFakeGamerDeath(player, null, true);


    }

    public void teleportResetLocation(@NotNull Player player) {
        Entity vehicle = player.getVehicle();
        super.teleportResetLocation(player);
        if (isGamer(player) && (getPhase() == Phase.PRE_START || getPhase() == Phase.PLAYING)) {
            Location loc = player.getLocation();
            Bukkit.getScheduler().runTaskLater(getMinigameType().getPlugin(), () -> {
                getOption().spawnRide(loc).addPassenger(player);
                if (vehicle != null && vehicle.isValid())
                    vehicle.remove();
            }, 1L);
        }
    }

    public void onGamerVehicleMoveEvent(VehicleMoveEvent event, Player player) {
        onGamerMoveInsideArena(new PlayerMoveEvent(player, event.getFrom(), event.getTo()));
    }


    //###
    @Override
    protected void onGamerReachRaceFinishArea(Player player) {
        //TODO won notify && celebrate
        getVictoryStat().add(player,1);
        gameEnd();
    }

    @Deprecated
    public void assignTeam(@NotNull Player player) {//TODO choose how to fill with options
        if (getTeam(player) != null)
            return;
        MessageUtil.debug(getId() + " assigning team to " + player.getName());
        List<T> teams = new ArrayList<>(getTeams());
        teams.sort(Comparator.comparingInt(ColoredTeam::getUsersAmount));
        for (T team : teams)
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

    @Override //TODO
    public boolean gameCanPreStart() {
        return getGamers().size() >= 1;
    } //TODO autostart se solo

    @Override //TODO
    public boolean gameCanStart() {
        int counter = 0;
        for (T team : getTeams())
            if (getGamers(team).size() > 0)
                counter++;
        return counter >= 1; //TODO autostart se solo
    }


    public void gameStart() {
        super.gameStart();
        for (Player player : getGamers()) {
            getPlayedStat().add(player, 1);
            PlayerStat.GAME_PLAYED.add(player, 1);
        }
        GameStat.PLAY_TIMES.add(this, 1);
        getTeams().forEach(team -> {
            if (!team.hasLost()) setScore(team.getName(), 0);
        });
    }

    public void onGamerDismountEvent(EntityDismountEvent event, Player player) {
        if (getPhase() == Phase.PLAYING || getPhase() == Phase.PRE_START) {
            Bukkit.getScheduler().runTaskLater(getMinigameType().getPlugin(), () -> {
                if (event.getDismounted().isValid())
                    event.getDismounted().remove();
                if (player.getVehicle() == null)
                    teleportResetLocation(player);
            }, 1L);
        }
    }


    public void onQuitGame(@NotNull Player player) {
        boolean gamer = isGamer(player);
        super.onQuitGame(player);
        if (gamer && player.getVehicle() != null) {
            Entity vehicle = player.getVehicle();
            vehicle.removePassenger(player);
            vehicle.remove();
        }
    }

}
