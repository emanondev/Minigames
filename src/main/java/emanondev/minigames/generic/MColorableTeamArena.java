package emanondev.minigames.generic;

import org.bukkit.DyeColor;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface MColorableTeamArena extends MArena {

    @NotNull
    Set<DyeColor> getColors();
}