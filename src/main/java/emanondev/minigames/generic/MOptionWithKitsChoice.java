package emanondev.minigames.generic;

import emanondev.minigames.Kit;
import emanondev.minigames.OptionManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface MOptionWithKitsChoice extends MOption {
    Collection<Kit> getKits();

    default boolean hasKit(Kit kit){
        return kit!=null && hasKitId(kit.getId());
    }

    boolean hasKitId(String kit);

    default void addKit(@NotNull Kit kit) {
        addKitId(kit.getId());
    }

    default void removeKit(@NotNull Kit kit) {
        removeKitId(kit.getId());
    }

    default void toggleKit(@NotNull Kit kit) {
        toggleKitId(kit.getId());
    }

    public void addKitId(@Nullable String kitId);

    public void removeKitId(@Nullable String kitId);

    default void toggleKitId(@Nullable String kitId) {
        if (hasKitId(kitId))
            removeKitId(kitId);
        else
            addKitId(kitId);
    }

}
