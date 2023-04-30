package emanondev.minigames.locations;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class BlockLocationOffset3D implements ConfigurationSerializable {

    public final int x;
    public final int y;
    public final int z;

    public BlockLocationOffset3D(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @NotNull
    @Contract("_ -> new")
    public static BlockLocationOffset3D fromLocation(@NotNull Location loc) {
        return new BlockLocationOffset3D(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockLocationOffset3D that = (BlockLocationOffset3D) o;
        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    private BlockLocationOffset3D(@NotNull Map<String, Object> map) {
        this.x = (int) map.get("x");
        this.y = (int) map.get("y");
        this.z = (int) map.get("z");
    }

    @NotNull
    public static BlockLocationOffset3D fromString(@NotNull String from) {
        String[] args = from.split(":");
        return new BlockLocationOffset3D(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
    }

    @NotNull
    public Location add(@NotNull BlockLocation3D bLoc) {
        return new Location(bLoc.getWorld(), bLoc.x + x, bLoc.y + y, bLoc.z + z);
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("x", x);
        map.put("y", y);
        map.put("z", z);
        return map;
    }

    @Override
    @NotNull
    public String toString() {
        return x + ":" + y + ":" + z;
    }
}
