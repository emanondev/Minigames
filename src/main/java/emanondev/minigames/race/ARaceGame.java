package emanondev.minigames.race;

import emanondev.minigames.MessageUtil;
import emanondev.minigames.generic.AbstractMColorSchemGame;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class ARaceGame<T extends ARaceTeam, O extends ARaceOption> extends AbstractMColorSchemGame<T, RaceArena, O> {

    private final HashMap<UUID, Integer> currentCheckpoint = new HashMap<>();
    private final List<BoundingBox> checkpointsAreas = new ArrayList<>();
    private final List<BoundingBox> falloutAreas = new ArrayList<>();
    private BoundingBox finishArea;


    public int getCurrentCheckpoint(OfflinePlayer player) {
        return getCurrentCheckpoint(player.getUniqueId());
    }


    public void gameInitialize() {
        super.gameInitialize();
        checkpointsAreas.clear();
        falloutAreas.clear();
        checkpointsAreas.addAll(getArena().getCheckpoints());
        falloutAreas.addAll(getArena().getFallAreas());
        Location offset = getGameLocation().toLocation();
        for (BoundingBox box : checkpointsAreas)
            box.shift(offset);
        for (BoundingBox box : falloutAreas)
            box.shift(offset);
        finishArea = getArena().getFinishArea().shift(offset);
    }

    public void gameRestart() {
        super.gameRestart();
        currentCheckpoint.clear();
    }


    /**
     * @return -1 for no checkpoints or checkpoint number starting from 0
     */
    public int getCurrentCheckpoint(UUID player) {
        return currentCheckpoint.getOrDefault(player, -1);
    }

    public ARaceGame(@NotNull Map<String, Object> map) {
        super(map);
    }

    @Override
    public void onGamerInventoryOpen(@NotNull InventoryOpenEvent event, @NotNull Player player) {
    }

    @Override
    public void onGamerInventoryClose(@NotNull InventoryCloseEvent event, @NotNull Player player) {
    }

    @Override
    public void onGamerCombustEvent(@NotNull EntityCombustEvent event, @NotNull Player player) {
    }

    @Override
    public void onGamerHitByProjectile(@NotNull ProjectileHitEvent event) {
    }

    @Override
    public void onGamerRegainHealth(@NotNull EntityRegainHealthEvent event, @NotNull Player player) {
    }


    @Override
    public void onGameEntityDamaged(@NotNull EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (!getOption().getAllowFallDamage())
                event.setCancelled(true);
        } else if (!getOption().getAllowEnvironmentDamage())
            event.setCancelled(true);
    }

    @Override
    public void onGamerDamaging(@NotNull EntityDamageByEntityEvent event, @NotNull Player damager, boolean direct) {
        if (event.getEntity() instanceof Player && !getOption().getAllowPvp()) {
            event.setCancelled(true);
            return;
        }
        if (!(event.getEntity() instanceof Player) && !getOption().getAllowPve()) {
            event.setCancelled(true);
        }
    }

    public void onGamerDamaged(@NotNull EntityDamageEvent event, @NotNull Player hitPlayer) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (!getOption().getAllowFallDamage())
                event.setCancelled(true);
        } else if (!getOption().getAllowEnvironmentDamage())
            event.setCancelled(true);
        super.onGamerDamaged(event, hitPlayer);
    }

    @Override
    public void onCreatureSpawn(@NotNull CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL)
            event.setCancelled(true);
    }

    @Override
    public void onEntityRegainHealth(@NotNull EntityRegainHealthEvent event) {//ok
    }

    @Override
    public void onFakeGamerDeath(@NotNull Player dead, @Nullable Player killer, boolean direct) {
        //TODO inc death counter
        teleportResetLocation(dead);
        //TODO notify
    }

    @Override
    protected void onGamerFallOutsideArena(@NotNull PlayerMoveEvent event) {
        switch (getPhase()) {
            case PLAYING -> {
                Player damager = null;
                boolean direct = false;
                if (event.getPlayer().getLastDamageCause() instanceof EntityDamageByEntityEvent evt) {
                    if (evt.getDamager() instanceof Player) {
                        direct = true;
                        damager = (Player) evt.getDamager();
                    } else if (evt.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter)
                        damager = shooter;
                    else if (evt.getDamager() instanceof TNTPrimed tnt && tnt.getSource() instanceof Player terrorist)
                        damager = terrorist;
                }
                onFakeGamerDeath(event.getPlayer(), damager, direct);
                if (isSpectator(event.getPlayer()))
                    teleportResetLocation(event.getPlayer());
            }
            case PRE_START, COLLECTING_PLAYERS, END -> teleportResetLocation(event.getPlayer());
        }
    }

    @Override
    public int getMaxGamers() {
        return getArena().getColors().size() * getOption().getTeamMaxSize();
    }


    public void onGamerMoveInsideArena(@NotNull PlayerMoveEvent event) {
        MessageUtil.debug(getId() + " moveinsidearena " + event.getPlayer().getName());
        super.onGamerMoveInsideArena(event);
        if (isSpectator(event.getPlayer()))
            return;
        if (getPhase() != Phase.PLAYING && getPhase() != Phase.END)
            return;

        BoundingBox box = event.getPlayer().getBoundingBox();
        if (getPhase() == Phase.PLAYING) {
            if (finishArea.overlaps(box)) { //TODO CHECKPOINTS POLICY
                currentCheckpoint.remove(event.getPlayer().getUniqueId());
                onGamerReachRaceFinishArea(event.getPlayer());
                return;
            }
        }
        for (int i = getCurrentCheckpoint(event.getPlayer()) + 1; i < checkpointsAreas.size(); i++) //TODO CHECKPOINTS POLICY
            if (checkpointsAreas.get(i).overlaps(box)) {

                getMinigameType().REACHED_CHECKPOINT.send(event.getPlayer(), "%checkpoint%", String.valueOf(i + 1));
                currentCheckpoint.put(event.getPlayer().getUniqueId(), i);
                setScore(getTeam(event.getPlayer()).getName(), i + 1);
                break;
            }
        for (BoundingBox fall : falloutAreas)
            if (fall.overlaps(box)) {
                //TODO notify
                onGamerFallOutsideArena(event);
                return;
            }
    }

    public abstract @NotNull ARaceType<O> getMinigameType();

    protected abstract void onGamerReachRaceFinishArea(Player player);

    /**
     * Handle block break done by one of playingPlayers
     */
    @Override
    public void onGamerBlockBreak(@NotNull BlockBreakEvent event) {
        //if (getPhase() != Phase.PLAYING && getPhase() != Phase.END)
        event.setCancelled(true);
    }

    /**
     * Handle block place done by one of playingPlayers
     */
    @Override
    public void onGamerBlockPlace(@NotNull BlockPlaceEvent event) {
        //if (getPhase() != Phase.PLAYING && getPhase() != Phase.END)
        event.setCancelled(true);
    }
    //TODO blockbucket


    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected @NotNull T craftTeam(@NotNull DyeColor color) {
        return (T) new ARaceTeam(this, color);
    }

    @Override
    public boolean joinGameAsGamer(@NotNull Player player) {
        return addGamer(player);
    }

    public void teleportResetLocation(@NotNull Player player) {
        int checkpoint = getCurrentCheckpoint(player);
        if (checkpoint == -1)
            super.teleportResetLocation(player);
        else
            player.teleport(getArena().getCheckpointsRespawn().get(checkpoint).add(getGameLocation()));
    }


    public void onQuitGame(@NotNull Player player) {
        super.onQuitGame(player);
        if (getGamers().size() == 0 && getPhase() == Phase.PLAYING) {
            this.gameEnd();
        }
    }
}
