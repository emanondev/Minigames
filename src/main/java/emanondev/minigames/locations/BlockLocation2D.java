package emanondev.minigames.locations;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class BlockLocation2D implements ConfigurationSerializable {

    public final String worldName;
    public final int x;
    public final int z;

    public BlockLocation2D(@NotNull World w, int x, int z) {
        this(w.getName(), x, z);
    }

    public BlockLocation2D(@NotNull String w, int x, int z) {
        this.worldName = w;
        this.x = x;
        this.z = z;
    }

    public @Nullable World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    protected BlockLocation2D(@NotNull Map<String, Object> map) {
        this.worldName = (String) map.get("world");
        this.x = (int) map.get("x");
        this.z = (int) map.get("z");
        if (worldName == null)
            throw new IllegalStateException("invalid world");
    }


    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("world", worldName);
        map.put("x", x);
        map.put("z", z);
        return map;
    }

    public static @NotNull BlockLocation2D fromString(@NotNull String from) {
        String[] args = from.split(":");
        return new BlockLocation2D(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
    }

    @Override
    public @NotNull String toString() {
        return worldName + ":" + x + ":" + z;
    }
}
