package emanondev.minigames.games.eggwars;

import emanondev.minigames.games.AbstractMColorSchemArena;
import emanondev.minigames.locations.BlockLocationOffset3D;
import emanondev.minigames.locations.LocationOffset3D;
import org.bukkit.DyeColor;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SerializableAs("EggWarsArena")
public class EggWarsArena extends AbstractMColorSchemArena {

    private final Map<DyeColor, List<LocationOffset3D>> spawnLocations = new EnumMap<>(DyeColor.class);
    private final Map<DyeColor, BlockLocationOffset3D> eggs = new EnumMap<>(DyeColor.class);
    private final Map<DyeColor, LocationOffset3D> respawnLocations = new EnumMap<>(DyeColor.class);
    private final HashSet<LocationOffset3D> villagers = new HashSet<>();
    private final HashMap<String,HashMap<BlockLocationOffset3D,Integer>> generatorsRaw = new HashSet<>();
    private final HashMap<EggWarsGeneratorType,HashMap<BlockLocationOffset3D,Integer>> generators = new HashSet<>();
    private final int teamsSize;
    private final LocationOffset3D spectatorsOffset;
    private final HashSet<BoundingBox> noBuildAreas = new HashSet<>();
    /*
     * teams:
     *   <color>: //at least 2
     *     spawnoffset: (schematic offset)
     */
    public EggWarsArena(@NotNull Map<String, Object> map) {
        super(map);
        Map<String, ?> teamMap = (Map<String, ?>) map.get("teams");
        teamMap.forEach((k, v) -> spawnLocations.put(DyeColor.valueOf(k), LocationOffset3D.fromString((String) ((Map<String, ?>) v).get("spawnOffset"))));
        if (spawnLocations.size() < 2)
            throw new IllegalStateException("not enough teams");


        LinkedHashMap<String, Map<String, Object>> teamsMap = new LinkedHashMap<>();
        for (DyeColor color : teams) {
            HashMap<String, Object> teamMap = new HashMap<>();
            List<String> wrap = new ArrayList<>();
            teamsSpawns.get(color).forEach(loc -> wrap.add(loc.toString()));
            teamMap.put("spawn_loc", wrap);
            teamMap.put("respawn_loc", teamsRespawn.get(color).toString());
            teamMap.put("egg", teamsEgg.get(color).toString());
            teamsMap.put(color.name(), teamMap);
        }
        map.put("teams", teamsMap);
        List<String> wrap = new ArrayList<>();
        villagers.forEach(v -> wrap.add(v.toString()));
        map.put("villagers", wrap);
        HashMap<String, Map<String, Integer>> genMap = new HashMap<>();
        for (EggWarsGeneratorType type : generators.keySet()) {
            HashMap<String, Integer> subMap = new HashMap<>();
            Map<BlockLocationOffset3D, Integer> generatorsOfType = generators.get(type);
            generatorsOfType.forEach((k, v) -> subMap.put(k.toString(), v));
            genMap.put(type.getType(), subMap);
        }
        map.put("generators", genMap);
        noBuildAreas.forEach((box) -> box.shift(getArea().getMin().multiply(-1)));
        map.put("no_build_areas", noBuildAreas);
        map.put("team_size", teamsSize);
        map.put("spectator_spawn_offset", spectatorsOffset.toString());

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
}
