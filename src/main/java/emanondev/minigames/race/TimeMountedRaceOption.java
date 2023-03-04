package emanondev.minigames.race;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TimeMountedRaceOption extends ARaceOption {

    public TimeMountedRaceOption() {
        this(new HashMap<>());
    }

    public TimeMountedRaceOption(@NotNull Map<String, Object> map) {
        super(map);
    }
}
