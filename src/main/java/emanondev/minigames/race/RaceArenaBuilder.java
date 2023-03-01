package emanondev.minigames.race;

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

public class RaceArenaBuilder extends SchematicArenaBuilder {

    private final HashMap<DyeColor, LocationOffset3D> spawnLocations = new HashMap<>();
    private LocationOffset3D spectatorsOffset;

    private final List<BoundingBox> checkPoints = new ArrayList<>();
    private final List<LocationOffset3D> checkPointsRespawn = new ArrayList<>();
    private final List<BoundingBox> fallAreas = new ArrayList<>();
    private BoundingBox endArea;

    public RaceArenaBuilder(@NotNull UUID user, @NotNull String id, @NotNull String label) {
        super(user, id, label);
    }

    private static final int PHASE_SELECT_AREA = 1;
    private static final int PHASE_SET_TEAM_SPAWNS = 2;
    private static final int PHASE_SET_TEAM_SPAWNS_OR_NEXT = 3;
    private static final int PHASE_SET_SPECTATOR_SPAWN = 4;
    private static final int PHASE_SET_SPECTATOR_SPAWN_OR_NEXT = 5;


    @Override
    protected void onPhaseStart() {

    }

    @Override
    public @NotNull DMessage getCurrentBossBarMessage() {
        return new DMessage(Minigames.get(), getBuilder()).appendLang("skywars.arenabuilder.bossbar.phase" + getPhase());
    }

    @Override
    public @NotNull DMessage getRepeatedMessage() {
        return switch (getPhase()) {
            case PHASE_SET_TEAM_SPAWNS, PHASE_SET_TEAM_SPAWNS_OR_NEXT -> {
                YMLSection sect = Minigames.get().getLanguageConfig(getBuilder()).loadSection("skywars.arenabuilder.repeatmessage");
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
                    Minigames.get().getLanguageConfig(getBuilder()).getString("skywars.arenabuilder.repeatmessage.phase" + getPhase(), ""));
        };
    }

    @Override
    public void handleCommand(@NotNull Player player, String label, @NotNull String[] args) {
        if (args.length == 0) {
            MessageUtil.sendMessage(player, "race.arenabuilder.error.unknown_action");
            return;
        }
        switch (getPhase()) {
            case 1 -> {
                if (!args[0].equalsIgnoreCase("selectarea")) {
                    MessageUtil.sendMessage(player, "race.arenabuilder.error.unknown_action");
                    return;
                }
                try {
                    setArea(player);
                    MessageUtil.sendMessage(player, "race.arenabuilder.success.select_area",
                            "%world%", getWorld().getName(),
                            "%x1%", String.valueOf((int) getArea().getMinX()),
                            "%x2%", String.valueOf((int) getArea().getMaxX()),
                            "%y1%", String.valueOf((int) getArea().getMinY()),
                            "%y2%", String.valueOf((int) getArea().getMaxY()),
                            "%z1%", String.valueOf((int) getArea().getMinZ()),
                            "%z2%", String.valueOf((int) getArea().getMaxZ()));

                    setPhaseRaw(getPhase() + 1);
                } catch (IncompleteRegionException e) {
                    MessageUtil.sendMessage(player, "race.arenabuilder.error.unselected_area");
                }
            }
            case 2, 3 -> {
                if (spawnLocations.size() >= 2 && args[0].equalsIgnoreCase("next")) {
                    MessageUtil.sendMessage(player, "race.arenabuilder.success.next");
                    setPhaseRaw(getPhase() + 1);
                    return;
                }
                if (!args[0].equalsIgnoreCase("setteamspawn")) {
                    MessageUtil.sendMessage(player, "race.arenabuilder.error.unknown_action");
                    return;
                }
                try {
                    DyeColor color = DyeColor.valueOf(args[1].toUpperCase());
                    if (!this.isInside(player.getLocation())) {
                        MessageUtil.sendMessage(player, "race.arenabuilder.error.outside_area");
                        return;
                    }
                    LocationOffset3D loc = LocationOffset3D.fromLocation(player.getLocation().subtract(getArea().getMin()));
                    boolean override = spawnLocations.containsKey(color);
                    spawnLocations.put(color, loc);
                    MessageUtil.sendMessage(player, override ? "race.arenabuilder.success.override_team_spawn"
                            : "race.arenabuilder.success.set_team_spawn", "%color%", color.name());
                    if (getPhase() == 2 && spawnLocations.size() >= 2)
                        setPhaseRaw(getPhase() + 1);
                } catch (Exception e) {
                    MessageUtil.sendMessage(player, "race.arenabuilder.error.invalid_color");
                }
            }
            case 4, 5 -> {
                if (spectatorsOffset != null && args[0].equalsIgnoreCase("next")) {
                    MessageUtil.sendMessage(player, "race.arenabuilder.success.completed");
                    ArenaManager.get().onArenaBuilderCompletedArena(this);
                    return;
                }
                if (!args[0].equalsIgnoreCase("setspectatorspawn")) {
                    MessageUtil.sendMessage(player, "race.arenabuilder.error.unknown_action");
                    return;
                }
                try {
                    if (!this.isInside(player.getLocation())) {
                        MessageUtil.sendMessage(player, "race.arenabuilder.error.outside_area");
                        return;
                    }
                    boolean override = spectatorsOffset != null;
                    spectatorsOffset = LocationOffset3D.fromLocation(player.getLocation().subtract(getArea().getMin()));
                    MessageUtil.sendMessage(player, override ? "race.arenabuilder.success.override_spectators_spawn"
                            : "race.arenabuilder.success.set_spectators_spawn");
                    if (getPhase() == 4)
                        setPhaseRaw(getPhase() + 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            case 6 -> { //checkpointarea or next
                if (args[0].equalsIgnoreCase("next")) {
                    MessageUtil.sendMessage(player, "race.arenabuilder.success.next");
                    setPhaseRaw(8);
                    return;
                }
                if (!args[0].equalsIgnoreCase("checkpointarea")) {
                    MessageUtil.sendMessage(player, "race.arenabuilder.error.unknown_action");
                    return;
                }
                try {
                    World world = player.getWorld();
                    Region sel = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player))
                            .getSelection(BukkitAdapter.adapt(world));
                    BoundingBox checkPointArea = new BoundingBox(sel.getMinimumPoint().getX(), sel.getMinimumPoint().getY(),
                            sel.getMinimumPoint().getZ(), sel.getMaximumPoint().getX(), sel.getMaximumPoint().getY(),
                            sel.getMaximumPoint().getZ());
                    if (this.getArea().getVolume() == checkPointArea.getVolume()) {
                        //il checkpointarea deve avere un'area minore

                        return;
                    }
                    if (!this.getArea().contains(checkPointArea)) {
                        //il checkpointarea dev'essere all'interno dell'area

                        return;
                    }
                    for (BoundingBox box : checkPoints)
                        if (checkPointArea.overlaps(box)) {
                            //should not overlap another checkpoint
                            return;
                        }
                    checkPoints.add(checkPointArea);
                    setPhaseRaw(7);
                } catch (IncompleteRegionException e) {
                    MessageUtil.sendMessage(player, "race.arenabuilder.error.unselected_area");
                }
            }
            case 7 -> { //checkpoint checkpointrespawnloc || undo
                if (args[0].equalsIgnoreCase("undo")) {
                    MessageUtil.sendMessage(player, "race.arenabuilder.success.undo");
                    checkPoints.remove(checkPoints.size() - 1);
                    setPhaseRaw(6);
                    return;
                }
                if (!args[0].equalsIgnoreCase("checkpointrespawnloc")) {
                    MessageUtil.sendMessage(player, "race.arenabuilder.error.unknown_action");
                    return;
                }
                if (!checkPoints.get(checkPoints.size() - 1).contains(player.getLocation().toVector())) {
                    //not inside checkpointArea

                    return;
                }
                checkPointsRespawn.add(LocationOffset3D.fromLocation(player.getLocation().subtract(getArea().getMin())));
                setPhaseRaw(6);
            }
            case 8 -> { // finisharea
                if (!args[0].equalsIgnoreCase("finisharea")) {
                    MessageUtil.sendMessage(player, "race.arenabuilder.error.unknown_action");
                    return;
                }
                try {
                    World world = player.getWorld();
                    Region sel = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player))
                            .getSelection(BukkitAdapter.adapt(world));
                    BoundingBox finishArea = new BoundingBox(sel.getMinimumPoint().getX(), sel.getMinimumPoint().getY(),
                            sel.getMinimumPoint().getZ(), sel.getMaximumPoint().getX(), sel.getMaximumPoint().getY(),
                            sel.getMaximumPoint().getZ());
                    if (!this.getArea().contains(finishArea)) {
                        //il checkpointarea dev'essere all'interno dell'area

                        return;
                    }
                    for (BoundingBox box : checkPoints)
                        if (finishArea.overlaps(box)) {
                            //should not overlap a checkpoint

                            return;
                        }
                    this.endArea = finishArea;
                    setPhaseRaw(9);
                } catch (IncompleteRegionException e) {
                    MessageUtil.sendMessage(player, "race.arenabuilder.error.unselected_area");
                }
            }
            case 9 -> { //zone di caduta
                if (args[0].equalsIgnoreCase("next")) {
                    MessageUtil.sendMessage(player, "race.arenabuilder.success.next");
                    setPhaseRaw(10);//TODO ???
                    return;
                }
                if (!args[0].equalsIgnoreCase("fallingzone")) {
                    MessageUtil.sendMessage(player, "race.arenabuilder.error.unknown_action");
                    return;
                }
                try {
                    World world = player.getWorld();
                    Region sel = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player))
                            .getSelection(BukkitAdapter.adapt(world));
                    BoundingBox fallArea = new BoundingBox(sel.getMinimumPoint().getX(), sel.getMinimumPoint().getY(),
                            sel.getMinimumPoint().getZ(), sel.getMaximumPoint().getX(), sel.getMaximumPoint().getY(),
                            sel.getMaximumPoint().getZ());
                    if (this.getArea().getVolume() == fallArea.getVolume()) {
                        //il checkpointarea deve avere un'area minore

                        return;
                    }
                    if (!this.getArea().contains(fallArea)) {
                        //il checkpointarea dev'essere all'interno dell'area

                        return;
                    }
                    for (BoundingBox box : checkPoints)
                        if (fallArea.overlaps(box)) {
                            //should not overlap a checkpoint

                            return;
                        }
                    if (fallArea.overlaps(endArea)) {
                        //should not overlap a endArea

                        return;
                    }
                    fallAreas.add(fallArea);
                } catch (IncompleteRegionException e) {
                    MessageUtil.sendMessage(player, "race.arenabuilder.error.unselected_area");
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
            case 6 -> UtilsCommand.complete(args[0], List.of("checkpointarea", "next"));
            case 7 -> UtilsCommand.complete(args[0], List.of("checkpointrespawnloc", "undo"));
            case 8 -> UtilsCommand.complete(args[0], List.of("finisharea"));
            case 9 -> UtilsCommand.complete(args[0], List.of("fallingzone", "next"));
            default -> throw new IllegalStateException("Unexpected value: " + getPhase());
        };
    }

    @Override
    public RaceArena build() {
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
        return new RaceArena(map);
    }


    private int timerTick = 0;

    @Override
    public void onTimerCall() {
        Player p = getBuilder();
        if (p == null || !p.isOnline())
            return;
        timerTick++;
        org.bukkit.util.Vector min;
        Vector max;

        if (getPhase() > 1) {
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
}
