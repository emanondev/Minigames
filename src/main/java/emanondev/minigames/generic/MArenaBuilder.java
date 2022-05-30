package emanondev.minigames.generic;

import emanondev.core.UtilsMessages;
import emanondev.core.UtilsString;
import emanondev.minigames.Minigames;
import org.bukkit.Bukkit;
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

    public MArenaBuilder(@NotNull UUID user, @NotNull String id) {
        this.builder = user;
        if (!UtilsString.isLowcasedValidID(id))
            throw new IllegalArgumentException();
        this.id = id.toLowerCase();
        timerRunnable = Bukkit.getScheduler().runTaskTimer(Minigames.get(), () -> {
            Player p = Bukkit.getPlayer(builder);
            if (p != null && p.isOnline()) {
                String msg = getCurrentActionMessage();
                if (msg != null && !msg.isEmpty())
                    UtilsMessages.sendActionbar(p, UtilsString.fix(msg, null, true));
            }
            onTimerCall();
        }, 10, 15);

    }

    public @NotNull String getId() {
        return id;
    }

    public @NotNull UUID getUser() {
        return builder;
    }

    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(builder);
    }

    public boolean isBuilder(@NotNull Player player) {
        return builder.equals(player.getUniqueId());
    }

    public abstract @Nullable String getCurrentActionMessage();

    public abstract void handleCommand(Player sender, @NotNull String[] args);

    public abstract List<String> handleComplete(@NotNull String[] args);

    public void abort() {
        timerRunnable.cancel();
    }

    public abstract MArena build();

    public abstract void onTimerCall();
}
