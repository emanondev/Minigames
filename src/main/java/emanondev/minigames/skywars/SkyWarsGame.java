package emanondev.minigames.skywars;

import emanondev.core.ItemBuilder;
import emanondev.core.MessageBuilder;
import emanondev.core.UtilsInventory;
import emanondev.core.VaultEconomyHandler;
import emanondev.core.gui.PagedListFGui;
import emanondev.minigames.*;
import emanondev.minigames.generic.AbstractMColorSchemGame;
import emanondev.minigames.generic.ColoredTeam;
import emanondev.minigames.generic.MFiller;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SkyWarsGame extends AbstractMColorSchemGame<SkyWarsTeam, SkyWarsArena, SkyWarsOption> {

    private final HashSet<Block> ignoredChest = new HashSet<>();
    private final HashSet<Block> filledChests = new HashSet<>();
    private final HashMap<Player, String> kitPreference = new HashMap<>();

    @Override
    public void gamePreStart() {
        //MessageUtil.debug(  getId() + " gamePREPreStart");
        List<Player> list = new ArrayList<>(this.getCollectedPlayers());
        Collections.shuffle(list);
        for (Player p : list) {
            addPlayingPlayer(p);
            Configurations.applyGamePreStartSnapshot(p);
            //p.getInventory().setHeldItemSlot(4);
            if (getOption().getKits().size() > 0) //only if you can choose a kit
                p.getInventory().setItem(4, Configurations.getKitSelectorItem(p));
            p.getInventory().setItem(8, Configurations.getGameLeaveItem(p));
        }
        //TODO eventually combine teams

        ignoredChest.clear();
        filledChests.clear();

        super.gamePreStart();
    }

    @Override
    public void gameStart() {
        super.gameStart();
        VaultEconomyHandler ecoHandler = new VaultEconomyHandler();
        for (Player player : getPlayingPlayers()) {
            if (kitPreference.containsKey(player)) {
                Kit kit = KitManager.get().getKit(kitPreference.get(player));
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
    }

    /**
     * Assign a party to user or throw an exception
     */
    public void assignTeam(@NotNull Player player) {//TODO choose how to fill with options
        if (getTeam(player) != null)
            return;
        MessageUtil.debug(getId() + " assigning team to " + player.getName());
        List<SkyWarsTeam> teams = new ArrayList<>(getTeams());
        teams.sort(Comparator.comparingInt(ColoredTeam::getUsersAmount));
        for (SkyWarsTeam team : teams)
            if (team.addUser(player)) {
                MessageUtil.sendMessage( player,"skywars.game.assign_team",  "%color%", team.getColor().name());
                return;
            }
        throw new IllegalStateException("unable to add user to a party");
    }

    @Override
    public boolean canSwitchToSpectator(Player player) {
        return true;
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
    public void onPlayingPlayerBlockPlace(@NotNull BlockPlaceEvent event) {
        if (getPhase() == Phase.PRE_START)
            event.setCancelled(true);
        if (event.getBlock().getType() == Material.CHEST) {
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

    @Override
    public void onPlayingPlayerInteract(PlayerInteractEvent event) {
        super.onPlayingPlayerInteract(event);
        if (getPhase() != Phase.PRE_START)
            return;
        if (event.getPlayer().getInventory().getHeldItemSlot() != 4)
            return;
        List<Kit> kits = getOption().getKits();
        if (kits.size() == 0) //only if you can choose a kit
            return;
        PagedListFGui<Kit> gui = new PagedListFGui<>(
                MessageUtil.getMessage( event.getPlayer(),"generic.gui.kitselector_title")
                , 3, event.getPlayer(), null, Minigames.get(),
                false,
                (evt, kit) -> {
                    if (kit.getId().equals(kitPreference.get(event.getPlayer())))
                        kitPreference.remove(event.getPlayer());
                    else
                        kitPreference.put(event.getPlayer(), kit.getId());
                    return true;
                }, (kit) -> new ItemBuilder(Material.IRON_CHESTPLATE).setDescription(//TODO
                Minigames.get().getLanguageConfig(event.getPlayer())

                        .loadMultiMessage("generic.gui.kitselector_description"
                                , new ArrayList<>(), "%id%", kit.getId(), "%price%", kit.getPrice() == 0 ? "free" : String.valueOf(kit.getPrice()))
        ).setGuiProperty().addEnchantment(Enchantment.DURABILITY,
                kit.getId()
                        .equals(kitPreference.get(event.getPlayer())) ? 1 : 0).setGuiProperty().build());
        gui.addElements(kits);
        gui.updateInventory();
        gui.open(event.getPlayer());

    }

    @Override
    public void gameRestart() {
        kitPreference.clear();
        super.gameRestart();
    }
}
