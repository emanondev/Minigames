package emanondev.minigames.minigames.generic;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import emanondev.core.util.WorldEditUtility;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public interface MSchemGame<T extends MTeam, A extends MSchemArena, O extends MOption> extends MGame<T, A, O> {


    @NotNull
    default Clipboard getSchematic() {
        return getArena().getSchematic();
    }

    @NotNull
    default BoundingBox getBoundingBox() {
        BlockVector3 dimension = getSchematic().getDimensions();
        return new BoundingBox(getGameLocation().x, getGameLocation().y, getGameLocation().z, getGameLocation().x + dimension.getBlockX(), getGameLocation().y + dimension.getBlockY(), getGameLocation().z + dimension.getBlockZ());
    }

    default void pasteSchematic() {
        WorldEditUtility.paste(getGameLocation().toLocation(), getSchematic());
    }
}
