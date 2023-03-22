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

public class MountedRaceGame extends ARaceGame<ARaceTeam<MountedRaceGame>, MountedRaceOption> {

    public MountedRaceGame(@NotNull Map<String, Object> map) {
        super(map);
    }


    @Override
    public @NotNull MountedRaceType getMinigameType() {
        return MinigameTypes.MOUNTED_RACE;
    }

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
        MessageUtil.debug(getId() + " vehiclemove " + player.getName());
        onGamerMoveInsideArena(new PlayerMoveEvent(player, event.getFrom(), event.getTo()));
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
        if (getGamers().size() <= 1)
            this.gameEnd();
    }


    public boolean canAddGamer(@NotNull Player player) {
        return getPhase() != Phase.PLAYING && super.canAddGamer(player);
    }

    public void gamePlayingTimer() {
        super.gamePlayingTimer();
        //TODO test?
        //for (Player gamer:getGamers())
        //    onGamerMoveInsideArena(new PlayerMoveEvent(gamer,gamer.getLocation(),gamer.getLocation()));
    }


    @Override //TODO
    public boolean gameCanPreStart() {
        return getGamers().size() >= 1;
    } //TODO autostart se solo

    @Override //TODO
    public boolean gameCanStart() {
        int counter = 0;
        for (ARaceTeam<MountedRaceGame> team : getTeams())
            if (getGamers(team).size() > 0)
                counter++;
        return counter >= 1; //TODO autostart se solo
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


    public void gameStart() {
        super.gameStart();
        for (Player player : getGamers()) {
            PlayerStat.MOUNTEDRACE_PLAYED.add(player, 1);
            PlayerStat.GAME_PLAYED.add(player, 1);
        }
        GameStat.PLAY_TIMES.add(this, 1);
        getTeams().forEach(team -> {
            if (!team.hasLost()) setScore(team.getName(), 0);
        });
    }


    public void onGamerMountEvent(EntityMountEvent event, Player player) {
        //event.setCancelled(true);
    }

    public void onGamerDismountEvent(EntityDismountEvent event, Player player) {
        MessageUtil.debug(getId() + " dismounted " + player.getName());
        if (getPhase() == Phase.PLAYING || getPhase() == Phase.PRE_START) {
            Bukkit.getScheduler().runTaskLater(getMinigameType().getPlugin(), () -> {
                if (event.getDismounted().isValid())
                    event.getDismounted().remove();
                if (player.getVehicle() == null)
                    teleportResetLocation(player);
            }, 1L);
            //event.setCancelled(true);
            //event.getDismounted().remove(); //buggoso????
        }
    }


    public void onQuitGame(@NotNull Player player) {
        boolean gamer = false;
        if (isGamer(player))
            gamer = true;
        super.onQuitGame(player);
        if (gamer && player.getVehicle() != null) {
            Entity vehicle = player.getVehicle();
            vehicle.removePassenger(player);
            vehicle.remove();
        }
    }

}
