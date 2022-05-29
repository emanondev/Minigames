package emanondev.minigames.minigames;

import emanondev.minigames.minigames.generic.MType;
import emanondev.minigames.minigames.skywars.SkyWarsType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class MinigameTypes {

    public MinigameTypes() {
        instance = this;
        register(SKYWARS);
    }

    private void register(@NotNull MType type) {
        //TODO check no clones, //TODO valid id
        types.put(type.getType(), type);
    }

    private static MinigameTypes instance;

    public static MinigameTypes get() {
        return instance;
    }

    private final HashMap<String, MType> types = new HashMap<>();

    public final @NotNull Collection<MType> getTypes() {
        return Collections.unmodifiableCollection(types.values());
    }

    public final @NotNull Set<String> getTypesId() {
        return Collections.unmodifiableSet(types.keySet());
    }

    public @Nullable MType getType(String id) {
        return types.get(id.toLowerCase());
    }

    public static final SkyWarsType SKYWARS = new SkyWarsType();
}
