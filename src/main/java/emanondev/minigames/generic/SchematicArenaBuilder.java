package emanondev.minigames.generic;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class SchematicArenaBuilder extends MArenaBuilder {


    private World world;
    private BoundingBox area;
    protected int phase = 1;

    public SchematicArenaBuilder(@NotNull UUID user, @NotNull String id) {
        super(user, id);
    }


    public boolean isInside(@NotNull Location loc) {
        return loc.getWorld().equals(world) && area.contains(loc.toVector());
    }

    @Override
    public MArena build() {
        return null;
    }

    protected void setArea(@NotNull Player player) throws IncompleteRegionException {
        world = player.getWorld();
        Region sel = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player))
                .getSelection(BukkitAdapter.adapt(world));
        area = new BoundingBox(sel.getMinimumPoint().getX(), sel.getMinimumPoint().getY(),
                sel.getMinimumPoint().getZ(), sel.getMaximumPoint().getX(), sel.getMaximumPoint().getY(),
                sel.getMaximumPoint().getZ());
    }

    public BoundingBox getArea() {
        return area;
    }

    public World getWorld() {
        return world;
    }
}
