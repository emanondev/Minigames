package emanondev.minigames;

import emanondev.core.UtilsString;
import emanondev.core.YMLConfig;
import emanondev.minigames.games.Registrable;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Manager<T extends Registrable> {

    private final String subFolder;

    public @NotNull File getFolder() {
        return new File(Minigames.get().getDataFolder(), subFolder);
    }

    public @NotNull YMLConfig getConfig(String fileName) {
        return Minigames.get().getConfig(subFolder + File.separator + fileName);
    }


    private final HashMap<String, T> registrables = new HashMap<>();
    private final HashMap<String, YMLConfig> registrablesFile = new HashMap<>();

    public Manager(@NotNull String subFolder) {
        this.subFolder = subFolder;
    }

    public void reload() {
        registrables.clear();
        registrablesFile.clear();

        File rFolder = getFolder();
        if (rFolder.isDirectory()) {
            File[] files = rFolder.listFiles((f, n) -> (n.endsWith(".yml")));
            if (files != null)
                for (File file : files) {
                    YMLConfig config = getConfig(file.getName());
                    for (String key : config.getKeys(false)) {
                        try {
                            T value = (T) config.get(key);
                            register(key, value, config);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        }
    }

    public @Nullable T get(@NotNull String id) {
        return registrables.get(id.toLowerCase(Locale.ENGLISH));
    }


    public @NotNull Map<String, T> getAll() {
        return Collections.unmodifiableMap(registrables);
    }


    public void register(@NotNull String id, @NotNull T registrable, @NotNull OfflinePlayer player) {
        register(id, registrable, getConfig(player.getName()));
        save(registrable);
    }

    public void register(@NotNull String id, @NotNull T registrable, @NotNull YMLConfig config) {
        if (!UtilsString.isLowcasedValidID(id) || registrables.containsKey(id))
            throw new IllegalStateException("invalid id");
        if (registrable.isRegistered())
            throw new IllegalStateException();
        registrable.setRegistered(id);
        registrables.put(registrable.getId(), registrable);
        registrablesFile.put(registrable.getId(), config);
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "D Registered " + registrable.getClass().getSimpleName() + " &e" + id);
    }

    public void save(@NotNull T registrable) {
        if (!registrable.isRegistered())
            throw new IllegalStateException();
        if (registrables.get(registrable.getId()) != registrable)
            throw new IllegalStateException();
        registrablesFile.get(registrable.getId()).set(registrable.getId(), registrable);
        registrablesFile.get(registrable.getId()).save();
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "D Updated " + registrable.getClass().getSimpleName() + " &e" + registrable.getId());
    }


    public void delete(@NotNull T type) {
        delete(type.getId());
    }

    public void delete(String id) {
        id = id.toLowerCase(Locale.ENGLISH);
        if (!registrables.containsKey(id))
            throw new IllegalStateException();
        YMLConfig config = registrablesFile.get(id);
        config.set(id, null);
        config.save();
        registrables.remove(id).setUnregister();
        registrablesFile.remove(id);
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "D " + this.getClass().getSimpleName() + " Deleted &e" + id);
    }


}
