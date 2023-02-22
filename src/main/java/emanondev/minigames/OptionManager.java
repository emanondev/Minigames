package emanondev.minigames;

import emanondev.minigames.generic.MOption;
import emanondev.minigames.generic.MType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class OptionManager extends Manager<MOption> {

    private static OptionManager instance;

    public static OptionManager get() {
        return instance;
    }

    public OptionManager() {
        super("options");
        instance = this;
    }


    public <O extends MOption> @NotNull Map<String, O> getCompatibles(@NotNull MType<?, O> type) {
        HashMap<String, O> map = new HashMap<>();
        getAll().forEach((k, v) -> {
            if (type.matchType(v)) map.put(k, (O) v);
        });
        return map;
    }

}
