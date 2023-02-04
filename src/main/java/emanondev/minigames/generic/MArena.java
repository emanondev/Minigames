package emanondev.minigames.generic;

import emanondev.minigames.locations.LocationOffset3D;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MArena extends ConfigurationSerializable, Cloneable, Registrable {

    @NotNull LocationOffset3D getSpectatorsOffset();

    @NotNull String getDisplayName();

    void setDisplayName(@Nullable String displayName);

}
