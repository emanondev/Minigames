package emanondev.minigames;

import emanondev.core.PlayerSnapshot;
import emanondev.core.UtilsString;
import emanondev.core.YMLConfig;
import emanondev.core.YMLSection;
import emanondev.core.util.ConsoleLogger;
import emanondev.minigames.event.PlayerJoinedGameEvent;
import emanondev.minigames.event.PlayerQuitGameEvent;
import emanondev.minigames.event.PlayerSpectateGameEvent;
import emanondev.minigames.generic.*;
import emanondev.minigames.locations.BlockLocation3D;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.*;

@SuppressWarnings("rawtypes")
public class GameManager extends Manager<MGame> implements Listener, ConsoleLogger {

    private static GameManager instance;
    private final List<List<MGame>> gameTickList;

    private final BukkitTask gameTick = new BukkitRunnable() {
        private int i = 0;

        public void run() {
            for (MGame game : gameTickList.get(i % 20))
                try {
                    switch (game.getPhase()) {
                        case END -> game.gameEndTimer();
                        case PLAYING -> game.gamePlayingTimer();
                        case COLLECTING_PLAYERS -> game.gameCollectingPlayersTimer();
                        case PRE_START -> game.gamePreStartTimer();
                        case RESTART -> game.gameRestartTimer();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            i++;
        }
    }.runTaskTimer(Minigames.get(), C.getStartupGameInitializeDelayTicks(), 1L);

    public static @NotNull GameManager get() {
        return instance;
    }

    protected @NotNull YMLSection getGlobalSection() {
        return Minigames.get().getConfig("minigamesConfig.yml")
                .loadSection("global");
    }

    public GameManager() {
        super("games");
        if (instance != null)
            throw new IllegalStateException();
        instance = this;
        this.gameTickList = new ArrayList<>(20);
        for (int i = 0; i < 20; i++)
            this.gameTickList.add(new ArrayList<>());
    }


    @NotNull
    public YMLSection getSection(@SuppressWarnings("rawtypes") @NotNull MType minigameType) {
        return Minigames.get().getConfig("minigamesConfig.yml")
                .loadSection(minigameType.getType());
    }

    public @Nullable Location getGlobalLobby() {
        return getGlobalSection().getLocation("lobby");
    }


    public void reload() {
        getAll().forEach((k, v) -> v.gameAbort());
        super.reload();
        long counter = 1;

        for (@SuppressWarnings("rawtypes") MGame game : getAll().values()) {
            new BukkitRunnable() {
                public void run() {
                    game.initialize();
                }
            }.runTaskLater(Minigames.get(), counter);
            counter += C.getStartupGameInitializeDelayTicks();
        }

    }

    public void register(@NotNull String id, @SuppressWarnings("rawtypes") @NotNull MGame game, @NotNull YMLConfig config) {
        if (!UtilsString.isLowcasedValidID(id) || get(id) != null)
            throw new IllegalStateException("invalid id");
        boolean save = false;
        logTetraStar(ChatColor.DARK_RED, "D Registering Game &e" + id + "&f of type &e" + game.getMinigameType().getType());
        if (!isValidLocation(game.getGameLocation(), game.getArena(), game.getWorld())) {
            logTetraStar(ChatColor.DARK_RED, "D Game Location is invalid assigning new location");
            game.setLocation(generateLocation(game.getArena(), game.getWorld()));
            save = true;
        }
        super.register(id, game, config);
        Collections.min(gameTickList, Comparator.comparingInt(List::size)).add(game);
        if (save)
            save(game);
        logTetraStar(ChatColor.DARK_RED, "D Registered Game &e" + id + "&f of type &e" + game.getMinigameType().getType()
                + "&f at location &e" + game.getGameLocation().toString().replace(":", " ")
                + (game.getArena() instanceof MSchemArena mArena ? " to " +
                (game.getGameLocation().x + mArena.getSize().getBlockX()) + " "
                + (game.getGameLocation().y + mArena.getSize().getBlockY()) + " " +
                (game.getGameLocation().z + mArena.getSize().getBlockZ()) : ""));
    }


    public void delete(String id) {
        @Nullable MGame game = get(id);
        if (game != null) {
            game.gameAbort();
        }
        super.delete(id);
        if (game != null) {
            gameTickList.forEach(l -> l.remove(game));
        }
    }

    /**
     * Generate a new Location for selected arena on selected world
     */
    @Contract("_, _ -> new")
    public @NotNull BlockLocation3D generateLocation(@NotNull MArena arena, @Nullable World world) {
        int counter = 0;
        int offset = 500;
        boolean isValid = false;
        BlockLocation3D loc = null;
        if (world == null)
            world = Bukkit.getWorld(getGlobalSection().getString("defaultWorld", ""));
        if (world == null)
            world = Bukkit.getWorlds().get(0);
        while (!isValid && counter < 500) {
            loc = new BlockLocation3D(world, 10000 + offset * counter, 0, 10000);
            isValid = isValidLocation(loc, arena, world);
            counter++;
        }
        if (!isValid) //TODO
            throw new IllegalStateException();
        logTetraStar(ChatColor.DARK_RED, "D Generated Game Location at &e"
                + loc.toString().replace(":", " "));
        return loc;
    }


    public boolean isValidLocation(@NotNull BlockLocation3D loc, @NotNull MArena arena, @NotNull World w) {
        if (arena instanceof MSchemArena schemArena) {
            BlockVector dim = schemArena.getSize();
            //TODO eventually move the y
            BoundingBox box = new BoundingBox(
                    loc.x, loc.y, loc.z,
                    loc.x + dim.getX(), loc.y + dim.getY(), loc.z + dim.getZ());
            box.expand(C.getGameMinimalSpaceDistancing());

            for (@SuppressWarnings("rawtypes") MGame game : getAll().values()) {
                if (game.getArena() instanceof MSchemArena schemArena2) {
                    if (!game.getWorld().getName().equals(w.getName()))
                        continue;
                    BlockVector dim2 = schemArena2.getSize();
                    //BlockVector3 dim2 = clip2.getDimensions();
                    BlockLocation3D loc2 = game.getGameLocation();
                    BoundingBox box2 = new BoundingBox(
                            loc2.x, loc2.y, loc2.z,
                            loc2.x + dim2.getX(), loc2.y + dim2.getY(), loc2.z + dim2.getZ());
                    if (box.overlaps(box2))
                        return false;
                } else
                    throw new UnsupportedOperationException("invalid arena type");
            }
            return true;
        }
        throw new UnsupportedOperationException("invalid arena type");
    }

    @SuppressWarnings("unchecked")
    public @NotNull <A extends MArena, O extends MOption> Map<String, MGame<?, A, O>> getGameInstances(@NotNull MType<A, O> type) {
        Map<String, MGame<?, A, O>> map = new HashMap<>();
        getAll().forEach((k, v) -> {
            if (v.getMinigameType().equals(type))
                map.put(k, v);
        });
        return map;
    }

    @SuppressWarnings("unchecked")
    public @NotNull Map<String, MGame> getGameInstances(@NotNull Collection<MType> types) {
        Map<String, MGame> map = new HashMap<>();
        for (MType type : types)
            map.putAll(getGameInstances(type));
        return map;
    }

    /**
     * Returns the Game the player is currently on
     *
     * @return the Game the player is currently on
     */
    @SuppressWarnings("rawtypes")
    @Nullable
    public MGame getCurrentGame(@NotNull Player player) {
        return playerGames.get(player);
    }

    @SuppressWarnings("rawtypes")
    private final HashMap<Player, MGame> playerGames = new HashMap<>();
    private final HashMap<Player, Scoreboard> playerBoards = new HashMap<>();
    private final HashMap<Player, PlayerSnapshot> playerSnapshots = new HashMap<>();

    @SuppressWarnings("rawtypes")
    public boolean joinGameAsGamer(Player player, List<MGame> gameList) {
        MGame gameOld = getCurrentGame(player);

        if (gameOld != null)
            gameOld.onQuitGame(player);
        if (gameOld == null) {
            playerSnapshots.put(player, new PlayerSnapshot(player));
            playerBoards.put(player, player.getScoreboard());
        }
        for (MGame game : gameList)
            if (game.joinGameAsGamer(player)) {
                Bukkit.getPluginManager().callEvent(new PlayerJoinedGameEvent(game, player));
                logTetraStar(ChatColor.DARK_RED, "D user &e" + player.getName() + "&f joined game &e" + game.getId());
                playerGames.put(player, game);
                player.setScoreboard(game.getScoreboard());
                return true;
            }
        if (gameOld != null) {
            Location respawn = C.getRespawnLocation();
            if (respawn!=null){
                EnumSet<PlayerSnapshot.FieldType> values = EnumSet.allOf(PlayerSnapshot.FieldType.class);
                values.remove(PlayerSnapshot.FieldType.LOCATION);
                player.teleport(respawn);
                playerSnapshots.remove(player).apply(player,values);
            }else
                playerSnapshots.remove(player).apply(player);
            player.setScoreboard(playerBoards.remove(player)); //TODO not working
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    public boolean joinGameAsGamer(Player player, MGame game) {
        return this.joinGameAsGamer(player, List.of(game));
    }

    @SuppressWarnings("rawtypes")
    public boolean joinGameAsSpectator(Player player, MGame game) {
        MGame gameOld = getCurrentGame(player);

        if (gameOld != null)
            gameOld.onQuitGame(player);
        if (gameOld == null) {
            playerSnapshots.put(player, new PlayerSnapshot(player));
            playerBoards.put(player, player.getScoreboard());
        }
        if (game.joinGameAsSpectator(player)) {
            Bukkit.getPluginManager().callEvent(new PlayerSpectateGameEvent(game, player));
            logTetraStar(ChatColor.DARK_RED, "D user &e" + player.getName() + "&f joined game (as specatator) &e" + game.getId());
            playerGames.put(player, game);
            player.setScoreboard(game.getScoreboard());
            return true;
        }
        if (gameOld != null) {
            Location respawn = C.getRespawnLocation();
            if (respawn!=null){
                EnumSet<PlayerSnapshot.FieldType> values = EnumSet.allOf(PlayerSnapshot.FieldType.class);
                values.remove(PlayerSnapshot.FieldType.LOCATION);
                player.teleport(respawn);
                playerSnapshots.remove(player).apply(player,values);
            }else
                playerSnapshots.remove(player).apply(player);
            player.setScoreboard(playerBoards.remove(player));
        }
        return false;
    }

    public void quitGame(Player player) {
        @SuppressWarnings("rawtypes") MGame game = getCurrentGame(player);
        if (game == null)
            return;
        game.onQuitGame(player);
        playerGames.remove(player);
        playerSnapshots.remove(player).apply(player);
        player.setScoreboard(playerBoards.remove(player));
        Bukkit.getPluginManager().callEvent(new PlayerQuitGameEvent(game, player));
        logTetraStar(ChatColor.DARK_RED, "D user &e" + player.getName() + "&f quitted game &e" + game.getId());
    }

    @EventHandler
    private void event(PlayerQuitEvent event) {
        quitGame(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void event(@NotNull BlockPlaceEvent event) {
        @SuppressWarnings("rawtypes") MGame game = getCurrentGame(event.getPlayer());
        if (game == null)
            return;
        if (game.isGamer(event.getPlayer()) && game.containsLocation(event.getBlock())) {
            game.onGamerBlockPlace(event);
            return;
        }
        event.setCancelled(true); //outside game bounds or spectator
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void event(@NotNull BlockBreakEvent event) {
        @SuppressWarnings("rawtypes") MGame game = getCurrentGame(event.getPlayer());
        if (game == null)
            return;
        if (game.isGamer(event.getPlayer()) && game.containsLocation(event.getBlock())) {
            game.onGamerBlockBreak(event);
            return;
        }
        event.setCancelled(true); //outside game bounds or spectator
    }


    @EventHandler
    private void event(PlayerInteractEvent event) {
        @SuppressWarnings("rawtypes") MGame game = getCurrentGame(event.getPlayer());
        if (game == null)
            return;
        if (game.isGamer(event.getPlayer())) {
            game.onGamerInteract(event);
            return;
        }
        //player is a spectator
        event.setCancelled(true);
        game.onSpectatorInteract(event);
    }

    @EventHandler
    private void event(PlayerInteractEntityEvent event) {
        @SuppressWarnings("rawtypes") MGame game = getCurrentGame(event.getPlayer());
        if (game == null)
            return;
        if (game.isGamer(event.getPlayer())) {
            game.onGamerInteractEntity(event);
            return;
        }
        //player is a spectator
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void event(PlayerMoveEvent event) { //!=teleportevent
        @SuppressWarnings("rawtypes") MGame game = getCurrentGame(event.getPlayer());
        if (game == null)
            return;
        if (event.getTo() == null) { //Dead ? should not happen
            new IllegalStateException("moved to null").printStackTrace();
            return;
        }
        if (game.isGamer(event.getPlayer())) {
            if (!game.containsLocation(event.getTo())) {
                //MessageUtil.debug(game.getId() + " move outside arena");
                game.onGamerMoveOutsideArena(event);
                //event.setCancelled(true);
                return;
            }
            game.onGamerMoveInsideArena(event);
            return;
        }
        //it's a spectator
        game.onSpectatorMove(event);
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void event(PlayerTeleportEvent event) {
        @SuppressWarnings("rawtypes") MGame game = getCurrentGame(event.getPlayer());
        if (game == null)
            return;
        if (event.getTo() == null) { //dead?
            new IllegalStateException("moved to null").printStackTrace();
            return;
        }
        if (game.isGamer(event.getPlayer())) {
            if (!game.containsLocation(event.getTo())) {
                game.onGamerMoveOutsideArena(event);
                event.setCancelled(true);
            } else
                game.onGamerTeleport(event);
            return;
        }
        if (event.getTo().getWorld() != event.getFrom().getWorld())  //teleported away by something ?
            switch (event.getCause()) {
                case END_GATEWAY, END_PORTAL, NETHER_PORTAL, ENDER_PEARL -> event.setCancelled(true);
                default -> quitGame(event.getPlayer());
            }
        else if (!game.containsLocation(event.getTo()))
            event.setCancelled(true);
    }

    @EventHandler
    private void event(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Player player) {
            @SuppressWarnings("rawtypes") MGame game = getCurrentGame(player);
            if (game == null)
                return;
            if (game.isGamer(player))
                game.onGamerLaunchProjectile(event);
            else
                event.setCancelled(true);
        }
    }

    @EventHandler
    private void event(ProjectileHitEvent event) {
        if (event.getHitEntity() instanceof Player player) {
            @SuppressWarnings("rawtypes") MGame game = getCurrentGame(player);
            if (game == null)
                return;
            if (game.isGamer(player))
                game.onGamerHitByProjectile(event);
            else
                event.setCancelled(true);
        }
    }

    @EventHandler
    private void event(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            @SuppressWarnings("rawtypes") MGame game = getCurrentGame(player);
            if (game == null)
                return;
            if (game.isGamer(player))
                game.onGamerPickupItem(event, player);
            else
                event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void event(PlayerDropItemEvent event) {
        @SuppressWarnings("rawtypes") MGame game = getCurrentGame(event.getPlayer());
        if (game == null)
            return;
        if (game.isGamer(event.getPlayer()))
            game.onGamerDropItem(event);
        else
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    private void event(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player player) {
            @SuppressWarnings("rawtypes") MGame game = getCurrentGame(player);
            if (game != null && !game.isGamer(player))
                event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void event(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            @SuppressWarnings("rawtypes") MGame game = getCurrentGame(player);
            if (game != null) {
                if (!game.isGamer(player)) //that's a spectator
                    event.setCancelled(true);
                else
                    game.onGamerInventoryClick(event, player);

            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void event(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            @SuppressWarnings("rawtypes") MGame game = getCurrentGame(player);
            if (game != null && game.isGamer(player))
                game.onGamerInventoryClose(event, player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void event(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player player) {
            @SuppressWarnings("rawtypes") MGame game = getCurrentGame(player);
            if (game != null && game.isGamer(player))
                game.onGamerInventoryOpen(event, player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void event(PlayerSwapHandItemsEvent event) {
        @SuppressWarnings("rawtypes") MGame game = getCurrentGame(event.getPlayer());
        if (game != null) {
            if (!game.isGamer(event.getPlayer()))
                event.setCancelled(true);
            else
                game.onGamerSwapHandItems(event);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void event(EntityCombustEvent event) {
        if (event.getEntity() instanceof Player player) {
            @SuppressWarnings("rawtypes") MGame game = getCurrentGame(player);
            if (game != null) {
                if (!game.isGamer(player))
                    event.setCancelled(true);
                else
                    game.onGamerCombustEvent(event, player);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void event(EntityMountEvent event) {
        if (event.getEntity() instanceof Player player) {
            @SuppressWarnings("rawtypes") MGame game = getCurrentGame(player);
            if (game != null) {
                if (!game.isGamer(player))
                    event.setCancelled(true);
                else
                    game.onGamerMountEvent(event, player);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void event(EntityDismountEvent event) {
        if (event.getEntity() instanceof Player player) {
            @SuppressWarnings("rawtypes") MGame game = getCurrentGame(player);
            if (game != null) {
                if (!game.isGamer(player))
                    event.setCancelled(true);
                else
                    game.onGamerDismountEvent(event, player);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void event(VehicleMoveEvent event) {
        for (Entity e : event.getVehicle().getPassengers()) {
            if (e instanceof Player player) {
                @SuppressWarnings("rawtypes") MGame game = getCurrentGame(player);
                if (game != null && game.isGamer(player))
                    game.onGamerVehicleMoveEvent(event, player);

            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void event(PlayerPickupArrowEvent event) {
        @SuppressWarnings("rawtypes") MGame game = getCurrentGame(event.getPlayer());
        if (game != null && !game.isGamer(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    private void event(EntityExhaustionEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;
        @SuppressWarnings("rawtypes") MGame game = getCurrentGame(player);
        if (game != null)
            if (game.isGamer(player))
                game.onGamerExhaustionEvent(event, player);
            else
                event.setCancelled(true);
    }

    @Override
    public void log(String s) {
        Minigames.get().log("(GameManager) " + s);
    }

}