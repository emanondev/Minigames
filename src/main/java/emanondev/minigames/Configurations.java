package emanondev.minigames;

import emanondev.core.ItemBuilder;
import emanondev.core.PlayerSnapshot;
import emanondev.core.SoundInfo;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class Configurations {

    public static void applyGameCollectingPlayersSnapshot(@NotNull Player player) {
        applySnapshot(player, "collecting_players");
    }

    public static void applyGamePreStartSnapshot(@NotNull Player player) {
        applySnapshot(player, "prestart");
    }

    public static void applyGameSpectatorSnapshot(@NotNull Player player) {
        applySnapshot(player, "spectator");
    }

    public static void applyGameEndSnapshot(@NotNull Player player) {
        applySnapshot(player, "end");
    }

    public static void applyGameEmptyStartSnapshot(@NotNull Player player) {
        applySnapshot(player, "empty_start");
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
    private static ItemBuilder getItem(@NotNull Player player, @NotNull String path, @Nullable String pathText, String... holders) {
        ItemBuilder b = Minigames.get().getConfig("configurations" + File.separator + "items.yml")
                .getGuiItem(path, (ItemBuilder) null);
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

    public static ItemStack getFillerToggleViewItem(Player player) {
        return getItem(player,"filler_toggle_view","minifiller.buttons.object_info").build();
    }
}
