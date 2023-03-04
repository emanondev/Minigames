package emanondev.minigames.race;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class RaceOption extends ARaceOption {

    public RaceOption() {
        this(new HashMap<>());
    }

    public RaceOption(@NotNull Map<String, Object> map) {
        super(map);
    }
}
