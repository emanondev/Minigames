package emanondev.minigames.eggwars;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import emanondev.core.UtilsCommand;
import emanondev.core.YMLSection;
import emanondev.core.message.DMessage;
import emanondev.core.util.WorldEditUtility;
import emanondev.minigames.ArenaManager;
import emanondev.minigames.MessageUtil;
import emanondev.minigames.Minigames;
import emanondev.minigames.UtilColor;
import emanondev.minigames.generic.SchematicArenaBuilder;
import emanondev.minigames.locations.LocationOffset3D;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class EggWarsArenaBuilder extends SchematicArenaBuilder {

    private final HashMap<DyeColor, LocationOffset3D> spawnLocations = new HashMap<>();
    private LocationOffset3D spectatorsOffset;

    public EggWarsArenaBuilder(@NotNull UUID user, @NotNull String id, @NotNull String label) {
        super(user, id, label);
    }

    @Override
    protected void onPhaseStart() {

    }

    private static final int PHASE_SELECT_AREA = 1;
    private static final int PHASE_SET_TEAM_SPAWNS = 2;
    private static final int PHASE_SET_TEAM_SPAWNS_OR_NEXT = 3;
    private static final int PHASE_SET_SPECTATOR_SPAWN = 4;
    private static final int PHASE_SET_SPECTATOR_SPAWN_OR_NEXT = 5;


    @Override
    public @NotNull DMessage getCurrentBossBarMessage() {
        return new DMessage(Minigames.get(), getBuilder()).appendLang("eggwars.arenabuilder.bossbar.phase" + getPhase());
    }

    @Override
    public @NotNull DMessage getRepeatedMessage() {
        return switch (getPhase()) {
            case PHASE_SET_TEAM_SPAWNS, PHASE_SET_TEAM_SPAWNS_OR_NEXT -> {
                YMLSection sect = Minigames.get().getLanguageConfig(getBuilder()).loadSection("eggwars.arenabuilder.repeatmessage");
                StringBuilder teamSet = new StringBuilder();
                for (DyeColor color : DyeColor.values())
                    if (!spawnLocations.containsKey(color))
                        teamSet.append(sect.loadMessage("setteamcolor", "", (CommandSender) null, "%color%", color.name(), "%hexa%", UtilColor.getColorHexa(color)));
                StringBuilder teamDelete = new StringBuilder();
                for (DyeColor color : spawnLocations.keySet())
                    teamDelete.append(sect.loadMessage("deleteteamcolor", "", (CommandSender) null, "%color%", color.name()));
                yield new DMessage(Minigames.get(), getBuilder()).append(sect.loadMessage("phase" + getPhase(), "", (CommandSender) null
                        , "%setteamsspawn%", teamSet.toString()
                        , "%deleteteamsspawn%", teamDelete.toString()));
            }
            default -> new DMessage(Minigames.get(), getBuilder()).append(
                    Minigames.get().getLanguageConfig(getBuilder()).getString("eggwars.arenabuilder.repeatmessage.phase" + getPhase(), ""));
        };
    }
    /*

    @Override
    @Nullable
    public String getCurrentActionMessage() {
        return Minigames.get().getLanguageConfig(getBuilder()).getString("eggwars.arenabuilder.actionbar.phase" + phase);
    }*/

    @Override
    public void handleCommand(@NotNull Player player, String label, @NotNull String[] args) {
        if (args.length == 0) {
            MessageUtil.sendMessage(player, "eggwars.arenabuilder.error.unknown_action");
            return;
        }
        switch (getPhase()) {
            case PHASE_SELECT_AREA -> {
                if (!args[0].equalsIgnoreCase("selectarea")) {
                    MessageUtil.sendMessage(player, "eggwars.arenabuilder.error.unknown_action");
                    return;
                }
                try {
                    setArea(player);
                    MessageUtil.sendMessage(player, "eggwars.arenabuilder.success.select_area",
                            "%world%", getWorld().getName(),
                            "%x1%", String.valueOf((int) getArea().getMinX()),
                            "%x2%", String.valueOf((int) getArea().getMaxX()),
                            "%y1%", String.valueOf((int) getArea().getMinY()),
                            "%y2%", String.valueOf((int) getArea().getMaxY()),
                            "%z1%", String.valueOf((int) getArea().getMinZ()),
                            "%z2%", String.valueOf((int) getArea().getMaxZ()));

                    setPhaseRaw(PHASE_SET_TEAM_SPAWNS);
                } catch (IncompleteRegionException e) {
                    MessageUtil.sendMessage(player, "eggwars.arenabuilder.error.unselected_area");
                }
            }
            case PHASE_SET_TEAM_SPAWNS, PHASE_SET_TEAM_SPAWNS_OR_NEXT -> {
                if (spawnLocations.size() >= 2 && args[0].equalsIgnoreCase("next")) {
                    MessageUtil.sendMessage(player, "eggwars.arenabuilder.success.next");
                    setPhaseRaw(PHASE_SET_SPECTATOR_SPAWN);
                    return;
                }
                if (!args[0].equalsIgnoreCase("setteamspawn")) {
                    MessageUtil.sendMessage(player, "eggwars.arenabuilder.error.unknown_action");
                    return;
                }
                try {
                    DyeColor color = DyeColor.valueOf(args[1].toUpperCase());
                    if (!this.isInside(player.getLocation())) {
                        MessageUtil.sendMessage(player, "eggwars.arenabuilder.error.outside_area");
                        return;
                    }
                    LocationOffset3D loc = LocationOffset3D.fromLocation(player.getLocation().subtract(getArea().getMin()));
                    boolean override = spawnLocations.containsKey(color);
                    spawnLocations.put(color, loc);
                    MessageUtil.sendMessage(player, override ? "eggwars.arenabuilder.success.override_team_spawn"
                            : "eggwars.arenabuilder.success.set_team_spawn", "%color%", color.name());
                    if (PHASE_SET_TEAM_SPAWNS == getPhase() && spawnLocations.size() >= 2)
                        setPhaseRaw(PHASE_SET_TEAM_SPAWNS_OR_NEXT);
                    ;
                } catch (Exception e) {
                    MessageUtil.sendMessage(player, "eggwars.arenabuilder.error.invalid_color");
                }
            }
            case PHASE_SET_SPECTATOR_SPAWN, PHASE_SET_SPECTATOR_SPAWN_OR_NEXT -> {
                if (spectatorsOffset != null && args[0].equalsIgnoreCase("next")) {
                    MessageUtil.sendMessage(player, "eggwars.arenabuilder.success.completed");
                    ArenaManager.get().onArenaBuilderCompletedArena(this);
                    return;
                }
                if (!args[0].equalsIgnoreCase("setspectatorspawn")) {
                    MessageUtil.sendMessage(player, "eggwars.arenabuilder.error.unknown_action");
                    return;
                }
                try {
                    if (!this.isInside(player.getLocation())) {
                        MessageUtil.sendMessage(player, "eggwars.arenabuilder.error.outside_area");
                        return;
                    }
                    boolean override = spectatorsOffset != null;
                    spectatorsOffset = LocationOffset3D.fromLocation(player.getLocation().subtract(getArea().getMin()));
                    MessageUtil.sendMessage(player, override ? "eggwars.arenabuilder.success.override_spectators_spawn"
                            : "eggwars.arenabuilder.success.set_spectators_spawn");
                    if (getPhase() == PHASE_SET_SPECTATOR_SPAWN)
                        setPhaseRaw(PHASE_SET_SPECTATOR_SPAWN_OR_NEXT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            default -> {
                new IllegalStateException().printStackTrace();
            }
        }

    }

    @Override
    public List<String> handleComplete(@NotNull String[] args) {
        return switch (getPhase()) {
            case 1 -> args.length == 1 ? UtilsCommand.complete(args[0], List.of("selectarea")) : Collections.emptyList();
            case 2 -> args.length == 1 ? UtilsCommand.complete(args[0], List.of("setteamspawn")) :
                    args.length == 2 ? UtilsCommand.complete(args[1], DyeColor.class, (DyeColor c) -> !spawnLocations.containsKey(c)) : Collections.emptyList();
            case 3 -> {
                if (args.length == 1)
                    yield UtilsCommand.complete(args[0], List.of("setteamspawn", "next"));
                if (args.length == 2)
                    yield UtilsCommand.complete(args[1], DyeColor.class, (DyeColor c) -> !spawnLocations.containsKey(c));
                yield Collections.emptyList();
            }
            case 4 -> UtilsCommand.complete(args[0], List.of("setspectatorspawn"));
            case 5 -> UtilsCommand.complete(args[0], List.of("setspectatorspawn", "next"));
            default -> throw new IllegalStateException("Unexpected value: " + getPhase());
        };
    }

    @Override
    public EggWarsArena build() {
        Map<String, Object> map = new LinkedHashMap<>();

        String schemName = Bukkit.getOfflinePlayer(getUser()).getName() + "_" + getId();
        File schemFile = new File(Minigames.get().getDataFolder(), "schematics/" + schemName);
        String schemNameFinal = schemName;

        //avoid overriding existing schematics
        int counter = 1;
        while (schemFile.exists()) {
            schemNameFinal = schemName + "_" + counter;
            schemFile = new File(Minigames.get().getDataFolder(), "schematics/" + schemNameFinal);
            counter++;
        }


        map.put("schematic", schemNameFinal);
        WorldEditUtility.save(schemFile,
                WorldEditUtility.copy(getWorld(), getArea(), false, true));


        Map<String, Object> teams = new LinkedHashMap<>();
        map.put("spectatorSpawnOffset", spectatorsOffset.toString());
        for (DyeColor color : spawnLocations.keySet()) {
            Map<String, Object> teamInfo = new LinkedHashMap<>();
            teamInfo.put("spawnOffset", spawnLocations.get(color).toString());
            teams.put(color.name(), teamInfo);
        }

        map.put("teams", teams);
        return new EggWarsArena(map);
    }
    private int timerTick=0;

    @Override
    public void onTimerCall() {
        Player p = getBuilder();
        if (p == null || !p.isOnline())
            return;
        timerTick++;
        if (timerTick % 3 == 0) { //every 15 game ticks
            Vector min;
            Vector max;

            if (getPhase() > PHASE_SELECT_AREA) {
                min = getArea().getMin();
                max = getArea().getMax();
            } else {
                try {
                    World world = p.getWorld();
                    Region sel = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(p))
                            .getSelection(BukkitAdapter.adapt(world));
                    BoundingBox area = new BoundingBox(sel.getMinimumPoint().getX(), sel.getMinimumPoint().getY(),
                            sel.getMinimumPoint().getZ(), sel.getMaximumPoint().getX(), sel.getMaximumPoint().getY(),
                            sel.getMaximumPoint().getZ());
                    min = area.getMin();
                    max = area.getMax();
                } catch (Exception e) {
                    return;
                }
            }

            for (int i = min.getBlockX(); i <= max.getBlockX(); i++) {
                p.spawnParticle(Particle.COMPOSTER, i, min.getY(), min.getZ(), 1);
                p.spawnParticle(Particle.COMPOSTER, i, max.getY() + 1, min.getZ(), 1);
                p.spawnParticle(Particle.COMPOSTER, i, min.getY(), max.getZ() + 1, 1);
                p.spawnParticle(Particle.COMPOSTER, i, max.getY() + 1, max.getZ() + 1, 1);
            }
            for (int i = min.getBlockY(); i <= max.getBlockY(); i++) {
                p.spawnParticle(Particle.COMPOSTER, min.getX(), i, min.getZ(), 1);
                p.spawnParticle(Particle.COMPOSTER, max.getX() + 1, i, min.getZ(), 1);
                p.spawnParticle(Particle.COMPOSTER, min.getX(), i, max.getZ() + 1, 1);
                p.spawnParticle(Particle.COMPOSTER, max.getX() + 1, i, max.getZ() + 1, 1);
            }
            for (int i = min.getBlockZ(); i <= max.getBlockZ(); i++) {
                p.spawnParticle(Particle.COMPOSTER, min.getX(), min.getY(), i, 1);
                p.spawnParticle(Particle.COMPOSTER, max.getX() + 1, min.getY(), i, 1);
                p.spawnParticle(Particle.COMPOSTER, min.getX(), max.getY() + 1, i, 1);
                p.spawnParticle(Particle.COMPOSTER, max.getX() + 1, max.getY() + 1, i, 1);
            }
            for (int i = 0; i < 8; i++) {
                double xOffset = min.getX() + 0.4 * Math.sin(i * Math.PI / 4);
                double zOffset = min.getZ() + 0.4 * Math.cos(i * Math.PI / 4);
                double yOffset = min.getY() + 0.05D;
                spawnLocations.forEach((k, v) ->
                        p.spawnParticle(Particle.REDSTONE, v.x + xOffset, v.y + yOffset,
                                v.z + zOffset, 1, new Particle.DustOptions(k.getColor(), 1F)));
                if (spectatorsOffset != null)
                    p.spawnParticle(Particle.WAX_ON, spectatorsOffset.x + xOffset,
                            spectatorsOffset.y + yOffset, spectatorsOffset.z + zOffset, 1);
            }
        }
        if (timerTick % 60 == 0) { //every 15 seconds
            getRepeatedMessage().send();
        }
    }

    /*
    @Override
    public void onTimerCall() {
        Player p = getBuilder();
        if (p == null || !p.isOnline())
            return;
        Vector min;
        Vector max;

        if (phase > 1) {
            min = getArea().getMin();
            max = getArea().getMax();
        } else {
            try {
                World world = p.getWorld();
                Region sel = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(p))
                        .getSelection(BukkitAdapter.adapt(world));
                BoundingBox area = new BoundingBox(sel.getMinimumPoint().getX(), sel.getMinimumPoint().getY(),
                        sel.getMinimumPoint().getZ(), sel.getMaximumPoint().getX(), sel.getMaximumPoint().getY(),
                        sel.getMaximumPoint().getZ());
                min = area.getMin();
                max = area.getMax();
            } catch (Exception e) {
                return;
            }
        }

        for (int i = min.getBlockX(); i <= max.getBlockX(); i++) {
            p.spawnParticle(Particle.COMPOSTER, i, min.getY(), min.getZ(), 1);
            p.spawnParticle(Particle.COMPOSTER, i, max.getY() + 1, min.getZ(), 1);
            p.spawnParticle(Particle.COMPOSTER, i, min.getY(), max.getZ() + 1, 1);
            p.spawnParticle(Particle.COMPOSTER, i, max.getY() + 1, max.getZ() + 1, 1);
        }
        for (int i = min.getBlockY(); i <= max.getBlockY(); i++) {
            p.spawnParticle(Particle.COMPOSTER, min.getX(), i, min.getZ(), 1);
            p.spawnParticle(Particle.COMPOSTER, max.getX() + 1, i, min.getZ(), 1);
            p.spawnParticle(Particle.COMPOSTER, min.getX(), i, max.getZ() + 1, 1);
            p.spawnParticle(Particle.COMPOSTER, max.getX() + 1, i, max.getZ() + 1, 1);
        }
        for (int i = min.getBlockZ(); i <= max.getBlockZ(); i++) {
            p.spawnParticle(Particle.COMPOSTER, min.getX(), min.getY(), i, 1);
            p.spawnParticle(Particle.COMPOSTER, max.getX() + 1, min.getY(), i, 1);
            p.spawnParticle(Particle.COMPOSTER, min.getX(), max.getY() + 1, i, 1);
            p.spawnParticle(Particle.COMPOSTER, max.getX() + 1, max.getY() + 1, i, 1);
        }
        for (int i = 0; i < 8; i++) {
            double xOffset = min.getX() + 0.4 * Math.sin(i * Math.PI / 4);
            double zOffset = min.getZ() + 0.4 * Math.cos(i * Math.PI / 4);
            double yOffset = min.getY() + 0.05D;
            spawnLocations.forEach((k, v) ->
                    p.spawnParticle(Particle.REDSTONE, v.x + xOffset, v.y + yOffset,
                            v.z + zOffset, 1, new Particle.DustOptions(k.getColor(), 1F)));
            if (spectatorsOffset != null)
                p.spawnParticle(Particle.WAX_ON, spectatorsOffset.x + xOffset,
                        spectatorsOffset.y + yOffset, spectatorsOffset.z + zOffset, 1);
        }
    }*/
}

