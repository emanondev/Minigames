package emanondev.minigames.generic;

import emanondev.core.SoundInfo;
import emanondev.core.UtilsString;
import emanondev.core.VaultEconomyHandler;
import emanondev.core.gui.Gui;
import emanondev.core.gui.PagedListFGui;
import emanondev.core.message.DMessage;
import emanondev.core.message.SimpleMessage;
import emanondev.minigames.*;
import emanondev.minigames.locations.BlockLocation3D;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractMGame<T extends ColoredTeam, A extends MArena, O extends MOption> extends ARegistrable implements MGame<T, A, O>, Listener {

    private final Objective objective;
    private int collectingPlayersCountdown = -1;
    private int endCountdown = -1;
    private int preStartCountdown = -1;
    private BlockLocation3D loc;
    private Phase phase = Phase.STOPPED;

    private final O option;
    private final A arena;
    private final String optionId;
    private final String arenaId;
    private final HashMap<Player, String> kitPreference = new HashMap<>();

    @SuppressWarnings("unchecked")
    public AbstractMGame(@NotNull Map<String, Object> map) {
        this.optionId = (String) map.get("option");
        if (this.optionId == null)
            throw new IllegalStateException("Arena ID is null");
        this.arenaId = (String) map.get("arena");
        if (this.arenaId == null)
            throw new IllegalStateException("Option ID is null");
        this.option = (O) OptionManager.get().get(optionId);
        this.arena = (A) ArenaManager.get().get(arenaId);
        if (this.arena == null)
            throw new IllegalStateException("Arena is null, couldn't find Arena ID '"+arenaId+"'");
        if (this.option == null)
            throw new IllegalStateException("Option is null, couldn't find Option ID '"+optionId+"'");
        try {
            String textLoc = (String) map.get("location");
            if (textLoc != null)
                this.loc = BlockLocation3D.fromString(textLoc);
            if (loc == null) {
                loc = GameManager.get().generateLocation(arena, getWorld());
            }
        } catch (Exception e) {
            e.printStackTrace();
            loc = GameManager.get().generateLocation(arena, getWorld());
        }
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = scoreboard.registerNewObjective("game", "dummy", getObjectiveDisplayName(), RenderType.INTEGER);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    protected String getObjectiveDisplayName() {
        return getMinigameType().getDisplayName();
    }

    @Override
    public Objective getObjective() {
        return objective;
    }

    private final Scoreboard scoreboard;

    @Override
    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public void setLocation(@NotNull BlockLocation3D loc) {
        boolean wasStopped = getPhase() != Phase.STOPPED;
        if (!wasStopped)
            this.gameAbort();
        this.loc = loc;
        if (!wasStopped)
            this.initialize();
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("option", optionId);
        map.put("arena", arenaId);
        map.put("location", loc.toString());
        return map;
    }

    @Override
    public CompletableFuture<Void> gameInitialize() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        new BukkitRunnable() {
            public void run() {
                try {
                    MessageUtil.debug(getId() + " gameInizialize");
                    if (phase != Phase.STOPPED)
                        throw new IllegalStateException();
                    phase = Phase.RESTART;
                    future.complete(null);
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                }
            }
        }.runTask(Minigames.get());
        return future;
    }

    @Override
    public CompletableFuture<Void> gameRestart() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        new BukkitRunnable() {
            public void run() {
                try {
                    MessageUtil.debug(getId() + " gameRestart");
                    if (phase != Phase.RESTART)
                        throw new IllegalStateException();

                    kitPreference.clear();
                    for (T team : getTeams())
                        team.clear();
                    future.complete(null);
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                }
            }
        }.runTask(Minigames.get());
        return future;
    }

    public void restart() {
        gameRestart().whenComplete((value, th) -> {
            MessageUtil.debug(this.getId() + " (" + getMinigameType().getType() + ") Restart terminato");
            if (th != null)
                this.gameAbort();
            else {
                phase = Phase.COLLECTING_PLAYERS;
                gameCollectingPlayers();
            }
        });
    }

    @Override
    public void gameCollectingPlayers() {
        if (phase != Phase.COLLECTING_PLAYERS)
            throw new IllegalStateException();
        MessageUtil.debug(getId() + " gameCollectingPlayers");
        registerAsListener(true);
        preStartCountdown = getOption().getPreStartPhaseCooldownMax();
        collectingPlayersCountdown = getOption().getCollectingPlayersPhaseCooldownMax();
    }


    @Override
    public void gameCollectingPlayersTimer() {
        if (phase != Phase.COLLECTING_PLAYERS)
            throw new IllegalStateException();
        if (this.gameCanPreStart()) {
            if (gamers.size() == getMaxGamers()) {
                //fast start
                phase = Phase.PRE_START;
                for (Player gamer : getGamers()) {
                    if (getTeam(gamer) == null)
                        assignTeam(gamer);
                }
                gamePreStart();
                return;
            }

            collectingPlayersCountdown--;
            if (collectingPlayersCountdown > 0) {
                String[] args = new String[]{
                        "%cooldown%", String.valueOf(collectingPlayersCountdown + preStartCountdown),
                        "%current_players%", String.valueOf(gamers.size()),
                        "%max_players%", String.valueOf(getMaxGamers())};
                SoundInfo tick = Configurations.getCollectingPlayersCooldownTickSound();
                for (Player player : getGamers()) {
                    getMinigameType().COLLECTINGPLAYERS_COOLDOWN_BAR.sendActionBar(player, args);
                    tick.play(player);
                }
                return;
            }
            phase = Phase.PRE_START;
            for (Player gamer : getGamers()) {
                if (getTeam(gamer) == null)
                    assignTeam(gamer);
            }

            gamePreStart();
            return;
        }
        collectingPlayersCountdown = getOption().getCollectingPlayersPhaseCooldownMax();
        String[] args = new String[]{
                "%current_players%", String.valueOf(getGamers().size()),
                "%max_players%", String.valueOf(getMaxGamers())};
        getGamers().forEach(player -> getMinigameType().COLLECTINGPLAYERS_NO_COOLDOWN_BAR.sendActionBar(player, args));
    }

    @Override
    public void gamePreStart() {
        MessageUtil.debug(getId() + " gamePreStart");
        if (phase != Phase.PRE_START)
            throw new IllegalStateException();
        preStartCountdown = getOption().getPreStartPhaseCooldownMax();
        for (Player gamer : gamers)
            teleportResetLocation(gamer);
    }

    @Override
    public void gamePreStartTimer() {
        preStartCountdown--;
        String[] args = new String[]{"%cooldown%", String.valueOf(preStartCountdown)};
        SoundInfo tick = Configurations.getPreStartPhaseCooldownTickSound();
        for (Player player : getGamers()) {
            getMinigameType().PRESTART_COOLDOWN_BAR.sendActionBar(player, args);
            getMinigameType().PRESTART_COOLDOWN_BAR.sendAsSubTitle(player, 0, 25, 0, args);
            tick.play(player);
        }
        if (preStartCountdown <= 0) {
            if (!gameCanStart()) { //some idiot disconnected //TODO back to Collecting Players (?)
                this.gameAbort(); //TODO here!!
                this.restart();
                return;
            }
            getGamers().forEach(SimpleMessage::sendEmptyActionBarMessage);
            getGamers().forEach(SimpleMessage::clearTitle);
            phase = Phase.PLAYING;
            gameStart();
        }
    }

    private final HashSet<String> scores = new HashSet<>();

    @Override
    public void setScore(String score, int value) {
        objective.getScore(score).setScore(value);
        scores.add(score);
    }


    @Override
    public int getScore(String score) {
        return objective.getScore(score).getScore();
    }

    @Override
    public void resetScore(String score) {
        scoreboard.resetScores(score);
        scores.remove(score);
    }

    @Override
    public void resetScores() {
        for (String score : scores)
            scoreboard.resetScores(score);
    }

    @Override
    public void gameStart() {
        MessageUtil.debug(getId() + " gameStart");
        if (phase != Phase.PLAYING)
            throw new IllegalStateException();
        for (Player player : getGamers()) {
            getMinigameType().GAME_START_MESSAGE.send(player);
            getMinigameType().applyDefaultPlayerSnapshot(player); //TODO again()
        }
        VaultEconomyHandler ecoHandler = new VaultEconomyHandler();
        for (Player player : getGamers()) {
            if (kitPreference.containsKey(player)) {
                Kit kit = KitManager.get().get(kitPreference.get(player));
                if (kit == null) {
                    //TODO apply default kit
                } else {
                    if (kit.getPrice() == 0 || ecoHandler.removeMoney(player, kit.getPrice())) {
                        kit.apply(player);
                    } else {
                        //TODO not enough money
                        //TODO apply default kit
                    }
                }
            } else {
                //TODO apply default kit
            }
            player.closeInventory();
        }
        //TODO notify game started?
    }

    @Override
    public void gamePlayingTimer() {
        //MessageUtil.debug(  getId() + " gamePlayingTimer");
        //TODO cooldown force end
    }

    @Override
    public void gameEnd() {
        MessageUtil.debug(getId() + " gameEnd");
        if (this.phase != Phase.PLAYING)
            throw new IllegalStateException();
        getGamers().forEach(player -> getMinigameType().GAME_END_MESSAGE.send(player));
        endCountdown = getOption().getEndPhaseCooldownMax();
        resetScores();
        this.phase = Phase.END;
    }

    @Override
    public void gameEndTimer() {
        if (endCountdown >= 1) {
            for (Player player : getGamers()) {
                Firework fire = (Firework) player.getLocation().getWorld().spawnEntity(player.getLocation()
                        .add(Math.random() * 6 - 3, 2, Math.random() * 6 - 3), EntityType.FIREWORK);
                FireworkMeta meta = fire.getFireworkMeta();
                meta.addEffect(FireworkEffect.builder().withColor(
                        Color.fromBGR((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256)),
                        Color.fromBGR((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256))).build());
                fire.setFireworkMeta(meta);
                Bukkit.getScheduler().runTaskLater(Minigames.get(), fire::detonate, 2L);
                String[] args = new String[]{"%cooldown%", String.valueOf(endCountdown)};
                getMinigameType().END_COOLDOWN_BAR.sendActionBar(player, args);
            }
            endCountdown--;
        }
        if (endCountdown <= 0)
            gameClose();
    }

    @Override
    public void gameClose() {
        MessageUtil.debug(getId() + " gameClose");
        new HashSet<>(getSpectators()).forEach(spectator -> GameManager.get().quitGame(spectator));
        new HashSet<>(getGamers()).forEach(player -> GameManager.get().quitGame(player));
        //timer.cancel();
        phase = Phase.RESTART;
        registerAsListener(false);
        restart();
    }

    @Override
    public void gameAbort() {
        MessageUtil.debug(getId() + " gameAbort");
        for (Player spectator : new HashSet<>(getSpectators())) { //bad
            GameManager.get().quitGame(spectator);
            getMinigameType().GAME_INTERRUPTED_MESSAGE.send(spectator);
        }
        for (Player player : new HashSet<>(getGamers())) {//bad
            GameManager.get().quitGame(player);
            getMinigameType().GAME_INTERRUPTED_MESSAGE.send(player);
        }
        if (phase == Phase.STOPPED)
            return;
        phase = Phase.STOPPED;
        //timer.cancel();
        registerAsListener(false);
    }

    private boolean registered = false;
    private final GameListener gameListener = new GameListener(this);

    private void registerAsListener(boolean value) {
        if (value) {
            if (!registered) {
                Minigames.get().registerListener(this);
                Minigames.get().registerListener(gameListener);
                registered = !registered;
                MessageUtil.debug(getId() + " registered listener");
            }
        } else {
            if (registered) {
                Minigames.get().unregisterListener(this);
                Minigames.get().unregisterListener(gameListener);
                registered = !registered;
                MessageUtil.debug(getId() + " UNregistered listener");
            }
        }
    }

    @Override
    public @NotNull Phase getPhase() {
        return phase;
    }

    @Override
    public @NotNull O getOption() {
        return option;
    }

    @Override
    public @NotNull A getArena() {
        return arena;
    }

    @Override
    public @NotNull BlockLocation3D getGameLocation() {
        return loc;
    }

    @Override
    public @NotNull Set<Player> getGamers() {
        return Collections.unmodifiableSet(gamers);
    }

    @Override
    public boolean isGamer(@NotNull Player player) {
        return gamers.contains(player);
    }

    @Override
    public final boolean addGamer(@NotNull Player player) {
        return switch (getPhase()) {
            case COLLECTING_PLAYERS, PRE_START, PLAYING -> {
                if (!canAddGamer(player))
                    yield false;
                if (gamers.add(player)) {
                    onGamerAdded(player);
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    @Override
    public void onGamerAdded(@NotNull Player player) {
        resetGamer(player);
        for (Player spectator : getSpectators())
            player.hidePlayer(Minigames.get(), spectator);
    }

    public void resetGamer(@NotNull Player player) {
        switch (getPhase()) {
            case COLLECTING_PLAYERS -> {
                teleportResetLocation(player);
                Configurations.applyGameCollectingPlayersSnapshot(player);
                player.getInventory().setHeldItemSlot(4);
                if (getOption().allowSelectingTeam()) //only if you can choose a kit
                    player.getInventory().setItem(2, Configurations.getTeamSelectorItem(player));
                if (getOption() instanceof MOptionWithKitsChoice opt && opt.getKits().size() > 0) //only if you can choose a kit
                    player.getInventory().setItem(4, Configurations.getKitSelectorItem(player));
                player.getInventory().setItem(8, Configurations.getGameLeaveItem(player));
            }
            case PRE_START -> {
                assignTeam(player);
                //getScoreboard().getTeam(getTeam(player).getColor().name()).addEntry(player.getName());
                teleportResetLocation(player);
                Configurations.applyGamePreStartSnapshot(player);
                player.getInventory().setHeldItemSlot(4);
                if (getOption() instanceof MOptionWithKitsChoice opt && opt.getKits().size() > 0) //only if you can choose a kit
                    player.getInventory().setItem(4, Configurations.getKitSelectorItem(player));
                player.getInventory().setItem(8, Configurations.getGameLeaveItem(player));
            }
            case PLAYING -> {
                assignTeam(player);
                //getScoreboard().getTeam(getTeam(player).getColor().name()).addEntry(player.getName());
                teleportResetLocation(player);
                Configurations.applyGameEmptyStartSnapshot(player);
                //TODO assign kit
            }
        }
    }

    @Override
    public boolean canAddGamer(@NotNull Player player) {
        if (getMaxGamers() <= gamers.size())
            return false;
        return !gamers.contains(player);
    }


    @Override
    public boolean removeGamer(@NotNull Player player) {
        if (gamers.remove(player)) {
            onGamerRemoved(player);
            return true;
        }
        return false;
    }


    /**
     * Assign a party to user or throw an exception
     */
    @Deprecated
    protected abstract void assignTeam(Player p);

    private final Set<Player> spectators = new HashSet<>();

    public final boolean switchToSpectator(@NotNull Player player) {
        if (canSwitchToSpectator(player) && removeGamer(player) && spectators.add(player)) {
            onSpectatorAdded(player);
            return true;
        }
        return false;
    }

    @Override
    public final boolean addSpectator(@NotNull Player player) {
        if (getPhase() != Phase.PLAYING)
            return false;
        if (isGamer(player)) {
            if (canSwitchToSpectator(player) && switchToSpectator(player)) {
                onSpectatorAdded(player);
                return true;
            }
            return false;
        }
        if (canAddSpectator(player) && spectators.add(player)) {
            onSpectatorAdded(player);
            return true;
        }
        return false;
    }

    @Override
    public void onSpectatorRemoved(@NotNull Player player) {
        for (Player gamer : getGamers())
            gamer.showPlayer(Minigames.get(), player);
        for (Player spectator : getSpectators()) {
            spectator.showPlayer(Minigames.get(), player);
            player.showPlayer(Minigames.get(), spectator);
        }
    }


    @Override
    public void onGamerRemoved(@NotNull Player player) {
        for (Player spectator : getSpectators())
            player.showPlayer(Minigames.get(), spectator);
    }

    @Override
    public final boolean removeSpectator(@NotNull Player player) {
        if (spectators.remove(player)) {
            onSpectatorRemoved(player);
            return true;
        }
        return false;
    }

    @Override
    public final boolean isSpectator(@NotNull Player player) {
        return spectators.contains(player);
    }

    @Override
    @NotNull
    public final Set<Player> getSpectators() {
        return Collections.unmodifiableSet(spectators);
    }

    private final Set<Player> gamers = new HashSet<>();

    @Override
    public final boolean joinGameAsSpectator(@NotNull Player player) {
        return addSpectator(player);
    }

    @Override
    @Nullable
    public World getWorld() {
        return this.loc == null ? null : this.loc.getWorld();
    }

    @Override
    public void onGamerPvpDamage(@NotNull EntityDamageByEntityEvent event, @NotNull Player p, @NotNull Player damager, boolean directDamage) {
        if (p.equals(damager)) //TODO cant arrowhit himself
            return;
        if (event.getDamager() instanceof Player && Objects.equals(getTeam(p), getTeam(damager))) //TODO flag?
            event.setCancelled(true);
    }

    /**
     * when an entity inside game (not a player) is damaged
     */
    @Override
    public abstract void onGameEntityDamaged(@NotNull EntityDamageEvent event);

    /**
     * Called when a creature spawns inside the arena
     */
    @Override
    public abstract void onCreatureSpawn(@NotNull CreatureSpawnEvent event);

    /**
     * Called when an entity (not a player) die inside the arena
     */
    @Override
    public abstract void onEntityDeath(@NotNull EntityDeathEvent event);

    /**
     * called when a playing player heals
     */
    @Override
    public abstract void onGamerRegainHealth(@NotNull EntityRegainHealthEvent event, @NotNull Player player);

    /**
     * called when an entity, not a player heals inside the arena
     */
    @Override
    public abstract void onEntityRegainHealth(@NotNull EntityRegainHealthEvent event);

    /**
     * Handle block break done by one of playingPlayers
     */
    @Override
    public void onGamerBlockBreak(@NotNull BlockBreakEvent event) {
        if (getPhase() != Phase.PLAYING && getPhase() != Phase.END)
            event.setCancelled(true);
    }

    /**
     * Handle block place done by one of playingPlayers
     */
    @Override
    public void onGamerBlockPlace(@NotNull BlockPlaceEvent event) {
        if (getPhase() != Phase.PLAYING && getPhase() != Phase.END)
            event.setCancelled(true);
    }

    /**
     * Handle portal creation inside arena
     */
    public void onPortalCreate(@NotNull PortalCreateEvent event) {
        event.setCancelled(true);
    }

    @Override
    public boolean canAddSpectator(@NotNull Player player) {
        return getOption().getAllowSpectators() && switch (getPhase()) {
            case PLAYING, END -> true;
            default -> false;
        };
    }

    @Override
    public void onSpectatorAdded(@NotNull Player player) {
        Configurations.applyGameSpectatorSnapshot(player);
        for (Player gamer : getGamers())
            gamer.hidePlayer(Minigames.get(), player);
        for (Player spectator : getSpectators()) {
            spectator.showPlayer(Minigames.get(), player);
            player.showPlayer(Minigames.get(), spectator);
        }
        //TODO clock
        player.getInventory().setItem(8, Configurations.getGameLeaveItem(player));
    }

    /**
     * should also
     */
    @Override
    public void onGamerMoveInsideArena(@NotNull PlayerMoveEvent event) {
        if (getPhase() == Phase.PRE_START && event.getFrom().distanceSquared(event.getTo()) > 0)
            event.setCancelled(true);
    }

    @Override
    public void onGamerPickupItem(@NotNull EntityPickupItemEvent event, @NotNull Player p) {
        if (getPhase() != Phase.PLAYING && getPhase() != Phase.END)
            event.setCancelled(true);
    }

    public void onGamerDropItem(@NotNull PlayerDropItemEvent event) {
        if (getPhase() != Phase.PLAYING && getPhase() != Phase.END)
            event.setCancelled(true);
    }

    public void onGamerLaunchProjectile(@NotNull ProjectileLaunchEvent event) {
        if (getPhase() != Phase.PLAYING && getPhase() != Phase.END)
            event.setCancelled(true);
    }

    public void onGamerDamaged(@NotNull EntityDamageEvent event, @NotNull Player hitPlayer) {
        if (getPhase() != Phase.PLAYING)
            event.setCancelled(true);
    }


    /**
     * Handle any player interaction with entities inside arena
     */
    public void onGamerInteractEntity(@NotNull PlayerInteractEntityEvent event) {
        if (getPhase() != Phase.PLAYING && getPhase() != Phase.END)
            event.setCancelled(true);
    }

    /**
     * Handle any player interaction inside arena
     */
    @Override
    public void onGamerInteract(@NotNull PlayerInteractEvent event) {
        if (getPhase() != Phase.PLAYING && getPhase() != Phase.END)
            event.setCancelled(true);
        if (getPhase() == Phase.COLLECTING_PLAYERS || getPhase() == Phase.PRE_START)
            switch (event.getPlayer().getInventory().getHeldItemSlot()) {
                case 4 -> {
                    if (getOption() instanceof MOptionWithKitsChoice opt && opt.getKits().size() > 0)
                        getKitSelectorGui(event.getPlayer()).open(event.getPlayer());
                }
                case 2 -> {
                    if (getPhase() == Phase.COLLECTING_PLAYERS && getOption().allowSelectingTeam())
                        getTeamSelectorGui(event.getPlayer()).open(event.getPlayer());
                }
                case 8 -> GameManager.get().quitGame(event.getPlayer());
            }
    }

    private @NotNull Gui getTeamSelectorGui(@NotNull Player player) {
        PagedListFGui<T> gui = new PagedListFGui<>(
                new DMessage(getMinigameType().getPlugin(), player).appendLang("generic.gui.teamselector_title").toLegacy(), 3,
                player, null, Minigames.get(), false,
                (evt, team) -> {
                    if (team.containsUser(player))
                        onGamerLeaveTeam(player, team);
                    else
                        onGamerChooseTeam(player, team);
                    return true;
                }, (team) -> Configurations.getTeamItem(player, team));
        gui.addElements(getTeams());
        gui.updateInventory();
        return gui;
    }

    protected boolean onGamerChooseTeam(@NotNull OfflinePlayer player, @NotNull T team) {
        T pTeam = getTeam(player);
        if (team == pTeam)
            return true;
        if (pTeam != null)
            pTeam.removeUser(player);
        return team.addUser(player);
    }

    protected boolean onGamerLeaveTeam(@NotNull OfflinePlayer player, @NotNull T team) {
        return team.removeUser(player);
    }

    @Override
    public void onSpectatorInteract(@NotNull PlayerInteractEvent event) {
        event.setCancelled(true);
        //TODO clock
        if (event.getPlayer().getInventory().getHeldItemSlot() == 8) //only if you can choose a kit
            GameManager.get().quitGame(event.getPlayer());
    }

    private PagedListFGui<Kit> getKitSelectorGui(Player player) {
        PagedListFGui<Kit> gui = new PagedListFGui<>(
                new DMessage(getMinigameType().getPlugin(), player).appendLang("generic.gui.kitselector_title").toLegacy(), 3,
                player, null, Minigames.get(), true,
                (evt, kit) -> {
                    MessageUtil.debug(player.getName() + " selected kit " + kit.getId());
                    if (kit.getId().equals(kitPreference.get(player)))
                        kitPreference.remove(player);
                    else
                        kitPreference.put(player, kit.getId());
                    return true;
                }, (kit) -> kit.getGuiSelectorItem(player).addEnchantment(Enchantment.DURABILITY,
                kit.getId()
                        .equals(kitPreference.get(player)) ? 1 : 0).build());
        if (getOption() instanceof MOptionWithKitsChoice opt)
            gui.addElements(opt.getKits());
        gui.updateInventory();
        return gui;
    }

    public void onGamerInventoryClick(@NotNull InventoryClickEvent event, @NotNull Player player) {
        switch (getPhase()) {
            case COLLECTING_PLAYERS, PRE_START -> {
                if (!(event.getView().getTopInventory().getHolder() instanceof Gui))
                    event.setCancelled(true);
            }
        }
    }

    public void onGamerSwapHandItems(@NotNull PlayerSwapHandItemsEvent event) {
        switch (getPhase()) {
            case COLLECTING_PLAYERS, PRE_START -> event.setCancelled(true);
        }
    }

    protected void givePoints(Player target,double amount) {
        if (amount > 0) {
            try {
                new VaultEconomyHandler().addMoney(target, amount);
            } catch (Exception e){
                e.printStackTrace();
            }
            new DMessage(Minigames.get(), target).appendLang("generic.obtain_points", "%amount%", UtilsString.formatOptional2Digit(amount)).send();
        }
    }

    protected void sendMsg(CommandSender target, String path, String... holders) {
        new DMessage(getPlugin(), target).appendLang(path, holders).send();
    }

    protected void sendMsgList(CommandSender target, String path, String... holders) {
        new DMessage(getPlugin(), target).appendLangList(path, holders).send();
    }

}
