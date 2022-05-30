package emanondev.minigames;

import emanondev.core.UtilsString;
import emanondev.core.YMLConfig;
import emanondev.minigames.generic.MOption;
import emanondev.minigames.generic.MType;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OptionManager {

    public @NotNull File getOptionsFolder() {
        return new File(Minigames.get().getDataFolder(), "options");
    }

    public @NotNull YMLConfig getOptionConfig(String fileName) {
        return Minigames.get().getConfig("options" + File.separator + fileName);
    }

    private static OptionManager instance;

    public static OptionManager get() {
        return instance;
    }


    private final HashMap<String, MOption> options = new HashMap<>();
    private final HashMap<String, YMLConfig> optionsFile = new HashMap<>();

    public OptionManager() {
        instance = this;
    }

    public void reload() {
        options.clear();
        optionsFile.clear();

        File optionsFolder = getOptionsFolder();
        if (optionsFolder.isDirectory()) {
            File[] files = optionsFolder.listFiles((f, n) -> (n.endsWith(".yml")));
            if (files != null)
                for (File file : files) {
                    YMLConfig config = getOptionConfig(file.getName());
                    for (String key : config.getKeys(false)) {
                        try {
                            Object value = config.get(key);
                            if (value instanceof MOption option)
                                registerOption(key, option, config);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        }
    }

    public <O extends MOption> @NotNull Map<String, O> getCompatibleOptions(@NotNull MType<?, O> type) {
        HashMap<String, O> map = new HashMap<>();
        options.forEach((k, v) -> {
            if (type.matchType(v)) map.put(k, (O) v);
        });
        return map;
    }

    public @Nullable MOption getOption(@NotNull String id) {
        return options.get(id.toLowerCase());
    }


    public @NotNull Map<String, MOption> getOptions() {
        return Collections.unmodifiableMap(options);
    }


    public void registerOption(@NotNull String id, @NotNull MOption mOption, @NotNull OfflinePlayer player) {
        registerOption(id, mOption, getOptionConfig(player.getName()));
    }

    public void registerOption(@NotNull String id, @NotNull MOption option, @NotNull YMLConfig config) {
        if (!UtilsString.isLowcasedValidID(id) || options.containsKey(id))
            throw new IllegalStateException("invalid id");
        if (option.isRegistered())
            throw new IllegalStateException();
        option.setRegistered(id);
        options.put(option.getId(), option);
        optionsFile.put(option.getId(), config);
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "D Registered Option &e" + id);
    }

    public void save(@NotNull MOption mOption) {
        if (!mOption.isRegistered())
            throw new IllegalStateException();
        if (options.get(mOption.getId()) != mOption)
            throw new IllegalStateException();
        optionsFile.get(mOption.getId()).set(mOption.getId(), mOption);
        optionsFile.get(mOption.getId()).save();
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "D Updated Option &e" + mOption.getId());
    }
}
