package emanondev.minigames.games.eggwars;

import emanondev.core.SoundInfo;
import emanondev.core.UtilsInventory;
import emanondev.core.message.DMessage;
import emanondev.minigames.MessageUtil;
import emanondev.minigames.MinigameTypes;
import emanondev.minigames.Minigames;
import emanondev.minigames.data.GameStat;
import emanondev.minigames.data.PlayerStat;
import emanondev.minigames.event.eggwars.EggWarsDeathEvent;
import emanondev.minigames.event.eggwars.EggWarsStartEvent;
import emanondev.minigames.event.eggwars.EggWarsWinEvent;
import emanondev.minigames.games.AbstractMColorSchemGame;
import emanondev.minigames.games.ColoredTeam;
import emanondev.minigames.games.DropsFiller;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.EnderChest;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SerializableAs("EggWarsGame")
public class EggWarsGame extends AbstractMColorSchemGame<EggWarsTeam, EggWarsArena, EggWarsOption> {


    private final HashSet<Block> addedBlocks = new HashSet<>();
    private final HashMap<Player, ShopsMenu> menus = new HashMap<>();
    private final HashMap<DyeColor, Inventory> enderChests = new HashMap<>(); //is that ok?

    public Inventory getTeamEnderChest(EggWarsTeam team){
        if (!enderChests.containsKey(team.getColor()))
            enderChests.put(team.getColor(),Bukkit.createInventory(null,InventoryType.ENDER_CHEST));
        return enderChests.get(team.getColor());
    }//TODO test

    public EggWarsGame(@NotNull Map<String, Object> map) {
        super(map);
    }

    @Override
    public void gamePreStart() {
        //TODO assignTeams!

        List<Player> list = new ArrayList<>(this.getGamers());
        Collections.shuffle(list);
        list.forEach(this::onGamerAdded);

        super.gamePreStart();
        menus.clear();
        enderChests.clear();
    }

    public void gameStart() {
        super.gameStart();
        this.addedBlocks.clear();
        for (Player player : getGamers()) {
            PlayerStat.EGGWARS_PLAYED.add(player, 1);
            PlayerStat.GAME_PLAYED.add(player, 1);
        }
        GameStat.PLAY_TIMES.add(this, 1);
        getTeams().forEach(team -> {
            if (!team.hasLost()) setScore(team.getName(), 0);
        });
    }

    @Override
    protected void craftAndCallGameStartEvent() {
        Bukkit.getPluginManager().callEvent(new EggWarsStartEvent(this));
    }

    public boolean canAddGamer(@NotNull Player player) {
        return switch (getPhase()) {
            case PRE_START, COLLECTING_PLAYERS -> super.canAddGamer(player);
            default -> false;
        };
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
                new DMessage(Minigames.get(), player).appendLang(getMinigameType().getType() + ".game.assign_team",
                        "%color%", team.getColor().name());
                return;
            }
        throw new IllegalStateException("unable to add user to a party");
    }

    @Override
    public void onGameEntityDamaged(@NotNull EntityDamageEvent event) {

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
    public void onGamerRegainHealth(@NotNull EntityRegainHealthEvent event, @NotNull Player player) {

    }

    @Override
    public void onEntityRegainHealth(@NotNull EntityRegainHealthEvent event) {

    }

    @Override
    public void onGamerBlockBreak(@NotNull BlockBreakEvent event) {
        super.onGamerBlockBreak(event);
        if (!event.isCancelled() && !addedBlocks.contains(event.getBlock()))
            event.setCancelled(true); //TODO feedback?
    }

    @Override
    public void onGamerBlockPlace(@NotNull BlockPlaceEvent event) {
        super.onGamerBlockPlace(event);
        if (!event.isCancelled()) {
            addedBlocks.add(event.getBlock());
        }
    }

    @Override
    public void onGamerPickupItem(@NotNull EntityPickupItemEvent event, @NotNull Player p) {
        super.onGamerPickupItem(event, p);
        if (event.isCancelled())
            return;
        ItemStack raw = event.getItem().getItemStack();
        EggWarsGeneratorType type = getMinigameType().getGenerator(raw);
        if (type != null) {
            event.getItem().setItemStack(type.getGeneratedItem(p, raw.getAmount()));
            Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                getMenu(p).recalculateCoins();
            }, 1L);
        }
    }

    @Override
    public void onGamerInteractEntity(@NotNull PlayerInteractEntityEvent event) {
        super.onGamerInteractEntity(event);
        if (!event.isCancelled() && event.getRightClicked() instanceof Villager)
            getMenu(event.getPlayer()).open(event.getPlayer());
    }

    @Override
    public void onGamerInteract(@NotNull PlayerInteractEvent event) {
        super.onGamerInteract(event);
        if (getPhase() != Phase.PLAYING)
            return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        DropsFiller filler = getOption().getKillRewardFiller();
        if (filler == null)
            return;
        ItemStack item = event.getItem();
        if (UtilsInventory.isAirOrNull(item))
            return;
        if (!item.isSimilar(getMinigameType().getKillRewardItem()))
            return;
        event.setCancelled(true); //TODO feedback
        new SoundInfo(Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1, 1, true).play(event.getPlayer());
        item.setAmount(item.getAmount() - 1);
        event.getPlayer().getInventory().setItem(event.getHand(), item);
        filler.getDrops().forEach((d) -> UtilsInventory.giveAmount(event.getPlayer(), d, d.getAmount(), UtilsInventory.ExcessManage.DROP_EXCESS));
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

    public @NotNull ShopsMenu getMenu(Player target) {
        if (!menus.containsKey(target))
            menus.put(target, new ShopsMenu(target, this));
        return menus.get(target);
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

    protected void craftAndCallGamerDeathEvent(Player player, Player killer) {
        Bukkit.getPluginManager().callEvent(new EggWarsDeathEvent(this, player, killer));
    }

    protected void craftAndCallWinEvent(EggWarsTeam winner) {
        if (winner == null)
            return;
        HashSet<Player> players = new HashSet<>();
        for (UUID user : winner.getUsers()) {
            Player player = Bukkit.getPlayer(user);
            if (player != null && getGamers().contains(player))
                players.add(player);
        }
        Bukkit.getPluginManager().callEvent(new EggWarsWinEvent(this, players));
    }

    @Override
    public int getMaxGamers() {
        return getArena().getColors().size() * getOption().getTeamMaxSize();
    }

    @Override
    public @NotNull EggWarsType getMinigameType() {
        return MinigameTypes.EGGWARS;
    }

    public void checkGameEnd() {
        if (getPhase() != Phase.PLAYING)
            return;
        int alive = 0;
        EggWarsTeam winner = null;
        for (EggWarsTeam party : this.getTeams())
            if (!party.hasLost()) {
                winner = party;
                alive++;
            }
        craftAndCallWinEvent(winner);
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
                PlayerStat.EGGWARS_VICTORY.add(user, 1);
                givePoints(p, getMinigameType().getWinPoints());
                giveGameExp(p, getMinigameType().getWinExp());
            }
        }
        this.gameEnd();
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
        new IllegalStateException("debug " + getPhase()).printStackTrace();
        MessageUtil.debug(getId() + " onFakeGamerDeath " + player.getName() + " " + (killer == null ? "" : killer.getName()));
        craftAndCallGamerDeathEvent(player, killer);
        if (containsLocation(player))
            for (ItemStack item : player.getInventory().getContents())
                if (!UtilsInventory.isAirOrNull(item))
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
        player.getInventory().clear();
        new SoundInfo(Sound.ENTITY_PLAYER_DEATH, 1, 1, false).play(player);
        PlayerStat.EGGWARS_DEATHS.add(player, 1);
        EggWarsTeam team = getTeam(player);
        switchToSpectator(player);
        new SoundInfo(Sound.ENTITY_GHAST_DEATH, 1, 1, true).play(player); //self death notify
        getGamers().forEach(gamer -> {
            if (gamer.equals(player)) {
                if (killer == null)
                    sendDMessage(gamer, getMinigameType() + ".game.you_have_been_eliminated");
                else
                    sendDMessage(gamer, getMinigameType() + ".game.you_have_been_eliminated_by", "%who%", killer.getName());
            } else if (gamer.equals(killer)) {
                sendDMessage(gamer, getMinigameType() + ".game.you_eliminated", "%who%", player.getName());
            } else {
                if (killer == null)
                    sendDMessage(gamer, getMinigameType() + ".game.user_have_been_eliminated", "%who%", player.getName());
                else
                    sendDMessage(gamer, getMinigameType() + ".game.user_have_been_eliminated_by", "%who%", player.getName(), "%killer%", killer.getName());
            }

        });
        if (team != null && team.hasLost())
            setScore(team.getName(), -1);
        if (killer != null && isGamer(killer)) {
            PlayerStat.EGGWARS_KILLS.add(killer, 1);
            givePoints(killer, getMinigameType().getDefinitiveKillPoints());
            giveGameExp(killer, getMinigameType().getDefinitiveKillExp());
            if (getOption().getKillRewardFiller() != null) {
                //TODO add sound
                UtilsInventory.giveAmount(player, getMinigameType().getKillRewardItem(), 1, UtilsInventory.ExcessManage.DROP_EXCESS);
            }

            team = getTeam(killer);
            if (team != null)
                addScore(team.getName(), 1);
        }
        checkGameEnd();

        //TODO
    }

    @Override
    public void onGamerDamaging(@NotNull EntityDamageByEntityEvent event, @NotNull Player damager, boolean direct) {

    }

    @Override
    public boolean joinGameAsGamer(@NotNull Player player) {
        return addGamer(player);
    }

    @Override
    public void onGamerInventoryOpen(@NotNull InventoryOpenEvent event, @NotNull Player player) { //TODO
        if (event.getView().getTopInventory().getType() == InventoryType.ENCHANTING)
            event.getView().getTopInventory().setItem(1, new ItemStack(Material.LAPIS_LAZULI, 64));
    }

    @Override
    public void onGamerInventoryClose(@NotNull InventoryCloseEvent event, @NotNull Player player) {
        if (event.getView().getTopInventory().getType() == InventoryType.ENCHANTING)
            event.getView().getTopInventory().setItem(1, null);
    }

    @Override
    public void onGamerCombustEvent(@NotNull EntityCombustEvent event, @NotNull Player player) {
    }

    @Override
    public void onGamerHitByProjectile(@NotNull ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball) { //adds push
            if (!getPhase().equals(Phase.PLAYING))
                return;
            if (!(event.getHitEntity() instanceof Player hitted))
                return;
            if (event.getEntity().getShooter() instanceof Player launcher &&
                    Objects.equals(getTeam(launcher), getTeam(hitted)))
                return;
            double push = getMinigameType().getSnowballPush();
            if (push != 0) {
                event.getHitEntity().setVelocity(event.getEntity().getVelocity().setY(0).normalize().multiply(push).setY(getMinigameType().getSnowballVerticalPush()));
            }
        }
    }

    @Override
    public void onGamerCraftItem(CraftItemEvent event) {
        event.setCancelled(true);
    }
}
