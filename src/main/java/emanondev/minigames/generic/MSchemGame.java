package emanondev.minigames.generic;

import com.sk89q.worldedit.EditSession;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface MSchemGame<T extends MTeam, A extends MSchemArena, O extends MOption> extends MGame<T, A, O> {


    /*
    @NotNull
    default Clipboard getSchematic() {
        return getArena().getSchematic();
    }*/

    BoundingBox getBoundingBox();

    boolean containsLocation(@NotNull Location loc);

    default CompletableFuture<EditSession> pasteSchematic() {
        return getArena().paste(getGameLocation().toLocation());//WorldEditUtility.paste(getGameLocation().toLocation(), getSchematic(), true, Minigames.get(), false, false, true);
    }
}
