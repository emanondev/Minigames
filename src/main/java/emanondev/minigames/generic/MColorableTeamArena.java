package emanondev.minigames.generic;

import emanondev.minigames.locations.LocationOffset3D;
import org.bukkit.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface MColorableTeamArena extends MArena {

    @NotNull
    Set<DyeColor> getColors();

    @Nullable
    LocationOffset3D getSpawnOffset(@NotNull DyeColor color);
}