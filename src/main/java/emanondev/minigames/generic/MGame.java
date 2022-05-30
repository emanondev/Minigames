package emanondev.minigames.generic;

import emanondev.minigames.locations.BlockLocation3D;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.world.PortalCreateEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public interface MGame<T extends MTeam, A extends MArena, O extends MOption> extends ConfigurationSerializable, Cloneable, Registrable {


    @NotNull BlockLocation3D getGameLocation();

    @NotNull Set<Player> getCollectedPlayers();

    boolean isCollectedPlayer(@NotNull Player player);

    boolean addCollectedPlayer(@NotNull Player player);

    boolean canAddCollectedPlayer(@NotNull Player player);

    boolean removeCollectedPlayer(@NotNull Player player);

    @Nullable T getTeam(@NotNull Player player);

    @NotNull Collection<T> getTeams();

    @NotNull Phase getPhase();

    @NotNull O getOption();

    @NotNull A getArena();

    default boolean isOngoing() {
        return switch (getPhase()) {
            case PRE_START, PLAYING, END -> true;
            default -> false;
        };
    }

    void teleportToStartLocation(@NotNull Player player);

    int getMaxPlayers();

    @NotNull MType<A, O> getMinigameType();

    boolean canSwitchToSpectator(Player player);

    boolean addSpectator(@NotNull Player player);

    boolean removeSpectator(@NotNull Player player);

    boolean isSpectator(@NotNull Player player);

    @NotNull Set<Player> getSpectators();

    boolean addPlayingPlayer(@NotNull Player player);

    boolean removePlayingPlayer(@NotNull Player player);

    boolean isPlayingPlayer(@NotNull Player player);

    @NotNull Set<Player> getPlayingPlayers();

    @NotNull
    default Set<Player> getPlayingPlayers(@NotNull T team) {
        Set<UUID> users = team.getUsers();
        Set<Player> players = new HashSet<>();
        for (Player p : this.getPlayingPlayers())
            if (users.contains(p.getUniqueId()))
                players.add(p);
        return players;
    }

    boolean containsLocation(@NotNull Location loc);

    default boolean containsLocation(@NotNull Entity en) {
        return containsLocation(en.getLocation());
    }

    default boolean containsLocation(@NotNull Block b) {
        return containsLocation(b.getLocation());
    }

    default boolean containsLocation(@NotNull BlockState b) {
        return containsLocation(b.getLocation());
    }

    @Nullable World getWorld();

    /**
     * must set phase to RESTART and call gameRestart()
     * <p>
     * also do anything which should be done when the game start for the first time and do not need to be done on game restart
     */
    void gameInitialize();

    /**
     * reset any variables which needs to be cleared on game restart
     * after that register the timer task
     * and finally set the COLLECTING_PLAYERS phase
     */
    void gameRestart();

    /**
     * called by the timer, uses canPreStart() to understand if can go to next phase
     * <p>
     * once he can it sets phase PRE_START and calls gamePreStart()
     */
    void gameCollectingPlayersTimer();

    /**
     * Phase is already PRE_START
     * this needs to assign teams to players
     * also eventually need to teleport players to their start location
     * should also allow to select a kit
     */
    void gamePreStart();

    /**
     * this should finally call gameStart() or gameAbort() if canStart() turns out to be false
     * eventually set phase to PLAYING
     */
    void gamePreStartTimer();

    /**
     * im'not sure what this method should do phase is PLAYING
     */
    void gameStart();

    /**
     * im'not sure what this method should do phase is PLAYING
     * or maybe shall put a time limit, whatever it may call gameEnd() but may also be called by any Event
     */
    void gamePlayingTimer();

    /**
     * annunce the winners maybe?
     */
    void gameEnd();

    /**
     * just a timer to let winners enjoy the win, should celebrate victory with fireworks and stuffs
     * <p>
     * finally call gameClose()
     */
    void gameEndTimer();

    /**
     * teleport everyone away players
     * finally set RESTART phase and calls gameRestart()
     */
    void gameClose();

    /**
     * kick users out of the game, cancel any task of the game
     * finally set phase STOPPED
     * <p>
     * to restart the game someone has to call gameInizialize
     */
    void gameAbort();

    /**
     * @return true if the game can switch from COLLECTING_PLAYER phase to PRE_START phase
     */
    boolean gameCanPreStart();

    /**
     * @return true if the game can switch from PRE_START phase to PLAYING phase
     */
    boolean gameCanStart();

    void setLocation(@NotNull BlockLocation3D generateLocation);

    void onPlayingPlayerDropItem(PlayerDropItemEvent event);

    void onPlayingPlayerPickupItem(EntityPickupItemEvent event, Player p);

    void onPlayingPlayerTeleport(PlayerTeleportEvent event);

    void onPlayingPlayerLaunchProjectile(ProjectileLaunchEvent event);

    void onPlayingPlayerMoveOutsideArena(PlayerMoveEvent event);

    void onPlayingPlayerMove(PlayerMoveEvent event);

    void onPlayingPlayerInteractEntity(PlayerInteractEntityEvent event);

    void onPlayingPlayerInteract(PlayerInteractEvent event);

    void onEntityRegainHealth(EntityRegainHealthEvent event);

    void onPlayingPlayerRegainHealth(EntityRegainHealthEvent event, Player p);

    void onEntityDeath(EntityDeathEvent event);

    void onFakePlayingPlayerDeath(Player p, Player damager);

    void onPlayingPlayerDamaging(EntityDamageByEntityEvent event, Player damager);

    void onPlayingPlayerPvpDamage(EntityDamageByEntityEvent event, Player p, Player damager);

    void onGameEntityDamaged(EntityDamageEvent event);

    void onPlayingPlayerDamaged(EntityDamageEvent event, Player p);

    void onCreatureSpawn(@NotNull CreatureSpawnEvent event);

    void onPortalCreate(PortalCreateEvent event);

    void onPlayingPlayerBlockBreak(BlockBreakEvent event);

    void onPlayingPlayerBlockPlace(BlockPlaceEvent event);

    default void onQuitGame(Player player) {
        if (isSpectator(player)) {
            removeSpectator(player);
            return;
        }
        switch (getPhase()) {
            case PLAYING, PRE_START, END -> {
                if (!isPlayingPlayer(player)) {
                    return;
                }
                removePlayingPlayer(player);
            }
            case COLLECTING_PLAYERS -> removeCollectedPlayer(player);
        }
    }

    boolean joinGameAsPlayer(Player player);

    boolean joinGameAsSpectator(Player player);

    enum Phase {
        STOPPED,
        COLLECTING_PLAYERS,
        PRE_START,
        PLAYING,
        RESTART,
        END
    }
}
