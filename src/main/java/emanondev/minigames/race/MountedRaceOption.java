package emanondev.minigames.race;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MountedRaceOption extends ARaceOption {

    public MountedRaceOption() {
        this(new HashMap<>());
    }

    public MountedRaceOption(@NotNull Map<String, Object> map) {
        super(map);
    }
}
