package emanondev.minigames.games;

import emanondev.core.CorePlugin;
import emanondev.core.UtilsString;
import emanondev.core.message.DMessage;
import emanondev.core.util.CorePluginLinked;
import emanondev.core.utility.CompletionHelper;
import emanondev.minigames.Minigames;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public abstract class MArenaBuilder implements CompletionHelper, CorePluginLinked {

    private final UUID builder;
    private final BukkitTask timerRunnable;
    private final String id;
    private final BossBar bar = Bukkit.createBossBar(" ", BarColor.RED, BarStyle.SOLID);
    private final String label;
    private final CorePlugin plugin;
    private int phase = 1;

    public MArenaBuilder(@NotNull UUID user, @NotNull String id, @NotNull String label, @NotNull CorePlugin plugin) {
        this.builder = user;
        if (!UtilsString.isLowcasedValidID(id))
            throw new IllegalArgumentException();
        this.id = id.toLowerCase();
        this.label = label;
        this.plugin = plugin;
        timerRunnable = Bukkit.getScheduler().runTaskTimer(Minigames.get(), () -> {
            Player p = getBuilder();
            if (p != null && p.isOnline() && p.isValid()) {
                bar.addPlayer(p);
                onTimerCall();
            }
        }, 10, 5);
        setPhaseRaw(1);
    }

    public @Nullable Player getBuilder() {
        return Bukkit.getPlayer(builder);
    }

    /**
     * Timer Runnable call
     * Note: this funcion is called 4 times each second (every 5 game ticks)
     */
    public abstract void onTimerCall(/*int timerTick*/);

    public abstract @NotNull DMessage getCurrentBossBarMessage();

    public abstract @NotNull DMessage getRepeatedMessage();

    public @NotNull String getLabel() {
        return label;
    }

    public @NotNull CorePlugin getPlugin() {
        return plugin;
    }

    public @NotNull String getId() {
        return id;
    }

    public @NotNull UUID getUser() {
        return builder;
    }

    public boolean isBuilder(@NotNull OfflinePlayer player) {
        return isBuilder(player.getUniqueId());
    }

    public boolean isBuilder(@NotNull UUID uuid) {
        return builder.equals(uuid);
    }

    public abstract void handleCommand(Player sender, String label, @NotNull String[] args);

    public abstract List<String> handleComplete(@NotNull String[] args);

    public void abort() {
        timerRunnable.cancel();
        bar.removeAll();
    }

    public abstract MArena build();

    protected void setPhaseRaw(int phase) {
        this.phase = phase;
        bar.setTitle(getCurrentBossBarMessage().toLegacy());
        onPhaseStart();
        Player p = getBuilder();
        if (p != null) {
            bar.addPlayer(p);
            getRepeatedMessage().send();
        }
    }

    protected abstract void onPhaseStart();

    protected int getPhase() {
        return phase;
    }

}
