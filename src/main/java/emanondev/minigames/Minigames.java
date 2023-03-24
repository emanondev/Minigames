package emanondev.minigames;

import emanondev.core.CorePlugin;
import emanondev.core.Hooks;
import emanondev.minigames.command.*;
import emanondev.minigames.compability.MinigamePlaceholders;
import emanondev.minigames.generic.DropGroup;
import emanondev.minigames.generic.DropsFiller;
import emanondev.minigames.generic.MArenaBuilder;
import emanondev.minigames.generic.MGame;
import emanondev.minigames.locations.BlockLocation2D;
import emanondev.minigames.locations.BlockLocation3D;
import emanondev.minigames.locations.BlockLocationOffset3D;
import emanondev.minigames.locations.LocationOffset3D;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

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
        GameManager.get().getAll().values().forEach(MGame::gameAbort);
        ArrayList<MArenaBuilder> coll = new ArrayList<>(ArenaManager.get().getAllBuildings());
        coll.forEach(arenaBuilder -> ArenaManager.get().unregisterBuilder(arenaBuilder.getUser()));
    }

    @Override
    public void reload() {
        C.reload();
        FillerManager.get().reload();
        KitManager.get().reload();
        ArenaManager.get().reload();
        OptionManager.get().reload();
        GameManager.get().reload();
    }

    @Override
    public void enable() {
        C.reload();
        new DropGroupManager();
        new KitManager();
        new FillerManager();
        new ArenaManager();
        new OptionManager();

        this.registerListener(new GameManager());

        new MinigameTypes();

        DropGroupManager.get().reload();
        KitManager.get().reload();
        FillerManager.get().reload();
        ArenaManager.get().reload();
        OptionManager.get().reload();
        GameManager.get().reload();

        registerCommands();

        //TODO remove later
        this.registerCommand(new TestCommand());
        this.registerCommand(new SnapshotSupportCommand());

        if (Hooks.isPAPIEnabled())
            new MinigamePlaceholders().register();

    }

    private void registerCommands() {
        this.registerCommand(new MiniKitCommand());
        this.registerCommand(new MiniDropGroupCommand());
        this.registerCommand(new MiniDropsFillerCommand());
        this.registerCommand(new MiniArenaBuilderCommand());
        this.registerCommand(new MiniArenaCommand());
        this.registerCommand(new MiniOptionCommand());
        this.registerCommand(new MiniGameCommand());
        this.registerCommand(new FastJoinCommand());
        this.registerCommand(new JoinCommand());
        this.registerCommand(new LeaveCommand());
    }

    private void registerConfigurationSerializables() {
        ConfigurationSerialization.registerClass(BlockLocation2D.class);
        ConfigurationSerialization.registerClass(BlockLocation3D.class);
        ConfigurationSerialization.registerClass(BlockLocationOffset3D.class);
        ConfigurationSerialization.registerClass(LocationOffset3D.class);
        ConfigurationSerialization.registerClass(Kit.class);
        ConfigurationSerialization.registerClass(DropGroup.class);
        ConfigurationSerialization.registerClass(DropsFiller.class);
    }

    @Override
    public void load() {
        instance = this;
        registerConfigurationSerializables();
    }
}
