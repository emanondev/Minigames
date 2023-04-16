package emanondev.minigames;

import emanondev.core.ItemBuilder;
import emanondev.core.PlayerSnapshot;
import emanondev.core.SoundInfo;
import emanondev.core.YMLConfig;
import emanondev.minigames.games.ColoredTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

public class Configurations {

    public static void applyGameCollectingPlayersSnapshot(@NotNull Player player) {
        applySnapshot(player, "collecting_players");
        player.setAllowFlight(true);//TODO hotfix
        player.setFlying(true);
    }

    public static void applyGamePreStartSnapshot(@NotNull Player player) {
        applySnapshot(player, "prestart");
    }

    public static void applyGameSpectatorSnapshot(@NotNull Player player) {
        applySnapshot(player, "spectator");
        player.setAllowFlight(true);
        player.setFlying(true);
    }

    public static void applyGameEndSnapshot(@NotNull Player player) {
        applySnapshot(player, "end");
        player.setAllowFlight(true);//TODO hotfix
    }

    public static void applyGameEmptyStartSnapshot(@NotNull Player player) {
        applySnapshot(player, "empty_start");
        for (PotionEffect effect : new ArrayList<>(player.getActivePotionEffects()))
            player.removePotionEffect(effect.getType());
    }

    @Contract("_ -> new")
    @NotNull
    public static ItemStack getKitSelectorItem(@NotNull Player player) {
        return getItem(player, "kit_selector", "items.kit_selector").build();
    }

    @Contract("_ -> new")
    @NotNull
    public static ItemStack getGameLeaveItem(@NotNull Player player) {
        return getItem(player, "game_leave", "items.game_leave").build();
    }

    @Contract("_, _ -> new")
    @NotNull
    public static ItemBuilder getOptionAllowSpectatorItem(@NotNull Player player, String... holders) {
        return getItem(player, "option_allow_spectator", "minioption.buttons.allowspectators", holders);
    }

    @Contract("_ -> new")
    @NotNull
    public static ItemBuilder getCollectingPlayersPhaseCooldownMaxItem(Player player) {
        return getItem(player, "collecting_players_phase_cooldown_max", null);
    }

    @Contract("_ -> new")
    @NotNull
    public static ItemBuilder getEndPhaseCooldownMaxItem(Player player) {
        return getItem(player, "end_phase_cooldown_max", null);
    }

    @Contract("_ -> new")
    @NotNull
    public static ItemBuilder getPreStartPhaseCooldownMaxItem(Player player) {
        return getItem(player, "prestart_phase_cooldown_max", null);
    }

    @NotNull
    @Deprecated
    private static ItemBuilder getItem(@NotNull Player player, @NotNull String path, @Nullable String pathText, String... holders) {
        YMLConfig conf = Minigames.get().getConfig("configurations" + File.separator + "items.yml");
        ItemBuilder b = conf.contains(path) ? conf.getGuiItem(path, new ItemBuilder(Material.STONE)) : null;
        if (b == null) {
            b = new ItemBuilder(Material.STONE);
            MessageUtil.debug("No item found at &e" + path + "&f on file &econfigurations" + File.separator + "items.yml");
        }
        if (pathText != null)
            b.setDescription(MessageUtil.getMultiMessage(player, pathText, holders), false);
        return b.setGuiProperty();
    }

    private static void applySnapshot(@NotNull Player player, @NotNull String path) {
        PlayerSnapshot snap = ((PlayerSnapshot) Minigames.get().getConfig("configurations" + File.separator + "snapshots.yml")
                .get(path));
        if (snap != null)
            snap.apply(player);
        else
            MessageUtil.debug("No snapshot found at &e" + path + "&f on file &econfigurations" + File.separator + "snapshots.yml");
    }

    private static final SoundInfo defSound = new SoundInfo(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5, 5, true);

    private static @NotNull SoundInfo getSoundInfo(@NotNull String path) {
        SoundInfo sInfo = Minigames.get().getConfig("configurations" + File.separator + "sounds.yml")
                .getSoundInfo(path, null);
        if (sInfo == null) {
            MessageUtil.debug("No SoundInfo found at &e" + path + "&f on file &econfigurations" + File.separator + "sounds.yml");
            return defSound;
        }
        return sInfo;
    }

    public static SoundInfo getCollectingPlayersCooldownTickSound() {
        return getSoundInfo("collecting_players_cooldown_tick");
    }

    public static SoundInfo getPreStartPhaseCooldownTickSound() {
        return getSoundInfo("prestart_cooldown_tick");
    }

    public static ItemStack getTeamSelectorItem(Player player) {
        return getItem(player, "team_selector", "items.team_selector").build();
    }

    public static ItemStack getTeamItem(Player player, ColoredTeam team) {
        ItemBuilder b = getItem(player, "team_info", "items.team_info",
                "%team%", team.getName(),
                "%color%", team.getChatColor().toString(),
                "%users%", String.valueOf(team.getUsers().size()),
                "%max_users%", String.valueOf(team.getGame().getMaxGamers())).setColor(team.getColor().getColor());
        for (UUID uuid : team.getUsers())
            b.addLore(ChatColor.WHITE + Bukkit.getOfflinePlayer(uuid).getName());
        return b.build();
    }
}
