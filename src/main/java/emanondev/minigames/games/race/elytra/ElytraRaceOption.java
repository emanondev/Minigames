package emanondev.minigames.games.race.elytra;

import emanondev.minigames.games.race.ARaceOption;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@SerializableAs(value = "ElytraRaceOption")
public class ElytraRaceOption extends ARaceOption {

    public ElytraRaceOption() {
        this(new HashMap<>());
    }

    public ElytraRaceOption(@NotNull Map<String, Object> map) {
        super(map);
    }
}
