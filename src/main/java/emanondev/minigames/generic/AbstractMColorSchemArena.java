package emanondev.minigames.generic;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import emanondev.core.ItemBuilder;
import emanondev.core.gui.Gui;
import emanondev.core.gui.LongEditorFButton;
import emanondev.core.gui.PagedMapGui;
import emanondev.core.gui.StringEditorFButton;
import emanondev.core.message.DMessage;
import emanondev.core.util.WorldEditUtility;
import emanondev.minigames.ArenaManager;
import emanondev.minigames.Configurations;
import emanondev.minigames.Minigames;
import emanondev.minigames.locations.LocationOffset3D;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractMColorSchemArena extends ARegistrable implements MSchemArena, MColorableTeamArena {

    private final String schematicName;
    private final LocationOffset3D spectatorsOffset;
    private String displayName;
    private SoftReference<Clipboard> schematicCache = null;
    private BlockVector size = null;
    private int minDurationEstimation;
    private int maxDurationEstimation;

    @Override
    public int getMinDurationEstimation() {
        return minDurationEstimation;
    }

    @Override
    public void setMinDurationEstimation(int minDurationEstimation) {
        this.minDurationEstimation = Math.max(0, minDurationEstimation);
        if (this.minDurationEstimation >= this.maxDurationEstimation)
            this.maxDurationEstimation = this.minDurationEstimation + 1;
        ArenaManager.get().save(this);
    }

    @Override
    public int getMaxDurationEstimation() {
        return maxDurationEstimation;
    }

    @Override
    public void setMaxDurationEstimation(int maxDurationEstimation) {
        this.maxDurationEstimation = Math.max(1, maxDurationEstimation);
        if (this.minDurationEstimation >= this.maxDurationEstimation)
            this.minDurationEstimation = this.maxDurationEstimation - 1;
        ArenaManager.get().save(this);
    }

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
        this.minDurationEstimation = Math.max(0, (int) map.getOrDefault("minDurationEstimation", 3));
        this.maxDurationEstimation = Math.max(this.minDurationEstimation + 1, (int) map.getOrDefault("maxDurationEstimation", 5));
    }

    public CompletableFuture<EditSession> paste(@NotNull Location location) {
        CompletableFuture<EditSession> future = new CompletableFuture<>();
        getSchematicAsync().whenComplete((val, th) -> {
            if (th != null)
                future.completeExceptionally(th);
            else {
                CompletableFuture<EditSession> f2 = WorldEditUtility.paste(
                        location, val, true, Minigames.get(), false, false, false);
                f2.whenComplete((val2, th2) -> {
                    if (th2 != null)
                        future.completeExceptionally(th2);
                    else
                        future.complete(val2);
                });
            }
        });
        //return WorldEditUtility.paste(location, getSchematic(), true, Minigames.get(), false, false, false);
        return future;
    }


    public BlockVector getSize() {
        if (size != null)
            return size.clone();
        BlockVector3 blockV = getSchematic().getDimensions();
        size = new BlockVector(blockV.getBlockX(), blockV.getBlockY(), blockV.getBlockZ());
        return size;
    }

    private Clipboard getSchematic() {
        Clipboard clip = schematicCache == null ? null : schematicCache.get();
        if (clip != null)
            return clip;
        File file = new File(ArenaManager.get().getSchematicsFolder(), schematicName);
        if (!file.isFile())
            throw new IllegalStateException("selected schematic do not exist");
        clip = WorldEditUtility.load(file);
        schematicCache = new SoftReference<>(clip);
        return clip;
    }

    private CompletableFuture<Clipboard> getSchematicAsync() {
        Clipboard clip = schematicCache == null ? null : schematicCache.get();
        if (clip != null)
            return CompletableFuture.completedFuture(clip);
        File file = new File(ArenaManager.get().getSchematicsFolder(), schematicName);
        if (!file.isFile())
            throw new IllegalStateException("selected schematic do not exist");
        CompletableFuture<Clipboard> future = WorldEditUtility.load(file, Minigames.get(), true);
        future.whenComplete((val, th) -> schematicCache = new SoftReference<>(val));
        return future;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("schematic", schematicName);
        map.put("spectatorSpawnOffset", spectatorsOffset.toString());
        map.put("minDurationEstimation", this.minDurationEstimation);
        map.put("maxDurationEstimation", this.maxDurationEstimation);

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
        ArenaManager.get().save(this);
    }


    @Override
    public Gui getEditorGui(Player target, Gui parent) {
        PagedMapGui gui = new PagedMapGui(
                new DMessage(Minigames.get(), target).appendLang("miniarena.gui.title", getPlaceholders()).toLegacy(),
                6, target, parent, Minigames.get());
        gui.addButton(new StringEditorFButton(gui, this::getDisplayName, this::setDisplayName,
                () -> new ItemBuilder(Material.MOJANG_BANNER_PATTERN).setDescription(
                        new DMessage(Minigames.get(), target)
                                .appendLangList("miniarena.gui.display_name_editor", getPlaceholders())
                ).setGuiProperty().build(), true));
        gui.addButton(new LongEditorFButton(gui, 1, 1, 100,
                () -> (long) getMinDurationEstimation(),
                (v) -> setMinDurationEstimation(v.intValue()),
                () -> Configurations.getCollectingPlayersPhaseCooldownMaxItem(gui.getTargetPlayer()).setAmount(Math.max(1, Math.min(101, getMinDurationEstimation())))
                        .setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                                "minioption.gui.min_duration_estimation", "%value%", String.valueOf(getMinDurationEstimation()))).build()));

        gui.addButton(new LongEditorFButton(gui, 1, 1, 100,
                () -> (long) getMaxDurationEstimation(),
                (v) -> setMaxDurationEstimation(v.intValue()),
                () -> Configurations.getCollectingPlayersPhaseCooldownMaxItem(gui.getTargetPlayer()).setAmount(Math.max(1, Math.min(101, getMaxDurationEstimation())))
                        .setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                                "minioption.gui.max_duration_estimation", "%value%",
                                String.valueOf(getMaxDurationEstimation()))).build()));
        return gui;
    }

}
