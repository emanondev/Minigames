package emanondev.minigames;

import emanondev.core.PlayerSnapshot;
import emanondev.core.UtilsString;
import emanondev.core.YMLConfig;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class KitManager {

    private static KitManager instance;

    public KitManager() {
        instance = this;
    }

    public static KitManager get() {
        return instance;
    }

    public File getKitsFolder() {
        return new File(Minigames.get().getDataFolder(), "kits");
    }

    public YMLConfig getKitConfig(@NotNull String fileName) {
        return Minigames.get().getConfig("kits" + File.separator + fileName);
    }

    private final Map<String, Kit> kits = new HashMap<>();
    private final HashMap<String, YMLConfig> kitsFile = new HashMap<>();

    public @Nullable Kit getKit(@NotNull String id) {
        return kits.get(id.toLowerCase(Locale.ENGLISH));
    }

    /*public boolean existKit(String id) {
        return kits.get(id.toLowerCase(Locale.ENGLISH)) != null;
    }

    public void applyKit(String id, Player player) {
        kits.get(id.toLowerCase(Locale.ENGLISH)).apply(player);
    }*/

    public @NotNull Set<String> getKitsId() {
        return Collections.unmodifiableSet(kits.keySet());
    }

    public void reload() {
        kits.clear();
        kitsFile.clear();
        File kitInstancesFolder = getKitsFolder();
        if (kitInstancesFolder.isDirectory()) {
            File[] files = kitInstancesFolder.listFiles((f, n) -> (n.endsWith(".yml")));
            if (files != null)
                for (File file : files) {
                    YMLConfig config = getKitConfig(file.getName());
                    for (String key : config.getKeys(false)) {
                        try {
                            Object value = config.get(key);
                            if (value instanceof Kit kit)
                                loadKit(key, kit, config);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        }
    }


    private void loadKit(@NotNull String id, @NotNull Kit kit, @NotNull YMLConfig config) {
        if (!UtilsString.isLowcasedValidID(id) || kits.containsKey(id))
            throw new IllegalStateException();
        if (kit.isRegistered())
            throw new IllegalStateException();
        kit.setRegistered(id);
        kits.put(id, kit);
        kitsFile.put(id, config);
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "D Loaded Kit &e" + id);
    }

    public void updateKit(@NotNull String id, @NotNull Player updater) {
        PlayerSnapshot snap = new PlayerSnapshot();
        snap.loadFrom(updater, PlayerSnapshot.FieldType.INVENTORY, PlayerSnapshot.FieldType.ARMOR);
        updateKit(id, snap);
    }

    private void updateKit(@NotNull String id, @NotNull PlayerSnapshot snap) {
        id = id.toLowerCase(Locale.ENGLISH);
        if (!UtilsString.isLowcasedValidID(id) || !kits.containsKey(id))
            throw new IllegalStateException();
        YMLConfig config = kitsFile.get(id);
        Kit kit = kits.get(id);
        kit.updateSnapshot(snap);
        save(kit);
    }

    public void save(@NotNull Kit kit) {
        if (!kit.isRegistered() || !kits.containsKey(kit.getId()))
            throw new IllegalStateException();
        YMLConfig config = kitsFile.get(kit.getId());
        config.set(kit.getId(), kit);
        config.save();
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "D Saved Kit &e" + kit.getId());
    }

    public void deleteKit(@NotNull String id) {
        id = id.toLowerCase(Locale.ENGLISH);
        if (!kits.containsKey(id))
            throw new IllegalStateException();
        YMLConfig config = kitsFile.get(id);
        config.set(id, null);
        config.save();
        kits.remove(id).setUnregister();
        kitsFile.remove(id);
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "D Deleted Kit &e" + id);
    }

    public void createKit(@NotNull String id, @NotNull Player creator) {
        PlayerSnapshot snap = new PlayerSnapshot();
        snap.loadFrom(creator, PlayerSnapshot.FieldType.INVENTORY, PlayerSnapshot.FieldType.ARMOR);
        createKit(id, snap, getKitConfig(creator.getName()));
    }

    private void createKit(@NotNull String id, @NotNull PlayerSnapshot snap, @NotNull OfflinePlayer creator) {
        createKit(id, snap, getKitConfig(creator.getName()));
    }

    private void createKit(@NotNull String id, @NotNull PlayerSnapshot snap, @NotNull YMLConfig config) {
        id = id.toLowerCase(Locale.ENGLISH);
        if (!UtilsString.isLowcasedValidID(id) || kits.containsKey(id))
            throw new IllegalStateException();
        Kit kit = Kit.fromPlayerSnapshot(snap);
        kitsFile.put(id, config);
        kits.put(id, kit);
        kit.setRegistered(id);
        save(kit);
    }

    public Map<String, Kit> getKits() {
        return Collections.unmodifiableMap(kits);
    }
}
