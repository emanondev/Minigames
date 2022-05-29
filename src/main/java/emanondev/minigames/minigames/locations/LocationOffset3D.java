package emanondev.minigames.minigames.locations;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

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

    public static LocationOffset3D fromLocation(Location loc) {
        return new LocationOffset3D(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public Location add(BlockLocation3D bLoc) {
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

    public static @NotNull LocationOffset3D fromString(@NotNull String from) {
        String[] args = from.split(":");
        return new LocationOffset3D(Double.parseDouble(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2])
                , Float.parseFloat(args[3]), Float.parseFloat(args[4]));

    }

    @Override
    public @NotNull String toString() {
        return x + ":" + y + ":" + z + ":" + yaw + ":" + pitch;
    }
}
