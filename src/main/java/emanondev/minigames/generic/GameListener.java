package emanondev.minigames.generic;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.jetbrains.annotations.NotNull;

public class GameListener implements Listener {

    private final @SuppressWarnings("rawtypes")
    MGame game;

    public GameListener(@SuppressWarnings("rawtypes") MGame game) {
        this.game = game;
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

    @EventHandler
    private void event(@NotNull CreatureSpawnEvent event) {
        if (!game.containsLocation(event.getLocation()))
            return;
        game.onCreatureSpawn(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void event(EntityDamageEvent event) {
        if (!game.containsLocation(event.getEntity()))
            return;
        Player p = event.getEntity() instanceof Player ? (Player) event.getEntity() : null;
        if (p != null && game.isSpectator(p)) {
            event.setCancelled(true);
            return;
        }
        boolean targetIsPlayingPlayer = p != null && game.isGamer(p);
        if (targetIsPlayingPlayer)
            game.onGamerDamaged(event, p);
        else
            game.onGameEntityDamaged(event);

        if (event.isCancelled())
            return;
        Player damager = null;
        boolean direct = false;
        if (event instanceof EntityDamageByEntityEvent evt) {
            if (evt.getDamager() instanceof Player && game.isSpectator((Player) evt.getDamager())) {
                event.setCancelled(true);
                return;
            }
            if (evt.getDamager() instanceof Player) {
                damager = (Player) evt.getDamager();
                direct = true;
            } else if (evt.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter)
                damager = shooter;
            else if (evt.getDamager() instanceof TNTPrimed tnt && tnt.getSource() instanceof Player terrorist)
                damager = terrorist;
        }
        if (damager != null) {
            if (targetIsPlayingPlayer)
                game.onGamerPvpDamage((EntityDamageByEntityEvent) event, p, damager, direct);
            else
                game.onGamerDamaging((EntityDamageByEntityEvent) event, damager, direct);
        }

        if (event.isCancelled())
            return;

        if (targetIsPlayingPlayer && p.getHealth() <= event.getFinalDamage()) {
            if (damager == null && p.getLastDamageCause() instanceof EntityDamageByEntityEvent evt) {
                if (evt.getDamager() instanceof Player) {
                    damager = (Player) evt.getDamager();
                    direct = true;
                } else if (evt.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter)
                    damager = shooter;
                else if (evt.getDamager() instanceof TNTPrimed tnt && tnt.getSource() instanceof Player terrorist)
                    damager = terrorist;
            }
            event.setCancelled(true);
            game.onFakeGamerDeath(p, damager, direct);
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
            if (event.getEntity() instanceof Player p && game.isGamer(p))
                game.onGamerRegainHealth(event, p);
            else
                game.onEntityRegainHealth(event);
    }

    @EventHandler
    private void event(BlockFromToEvent event) {
        if (game.containsLocation(event.getBlock()) != game.containsLocation(event.getToBlock()))
            event.setCancelled(true);
    }


    @EventHandler
    private void event(EntitiesLoadEvent event) {
        if (game.overlaps(event.getChunk()))
            game.onChunkEntitiesLoad(event.getChunk());
    }

}
