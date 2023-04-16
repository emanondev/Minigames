package emanondev.minigames.games;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Registrable {

    boolean isRegistered();

    /**
     * internal use only
     *
     * @param id if of this object
     */
    void setRegistered(@NotNull String id);

    /**
     * The id is null if this object is not registered
     *
     * @return id
     */
    @Nullable
    String getId();

    /**
     * internal use only
     */
    void setUnregister();

}
