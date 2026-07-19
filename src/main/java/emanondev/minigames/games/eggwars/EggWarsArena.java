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
    private final Set<LocationOffset3D> villagers = new HashSet<>();
    private final Map<String, Map<BlockLocationOffset3D, Integer>> generatorsRaw = new HashMap<>();
    private final Map<EggWarsGeneratorType, Map<BlockLocationOffset3D, Integer>> generators = new HashMap<>();
    private final Set<BoundingBox> noBuildAreas = new HashSet<>();
    private final int teamsSize;
    private final LocationOffset3D spectatorsOffset;

    /*
     * teams:
     *   <color>: //at least 2
     *     spawnoffset: (schematic offset)
     *     eggoffsetblock: (schematic offset)
     *     respawnoffset: (schematic offset)
     *  villagersoffsets:
     *   -
     *   -
     * generators:
     *   <type>: //ex oro
     *     <location>: level
     *     <location2>: level
     * spectatorsOffset:  (schematic offset)
     * areenoBuild:
     *
     */
    public EggWarsArena(@NotNull Map<String, Object> map) {
        super(map);
        Map<String, Object> teamsMap = (Map<String, Object>) map.get("teams");
        for (String key : teamsMap.keySet()) {
            DyeColor dyeColor = DyeColor.valueOf(key);
            Map<String, Object> teamMap = (Map<String, Object>) map.get(key);
            List<String> rawSpawnLocations = (List<String>) teamMap.get("spawn_loc");

            spawnLocations.put(dyeColor, rawSpawnLocations.stream().map(LocationOffset3D::fromString).toList());
            respawnLocations.put(dyeColor, LocationOffset3D.fromString((String) teamMap.get("respawn_loc")));
            eggs.put(dyeColor, (BlockLocationOffset3D) teamMap.get("egg"));
        }
        List<String> rawVillagers = (List<String>) teamsMap.get("villagers");
        villagers.addAll(rawVillagers.stream().map(LocationOffset3D::fromString).toList());

        Map<String, Object> rawGenerators = (Map<String, Object>) map.get("generators");
        for (String rawType : rawGenerators.keySet()) {
            Map<BlockLocationOffset3D, Integer> generatorTypeLocations = new HashMap<>();

            Map<String, Integer> rawLocs = (Map<String, Integer>) rawGenerators.get(rawType);

            rawLocs.forEach((key, value) -> generatorTypeLocations.put(BlockLocationOffset3D.fromString(key), value));
            generatorsRaw.put(rawType, generatorTypeLocations);
        }

        List<BoundingBox> noBuildAreas = (List<BoundingBox>) map.get("no_build_areas");
        if (noBuildAreas != null) {
            this.noBuildAreas.addAll(noBuildAreas);
        }

        teamsSize = (Integer) map.get("team_size");

        spectatorsOffset = LocationOffset3D.fromString((String) map.get("spectator_spawn_offset"));
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
        return spawnLocations.get(color).getFirst(); //TODO randomize?
    }
}
