package emanondev.minigames.locations;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class LocationOffset3D implements ConfigurationSerializable {

    public final double x;
    public final double y;
    public final double z;
    public final float yaw;
    public final float pitch;

    public LocationOffset3D(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @NotNull
    @Contract("_ -> new")
    public static LocationOffset3D fromLocation(@NotNull Location loc) {
        return new LocationOffset3D(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    @NotNull
    @Contract("_ -> new")
    public Location add(@NotNull BlockLocation3D bLoc) {
        return new Location(bLoc.getWorld(), bLoc.x + x, bLoc.y + y, bLoc.z + z, yaw, pitch);
    }

    private LocationOffset3D(@NotNull Map<String, Object> map) {
        this.x = (double) map.get("x");
        this.y = (double) map.get("y");
        this.z = (double) map.get("z");
        this.yaw = (float) map.get("yaw");
        this.pitch = (float) map.get("pitch");
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("x", x);
        map.put("y", y);
        map.put("z", z);
        map.put("yaw", yaw);
        map.put("pitch", pitch);
        return map;
    }

    @NotNull
    @Contract("_ -> new")
    public static LocationOffset3D fromString(@NotNull String from) {
        String[] args = from.split(":");
        return new LocationOffset3D(Double.parseDouble(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2])
                , Float.parseFloat(args[3]), Float.parseFloat(args[4]));

    }

    @Override
    @NotNull
    public String toString() {
        return x + ":" + y + ":" + z + ":" + yaw + ":" + pitch;
    }
}
