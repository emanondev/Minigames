package emanondev.minigames.generic;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import emanondev.core.UtilsString;
import emanondev.core.util.WorldEditUtility;
import emanondev.minigames.Minigames;
import emanondev.minigames.locations.LocationOffset3D;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractMColorSchemArena implements MSchemArena, MColorableTeamArena {

    private final String schematicName;
    private final LocationOffset3D spectatorsOffset;

    /*
     * schematic: (name)
     * spectatorSpawnOffset: (location)
     */
    public AbstractMColorSchemArena(@NotNull Map<String, Object> map) {
        schematicName = (String) map.get("schematic");
        if (schematicName == null)
            throw new IllegalStateException();
        spectatorsOffset = LocationOffset3D.fromString((String) map.get("spectatorSpawnOffset"));
        if (spectatorsOffset == null)
            throw new IllegalStateException();
    }

    private Clipboard schematicCache;

    @Override
    public Clipboard getSchematic() {
        if (schematicCache != null)
            return schematicCache;
        File file = new File(Minigames.get().getDataFolder(), "schematics" + File.separatorChar + schematicName);
        if (!file.isFile())
            throw new IllegalStateException("selected schematic do not exist");
        schematicCache = WorldEditUtility.load(file);
        return schematicCache;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("schematic", schematicName);
        map.put("spectatorSpawnOffset", spectatorsOffset.toString());
        return map;
    }


    private String id = null;

    public boolean isRegistered() {
        return id != null;
    }

    public void setRegistered(@NotNull String id) {
        if (!UtilsString.isLowcasedValidID(id))
            throw new IllegalStateException();
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setUnregister() {
        id = null;
    }

    @Override
    public LocationOffset3D getSpectatorsOffset() {
        return spectatorsOffset;
    }
}
