package emanondev.minigames.skywars;

import emanondev.core.UtilsInventory;
import emanondev.minigames.MessageUtil;
import emanondev.minigames.MinigameTypes;
import emanondev.minigames.Minigames;
import emanondev.minigames.generic.AbstractMColorSchemGame;
import emanondev.minigames.generic.ColoredTeam;
import emanondev.minigames.generic.MFiller;
import emanondev.minigames.data.GameStat;
import emanondev.minigames.data.PlayerStat;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SkyWarsGame extends AbstractMColorSchemGame<SkyWarsTeam, SkyWarsArena, SkyWarsOption> {

    private final HashSet<Block> ignoredChest = new HashSet<>();
    private final HashSet<Block> filledChests = new HashSet<>();

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

    public void gameStart(){
        super.gameStart();
        for (Player player:getGamers()) {
            PlayerStat.SKYWARS_PLAYED.add(player, 1);
            PlayerStat.GAME_PLAYED.add(player, 1);
        }
        GameStat.PLAY_TIMES.add(this,1);
        getTeams().forEach(team->team.setScore(team.hasLost()?-1:0));
    }

    /**
     * Assign a party to user or throw an exception
     */
    @Deprecated
    public void assignTeam(@NotNull Player player) {//TODO choose how to fill with options
        if (getTeam(player) != null)
            return;
        MessageUtil.debug(getId() + " assigning team to " + player.getName());
        List<SkyWarsTeam> teams = new ArrayList<>(getTeams());
        teams.sort(Comparator.comparingInt(ColoredTeam::getUsersAmount));
        for (SkyWarsTeam team : teams)
            if (team.addUser(player)) {
                MessageUtil.sendMessage(player, "skywars.game.assign_team", "%color%", team.getColor().name());
                return;
            }
        throw new IllegalStateException("unable to add user to a party");
    }

    @Override
    public void onGameEntityDamaged(@NotNull EntityDamageEvent event) {

    }

    @Override
    public boolean canSwitchToSpectator(Player player) {
        return getOption().allowSpectators();
    }

    public SkyWarsGame(@NotNull Map<String, Object> map) {
        super(map);
    }

    @Override
    protected @NotNull SkyWarsTeam craftTeam(@NotNull DyeColor color) {
        return new SkyWarsTeam(this, color);
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

            }
            case PRE_START, COLLECTING_PLAYERS -> teleportResetLocation(event.getPlayer());
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
                if (cHolder.update(false, false))
                    Minigames.get().logTetraStar(ChatColor.DARK_RED, "D updated broken chest debug");
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
        MFiller filler = getOption().getFiller();
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
    public @NotNull SkyWarsType getMinigameType() {
        return MinigameTypes.SKYWARS;
    }

    @Override
    public boolean gameCanPreStart() {
        return getGamers().size() >= 2;
    }

    @Override
    public boolean gameCanStart() {
        int counter = 0;
        for (SkyWarsTeam team : getTeams())
            if (getGamers(team).size() > 0)
                counter++;
        return counter >= 2;
    }

    public void onFakeGamerDeath(@NotNull Player player, @Nullable Player killer, boolean direct) {
        MessageUtil.debug(getId()+" onFakeGamerDeath "+player.getName()+" "+(killer==null?"":killer.getName()));
        if (containsLocation(player))
            for (ItemStack item : player.getInventory().getContents())
                if (!UtilsInventory.isAirOrNull(item))
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
        player.getInventory().clear();
        SkyWarsTeam team = getTeam(player);
        if (team!=null && team.hasLost())
            team.setScore(-1);
        switchToSpectator(player);
        if (killer != null && isGamer(killer)) {
            PlayerStat.SKYWARS_KILLS.add(killer,1);
            getMinigameType().applyKillPoints(killer);
            //TODO prize?
            team = getTeam(killer);
            if (team!=null)
                team.addScore(1);
        }
        checkGameEnd();
    }

    public void checkGameEnd() {
        if (getPhase() != Phase.PLAYING)
            return;
        //TODO check win conditions
        int alive = 0;
        SkyWarsTeam winner = null;
        for (SkyWarsTeam party : this.getTeams())
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
        for (UUID user:winner.getUsers()){
            Player p = Bukkit.getPlayer(user);
            if (p!=null && isGamer(p)) {
                PlayerStat.SKYWARS_VICTORY.add(user, 1);
                getMinigameType().applyWinPoints(p);
            }
        }
        this.gameEnd();
    }

}
