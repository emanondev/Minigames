package emanondev.minigames;

import emanondev.core.command.CoreCommand;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TestCommand extends CoreCommand {


    private long delay = 0;

    public TestCommand() {
        super("test2", Minigames.get(), new Permission("aaa.bbb"));
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p))
            throw new IllegalStateException();
        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                boolean done = false;
                while (!done)
                    try {
                        if (i>=Particle.values().length) {
                            this.cancel();
                            return;
                        }
                        Location l = p.getEyeLocation().add(p.getLocation().getDirection().multiply(3));
                        p.spawnParticle(Particle.values()[i/2], l, 1);
                        if (i%2==0)
                            p.sendMessage(Particle.values()[i/2].name());
                        done = true;
                    } catch (Exception e) {
                        i++;
                    }
                i++;
            }
        }.runTaskTimer(Minigames.get(), 20, 20);
    }

    @Override
    public @Nullable List<String> onComplete(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args, @Nullable Location location) {
        return null;
    }

    protected void spawnParticle(Player p, Particle particle, double x, double y, double z) {
        spawnParticle(p, particle, x, y, z, 1, null);
    }

    protected void spawnParticle(Player p, Particle particle, double x, double y, double z, int count) {
        spawnParticle(p, particle, x, y, z, count, null);
    }

    protected void spawnParticle(Player p, Particle particle, double x, double y, double z, Object data) {
        spawnParticle(p, particle, x, y, z, 0, data);
    }

    protected void spawnParticle(Player p, Particle particle, double x, double y, double z, int count, Object data) {
        p.spawnParticle(particle, x, y, z, count, 0, 0, 0, 0, data);
    }
}
