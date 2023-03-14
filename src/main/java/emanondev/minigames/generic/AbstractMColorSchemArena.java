package emanondev.minigames.generic;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import emanondev.core.ItemBuilder;
import emanondev.core.gui.Gui;
import emanondev.core.gui.PagedMapGui;
import emanondev.core.gui.StringEditorFButton;
import emanondev.core.gui.TextEditorFButton;
import emanondev.core.message.DMessage;
import emanondev.core.util.WorldEditUtility;
import emanondev.minigames.ArenaManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.locations.LocationOffset3D;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class AbstractMColorSchemArena extends ARegistrable implements MSchemArena, MColorableTeamArena {

    private final String schematicName;
    private final LocationOffset3D spectatorsOffset;
    private String displayName;
    private Clipboard schematicCache;

    /*
     * schematic: (name)
     * spectatorSpawnOffset: (location)
     */
    public AbstractMColorSchemArena(@NotNull Map<String, Object> map) {
        schematicName = (String) map.get("schematic");
        if (schematicName == null)
            throw new IllegalStateException();
        spectatorsOffset = LocationOffset3D.fromString((String) map.get("spectatorSpawnOffset"));
        this.displayName = (String) map.get("displayName");
    }

    @Override
    public Clipboard getSchematic() {
        if (schematicCache != null)
            return schematicCache;
        File file = new File(ArenaManager.get().getSchematicsFolder(), schematicName);
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

    @Override
    public @NotNull LocationOffset3D getSpectatorsOffset() {
        return spectatorsOffset;
    }

    @Override
    @NotNull
    public String getDisplayName() {
        return displayName != null ? displayName : getId() != null ? getId() : "";
    }

    @Override
    public void setDisplayName(@Nullable String displayName) {
        this.displayName = displayName;
    }


    @Override
    public Gui getEditorGui(Player target, Gui parent) {
        PagedMapGui gui = new PagedMapGui(
                new DMessage(Minigames.get(), target).appendLang("miniarena.gui.title", getPlaceholders()).toLegacy(),
                6, target, parent, Minigames.get());
        gui.addButton(new StringEditorFButton(gui, this::getDisplayName, this::setDisplayName,
                ()->new ItemBuilder(Material.MOJANG_BANNER_PATTERN).setDescription(
                        new DMessage(Minigames.get(), target)
                                .appendLangList("miniarena.gui.display_name_editor",getPlaceholders())
                ).setGuiProperty().build(),true));
        return gui;
    }
}
