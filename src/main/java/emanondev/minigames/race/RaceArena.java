package emanondev.minigames.race;

import emanondev.minigames.generic.AbstractMColorSchemArena;
import emanondev.minigames.locations.LocationOffset3D;
import org.bukkit.DyeColor;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RaceArena extends AbstractMColorSchemArena {
    private final Map<DyeColor, LocationOffset3D> spawnLocations = new EnumMap<>(DyeColor.class);
    private final List<BoundingBox> checkpoints = new ArrayList<>();
    private final List<LocationOffset3D> checkpointsRespawn = new ArrayList<>();
    private final BoundingBox finishArea;
    private final List<BoundingBox> fallAreas = new ArrayList<>();

    public RaceArena(@NotNull Map<String, Object> map) {
        super(map);
        Map<String, ?> teamMap = (Map<String, ?>) map.get("teams");
        teamMap.forEach((k, v) -> spawnLocations.put(DyeColor.valueOf(k), LocationOffset3D.fromString((String) ((Map<String, ?>) v).get("spawnOffset"))));
        List<BoundingBox> checkpoints = (List<BoundingBox>) map.get("checkpoints");
        if (checkpoints!=null)
            this.checkpoints.addAll(checkpoints);
        List<LocationOffset3D> checkpointsRespawn =  (List<LocationOffset3D>) map.get("checkpoints_respawn");
        if (checkpointsRespawn!=null)
            this.checkpointsRespawn.addAll(checkpointsRespawn);
        this.finishArea = (BoundingBox) map.get("end_area");
        if (finishArea==null)
            throw new IllegalStateException();
        List<BoundingBox> fallAreas = (List<BoundingBox>) map.get("fall_areas");
        if (fallAreas!=null)
            this.fallAreas.addAll(fallAreas);
        if (checkpoints.size()!=checkpointsRespawn.size())
            throw new IllegalArgumentException();
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        LinkedHashMap<String, Object> teams = new LinkedHashMap<>();
        map.put("teams", teams);
        for (DyeColor color : spawnLocations.keySet()) {
            Map<String, Object> teamInfo = new HashMap<>();
            teamInfo.put("spawnOffset", spawnLocations.get(color).toString());
            teams.put(color.name(), teamInfo);
        }
        map.put("checkpoints", checkpoints);
        map.put("checkpoints_respawn", checkpointsRespawn);
        map.put("end_area",finishArea);
        map.put("fall_areas",fallAreas);
        return map;
    }

    @NotNull
    public Set<DyeColor> getColors() {
        return Collections.unmodifiableSet(spawnLocations.keySet());
    }

    @NotNull
    public LocationOffset3D getSpawnOffset(@NotNull DyeColor color) {
        if (!spawnLocations.containsKey(color))
            throw new NullPointerException();
        return spawnLocations.get(color);
    }

    @Contract (" -> new")
    public List<BoundingBox> getCheckpoints(){
        List<BoundingBox> list = new ArrayList<>();
        for (BoundingBox box:checkpoints)
            list.add(box.clone());
        return list;
    }

    @Contract (" -> new")
    public List<BoundingBox> getFallAreas(){
        List<BoundingBox> list = new ArrayList<>();
        for (BoundingBox box:fallAreas)
            list.add(box.clone());
        return list;
    }

    @Contract (" -> new")
    public BoundingBox getFinishArea(){
        return finishArea.clone();
    }

    @Contract (" -> new")
    public List<LocationOffset3D> getCheckpointsRespawn(){
        return checkpointsRespawn;
    }
}
