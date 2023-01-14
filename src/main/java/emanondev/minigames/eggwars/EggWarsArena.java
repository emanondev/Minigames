package emanondev.minigames.eggwars;

import emanondev.minigames.generic.AbstractMColorSchemArena;
import emanondev.minigames.locations.LocationOffset3D;
import org.bukkit.DyeColor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EggWarsArena extends AbstractMColorSchemArena {

    /*
     * teams:
     *   <color>: //at least 2
     *     spawnoffset: (schematic offset)
     */
    public EggWarsArena(@NotNull Map<String, Object> map) {
        super(map);
        Map<String, ?> teamMap = (Map<String, ?>) map.get("teams");
        teamMap.forEach((k, v) -> {
            spawnLocations.put(DyeColor.valueOf(k),
                    LocationOffset3D.fromString((String) ((Map<String, ?>) v).get("spawnOffset")));
            eggLocations.put(DyeColor.valueOf(k),
                    LocationOffset3D.fromString((String) ((Map<String, ?>) v).get("eggOffset")));
        });

        if (spawnLocations.size() < 2)
            throw new IllegalStateException("not enough teams");
    }

    private final Map<DyeColor, LocationOffset3D> spawnLocations = new EnumMap<>(DyeColor.class);
    private final Map<DyeColor, LocationOffset3D> eggLocations = new EnumMap<>(DyeColor.class);

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        LinkedHashMap<String, Object> teams = new LinkedHashMap<>();
        map.put("teams", teams);
        for (DyeColor color : spawnLocations.keySet()) {
            Map<String, Object> teamInfo = new HashMap<>();
            teamInfo.put("spawnOffset", spawnLocations.get(color).toString());
            teamInfo.put("eggOffset", eggLocations.get(color).toString());
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

    @NotNull
    public LocationOffset3D getEggOffset(@NotNull DyeColor color) {
        if (!eggLocations.containsKey(color))
            throw new NullPointerException();
        return eggLocations.get(color);
    }


}
