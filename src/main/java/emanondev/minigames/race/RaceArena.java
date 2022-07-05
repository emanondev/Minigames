package emanondev.minigames.race;

import emanondev.minigames.generic.AbstractMColorSchemArena;
import emanondev.minigames.locations.LocationOffset3D;
import org.bukkit.DyeColor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RaceArena extends AbstractMColorSchemArena {
    private final Map<DyeColor, LocationOffset3D> spawnLocations = new EnumMap<>(DyeColor.class);

    public RaceArena(@NotNull Map<String, Object> map) {
        super(map);
        Map<String, ?> teamMap = (Map<String, ?>) map.get("teams");
        teamMap.forEach((k, v) -> spawnLocations.put(DyeColor.valueOf(k), LocationOffset3D.fromString((String) ((Map<String, ?>) v).get("spawnOffset"))));

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
