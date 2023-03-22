package emanondev.minigames.command;

import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import emanondev.core.command.CoreCommand;
import emanondev.minigames.ArenaManager;
import emanondev.minigames.Minigames;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class TestCommand extends CoreCommand {

    Clipboard schematicCache;

    public TestCommand() {
        super("test2", Minigames.get(), new Permission("aaa.bbb"));
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String s, String @NotNull [] args) {
        File dest = new File(ArenaManager.get().getSchematicsFolder(), "emanon____pillars");
        if (!dest.isFile())
            throw new IllegalStateException("selected schematic do not exist");

        ClipboardFormat format = ClipboardFormats.findByFile(dest);
        try (NBTInputStream nbtStream = new NBTInputStream(new GZIPInputStream(new FileInputStream(dest)))) {
            //ClipboardReader reader = format.getReader(new FileInputStream(dest));
            //log(reader.getClass().getName()+" "+reader.getClass().getSimpleName());
            Test test = new Test(nbtStream);
            test.read();
            nbtStream.close();
            //reader.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public @Nullable List<String> onComplete(@NotNull CommandSender sender, @NotNull String s, String @NotNull [] args, @Nullable Location location) {
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
