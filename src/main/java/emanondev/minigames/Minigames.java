package emanondev.minigames;

import emanondev.core.CorePlugin;
import emanondev.minigames.commands.*;
import emanondev.minigames.minigames.commands.*;
import emanondev.minigames.locations.BlockLocation2D;
import emanondev.minigames.locations.BlockLocation3D;
import emanondev.minigames.locations.BlockLocationOffset3D;
import emanondev.minigames.locations.LocationOffset3D;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;

public final class Minigames extends CorePlugin {

    private static Minigames instance;

    public static @NotNull Minigames get() {
        return instance;
    }

    @Override
    protected boolean registerReloadCommand() {
        return true;
    }

    @Override
    public void disable() {

    }

    @Override
    public void reload() {
        KitManager.get().reload();
        ArenaManager.get().reload();
        OptionManager.get().reload();
        GameManager.get().reload();
    }

    @Override
    public void enable() {
        registerConfigurationSerializables();
        new KitManager();
        new ArenaManager();
        new OptionManager();

        this.registerListener(new GameManager());

        new MinigameTypes();

        ArenaManager.get().reload();
        OptionManager.get().reload();
        GameManager.get().reload();

        this.registerCommand(new MiniKitCommand());
        this.registerCommand(new MiniChestFillerCommand());
        this.registerCommand(new ArenaBuilderCommand());
        this.registerCommand(new MiniOptionCommand());
        this.registerCommand(new MiniGameCommand());
        this.registerCommand(new FastJoinCommand());
        this.registerCommand(new LeaveCommand());

    }

    private void registerConfigurationSerializables() {
        ConfigurationSerialization.registerClass(BlockLocation2D.class);
        ConfigurationSerialization.registerClass(BlockLocation3D.class);
        ConfigurationSerialization.registerClass(BlockLocationOffset3D.class);
        ConfigurationSerialization.registerClass(LocationOffset3D.class);
        ConfigurationSerialization.registerClass(Kit.class);

        //Commands
    }

    @Override
    public void load() {
        instance = this;
    }
}
