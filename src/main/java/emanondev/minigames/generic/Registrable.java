package emanondev.minigames.generic;

import org.jetbrains.annotations.NotNull;

public interface Registrable {

    boolean isRegistered();

    /**
     * internal use only
     *
     * @param id
     */
    void setRegistered(@NotNull String id);

    String getId();

    /**
     * internal use only
     */
    void setUnregister();

}
