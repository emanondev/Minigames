package emanondev.minigames;

import emanondev.minigames.games.MOption;
import emanondev.minigames.games.MType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class OptionManager extends Manager<MOption> {

    private static OptionManager instance;

    public OptionManager() {
        super("options");
        instance = this;
    }

    public static OptionManager get() {
        return instance;
    }

    public <O extends MOption> @NotNull Map<String, O> getCompatibles(@NotNull MType<?, O> type) {
        HashMap<String, O> map = new HashMap<>();
        getAll().forEach((k, v) -> {
            if (type.matchType(v)) map.put(k, (O) v);
        });
        return map;
    }

}
