package emanondev.minigames.generic;

import emanondev.core.UtilsString;
import org.jetbrains.annotations.NotNull;

public abstract class ARegistrable implements Registrable {

    private String id = null;

    public final boolean isRegistered() {
        return id != null;
    }

    public final void setRegistered(@NotNull String id) {
        if (!UtilsString.isLowcasedValidID(id))
            throw new IllegalStateException();
        this.id = id;
    }

    public final @NotNull String getId() {
        return id;
    }

    public final void setUnregister() {
        id = null;
    }
}
