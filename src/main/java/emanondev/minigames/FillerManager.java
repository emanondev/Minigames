package emanondev.minigames;

import emanondev.core.UtilsString;
import emanondev.core.YMLConfig;
import emanondev.minigames.generic.MFiller;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FillerManager {

    public @NotNull File getFillersFolder() {
        return new File(Minigames.get().getDataFolder(), "fillers");
    }

    public @NotNull YMLConfig getFillerConfig(String fileName) {
        return Minigames.get().getConfig("fillers" + File.separator + fileName);
    }

    private static FillerManager instance;

    public static FillerManager get() {
        return instance;
    }


    private final HashMap<String, MFiller> fillers = new HashMap<>();
    private final HashMap<String, YMLConfig> fillersFile = new HashMap<>();

    public FillerManager() {
        instance = this;
    }

    public void reload() {
        fillers.clear();
        fillersFile.clear();

        File chestFillersFolder = getFillersFolder();
        if (chestFillersFolder.isDirectory()) {
            File[] files = chestFillersFolder.listFiles((f, n) -> (n.endsWith(".yml")));
            if (files != null)
                for (File file : files) {
                    YMLConfig config = getFillerConfig(file.getName());
                    for (String key : config.getKeys(false)) {
                        try {
                            Object value = config.get(key);
                            if (value instanceof MFiller chestFiller)
                                registerFiller(key, chestFiller, config);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        }
    }

    public @Nullable MFiller getFiller(@NotNull String id) {
        return fillers.get(id.toLowerCase());
    }


    public @NotNull Map<String, MFiller> getFillers() {
        return Collections.unmodifiableMap(fillers);
    }


    public void registerFiller(@NotNull String id, @NotNull MFiller filler, @NotNull OfflinePlayer player) {
        registerFiller(id, filler, getFillerConfig(player.getName()));
    }

    public void registerFiller(@NotNull String id, @NotNull MFiller filler, @NotNull YMLConfig config) {
        if (!UtilsString.isLowcasedValidID(id) || fillers.containsKey(id))
            throw new IllegalStateException("invalid id");
        if (filler.isRegistered())
            throw new IllegalStateException();
        filler.setRegistered(id);
        fillers.put(filler.getId(), filler);
        fillersFile.put(filler.getId(), config);
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "D Registered InventoryFiller &e" + id);
    }

    public void save(@NotNull MFiller filler) {
        if (!filler.isRegistered())
            throw new IllegalStateException();
        if (fillers.get(filler.getId()) != filler)
            throw new IllegalStateException();
        fillersFile.get(filler.getId()).set(filler.getId(), filler);
        fillersFile.get(filler.getId()).save();
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "D Updated InventoryFiller &e" + filler.getId());
    }
}
