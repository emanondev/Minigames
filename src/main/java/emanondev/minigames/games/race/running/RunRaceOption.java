package emanondev.minigames.games.race.running;

import emanondev.minigames.games.race.ARaceOption;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@SerializableAs(value = "RunRaceOption")
public class RunRaceOption extends ARaceOption {

    public RunRaceOption() {
        this(new HashMap<>());
    }

    public RunRaceOption(@NotNull Map<String, Object> map) {
        super(map);
    }
}
