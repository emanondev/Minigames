package emanondev.minigames.skywars;

import emanondev.core.MessageBuilder;
import emanondev.core.UtilsInventory;
import emanondev.minigames.MinigameTypes;
import emanondev.minigames.Minigames;
import emanondev.minigames.generic.AbstractMColorSchemGame;
import emanondev.minigames.generic.ColoredTeam;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
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
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "D " + getId() + " gamePREPreStart");
        for (Player p : new HashSet<>(this.getCollectedPlayers())) {
            addPlayingPlayer(p);
            getMinigameType().applyDefaultPlayerSnapshot(p);
            //TODO select kit & leave
            p.getInventory().setHeldItemSlot(4);
            p.getInventory().setItem(EquipmentSlot.HAND, new ItemStack(Material.CHEST));//GameManager.get().getKitSelectorItem(p));
            p.getInventory().setItem(8, new ItemStack(Material.IRON_BARS));//GameManager.get().getGameLeaveItem(p));
        }
        //TODO eventually combine teams

        ignoredChest.clear();
        filledChests.clear();

        super.gamePreStart();
    }

    /**
     * Assign a party to user or throw an exception
     */
    public void assignTeam(Player p) {//TODO choose how to fill with options
        if (getTeam(p) != null)
            return;
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "D " + getId() + " assigning team to " + p.getName());
        List<SkyWarsTeam> teams = new ArrayList<>(getTeams());
        teams.sort(Comparator.comparingInt(ColoredTeam::getUsersAmount));
        for (SkyWarsTeam team : teams)
            if (team.addUser(p)) {
                new MessageBuilder(Minigames.get(), p)
                        .addTextTranslation("skywars.game.assign_team", "", "%color%", team.getColor().name()).send();
                return;
            }
        throw new IllegalStateException("unable to add user to a party");
    }

    @Override
    public boolean canSwitchToSpectator(Player player) {
        return true;
    }

    @Override
    public void onPlayingPlayerPvpDamage(EntityDamageByEntityEvent event, Player p, Player damager) {
    }

    @Override
    public void onPlayingPlayerDamaging(EntityDamageByEntityEvent event, Player damager) {
    }

    @Override
    public void onGameEntityDamaged(EntityDamageEvent event) {
    }

    public SkyWarsGame(@NotNull Map<String, Object> map) {
        super(map);
    }

    @Override
    protected @NotNull SkyWarsTeam craftTeam(DyeColor color) {
        return new SkyWarsTeam(this, color);
    }

    @Override
    protected void onPlayingPlayerFallOutsideArena(PlayerMoveEvent event) {
        Player damager = null;
        if (event.getPlayer().getLastDamageCause() instanceof EntityDamageByEntityEvent evt) {
            if (evt.getDamager() instanceof Player)
                damager = (Player) evt.getDamager();
            else if (evt.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter)
                damager = shooter;
            else if (evt.getDamager() instanceof TNTPrimed tnt && tnt.getSource() instanceof Player terrorist)
                damager = terrorist;
        }
        onFakePlayingPlayerDeath(event.getPlayer(), damager);
    }

    @Override
    public void onPlayingPlayerBlockBreak(@NotNull BlockBreakEvent event) {
        if (getPhase() == Phase.PRE_START)
            event.setCancelled(true);
        if (event.getBlock().getType() == Material.CHEST) {
            Minigames.get().logTetraStar(ChatColor.DARK_RED, "D " + getId() + " fillchest break");

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
        if (!isPlayingPlayer(p))
            return;
        if (getPhase() != Phase.PLAYING)
            return;
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "D " + getId() + " fillchest openinv");

        if (event.getInventory().getType() != InventoryType.CHEST)
            return;
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "D " + getId() + " fillchest openinv A");
        if (!(event.getInventory().getHolder() instanceof Chest cHolder))
            return;
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "D " + getId() + " fillchest openinv B");
        Block b = cHolder.getBlock();
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "D " + getId() + " fillchest openinv fill " + filledChests.contains(b) + " ignored " + ignoredChest.contains(b));

        if (filledChests.contains(b) || ignoredChest.contains(b))
            return;
        filledChests.add(b);
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "D " + getId() + " fillchest openinv C");
        onFillChest(event.getInventory());
    }

    protected void onFillChest(Inventory inventory) {
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "D " + getId() + " fillchest filling");

        inventory.setItem(inventory.getSize() / 2, new ItemStack(Material.POPPY));
        //TODO fill chests
    }

    @Override
    public void onPlayingPlayerBlockPlace(@NotNull BlockPlaceEvent event) {
        if (getPhase() == Phase.PRE_START)
            event.setCancelled(true);
        if (event.getBlock().getType() == Material.CHEST) {
            Minigames.get().logTetraStar(ChatColor.DARK_RED, "D " + getId() + " fillchest ignoring");
            ignoredChest.add(event.getBlock());
        }
    }


    @Override
    public boolean joinGameAsPlayer(Player player) {
        if (isCollectedPlayer(player) || isPlayingPlayer(player)) {
            new IllegalStateException().printStackTrace();
            return true;
        }
        if (isSpectator(player))
            return false;
        return addCollectedPlayer(player);
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
    public void teleportToStartLocation(@NotNull Player player) {
        player.teleport(getArena().getSpawnOffset(getTeam(player).getColor())
                .add(getGameLocation()));
    }

    @Override
    public int getMaxPlayers() {
        return getArena().getColors().size() * getOption().getTeamMaxSize();
    }

    @Override
    public @NotNull SkyWarsType getMinigameType() {
        return MinigameTypes.SKYWARS;
    }

    @Override
    public boolean gameCanPreStart() {
        return getCollectedPlayers().size() >= 2;
    }

    @Override
    public boolean gameCanStart() {
        int counter = 0;
        for (SkyWarsTeam team : getTeams())
            if (getPlayingPlayers(team).size() > 0)
                counter++;
        return counter >= 2;
    }

    public void onFakePlayingPlayerDeath(@NotNull Player player, @Nullable Player killer) {
        if (containsLocation(player))
            for (ItemStack item : player.getInventory().getContents())
                if (!UtilsInventory.isAirOrNull(item))
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
        player.getInventory().clear();
        switchToSpectator(player);
        if (killer != null) {
            //TODO add points
        }
    }

    protected void checkGameEnd() {
        if (getPhase() != Phase.PLAYING)
            return;
        //TODO check win conditions
        int alive = 0;
        SkyWarsTeam winner;
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
        //TODO win!
        //TODO assign win points
        this.gameEnd();
    }


}
