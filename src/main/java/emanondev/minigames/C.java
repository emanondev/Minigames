package emanondev.minigames;

import emanondev.minigames.locations.LocationOffset3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public class C {

    private static int gameMinimalSpaceDistancing = 272;
    private static int startupGameInitializeDelayTicks = 80;
    private static LocationOffset3D respawnLocationCoordinates = null;
    private static String respawnLocationWorld = null;


    public static void reload() {
        gameMinimalSpaceDistancing = Math.max(Minigames.get().getConfig().getInt("game_minimal_distancing", 17 * 16), 8 * 16);
        startupGameInitializeDelayTicks = Math.max(Minigames.get().getConfig().getInt("startup_game_initialize_delay_ticks", 80), 20);
        String respawnLocationTmp = Minigames.get().getConfig().getString("respawnLocation.coordinates");
        respawnLocationWorld = Minigames.get().getConfig().getString("respawnLocation.world");
        respawnLocationCoordinates = respawnLocationTmp==null?null:LocationOffset3D.fromString(respawnLocationTmp);
    }

    public static int getGameMinimalSpaceDistancing() {
        return gameMinimalSpaceDistancing;
    }

    public static int getStartupGameInitializeDelayTicks() {
        return startupGameInitializeDelayTicks;
    }

    public static @Nullable Location getRespawnLocation() {
        if (respawnLocationWorld==null)
            return null;
        World w = Bukkit.getWorld(respawnLocationWorld);
        if (w==null)
            return null;
        return new Location(w,respawnLocationCoordinates.x,respawnLocationCoordinates.y,respawnLocationCoordinates.z,respawnLocationCoordinates.yaw,respawnLocationCoordinates.pitch);
    }

    public static void setRespawnLocation(@Nullable Location respawnLocation) {
        if (respawnLocation==null || respawnLocation.getWorld()==null){
            C.respawnLocationCoordinates = null;
            C.respawnLocationWorld = null;
            Minigames.get().getConfig().set("respawnLocation", null);
        }
        else {
            C.respawnLocationCoordinates = LocationOffset3D.fromLocation(respawnLocation);
            C.respawnLocationWorld = respawnLocation.getWorld().getName();
            Minigames.get().getConfig().set("respawnLocation.coordinates", C.respawnLocationCoordinates.toString());
            Minigames.get().getConfig().set("respawnLocation.world", C.respawnLocationWorld);
        }
    }
}
