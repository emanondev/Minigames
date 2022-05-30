package emanondev.minigames.generic;

import emanondev.core.UtilsMessages;
import emanondev.minigames.Minigames;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.world.PortalCreateEvent;
import org.jetbrains.annotations.NotNull;

public class GameListener implements Listener {

    private final MGame game;

    public GameListener(MGame game) {
        this.game = game;
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void event(@NotNull BlockPlaceEvent event) {
        if (!game.isPlayingPlayer(event.getPlayer()))
            return;
        if (game.containsLocation(event.getBlock())) {
            game.onPlayingPlayerBlockPlace(event);
            return;
        }
        event.setCancelled(true); //outside game bounds
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void event(@NotNull BlockBreakEvent event) {
        if (!game.isPlayingPlayer(event.getPlayer()))
            return;
        if (game.containsLocation(event.getBlock())) {
            game.onPlayingPlayerBlockBreak(event);
            return;
        }
        event.setCancelled(true); //outside game bounds
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void event(@NotNull PortalCreateEvent event) {
        if (event.getEntity() != null) {
            if (game.containsLocation(event.getEntity())) {
                game.onPortalCreate(event);
                return;
            }
        }
        for (BlockState block : event.getBlocks())
            if (game.containsLocation(block)) {
                game.onPortalCreate(event);
                return;
            }
    }

    @EventHandler(ignoreCancelled = true)
    protected void event(@NotNull EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player p && game.isSpectator(p))
            event.setCancelled(true);
    }

    @EventHandler
    private void event(@NotNull CreatureSpawnEvent event) {
        if (!game.containsLocation(event.getLocation()))
            return;
        game.onCreatureSpawn(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void event(EntityDamageEvent event) {
        if (!game.containsLocation(event.getEntity()))
            return;
        Player p = event.getEntity() instanceof Player ? (Player) event.getEntity() : null;
        if (p != null && game.isSpectator(p)) {
            event.setCancelled(true);
            return;
        }
        boolean targetIsPlayingPlayer = p != null && game.isPlayingPlayer(p);
        if (targetIsPlayingPlayer)
            game.onPlayingPlayerDamaged(event, p);
        else
            game.onGameEntityDamaged(event);

        if (event.isCancelled())
            return;
        Player damager = null;
        if (event instanceof EntityDamageByEntityEvent evt) {
            if (evt.getDamager() instanceof Player)
                damager = (Player) evt.getDamager();
            else if (evt.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter)
                damager = shooter;
            else if (evt.getDamager() instanceof TNTPrimed tnt && tnt.getSource() instanceof Player terrorist)
                damager = terrorist;
        }
        if (damager != null) {
            if (targetIsPlayingPlayer)
                game.onPlayingPlayerPvpDamage((EntityDamageByEntityEvent) event, p, damager);
            else
                game.onPlayingPlayerDamaging((EntityDamageByEntityEvent) event, damager);
        }

        if (event.isCancelled())
            return;

        if (targetIsPlayingPlayer && p.getHealth() <= event.getFinalDamage()) {
            if (damager == null && p.getLastDamageCause() instanceof EntityDamageByEntityEvent evt) {
                if (evt.getDamager() instanceof Player)
                    damager = (Player) evt.getDamager();
                else if (evt.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter)
                    damager = shooter;
                else if (evt.getDamager() instanceof TNTPrimed tnt && tnt.getSource() instanceof Player terrorist)
                    damager = terrorist;
            }
            event.setCancelled(true);
            game.onFakePlayingPlayerDeath(p, damager);
        }

    }


    @EventHandler
    private void event(EntityDeathEvent event) {
        if (game.containsLocation(event.getEntity()))
            game.onEntityDeath(event);
    }

    @EventHandler
    private void event(EntityRegainHealthEvent event) {
        if (game.containsLocation(event.getEntity()))
            if (event.getEntity() instanceof Player p && game.isPlayingPlayer(p))
                game.onPlayingPlayerRegainHealth(event, p);
            else
                game.onEntityRegainHealth(event);
    }

    @EventHandler
    private void event(PlayerInteractEvent event) {
        if (game.containsLocation(event.getPlayer()))
            if (!game.isPlayingPlayer(event.getPlayer()))
                event.setCancelled(true);
            else
                game.onPlayingPlayerInteract(event);
    }

    @EventHandler
    private void event(PlayerInteractEntityEvent event) {
        if (game.containsLocation(event.getRightClicked()))
            if (!game.isPlayingPlayer(event.getPlayer()))
                event.setCancelled(true);
            else
                game.onPlayingPlayerInteractEntity(event);
    }

    @EventHandler
    private void event(BlockFromToEvent event) {
        if (game.containsLocation(event.getBlock()) != game.containsLocation(event.getToBlock()))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void event(PlayerMoveEvent event) { //!=teleportevent
        if (game.isSpectator(event.getPlayer())) {
            if (event.getTo() == null) {
                new IllegalStateException("moved to null").printStackTrace();
                return;
            }
            if (event.getTo().getWorld() != event.getFrom().getWorld()) { //TODO redoundant for teleport?
                game.removeSpectator(event.getPlayer());
                return; //teleported away by something ?
            }
            if (!game.containsLocation(event.getTo())) {
                UtilsMessages.sendActionbar(event.getPlayer(), Minigames.get().getLanguageConfig(event.getPlayer())
                        .loadMessage(game.getMinigameType().getType() + ".game.spectator_cannot_go_outside_arena_bar", ""));
                event.setCancelled(true);
            }
            return;
        }
        if (!game.isPlayingPlayer(event.getPlayer()))
            return;

        if (event.getTo() == null) {
            new IllegalStateException("moved to null").printStackTrace();
            return;
        }

        if (!game.containsLocation(event.getTo())) {
            Minigames.get().logTetraStar(ChatColor.DARK_RED, "D " + game.getId() + " move outside arena");
            game.onPlayingPlayerMoveOutsideArena(event);
            //event.setCancelled(true); //TODO should be fine
            return;
        }

        game.onPlayingPlayerMove(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void event(PlayerTeleportEvent event) {
        if (game.isSpectator(event.getPlayer())) {
            if (event.getTo() == null) {
                new IllegalStateException("moved to null").printStackTrace();
                return;
            }
            if (event.getTo().getWorld() != event.getFrom().getWorld()) {
                game.removeSpectator(event.getPlayer());
                return; //teleported away by something ?
            }
            if (!game.containsLocation(event.getTo())) {
                event.setCancelled(true);
            }
        }
        if (!game.isPlayingPlayer(event.getPlayer())) {
            return;
        }

        if (event.getTo() == null) {
            new IllegalStateException("moved to null").printStackTrace();
            return;
        }
        if (!game.containsLocation(event.getTo())) {
            game.onPlayingPlayerMoveOutsideArena(event);
            event.setCancelled(true); //TODO should be fine
        }
        game.onPlayingPlayerTeleport(event);
    }

    @EventHandler
    private void event(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Player p && game.isPlayingPlayer(p)) {
            game.onPlayingPlayerLaunchProjectile(event);
            return;
        }
    }

    @EventHandler
    private void event(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player p) {
            if (game.isPlayingPlayer(p)) {
                game.onPlayingPlayerPickupItem(event, p);
                return;
            }
            if (game.isSpectator(p))
                event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void event(PlayerDropItemEvent event) {
        if (game.isSpectator(event.getPlayer())) {
            event.setCancelled(true);
            return;
        }
        if (game.isPlayingPlayer(event.getPlayer()))
            game.onPlayingPlayerDropItem(event);
    }
}
