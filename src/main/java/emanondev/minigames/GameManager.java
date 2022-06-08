package emanondev.minigames;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import emanondev.core.PlayerSnapshot;
import emanondev.core.UtilsString;
import emanondev.core.YMLConfig;
import emanondev.core.YMLSection;
import emanondev.minigames.generic.*;
import emanondev.minigames.locations.BlockLocation3D;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager implements Listener {

    public File getGamesFolder() {
        return new File(Minigames.get().getDataFolder(), "games");
    }

    public YMLConfig getGameConfig(@NotNull String fileName) {
        return Minigames.get().getConfig("games" + File.separator + fileName);
    }

    private static GameManager instance;

    @SuppressWarnings("rawtypes")
    private final Map<String, MGame> games = new HashMap<>();
    private final HashMap<String, YMLConfig> gamesFile = new HashMap<>();


    public static @NotNull GameManager get() {
        return instance;
    }

    protected @NotNull YMLSection getGlobalSection() {
        return Minigames.get().getConfig("minigamesConfig.yml")
                .loadSection("global");
    }

    public GameManager() {
        if (instance != null)
            throw new IllegalStateException();
        instance = this;
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

        games.forEach((k, v) -> v.gameAbort());
        games.clear();
        gamesFile.clear();
        File gameInstancesFolder = getGamesFolder();
        if (gameInstancesFolder.isDirectory()) {
            File[] files = gameInstancesFolder.listFiles((f, n) -> (n.endsWith(".yml")));
            if (files != null)
                for (File file : files) {
                    YMLConfig config = getGameConfig(file.getName());
                    for (String key : config.getKeys(false)) {
                        try {
                            Object value = config.get(key);
                            if (value instanceof MGame game)
                                registerGame(key, game, config);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        }
        long counter = 1;
        for (@SuppressWarnings("rawtypes") MGame game : games.values()) {
            new BukkitRunnable() {
                public void run() {
                    game.gameInitialize();
                }
            }.runTaskLater(Minigames.get(), counter);
            counter += 5;
        }

    }

    public void registerGame(@NotNull String id, @SuppressWarnings("rawtypes") @NotNull MGame game, @NotNull OfflinePlayer player) {
        registerGame(id, game, getGameConfig(player.getName()));
    }

    private void registerGame(@NotNull String id, @SuppressWarnings("rawtypes") @NotNull MGame game, @NotNull YMLConfig config) {
        if (!UtilsString.isLowcasedValidID(id) || games.containsKey(id))
            throw new IllegalStateException("invalid id");
        boolean save = false;
        if (!isValidLocation(game.getGameLocation(), game.getArena(), game.getWorld())) {
            game.setLocation(generateLocation(game.getArena(), game.getWorld()));
            save = true;
        }
        //throw new IllegalStateException("location"); //may just move it
        if (game.isRegistered())
            throw new IllegalStateException();
        game.setRegistered(id);
        games.put(id, game);
        gamesFile.put(id, config);
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "D Registered Game &e" + id + "&f of type &e" + game.getMinigameType().getType());
        if (save)
            save(game);
    }

    public void save(@SuppressWarnings("rawtypes") @NotNull MGame mGame) {
        if (!mGame.isRegistered())
            throw new IllegalStateException();
        if (games.get(mGame.getId()) != mGame)
            throw new IllegalStateException();
        gamesFile.get(mGame.getId()).set(mGame.getId(), mGame);
        gamesFile.get(mGame.getId()).save();
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "D Updated Game &e" + mGame.getId() + "&f of type &e" + mGame.getMinigameType().getType());
    }

    public @NotNull BlockLocation3D generateLocation(@NotNull MArena arena, @Nullable World w) {
        int counter = 0;
        int offset = 500;
        boolean isValid = false;
        BlockLocation3D loc = null;
        if (w == null)
            w = Bukkit.getWorld(getGlobalSection().getString("defaultWorld", ""));
        if (w == null)
            w = Bukkit.getWorlds().get(0);
        while (!isValid && counter < 200) { //TODO
            loc = new BlockLocation3D(w, 10000 + offset * counter, 0, 10000);
            isValid = isValidLocation(loc, arena, w);
            counter++;
        }
        if (!isValid) //TODO
            throw new IllegalStateException();
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "D Generated Game Location at &e"
                + loc.toString().replace(":", " "));
        return loc;
    }

    public boolean isValidLocation(@NotNull BlockLocation3D loc, @NotNull MArena arena, @NotNull World w) {
        if (arena instanceof MSchemArena schemArena) {
            Clipboard clip = schemArena.getSchematic();
            BlockVector3 min = clip.getMinimumPoint();
            BlockVector3 dim = clip.getDimensions();
            //TODO eventually move the y
            BoundingBox box = new BoundingBox(
                    loc.x + min.getX(), loc.y + min.getY(), loc.z + min.getZ(),
                    loc.x + min.getX() + dim.getX(), loc.y + min.getY() + dim.getY(), loc.z + min.getZ() + dim.getZ());
            box.expand(528); //32+1 chunk (528)

            for (@SuppressWarnings("rawtypes") MGame game : games.values()) {
                if (game.getArena() instanceof MSchemArena schemArena2) {
                    if (!game.getWorld().getName().equals(w.getName()))
                        continue;
                    Clipboard clip2 = schemArena2.getSchematic();
                    BlockVector3 min2 = clip2.getMinimumPoint();
                    BlockVector3 dim2 = clip2.getDimensions();
                    BlockLocation3D loc2 = game.getGameLocation();

                    BoundingBox box2 = new BoundingBox(
                            loc2.x + min2.getX(), loc2.y + min2.getY(), loc2.z + min2.getZ(),
                            loc2.x + min2.getX() + dim2.getX(), loc2.y + min2.getY() + dim2.getY(), loc2.z + min2.getZ() + dim2.getZ());

                    if (box.overlaps(box2)) {
                        return false;
                    }
                } else
                    throw new UnsupportedOperationException("invalid arena type");
            }
            return true;
        }
        throw new UnsupportedOperationException("invalid arena type");
    }

    @SuppressWarnings("rawtypes")
    @NotNull
    public Map<String, MGame> getGames() {
        return Collections.unmodifiableMap(games);
    }

    @SuppressWarnings("unchecked")
    public @NotNull <A extends MArena, O extends MOption> Map<String, MGame<?, A, O>> getPreMadeGameInstances(@NotNull MType<A, O> type) {
        Map<String, MGame<?, A, O>> map = new HashMap<>();
        games.forEach((k, v) -> {
            if (v.getMinigameType().equals(type))
                map.put(k, v);
        });
        return map;
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    public MGame getPreMadeGameInstance(@NotNull String id) {
        return games.get(id.toLowerCase());
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    public MGame getGame(@NotNull Player player) {
        return playerGames.get(player);
    }

    @SuppressWarnings("rawtypes")
    private final HashMap<Player, MGame> playerGames = new HashMap<>();
    private final HashMap<Player, Scoreboard> playerBoards = new HashMap<>();
    private final HashMap<Player, PlayerSnapshot> playerSnapshots = new HashMap<>();

    @SuppressWarnings("rawtypes")
    public boolean joinGameAsGamer(Player player, List<MGame> gameList) {
        MGame gameOld = getGame(player);

        if (gameOld != null)
            gameOld.onQuitGame(player);
        if (gameOld == null) {
            playerSnapshots.put(player, new PlayerSnapshot(player));
            playerBoards.put(player, player.getScoreboard());
        }
        for (MGame game : gameList)
            if (game.joinGameAsGamer(player)) {
                Minigames.get().logTetraStar(ChatColor.DARK_RED, "D user &e" + player.getName() + "&f joined game &e" + game.getId());
                playerGames.put(player, game);
                //MessageUtil.debug(game.getScoreboard().);
                player.setScoreboard(game.getScoreboard());
                return true;
            }
        if (gameOld != null) {
            playerSnapshots.remove(player).apply(player);
            player.setScoreboard(playerBoards.remove(player));
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    public boolean joinGameAsGamer(Player player, MGame game) {
        MGame gameOld = getGame(player);

        if (gameOld != null)
            gameOld.onQuitGame(player);
        if (gameOld == null) {
            playerSnapshots.put(player, new PlayerSnapshot(player));
            playerBoards.put(player, player.getScoreboard());
        }
        if (game.joinGameAsGamer(player)) {
            Minigames.get().logTetraStar(ChatColor.DARK_RED, "D user &e" + player.getName() + "&f joined game &e" + game.getId());
            playerGames.put(player, game);
            player.setScoreboard(game.getScoreboard());
            return true;
        }
        if (gameOld != null) {
            playerSnapshots.remove(player).apply(player);
            player.setScoreboard(playerBoards.remove(player));
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    public boolean joinGameAsSpectator(Player player, MGame game) {
        MGame gameOld = getGame(player);

        if (gameOld != null)
            gameOld.onQuitGame(player);
        if (gameOld == null) {
            playerSnapshots.put(player, new PlayerSnapshot(player));
            playerBoards.put(player, player.getScoreboard());
        }
        if (game.joinGameAsSpectator(player)) {
            Minigames.get().logTetraStar(ChatColor.DARK_RED, "D user &e" + player.getName() + "&f joined game (as specatator) &e" + game.getId());
            playerGames.put(player, game);
            player.setScoreboard(game.getScoreboard());
            return true;
        }
        if (gameOld != null) {
            playerSnapshots.remove(player).apply(player);
            player.setScoreboard(playerBoards.remove(player));
        }
        return false;
    }

    public void quitGame(Player player) {
        @SuppressWarnings("rawtypes") MGame game = getGame(player);
        if (game == null)
            return;
        game.onQuitGame(player);
        playerGames.remove(player);
        playerSnapshots.remove(player).apply(player);
        player.setScoreboard(playerBoards.remove(player));
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "D user &e" + player.getName() + "&f quitted game &e" + game.getId());
    }

    @EventHandler
    private void event(PlayerQuitEvent event) {
        quitGame(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void event(@NotNull BlockPlaceEvent event) {
        @SuppressWarnings("rawtypes") MGame game = getGame(event.getPlayer());
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
        @SuppressWarnings("rawtypes") MGame game = getGame(event.getPlayer());
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
        @SuppressWarnings("rawtypes") MGame game = getGame(event.getPlayer());
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
        @SuppressWarnings("rawtypes") MGame game = getGame(event.getPlayer());
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
        @SuppressWarnings("rawtypes") MGame game = getGame(event.getPlayer());
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
        @SuppressWarnings("rawtypes") MGame game = getGame(event.getPlayer());
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
            @SuppressWarnings("rawtypes") MGame game = getGame(player);
            if (game == null)
                return;
            if (game.isGamer(player))
                game.onGamerLaunchProjectile(event);
            else
                event.setCancelled(true);
        }
    }

    @EventHandler
    private void event(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            @SuppressWarnings("rawtypes") MGame game = getGame(player);
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
        @SuppressWarnings("rawtypes") MGame game = getGame(event.getPlayer());
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
            @SuppressWarnings("rawtypes") MGame game = getGame(player);
            if (game != null && !game.isGamer(player))
                event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void event(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            @SuppressWarnings("rawtypes") MGame game = getGame(player);
            if (game != null) {
                if (!game.isGamer(player))
                    event.setCancelled(true);
                else
                    game.onGamerClickEvent(event, player);

            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void event(PlayerSwapHandItemsEvent event) {
        @SuppressWarnings("rawtypes") MGame game = getGame(event.getPlayer());
        if (game != null) {
            if (!game.isGamer(event.getPlayer()))
                event.setCancelled(true);
            else
                game.onGamerSwapHandItems(event);
        }

    }
}