package emanondev.minigames.race.timedrace;

import emanondev.minigames.race.ARaceOption;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TimeRaceOption extends ARaceOption {

    public TimeRaceOption() {
        this(new HashMap<>());
    }

    public TimeRaceOption(@NotNull Map<String, Object> map) {
        super(map);
    }
}
