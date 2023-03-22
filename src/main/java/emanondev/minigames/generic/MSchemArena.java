package emanondev.minigames.generic;

import com.sk89q.worldedit.EditSession;
import org.bukkit.Location;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface MSchemArena extends MArena {

    CompletableFuture<EditSession> paste(@NotNull Location location);

    BlockVector getSize();
}
