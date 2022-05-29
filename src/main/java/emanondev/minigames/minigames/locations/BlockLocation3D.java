package emanondev.minigames.minigames.locations;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BlockLocation3D extends BlockLocation2D {

    public final int y;

    public BlockLocation3D(@NotNull World w, int x, int y, int z) {
        super(w, x, z);
        this.y = y;
    }

    public BlockLocation3D(@NotNull String w, int x, int y, int z) {
        super(w, x, z);
        this.y = y;
    }

    public Location toLocation() {
        return new Location(getWorld(), x, y, z);
    }


    private BlockLocation3D(@NotNull Map<String, Object> map) {
        super(map);
        this.y = (int) map.get("y");
    }


    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("y", y);
        return map;
    }


    public static @NotNull BlockLocation3D fromString(@NotNull String from) {
        String[] args = from.split(":");
        return new BlockLocation3D(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
    }

    @Override
    public @NotNull String toString() {
        return worldName + ":" + x + ":" + y + ":" + z;
    }
}
