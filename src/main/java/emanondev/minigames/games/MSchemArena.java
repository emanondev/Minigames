package emanondev.minigames.games;

import com.sk89q.worldedit.EditSession;
import org.bukkit.Location;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public interface MSchemArena extends MArena {

    @NotNull CompletableFuture<EditSession> paste(@NotNull Location location);

    @NotNull BlockVector getSize();

    @NotNull File getSchematicFile();

    void invalidateCache();
}
