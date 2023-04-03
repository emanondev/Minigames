package emanondev.minigames.generic;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import emanondev.core.CorePlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public abstract class SchematicArenaBuilder extends MArenaBuilder {


    private World world;
    private BoundingBox area;


    public SchematicArenaBuilder(@NotNull UUID user, @NotNull String id, @NotNull String label, @NotNull CorePlugin plugin) {
        super(user, id, label, plugin);
    }


    public boolean isInside(@NotNull Location loc) {
        return Objects.equals(loc.getWorld(), world) && area.contains(loc.toVector());
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
        return area.clone();
    }

    public Vector getAreaMin() {
        return getArea().getMin();
    }

    public World getWorld() {
        return world;
    }

    public BoundingBox getWorldEditSection(Player p) {
        try {
            Region sel = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(p))
                    .getSelection(BukkitAdapter.adapt(p.getWorld()));
            return new BoundingBox(sel.getMinimumPoint().getX(), sel.getMinimumPoint().getY(),
                    sel.getMinimumPoint().getZ(), sel.getMaximumPoint().getX(), sel.getMaximumPoint().getY(),
                    sel.getMaximumPoint().getZ());
        } catch (Exception e) {
            return null;
        }
    }


}
