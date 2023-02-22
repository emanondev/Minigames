package emanondev.minigames.generic;

import emanondev.minigames.locations.BlockLocation3D;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public interface MGame<T extends MTeam, A extends MArena, O extends MOption> extends ConfigurationSerializable, Cloneable, Registrable {

    @NotNull BlockLocation3D getGameLocation();

    //@NotNull Set<Player> getCollectedPlayers();

    //boolean isCollectedPlayer(@NotNull Player player);

    //boolean addCollectedPlayer(@NotNull Player player);

    //void onCollectedPlayerAdded(@NotNull Player player);

    //boolean canAddCollectedPlayer(@NotNull Player player);

    //boolean removeCollectedPlayer(@NotNull Player player);

    @Nullable T getTeam(@NotNull OfflinePlayer player);

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

    int getMaxGamers();

    @NotNull MType<A, O> getMinigameType();

    boolean canSwitchToSpectator(Player player);

    boolean addSpectator(@NotNull Player player);

    boolean removeSpectator(@NotNull Player player);

    boolean isSpectator(@NotNull Player player);

    @NotNull Set<Player> getSpectators();

    boolean addGamer(@NotNull Player player);

    void teleportResetLocation(@NotNull Player player);

    boolean canAddGamer(@NotNull Player player);

    boolean removeGamer(@NotNull Player player);

    boolean isGamer(@NotNull Player player);

    @NotNull Set<Player> getGamers();

    @NotNull
    default Set<Player> getGamers(@NotNull T team) {
        Set<UUID> users = team.getUsers();
        Set<Player> players = new HashSet<>();
        for (Player p : this.getGamers())
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

    /**
     * check if the game has finished, if has should calls for onGameEnd();
     * <p>
     * getPhase()==Phase.PLAYING may be a double security check
     */
    void checkGameEnd();

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

    void gameCollectingPlayers();

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

    void setScore(String score, int value);

    int getScore(String score);

    void resetScore(String score);

    void resetScores();

    default void addScore(String score, int value) {
        setScore(score, getScore(score) + value);
    }

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

    Objective getObjective();

    Scoreboard getScoreboard();

    void setLocation(@NotNull BlockLocation3D generateLocation);

    void onGamerDropItem(@NotNull PlayerDropItemEvent event);

    void onGamerPickupItem(@NotNull EntityPickupItemEvent event, @NotNull Player p);

    void onGamerTeleport(@NotNull PlayerTeleportEvent event);

    void onGamerLaunchProjectile(@NotNull ProjectileLaunchEvent event);

    boolean canAddSpectator(@NotNull Player player);

    void onSpectatorAdded(@NotNull Player player);

    void onSpectatorRemoved(@NotNull Player player);

    void onGamerRemoved(@NotNull Player player);

    void onGamerAdded(@NotNull Player player);

    void onGamerMoveOutsideArena(@NotNull PlayerMoveEvent event);

    void onGamerMoveInsideArena(@NotNull PlayerMoveEvent event);

    void onGamerInteractEntity(@NotNull PlayerInteractEntityEvent event);

    void onGamerInteract(@NotNull PlayerInteractEvent event);

    void onEntityRegainHealth(@NotNull EntityRegainHealthEvent event);

    /**
     * Called when playing player die inside the arena
     * <p>
     * the player do not die as the final damage is cancelled, but you should handle this instead
     * this may be called as result of player quitting the game, quitting the arena or being killed, falling below the arena
     */
    void onFakeGamerDeath(@NotNull Player dead, @Nullable Player killer, boolean direct);

    void onGamerRegainHealth(@NotNull EntityRegainHealthEvent event, @NotNull Player p);

    void onEntityDeath(@NotNull EntityDeathEvent event);

    void onGamerDamaging(@NotNull EntityDamageByEntityEvent event, @NotNull Player damager, boolean direct);

    void onGamerPvpDamage(@NotNull EntityDamageByEntityEvent event, @NotNull Player p, @NotNull Player damager, boolean direct);

    void onGameEntityDamaged(@NotNull EntityDamageEvent event);

    void onGamerDamaged(@NotNull EntityDamageEvent event, Player p);

    void onCreatureSpawn(@NotNull CreatureSpawnEvent event);

    void onPortalCreate(@NotNull PortalCreateEvent event);

    void onGamerBlockBreak(@NotNull BlockBreakEvent event);

    void onGamerBlockPlace(@NotNull BlockPlaceEvent event);

    default void onQuitGame(@NotNull Player player) {
        if (isSpectator(player)) {
            removeSpectator(player);
            return;
        }
        if (isGamer(player))
            removeGamer(player);
    }

    boolean joinGameAsGamer(@NotNull Player player);

    boolean joinGameAsSpectator(@NotNull Player player);

    /**
     * should eventually cancel the event if moving outside of x z borders
     *
     * @param event
     */
    void onSpectatorMove(@NotNull PlayerMoveEvent event);

    void onSpectatorInteract(@NotNull PlayerInteractEvent event);

    boolean overlaps(@NotNull Chunk chunk);

    void onChunkEntitiesLoad(@NotNull Chunk chunk);

    void onGamerInventoryClick(@NotNull InventoryClickEvent event, @NotNull Player player);

    void onGamerInventoryOpen(@NotNull InventoryOpenEvent event, @NotNull Player player);

    void onGamerInventoryClose(@NotNull InventoryCloseEvent event, @NotNull Player player);

    void onGamerSwapHandItems(@NotNull PlayerSwapHandItemsEvent event);

    void onGamerCombustEvent(@NotNull EntityCombustEvent event, @NotNull Player player);

    void onGamerHitByProjectile(ProjectileHitEvent event);

    default ItemStack getGameSelectorItem(Player player) {
        return getMinigameType().getGameSelectorBaseItem().setAmount(Math.max(1, getGamers().size()))
                .setMiniDescription(
                        getMinigameType().getPlugin().getLanguageConfig(player).loadMultiMessage(getMinigameType().getType() + ".gui.selector",
                                new ArrayList<>(), true, "%arena_name%", getArena().getDisplayName()
                                , "%players%", String.valueOf(getGamers().size())
                                , "%max_players%", String.valueOf(getMaxGamers()))
                ).build();
    }

    enum Phase {
        STOPPED,
        COLLECTING_PLAYERS,
        PRE_START,
        PLAYING,
        RESTART,
        END
    }
}
