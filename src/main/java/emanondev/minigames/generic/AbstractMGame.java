package emanondev.minigames.generic;

import emanondev.core.MessageBuilder;
import emanondev.core.SoundInfo;
import emanondev.core.UtilsMessages;
import emanondev.core.UtilsString;
import emanondev.minigames.*;
import emanondev.minigames.locations.BlockLocation3D;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractMGame<T extends ColoredTeam, A extends MArena, O extends MOption> implements MGame<T, A, O>, Listener {

    private int collectingPlayersCountdown = -1;
    private int endCountdown = -1;
    private int prestartCountdown = -1;

    private BukkitTask timer;
    private final HashSet<Player> collectedPlayers = new HashSet<>();
    private BlockLocation3D loc;
    private Phase phase = Phase.STOPPED;

    private final O option;
    private final A arena;
    private final String optionId;
    private final String arenaId;

    public AbstractMGame(@NotNull Map<String, Object> map) {
        this.optionId = (String) map.get("option");
        this.option = (O) OptionManager.get().getOption(optionId);
        //Minigames.get().logTetraStar(ChatColor.DARK_RED,"D &e"+optionId+" Null Option: &e"+(option==null));
        this.arenaId = (String) map.get("arena");
        this.arena = (A) ArenaManager.get().getArena(arenaId);

        //Minigames.get().logTetraStar(ChatColor.DARK_RED,"D &e"+arenaId+" Null Arena: &e"+(arena==null));
        if (this.arena == null || this.option == null)
            throw new NullPointerException();
        try {
            String textLoc = (String) map.get("location");
            if (textLoc != null)
                this.loc = BlockLocation3D.fromString(textLoc);
            if (loc == null)
                loc = GameManager.get().generateLocation(arena, getWorld());
        } catch (Exception e) {
            e.printStackTrace();
            loc = GameManager.get().generateLocation(arena, getWorld());
        }
    }

    public void setLocation(@NotNull BlockLocation3D loc) {
        boolean wasStopped = getPhase() != Phase.STOPPED;
        if (!wasStopped)
            this.gameAbort();
        this.loc = loc;
        if (!wasStopped)
            this.gameInitialize();
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
    public void gameInitialize() {
        MessageUtil.debug(getId() + " gameInizialize");
        if (phase != Phase.STOPPED)
            throw new IllegalStateException();

        phase = Phase.RESTART;
        gameRestart();
    }

    @Override
    public void gameRestart() {
        MessageUtil.debug(getId() + " gameRestart");
        if (phase != Phase.RESTART)
            throw new IllegalStateException();

        for (T team : getTeams())
            team.clear();
        timer = new BukkitRunnable() {
            public void run() {
                switch (phase) {
                    case END -> gameEndTimer();
                    case PLAYING -> gamePlayingTimer();
                    case COLLECTING_PLAYERS -> gameCollectingPlayersTimer();
                    case PRE_START -> gamePreStartTimer();
                    case STOPPED -> {
                        new IllegalStateException().printStackTrace();
                        this.cancel();
                    }
                    default -> new IllegalStateException().printStackTrace();
                }
            }
        }.runTaskTimer(Minigames.get(), 20L, 20L);
        phase = Phase.COLLECTING_PLAYERS;
    }

    @Override
    public void gameCollectingPlayersTimer() {
        //Minigames.get().logTetraStar(ChatColor.DARK_RED,"D "+getId()+" gameCollectingPlayersTimer");
        if (phase != Phase.COLLECTING_PLAYERS)
            throw new IllegalStateException();
        //Minigames.get().logTetraStar(ChatColor.DARK_RED,"D "+getId()+" canprestart? "+gameCanPreStart()+" countdown "+collectingPlayersCountdown);

        if (this.gameCanPreStart()) {
            if (AbstractMGame.this.getCollectedPlayers().size() == AbstractMGame.this.getMaxPlayers()) {
                //fast start
                phase = Phase.PRE_START;
                gamePreStart();
                return;
            }
            if (collectingPlayersCountdown == -1)
                collectingPlayersCountdown = getOption().getCollectingPlayersPhaseCooldownMax();
            else
                collectingPlayersCountdown--;

            if (collectingPlayersCountdown <= 0) {
                phase = Phase.PRE_START;
                gamePreStart();
                return;
            } else { //>0
                String[] args = new String[]{"%cooldown%", String.valueOf(collectingPlayersCountdown),
                        "%current_players%", String.valueOf(getCollectedPlayers().size()),
                        "%max_players%", String.valueOf(getMaxPlayers())};
                SoundInfo timerTickSound = Minigames.get().getConfig("sounds").loadSoundInfo(
                        "collecting_players_cooldown_tick", new SoundInfo(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 5, true));
                for (Player player : getCollectedPlayers()) {
                    UtilsMessages.sendActionbar(player, Minigames.get().getLanguageConfig(player)
                            .loadMessage(getMinigameType().getType() + ".game.collectingplayers_cooldown_bar", "",
                                    args));
                    timerTickSound.play(player);
                }
                return;
            }
        }
        collectingPlayersCountdown = -1;
        String[] args = new String[]{
                "%current_players%", String.valueOf(getCollectedPlayers().size()),
                "%max_players%", String.valueOf(getMaxPlayers())};
        for (Player player : getCollectedPlayers()) {
            UtilsMessages.sendActionbar(player, Minigames.get().getLanguageConfig(player)
                    .loadMessage(getMinigameType().getType() + ".game.collectingplayers_no_cooldown_bar", "",
                            args));
        }

    }

    @Override
    public void gamePreStart() {
        MessageUtil.debug(getId() + " gamePreStart");
        if (phase != Phase.PRE_START)
            throw new IllegalStateException();
        prestartCountdown = getOption().getPreStartPhaseCooldownMax();
        registerAsListener(true);
    }

    @Override
    public void gamePreStartTimer() {
        prestartCountdown--;
        String[] args = new String[]{"%cooldown%", String.valueOf(prestartCountdown)};
        SoundInfo timerTickSound = Minigames.get().getConfig("sounds").loadSoundInfo(
                "prestart_cooldown_tick", new SoundInfo(Sound.ENTITY_PLAYER_LEVELUP, 1, 5, true));
        for (Player player : getPlayingPlayers()) {
            UtilsMessages.sendActionbar(player, Minigames.get().getLanguageConfig(player)
                    .loadMessage(getMinigameType().getType() + ".game.prestart_cooldown_bar", "",
                            args));
            timerTickSound.play(player);
        }
        if (prestartCountdown <= 0) {
            if (!gameCanStart()) { //some idiot disconnected
                this.gameAbort();
                this.gameRestart();
                return;
            }
            phase = Phase.PLAYING;
            gameStart();
        }
    }

    @Override
    public void gameStart() {
        MessageUtil.debug(getId() + " gameStart");
        if (phase != Phase.PLAYING)
            throw new IllegalStateException();
        for (Player p : getPlayingPlayers()) {
            new MessageBuilder(Minigames.get(), p)
                    .addTextTranslation("skywars.game.game_start", "").send();
            getMinigameType().applyDefaultPlayerSnapshot(p); //TODO again()
        }
        //TODO apply kits
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
        for (Player p : getPlayingPlayers())
            new MessageBuilder(Minigames.get(), p)
                    .addTextTranslation("skywars.game.game_end", "").send();
        endCountdown = getOption().getEndPhaseCooldownMax();
        this.phase = Phase.END;
    }

    @Override
    public void gameEndTimer() {
        //MessageUtil.debug(  getId() + " gameEndTimer");
        //TODO celebrate?

        if (endCountdown >= 1)
            for (Player p : getPlayingPlayers()) {
                Firework fire = (Firework) p.getLocation().getWorld().spawnEntity(p.getLocation().add(Math.random() * 6 - 3, 2, Math.random() * 6 - 3), EntityType.FIREWORK);
                FireworkMeta meta = fire.getFireworkMeta();
                meta.addEffect(FireworkEffect.builder().withColor(
                        Color.fromBGR((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256))
                        ,
                        Color.fromBGR((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256))
                ).build());
                fire.setFireworkMeta(meta);
                Bukkit.getScheduler().runTaskLater(Minigames.get(), fire::detonate, 2L);
                String[] args = new String[]{"%cooldown%", String.valueOf(prestartCountdown)};
                UtilsMessages.sendActionbar(p, Minigames.get().getLanguageConfig(p)
                        .loadMessage(getMinigameType().getType() + ".game.end_cooldown_bar", "",
                                args));

            }
        if (endCountdown == -1)
            endCountdown = getOption().getEndPhaseCooldownMax();
        else
            endCountdown = endCountdown - 1;

        if (endCountdown <= 0) {
            gameClose();
        }
    }

    @Override
    public void gameClose() {
        MessageUtil.debug(getId() + " gameClose");
        for (Player spectator : new HashSet<>(getSpectators())) //bad
            GameManager.get().quitGame(spectator);
        for (Player player : new HashSet<>(getPlayingPlayers())) //bad
            GameManager.get().quitGame(player);
        phase = Phase.RESTART;
        timer.cancel();
        registerAsListener(false);
        gameRestart();
    }

    @Override
    public void gameAbort() {
        MessageUtil.debug(getId() + " gameAbort");
        for (Player spectator : new HashSet<>(getSpectators())) { //bad
            GameManager.get().quitGame(spectator);
            MessageUtil.sendMessage(spectator, "generic.game.game_interrupted");
        }
        for (Player player : new HashSet<>(getPlayingPlayers())) {//bad
            GameManager.get().quitGame(player);
            MessageUtil.sendMessage(player, "generic.game.game_interrupted");
        }
        for (Player player : new HashSet<>(getCollectedPlayers())) { //bad
            GameManager.get().quitGame(player);
            MessageUtil.sendMessage(player, "generic.game.game_interrupted");
        }
        if (phase == Phase.STOPPED)
            return;
        phase = Phase.STOPPED;
        timer.cancel();
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
    public @NotNull Set<Player> getCollectedPlayers() {
        return Collections.unmodifiableSet(collectedPlayers);
    }

    @Override
    public boolean isCollectedPlayer(@NotNull Player player) {
        return phase == Phase.COLLECTING_PLAYERS && collectedPlayers.contains(player);
    }

    @Override
    public final boolean addCollectedPlayer(@NotNull Player player) {
        if (!canAddCollectedPlayer(player))
            return false;
        return collectedPlayers.add(player);
    }

    @Override
    public boolean canAddCollectedPlayer(@NotNull Player player) {
        if (getMaxPlayers() <= collectedPlayers.size() || getPhase() != Phase.COLLECTING_PLAYERS)
            return false;
        return !collectedPlayers.contains(player);
    }


    @Override
    public boolean removeCollectedPlayer(@NotNull Player player) {
        return collectedPlayers.remove(player);
    }

    /**
     * Assign a party to user or throw an exception
     */
    protected abstract void assignTeam(Player p);

    private final Set<Player> spectators = new HashSet<>();

    @NotNull
    public final boolean switchToSpectator(@NotNull Player player) {
        if (!isPlayingPlayer(player))
            return false;
        if (canSwitchToSpectator(player) && !isSpectator(player)) {
            onQuitGame(player);
            spectators.add(player);
            onSpectatorAdded(player);
            return true;
        }
        return false;
    }

    @Override
    public abstract boolean canSwitchToSpectator(Player player);

    @Override
    public final boolean addSpectator(@NotNull Player player) {
        if (isPlayingPlayer(player)) {
            return switchToSpectator(player);
        }
        if (canAddSpectator(player) && !isSpectator(player)) {
            spectators.add(player);
            onSpectatorAdded(player);
            return true;
        }
        return false;
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
    public final @NotNull Set<Player> getSpectators() {
        return Collections.unmodifiableSet(spectators);
    }

    private final Set<Player> playing = new HashSet<>();

    @Override
    public boolean addPlayingPlayer(@NotNull Player player) {
        if (canAddPlayingPlayer(player) && playing.add(player)) {
            collectedPlayers.remove(player);
            assignTeam(player);
            onPlayingPlayerAdded(player);
            return true;
        }
        return false;
    }


    @Override
    public final boolean joinGameAsSpectator(Player player) {
        return addSpectator(player);
    }

    protected boolean canAddPlayingPlayer(Player player) {
        return collectedPlayers.contains(player);
    }

    @Override
    public final boolean removePlayingPlayer(@NotNull Player player) {
        if (playing.remove(player)) {
            onPlayingPlayerRemoved(player);
            if (getPhase() == Phase.PLAYING)
                checkGameEnd();
            return true;
        }
        return false;
    }

    /**
     * check if the game has finished, if has should calls for onGameEnd();
     * <p>
     * getPhase()==Phase.PLAYING may be a double security check
     */
    protected abstract void checkGameEnd();

    @Override
    public final boolean isPlayingPlayer(@NotNull Player player) {
        return playing.contains(player);
    }

    @Override
    public final @NotNull Set<Player> getPlayingPlayers() {
        return Collections.unmodifiableSet(playing);
    }

    @Override
    public @Nullable World getWorld() {
        return this.loc == null ? null : this.loc.getWorld();
    }

    private String id = null;

    public final boolean isRegistered() {
        return id != null;
    }

    public final void setRegistered(@NotNull String id) {
        if (!UtilsString.isLowcasedValidID(id))
            throw new IllegalStateException();
        this.id = id;
    }

    public final String getId() {
        return id;
    }

    public final void setUnregister() {
        id = null;
    }

    public void onPlayingPlayerPvpDamage(EntityDamageByEntityEvent event, Player p, Player damager) {
        if (p.equals(damager))
            return;
        if (event.getDamager() instanceof Player && getTeam(p).equals(getTeam(damager)))
            event.setCancelled(true);
    }

    public abstract void onPlayingPlayerDamaging(EntityDamageByEntityEvent event, Player damager);

    /**
     * when an entity inside game (not a player) is damaged
     *
     * @param event
     */
    public abstract void onGameEntityDamaged(EntityDamageEvent event);

    /**
     * Called when a creature spawns inside the arena
     *
     * @param event
     */
    public abstract void onCreatureSpawn(@NotNull CreatureSpawnEvent event);

    /**
     * Called when an entity (not a player) die inside the arena
     *
     * @param event
     */
    public abstract void onEntityDeath(@NotNull EntityDeathEvent event);

    /**
     * Called when playing player die inside the arena
     * <p>
     * the player do not die as the final damage is cancelled, but you should handle this instead
     * this may be called as result of player quitting the game, quitting the arena or being killed, falling below the arena
     */
    public abstract void onFakePlayingPlayerDeath(@NotNull Player dead, @Nullable Player killer);

    /**
     * called when a playing player heals
     *
     * @param event
     * @param player
     */
    public void onPlayingPlayerRegainHealth(@NotNull EntityRegainHealthEvent event, @NotNull Player player) {
    }

    /**
     * called when an entity, not a player heals inside the arena
     *
     * @param event
     */
    public void onEntityRegainHealth(@NotNull EntityRegainHealthEvent event) {
    }

    /**
     * Handle block break done by one of playingPlayers
     */
    public abstract void onPlayingPlayerBlockBreak(@NotNull BlockBreakEvent event);

    /**
     * Handle block place done by one of playingPlayers
     */
    public abstract void onPlayingPlayerBlockPlace(@NotNull BlockPlaceEvent event);

    /**
     * Handle portal creation inside arena
     */
    public void onPortalCreate(@NotNull PortalCreateEvent event) {
        event.setCancelled(true);
    }


    protected boolean canAddSpectator(Player player) {
        return switch (getPhase()) {
            case PRE_START, PLAYING, END -> true; //TODO allowSpectators()
            default -> false;
        };
    }

    protected void onSpectatorAdded(@NotNull Player player) {
        Configurations.applyGameSpectatorSnapshot(player);
    }

    protected void onSpectatorRemoved(@NotNull Player player) {
        //TODO
    }

    protected void onPlayingPlayerRemoved(@NotNull Player player) {
        //TODO
    }

    protected void onPlayingPlayerAdded(@NotNull Player player) {
        //TODO
    }


    /**
     * note the event is always cancelled
     *
     * @param event
     */
    public abstract void onPlayingPlayerMoveOutsideArena(PlayerMoveEvent event);

    /**
     * should also
     *
     * @param event
     */
    public void onPlayingPlayerMove(PlayerMoveEvent event) {
        if (getPhase() == Phase.PRE_START && event.getFrom().distanceSquared(event.getTo()) > 0) {
            event.setCancelled(true);
        }
    }


    public abstract void onPlayingPlayerTeleport(PlayerTeleportEvent event);


    public void onPlayingPlayerPickupItem(EntityPickupItemEvent event, Player p) {
        if (getPhase() == Phase.PRE_START)
            event.setCancelled(true);
    }

    public void onPlayingPlayerDropItem(PlayerDropItemEvent event) {
        if (getPhase() == Phase.PRE_START)
            event.setCancelled(true);
    }

    public void onPlayingPlayerLaunchProjectile(ProjectileLaunchEvent event) {
        if (getPhase() == Phase.PRE_START)
            event.setCancelled(true);
    }

    public void onPlayingPlayerDamaged(@NotNull EntityDamageEvent event, @NotNull Player p) {
        if (getPhase() != Phase.PLAYING)
            event.setCancelled(true);
    }


    /**
     * Handle any player interaction with entities inside arena
     */
    public void onPlayingPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (getPhase() == Phase.PRE_START)
            event.setCancelled(true);
    }

    /**
     * Handle any player interaction inside arena
     */
    public void onPlayingPlayerInteract(PlayerInteractEvent event) {
        if (getPhase() == Phase.PRE_START)
            event.setCancelled(true);
    }

}
