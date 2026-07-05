package emanondev.minigames.util;

import emanondev.core.util.ParticleUtility;
import emanondev.minigames.locations.LocationOffset3D;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ParticleHelper {

    public static void displayTeamHomes(Player p,
                                        Map<DyeColor, LocationOffset3D> teams,
                                        Vector min,
                                        long timerTick) {
        if (teams == null || teams.isEmpty()) {
            return;
        }
        teams.forEach((color, offset) ->
                ParticleUtility.spawnParticleCircle(p, Particle.DUST,
                        min.getX() + offset.x, min.getY() + offset.y, min.getZ() + offset.z,
                        0.4, timerTick % 4 == 0,
                        new Particle.DustOptions(color.getColor(), 1F)));

    }

    public static void displayTeamsSpawns(Player p,
                                          Map<DyeColor, List<LocationOffset3D>> teams,
                                          Vector min,
                                          long timerTick) {
        teams.forEach((color, values) -> {
            int index = 0;
            for (LocationOffset3D v : values) {
                index++;
                ParticleUtility.spawnParticleLine(p, Particle.DUST,
                        min.getX() + v.x, min.getY() + v.y + 1.62, min.getZ() + v.z,
                        v.getDirection().multiply(0.25), 1, 3, new Particle.DustOptions(color.getColor(), 1F));
                ParticleUtility.spawnParticleCircle(p, Particle.DUST,
                        min.getX() + v.x, min.getY() + v.y, min.getZ() + v.z,
                        0.4, timerTick % 4 == 0, new Particle.DustOptions(color.getColor(), 1F));
                if (timerTick % 4 == 0)
                    for (int i = 0; i <= index; i++)
                        ParticleUtility.spawnParticle(p, Particle.HEART, min.getX() + v.x, min.getY() +
                                v.y + 0.5 + (0.5 * (i)), min.getZ() + v.z);
            }
        });

    }

    public static void displaySpectatorHome(Player p,
                                            LocationOffset3D offset,
                                            Vector min,
                                            long timerTick) {
        if (offset == null) {
            return;
        }
        ParticleUtility.spawnParticleCircle(p, Particle.WAX_ON,
                min.getX() + offset.x, min.getY() + offset.y, min.getZ() + offset.z,
                0.4, timerTick % 4 == 0);
        if (timerTick % 4 == 0) {
            ParticleUtility.spawnParticle(p, Particle.SCULK_SOUL,
                    min.getX() + offset.x, min.getY() + offset.y + 1, min.getZ() + offset.z);
        }
    }

    public static void displayFallAreas(Player p, Collection<BoundingBox> fallAreas, int timerTick) {
        if (fallAreas == null || fallAreas.isEmpty()) {
            return;
        }
        fallAreas.forEach((fallArea) -> ParticleUtility.spawnParticleBoxFaces(p, timerTick, Particle.FLAME, fallArea, null));
    }


    public static void displayCheckPoints(Player p, List<BoundingBox> checkPoints, List<LocationOffset3D> checkPointsRespawn, BoundingBox endArea, Vector min, int timerTick) {
        for (int i = 0; i < checkPoints.size(); i++) {
            ParticleUtility.spawnParticleBoxEdges(p, Particle.DUST, checkPoints.get(i), new Particle.DustOptions(getCheckpointColor(i), 1F));
        }
        for (int j = 0; j < checkPointsRespawn.size(); j++) {
            ParticleUtility.spawnParticleCircle(p, Particle.DUST, min.getX() + checkPointsRespawn.get(j).x, min.getY() +
                            checkPointsRespawn.get(j).y, min.getZ() + checkPointsRespawn.get(j).z, 0.4,
                    timerTick % 4 == 0, new Particle.DustOptions(getCheckpointColor(j), 0.5F));
            if (timerTick % 4 == 0) {
                for (int i = 0; i <= j; i++) {
                    ParticleUtility.spawnParticle(p, Particle.HEART, min.getX() + checkPointsRespawn.get(j).x, min.getY() +
                            checkPointsRespawn.get(j).y + 0.5 + (0.5 * (i)), min.getZ() + checkPointsRespawn.get(j).z);
                }
            }
        }
        if (endArea != null) {
            ParticleUtility.spawnParticleBoxEdges(p, Particle.DUST, endArea, new Particle.DustOptions(getCheckpointColor(checkPoints.size()), 2F));
        }
    }

    public static void displayNoBuildAreas(Player p, List<BoundingBox> noBuildAreas, int timerTick) {
        noBuildAreas.forEach((area) -> ParticleUtility.spawnParticleBoxFaces(p, timerTick, Particle.FLAME, area, null));
    }


    private static Color getCheckpointColor(int i) {
        return switch (i % 10) {
            case 0 -> Color.fromBGR(Integer.decode("0xeb3434"));
            case 1 -> Color.fromBGR(Integer.decode("0xeb9f34"));
            case 2 -> Color.fromBGR(Integer.decode("0xebe234"));
            case 3 -> Color.fromBGR(Integer.decode("0xb4eb34"));
            case 4 -> Color.fromBGR(Integer.decode("0x34eb56"));
            case 5 -> Color.fromBGR(Integer.decode("0x34aeeb"));
            case 6 -> Color.fromBGR(Integer.decode("0x3440eb"));
            case 7 -> Color.fromBGR(Integer.decode("0x8334eb"));
            case 8 -> Color.fromBGR(Integer.decode("0xc934eb"));
            case 9 -> Color.fromBGR(Integer.decode("0xeb348f"));
            default -> throw new IllegalStateException("Unexpected value: " + i % 10);
        };
    }

}
