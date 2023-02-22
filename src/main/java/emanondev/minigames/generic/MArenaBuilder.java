package emanondev.minigames.generic;

import emanondev.core.UtilsString;
import emanondev.core.message.MessageComponent;
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

public abstract class MArenaBuilder {

    private final UUID builder;
    private final BukkitTask timerRunnable;
    private final String id;
    private final BossBar bar = Bukkit.createBossBar(" ", BarColor.RED, BarStyle.SOLID);
    private int phase = 1;

    public MArenaBuilder(@NotNull UUID user, @NotNull String id) {
        this.builder = user;
        if (!UtilsString.isLowcasedValidID(id))
            throw new IllegalArgumentException();
        this.id = id.toLowerCase();
        timerRunnable = Bukkit.getScheduler().runTaskTimer(Minigames.get(), new Runnable() {
            private int timerTick = 0;

            @Override
            public void run() {
                Player p = Bukkit.getPlayer(builder);
                if (p != null && p.isOnline()) {
                    //TODO message repeat, bossbar, actionbar
                }
                onTimerCall(timerTick);
                timerTick += 1;
            }
        }, 10, 5);

    }

    protected int getPhase() {
        return phase;
    }

    protected void setPhaseRaw(int phase) {
        this.phase = phase;
        bar.setTitle(getCurrentBossBarMessage());
        /*if (getBuilder() != null)
            new MessageComponent(Minigames.get(), getBuilder()).append(getCurrentActionMessage()).sendActionBar();*/
        onPhaseStart();
        new MessageComponent(Minigames.get(), getBuilder()).append(getRepeatedMessage()).send();
    }

    protected abstract void onPhaseStart();

    public @NotNull String getId() {
        return id;
    }

    public @NotNull UUID getUser() {
        return builder;
    }

    public @Nullable Player getBuilder() {
        return Bukkit.getPlayer(builder);
    }

    public boolean isBuilder(@NotNull OfflinePlayer player) {
        return builder.equals(player.getUniqueId());
    }

    //public abstract @Nullable String getCurrentActionMessage();

    public abstract @NotNull String getCurrentBossBarMessage();

    public abstract @NotNull String getRepeatedMessage();

    public abstract void handleCommand(Player sender, @NotNull String[] args);

    public abstract List<String> handleComplete(@NotNull String[] args);

    public void abort() {
        timerRunnable.cancel();
        bar.removeAll();
    }

    public abstract MArena build();

    /**
     * Timer Runnable call
     * Note: this funcion is called 4 times each second (every 5 game ticks)
     *
     * @param timerTick increasing value
     */
    public abstract void onTimerCall(int timerTick);
}
