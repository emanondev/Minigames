package emanondev.minigames;

import emanondev.core.ItemBuilder;
import emanondev.core.command.CoreCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class TestCommand extends CoreCommand {


    private long delay = 0;

    public TestCommand() {
        super("test2", Minigames.get(), new Permission("aaa.bbb"));
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args) {
        Player p = (Player) sender;
        if (args.length == 0) {

        }
        delay = 0;
        switch (args[0]) {
            case "1" -> {
                Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
                Objective obj = scoreboard.registerNewObjective("game", "dummy", "objDisplayName");
                obj.setDisplaySlot(DisplaySlot.SIDEBAR);
                //Objective obj = scoreboard.getObjective(DisplaySlot.SIDEBAR);
                Team t = scoreboard.registerNewTeam("red");
                t.addPlayer(p);
                t.addEntry(p.getName());
                t.addEntry(p.getName() + "test");
                t.setDisplayName("DisplayName");
                t.setPrefix(ChatColor.RED + "[RED] " + ChatColor.WHITE);
                //t.setColor(ChatColor.RED);
                t.addEntry(p.getName());
                obj.setDisplayName("ObjectiveDisplayName");
                t.addEntry(p.getName());
                obj.getScore(net.md_5.bungee.api.ChatColor.of(new Color(55, 92, 12)) + "Score1").setScore(1);
                obj.getScore("Score2").setScore(2);
                obj.getScore("Score3").setScore(3);
                obj.getScore("Score4").setScore(3);
                //t.addEntry(p.getName());
                p.setScoreboard(scoreboard);

                p.setScoreboard(scoreboard);
            }
            case "2" -> {
                GameManager.get().getAll().forEach((k, g) -> {
                    p.getInventory().addItem(g.getGameSelectorItem(p));
                });
                p.getInventory().addItem(new ItemBuilder(Material.SADDLE).setMiniDescription(List.of("aaddw", "adwdaw"))
                        .build());
            }
            case "3" -> {

            }
        }
    }

    private void later(Runnable r) {
        delay++;
        long tmp = delay;
        Bukkit.getScheduler().runTaskLater(getPlugin(), () ->
        {
            Bukkit.broadcastMessage("" + tmp);
        }, delay * 50L);
        Bukkit.getScheduler().runTaskLater(getPlugin(), r, delay * 50L);
    }

    @Override
    public @Nullable List<String> onComplete(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args, @Nullable Location location) {
        return null;
    }
}
