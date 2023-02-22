package emanondev.minigames;

import emanondev.core.PlayerSnapshot;
import emanondev.core.UtilsString;
import emanondev.core.YMLConfig;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class KitManager extends Manager<Kit> {

    private static KitManager instance;

    public KitManager() {
        super("kits");
        instance = this;
    }

    public static KitManager get() {
        return instance;
    }

}
