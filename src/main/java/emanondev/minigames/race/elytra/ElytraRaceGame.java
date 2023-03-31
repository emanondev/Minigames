package emanondev.minigames.race.elytra;

import emanondev.core.ItemBuilder;
import emanondev.core.message.DMessage;
import emanondev.minigames.MessageUtil;
import emanondev.minigames.MinigameTypes;
import emanondev.minigames.Minigames;
import emanondev.minigames.data.PlayerStat;
import emanondev.minigames.generic.ColoredTeam;
import emanondev.minigames.race.ARaceGame;
import emanondev.minigames.race.ARaceTeam;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@SerializableAs(value = "ElytraRaceGame")
public class ElytraRaceGame extends ARaceGame<ARaceTeam<ElytraRaceGame>, ElytraRaceOption> {

    public ElytraRaceGame(@NotNull Map<String, Object> map) {
        super(map);
    }


    @Override
    public @NotNull ElytraRaceType getMinigameType() {
        return MinigameTypes.ELYTRA_RACE;
    }

    @Override
    public void onEntityDeath(@NotNull EntityDeathEvent event) {
    }

    @Override
    public @NotNull PlayerStat getPlayedStat() {
        return PlayerStat.ELYTRARACE_PLAYED;
    }

    @Override
    public @NotNull PlayerStat getVictoryStat() {
        return PlayerStat.ELYTRARACE_VICTORY;
    }

    @Deprecated
    public void assignTeam(@NotNull Player player) {//TODO choose how to fill with options
        if (getTeam(player) != null)
            return;
        MessageUtil.debug(getId() + " assigning team to " + player.getName());
        List<ARaceTeam<ElytraRaceGame>> teams = new ArrayList<>(getTeams());
        teams.sort(Comparator.comparingInt(ColoredTeam::getUsersAmount));
        for (ARaceTeam<ElytraRaceGame> team : teams)
            if (team.addUser(player)) {
                new DMessage(Minigames.get(), player).appendLang(getMinigameType().getType() + ".game.assign_team",
                        "%color%", team.getColor().name());
                return;
            }
        throw new IllegalStateException("unable to add user to a party");
    }


    @Override
    public void checkGameEnd() {
        if (getGamers().size() <= 1)
            this.gameEnd();
    }

    public boolean canAddGamer(@NotNull Player player) {
        return getPhase() != Phase.PLAYING && super.canAddGamer(player);
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
}
