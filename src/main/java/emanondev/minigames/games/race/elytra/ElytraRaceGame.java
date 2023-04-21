package emanondev.minigames.games.race.elytra;

import emanondev.core.ItemBuilder;
import emanondev.minigames.MinigameTypes;
import emanondev.minigames.data.PlayerStat;
import emanondev.minigames.event.elytrarace.ElytraRaceStartEvent;
import emanondev.minigames.event.elytrarace.ElytraRaceWinFirstEvent;
import emanondev.minigames.event.elytrarace.ElytraRaceWinSecondEvent;
import emanondev.minigames.event.elytrarace.ElytraRaceWinThirdEvent;
import emanondev.minigames.games.race.ARaceGame;
import emanondev.minigames.games.race.ARaceTeam;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

@SerializableAs(value = "ElytraRaceGame")
public class ElytraRaceGame extends ARaceGame<ARaceTeam<ElytraRaceGame>, ElytraRaceOption> {


    public ElytraRaceGame(@NotNull Map<String, Object> map) {
        super(map);
    }

    @Override
    protected void craftAndCallGameStartEvent() {
        Bukkit.getPluginManager().callEvent(new ElytraRaceStartEvent(this));
    }

    @Override
    public void onEntityDeath(@NotNull EntityDeathEvent event) {
    }

    @Override
    public @NotNull ElytraRaceType getMinigameType() {
        return MinigameTypes.ELYTRA_RACE;
    }

    @Override
    protected void craftAndCallWinFirstEvent(@NotNull ARaceTeam<ElytraRaceGame> team, @NotNull Player lineCutter, @NotNull Set<Player> winners) {
        Bukkit.getPluginManager().callEvent(new ElytraRaceWinFirstEvent(team, lineCutter, winners));
    }

    @Override
    protected void craftAndCallWinSecondEvent(@NotNull ARaceTeam<ElytraRaceGame> team, @NotNull Player lineCutter, @NotNull Set<Player> winners) {
        Bukkit.getPluginManager().callEvent(new ElytraRaceWinSecondEvent(team, lineCutter, winners));
    }

    @Override
    protected void craftAndCallWinThirdEvent(@NotNull ARaceTeam<ElytraRaceGame> team, @NotNull Player lineCutter, @NotNull Set<Player> winners) {
        Bukkit.getPluginManager().callEvent(new ElytraRaceWinThirdEvent(team, lineCutter, winners));
    }

    public void teleportResetLocation(@NotNull Player player) {
        super.teleportResetLocation(player);
        if (isGamer(player) && (getPhase() == Phase.PRE_START || getPhase() == Phase.PLAYING)) {
            player.getInventory().setItem(EquipmentSlot.CHEST, new ItemBuilder(Material.ELYTRA)
                    .setGuiProperty().addEnchantment(Enchantment.BINDING_CURSE, 1).build());
            if (getPhase() == Phase.PLAYING && player.getLocation().getBlock().getRelative(BlockFace.DOWN).isPassable())
                player.setGliding(true);
        }
    }

    public void gameStart() {
        super.gameStart();
        for (Player player : getGamers()) {
            player.getInventory().setItem(EquipmentSlot.CHEST, new ItemBuilder(Material.ELYTRA)
                    .setGuiProperty().addEnchantment(Enchantment.BINDING_CURSE, 1).build());
        }
    }

    @Override
    public @NotNull PlayerStat getPlayedStat() {
        return PlayerStat.ELYTRARACE_PLAYED;
    }

    @Override
    public @NotNull PlayerStat getVictoryStat() {
        return PlayerStat.ELYTRARACE_VICTORY;
    }

    @Override
    public @NotNull PlayerStat getVictoryFirstStat() {
        return PlayerStat.ELYTRARACE_VICTORY_FIRST;
    }

    @Override
    public @NotNull PlayerStat getVictorySecondStat() {
        return PlayerStat.ELYTRARACE_VICTORY_SECOND;
    }

    @Override
    public @NotNull PlayerStat getVictoryThirdStat() {
        return PlayerStat.ELYTRARACE_VICTORY_THIRD;
    }
}
