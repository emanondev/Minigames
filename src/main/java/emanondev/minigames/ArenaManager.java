package emanondev.minigames;

import emanondev.core.UtilsString;
import emanondev.minigames.games.MArena;
import emanondev.minigames.games.MArenaBuilder;
import emanondev.minigames.games.MType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class ArenaManager extends Manager<MArena> {

    private static ArenaManager instance;

    public static ArenaManager get() {
        return instance;
    }

    private final HashMap<UUID, MArenaBuilder> builders = new HashMap<>();

    public ArenaManager() {
        super("arenas");
        instance = this;
    }

    public <A extends MArena> @NotNull Map<String, A> getCompatibleArenas(@NotNull MType<A, ?> type) {
        HashMap<String, A> map = new HashMap<>();
        getAll().forEach((k, v) -> {
            if (type.matchType(v)) map.put(k, (A) v);
        });
        return map;
    }

    public boolean registerBuilder(@NotNull UUID who, @NotNull String id, @NotNull String label, @NotNull MType<?, ?> type) {
        if (!UtilsString.isLowcasedValidID(id) || get(id) != null || builders.containsKey(who))
            return false;
        builders.put(who, type.getArenaBuilder(who, id, label));
        return true;
    }

    public void unregisterBuilder(@NotNull UUID who) {
        MArenaBuilder val = builders.remove(who);
        val.abort();
    }

    public File getSchematicsFolder() {
        return new File(Minigames.get().getDataFolder(), "schematics");
    }

    public void onArenaBuilderCompletedArena(@NotNull MArenaBuilder builder) {
        MArena arena = builder.build();
        register(builder.getId(), arena, Bukkit.getOfflinePlayer(builder.getUser()));
        save(arena);
        builders.remove(builder.getUser());
        builder.abort();
    }

    public boolean isBuilding(OfflinePlayer player) {
        return isBuilding(player.getUniqueId());
    }

    public MArenaBuilder getBuildingArena(OfflinePlayer player) {
        return getBuildingArena(player.getUniqueId());
    }

    public boolean isBuilding(UUID player) {
        return builders.containsKey(player);
    }

    public MArenaBuilder getBuildingArena(UUID player) {
        return builders.get(player);
    }

    public MArenaBuilder getBuildingArenaById(String id) {
        for (MArenaBuilder ab : builders.values())
            if (ab.getId().equalsIgnoreCase(id))
                return ab;
        return null;
    }

    public Collection<MArenaBuilder> getAllBuildings() {
        return Collections.unmodifiableCollection(builders.values());
    }

}
