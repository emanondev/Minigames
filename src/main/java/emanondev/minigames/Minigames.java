package emanondev.minigames;

import emanondev.core.CorePlugin;
import emanondev.core.Hooks;
import emanondev.minigames.command.*;
import emanondev.minigames.compability.MinigamePlaceHolders;
import emanondev.minigames.generic.ChestFiller;
import emanondev.minigames.generic.MGame;
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
        GameManager.get().getGames().values().forEach(MGame::gameAbort);
    }

    @Override
    public void reload() {
        FillerManager.get().reload();
        KitManager.get().reload();
        ArenaManager.get().reload();
        OptionManager.get().reload();
        GameManager.get().reload();
    }

    @Override
    public void enable() {
        registerConfigurationSerializables();
        new KitManager();
        new FillerManager();
        new ArenaManager();
        new OptionManager();

        this.registerListener(new GameManager());

        new MinigameTypes();

        KitManager.get().reload();
        FillerManager.get().reload();
        ArenaManager.get().reload();
        OptionManager.get().reload();
        GameManager.get().reload();

        this.registerCommand(new MiniKitCommand());
        this.registerCommand(new MiniFillerCommand());
        this.registerCommand(new ArenaBuilderCommand());
        this.registerCommand(new MiniOptionCommand());
        this.registerCommand(new MiniGameCommand());
        this.registerCommand(new FastJoinCommand());
        this.registerCommand(new JoinCommand());
        this.registerCommand(new LeaveCommand());
        this.registerCommand(new SnapshotSupportCommand());

        if (Hooks.isPAPIEnabled())
            new MinigamePlaceHolders().register();

    }

    private void registerConfigurationSerializables() {
        ConfigurationSerialization.registerClass(BlockLocation2D.class);
        ConfigurationSerialization.registerClass(BlockLocation3D.class);
        ConfigurationSerialization.registerClass(BlockLocationOffset3D.class);
        ConfigurationSerialization.registerClass(LocationOffset3D.class);
        ConfigurationSerialization.registerClass(Kit.class);
        ConfigurationSerialization.registerClass(ChestFiller.class);

        //Commands
    }

    @Override
    public void load() {
        instance = this;
    }
}
