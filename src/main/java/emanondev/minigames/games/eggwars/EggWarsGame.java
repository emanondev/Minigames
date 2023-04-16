package emanondev.minigames.games.eggwars;

import emanondev.core.UtilsInventory;
import emanondev.minigames.MessageUtil;
import emanondev.minigames.MinigameTypes;
import emanondev.minigames.data.GameStat;
import emanondev.minigames.data.PlayerStat;
import emanondev.minigames.event.eggwars.EggWarsStartEvent;
import emanondev.minigames.games.AbstractMColorSchemGame;
import emanondev.minigames.games.ColoredTeam;
import emanondev.minigames.games.DropsFiller;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EggWarsGame extends AbstractMColorSchemGame<EggWarsTeam, EggWarsArena, EggWarsOption> {

    private final HashSet<Block> ignoredChest = new HashSet<>();
    private final HashSet<Block> filledChests = new HashSet<>();

    @Override
    protected void craftAndCallGameStartEvent() {
        Bukkit.getPluginManager().callEvent(new EggWarsStartEvent(this));
    }

    @Override
    public void gamePreStart() {
        //MessageUtil.debug(  getId() + " gamePREPreStart");
        //TODO assignTeams!
        List<Player> list = new ArrayList<>(this.getGamers());
        Collections.shuffle(list);
        list.forEach(this::onGamerAdded);

        ignoredChest.clear();
        filledChests.clear();

        super.gamePreStart();
    }

    public void gameStart() {
        super.gameStart();
        for (Player player : getGamers()) {
            PlayerStat.SKYWARS_PLAYED.add(player, 1);
            PlayerStat.GAME_PLAYED.add(player, 1);
        }
        GameStat.PLAY_TIMES.add(this, 1);
        getTeams().forEach(team -> {
            if (!team.hasLost()) setScore(team.getName(), 0);
        });
    }

    /**
     * Assign a party to user or throw an exception
     */
    @Deprecated
    public void assignTeam(@NotNull Player player) {//TODO choose how to fill with options
        if (getTeam(player) != null)
            return;
        MessageUtil.debug(getId() + " assigning team to " + player.getName());
        List<EggWarsTeam> teams = new ArrayList<>(getTeams());
        teams.sort(Comparator.comparingInt(ColoredTeam::getUsersAmount));
        for (EggWarsTeam team : teams)
            if (team.addUser(player)) {
                sendDMessage(player, "eggwars.game.assign_team", "%color%", team.getColor().name());
                return;
            }
        throw new IllegalStateException("unable to add user to a party");
    }

    @Override
    public void onGameEntityDamaged(@NotNull EntityDamageEvent event) {

    }

    public EggWarsGame(@NotNull Map<String, Object> map) {
        super(map);
    }

    @Override
    protected @NotNull EggWarsTeam craftTeam(@NotNull DyeColor color) {
        return new EggWarsTeam(this, color);
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
    public void onGamerBlockBreak(@NotNull BlockBreakEvent event) {
        super.onGamerBlockBreak(event);
        if (getPhase() == Phase.PLAYING && !event.isCancelled())
            if (event.getBlock().getType() == Material.CHEST) {
                if (filledChests.contains(event.getBlock()) || ignoredChest.contains(event.getBlock()))
                    return;
                if (!(event.getBlock().getState() instanceof Chest cHolder))
                    return;
                onFillChest(cHolder.getBlockInventory());
                /** not dropped
                 if (cHolder.update(false, false))
                 Minigames.get().logTetraStar(ChatColor.DARK_RED, "D updated broken chest debug");*/
                Location dropLoc = event.getBlock().getLocation().add(0.5, 0.5, 0.5);
                for (ItemStack item : cHolder.getBlockInventory().getContents())
                    if (!UtilsInventory.isAirOrNull(item))
                        dropLoc.getWorld().dropItemNaturally(dropLoc, item);

            }
    }

    @EventHandler(ignoreCancelled = true)
    private void event(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player p))
            return;
        if (!isGamer(p))
            return;
        if (getPhase() != Phase.PLAYING)
            return;
        if (event.getInventory().getType() != InventoryType.CHEST)
            return;
        if (!(event.getInventory().getHolder() instanceof Chest cHolder))
            return;
        Block b = cHolder.getBlock();

        if (filledChests.contains(b) || ignoredChest.contains(b))
            return;
        filledChests.add(b);
        onFillChest(event.getInventory());
    }

    protected void onFillChest(Inventory inventory) {
        DropsFiller filler = getOption().getFiller();
        if (filler != null)
            filler.fillInventory(inventory);
    }

    @Override
    public void onGamerBlockPlace(@NotNull BlockPlaceEvent event) {
        if (getPhase() == Phase.PRE_START)
            event.setCancelled(true);
        if (event.getBlock().getType() == Material.CHEST) {
            ignoredChest.add(event.getBlock());
        }
    }


    @Override
    public boolean joinGameAsGamer(@NotNull Player player) {
        /*if (isGamer(player)) {
            new IllegalStateException().printStackTrace();
            return true;
        }
        if (isSpectator(player))
            return false;*/
        return addGamer(player);
    }

    @Override
    public void onGamerInventoryOpen(@NotNull InventoryOpenEvent event, @NotNull Player player) {
        if (event.getView().getTopInventory().getType() == InventoryType.ENCHANTING)
            event.getView().getTopInventory().setItem(1, new ItemStack(Material.LAPIS_LAZULI, 64));
    }

    @Override
    public void onGamerInventoryClose(@NotNull InventoryCloseEvent event, @NotNull Player player) {
        if (event.getView().getTopInventory().getType() == InventoryType.ENCHANTING)
            event.getView().getTopInventory().setItem(1, null);
    }


    @Override
    public void onGamerInventoryClick(@NotNull InventoryClickEvent event, @NotNull Player player) {
        super.onGamerInventoryClick(event, player);
        if (event.isCancelled())
            return;
        if (event.getView().getTopInventory().getType() == InventoryType.ENCHANTING)
            if (event.getRawSlot() == 1) //TODO test with numbered clicks
                event.setCancelled(true);
    }

    @Override
    public void onGamerCombustEvent(@NotNull EntityCombustEvent event, @NotNull Player player) {

    }


    @Override
    public void onCreatureSpawn(@NotNull CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL)
            event.setCancelled(true);
    }

    @Override
    public void onEntityDeath(@NotNull EntityDeathEvent event) {

    }

    @Override
    public void onGamerDamaging(@NotNull EntityDamageByEntityEvent event, @NotNull Player damager, boolean direct) {

    }

    @Override
    public void onGamerRegainHealth(@NotNull EntityRegainHealthEvent event, @NotNull Player player) {

    }

    @Override
    public void onEntityRegainHealth(@NotNull EntityRegainHealthEvent event) {

    }

    @Override
    public int getMaxGamers() {
        return getArena().getColors().size() * getOption().getTeamMaxSize();
    }

    @Override
    public @NotNull EggWarsType getMinigameType() {
        return MinigameTypes.EGGWARS;
    }

    @Override
    public boolean gameCanPreStart() {
        return getGamers().size() >= 2;
    }

    @Override
    public boolean gameCanStart() {
        int counter = 0;
        for (EggWarsTeam team : getTeams())
            if (getGamers(team).size() > 0)
                counter++;
        return counter >= 2;
    }

    public void onFakeGamerDeath(@NotNull Player player, @Nullable Player killer, boolean direct) {
        MessageUtil.debug(getId() + " onFakeGamerDeath " + player.getName() + " " + (killer == null ? "" : killer.getName()));
        if (containsLocation(player))
            for (ItemStack item : player.getInventory().getContents())
                if (!UtilsInventory.isAirOrNull(item))
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
        player.getInventory().clear();
        EggWarsTeam team = getTeam(player);
        switchToSpectator(player);
        if (team != null && team.hasLost())
            setScore(team.getName(), -1);
        if (killer != null && isGamer(killer)) {
            PlayerStat.SKYWARS_KILLS.add(killer, 1);
            getMinigameType().applyKillPoints(killer);
            //TODO prize?
            team = getTeam(killer);
            if (team != null)
                addScore(team.getName(), 1);
        }
        checkGameEnd();
    }

    public void checkGameEnd() {
        if (getPhase() != Phase.PLAYING)
            return;
        //TODO check win conditions
        int alive = 0;
        EggWarsTeam winner = null;
        for (EggWarsTeam party : this.getTeams())
            if (!party.hasLost()) {
                winner = party;
                alive++;
            }
        if (alive == 0) {
            new IllegalStateException("no winner").printStackTrace();
            gameEnd();
        }
        if (alive != 1)
            return;

        //has won
        for (UUID user : winner.getUsers()) {
            Player p = Bukkit.getPlayer(user);
            if (p != null && isGamer(p)) {
                PlayerStat.SKYWARS_VICTORY.add(user, 1);
                getMinigameType().applyWinPoints(p);
            }
        }
        this.gameEnd();
    }

    public boolean canAddGamer(@NotNull Player player) {
        return getPhase() != Phase.PLAYING && super.canAddGamer(player);
    }


    @Override
    public void onGamerHitByProjectile(@NotNull ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball) { //adds push
            if (!getPhase().equals(Phase.PLAYING))
                return;
            if (event.getEntity().getShooter() instanceof Player launcher &&
                    getTeam(launcher).equals(getTeam((Player) event.getHitEntity())))
                return;
            double push = getMinigameType().getSnowballPush();
            if (push != 0)
                event.getHitEntity().setVelocity(event.getEntity().getVelocity().normalize().multiply(push));
        }
    }

}