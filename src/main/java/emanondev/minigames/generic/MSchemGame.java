package emanondev.minigames.generic;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import emanondev.core.util.WorldEditUtility;
import emanondev.minigames.Minigames;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public interface MSchemGame<T extends MTeam, A extends MSchemArena, O extends MOption> extends MGame<T, A, O> {


    @NotNull
    default Clipboard getSchematic() {
        return getArena().getSchematic();
    }

    BoundingBox getBoundingBox();

    boolean containsLocation(@NotNull Location loc);

    default void pasteSchematic() {
        WorldEditUtility.paste(getGameLocation().toLocation(), getSchematic(), true, Minigames.get(), false, false, true);
    }
}
