package emanondev.minigames.race;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import emanondev.core.UtilsCommand;
import emanondev.core.UtilsString;
import emanondev.core.message.DMessage;
import emanondev.core.message.SimpleMessage;
import emanondev.core.util.WorldEditUtility;
import emanondev.minigames.ArenaManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.UtilColor;
import emanondev.minigames.generic.SchematicArenaBuilder;
import emanondev.minigames.locations.LocationOffset3D;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class RaceArenaBuilder extends SchematicArenaBuilder {
    private static final SimpleMessage ERR_UNKNOWN_ACTION = new SimpleMessage(Minigames.get(), "arenabuilder.race.error.unknown_action");
    private static final SimpleMessage ERR_OUTSIDE_ARENA = new SimpleMessage(Minigames.get(), "arenabuilder.race.error.outside_arena");
    private static final SimpleMessage ERR_SELECTED_AREA_OUTSIDE_ARENA = new SimpleMessage(Minigames.get(), "arenabuilder.race.error.selected_area_outside_arena");
    private static final SimpleMessage ERR_SELECTED_AREA_TOO_BIG = new SimpleMessage(Minigames.get(), "arenabuilder.race.error.selected_area_too_big");
    private static final SimpleMessage ERR_SELECTED_AREA_OVERLAPPING_CHECKPOINT = new SimpleMessage(Minigames.get(), "arenabuilder.race.error.selected_area_overlapping_checkpoint");
    private static final SimpleMessage WARN_SELECTED_AREA_VERY_BIG = new SimpleMessage(Minigames.get(), "arenabuilder.race.warn.selected_area_very_big");
    private static final SimpleMessage WARN_SELECTED_AREA_VERY_SMALL = new SimpleMessage(Minigames.get(), "arenabuilder.race.warn.selected_area_very_small");
    private static final SimpleMessage ERR_SELECTED_AREA_OVERLAPPING_FINISH_AREA = new SimpleMessage(Minigames.get(), "arenabuilder.race.error.selected_area_overlapping_finish_area");
    private static final SimpleMessage ERR_OUTSIDE_CHECKPOINT = new SimpleMessage(Minigames.get(), "arenabuilder.race.error.outside_checkpoint_area");

    private static final int PHASE_SELECT_AREA = 1;
    private static final int PHASE_SET_TEAM_SPAWNS = 2;
    private static final int PHASE_SET_TEAM_SPAWNS_OR_NEXT = 3;
    private static final int PHASE_SET_SPECTATOR_SPAWN = 4;
    private static final int PHASE_SET_SPECTATOR_SPAWN_OR_NEXT = 5;
    private static final int PHASE_CHECKPOINT_AREA_OR_NEXT = 6;
    private static final int PHASE_CHECKPOINT_SPAWN = 7;
    private static final int PHASE_FINISH_AREA = 8;
    private static final int PHASE_FALLING_ZONES = 9;

    private final HashMap<DyeColor, LocationOffset3D> spawnLocations = new HashMap<>();
    private LocationOffset3D spectatorsOffset;
    private final List<BoundingBox> checkPoints = new ArrayList<>();
    private final List<LocationOffset3D> checkPointsRespawn = new ArrayList<>();
    private final List<BoundingBox> fallAreas = new ArrayList<>();
    private BoundingBox endArea;
    private int timerTick = 0;

    public RaceArenaBuilder(@NotNull UUID user, @NotNull String id, @NotNull String label) {
        super(user, id, label, Minigames.get());
    }


    @Override
    protected void onPhaseStart() {
        timerTick = 0;
    }

    @Override
    public @NotNull DMessage getCurrentBossBarMessage() {
        return new DMessage(Minigames.get(), getBuilder()).appendLang("arenabuilder.race.bossbar.phase" + getPhase(), "%alias%", getLabel());
    }

    @Override
    public @NotNull DMessage getRepeatedMessage() {
        return switch (getPhase()) {
            case PHASE_SET_TEAM_SPAWNS, PHASE_SET_TEAM_SPAWNS_OR_NEXT -> {
                DMessage teamSet = new DMessage(Minigames.get(), getBuilder());
                for (DyeColor color : DyeColor.values())
                    if (!spawnLocations.containsKey(color))
                        teamSet.appendLang("arenabuilder.race.repeatmessage.setteamcolor", "%color%", color.name(), "%hexa%", UtilColor.getColorHexa(color), "%alias%", getLabel());
                DMessage teamDelete = new DMessage(Minigames.get(), getBuilder());
                for (DyeColor color : spawnLocations.keySet())
                    teamDelete.appendLang("arenabuilder.race.repeatmessage.deleteteamcolor", "%color%", color.name(), "%hexa%", UtilColor.getColorHexa(color), "%alias%", getLabel());
                yield new DMessage(Minigames.get(), getBuilder()).appendLang("arenabuilder.race.repeatmessage.phase" + getPhase()
                        , "%setteamsspawn%", teamSet.toString()
                        , "%deleteteamsspawn%", teamDelete.toString(), "%alias%", getLabel());
            }
            default -> new DMessage(Minigames.get(), getBuilder()).appendLang("arenabuilder.race.repeatmessage.phase" + getPhase(), "%alias%", getLabel());
        };
    }

    @Override
    public void handleCommand(@NotNull Player player, String label, @NotNull String[] args) {
        if (args.length == 0) {
            ERR_UNKNOWN_ACTION.send(player, "%alias%", label);
            return;
        }
        switch (getPhase()) {
            case PHASE_SELECT_AREA -> {
                if (!args[0].equalsIgnoreCase("selectarea")) {
                    ERR_UNKNOWN_ACTION.send(player, "%alias%", label);
                    return;
                }
                try {
                    setArea(player);
                    sendMsg(player, "arenabuilder.race.success.select_area",
                            "%world%", getWorld().getName(),
                            "%x1%", String.valueOf((int) getArea().getMinX()),
                            "%x2%", String.valueOf((int) getArea().getMaxX()),
                            "%y1%", String.valueOf((int) getArea().getMinY()),
                            "%y2%", String.valueOf((int) getArea().getMaxY()),
                            "%z1%", String.valueOf((int) getArea().getMinZ()),
                            "%z2%", String.valueOf((int) getArea().getMaxZ()), "%alias%", label);

                    setPhaseRaw(PHASE_SET_TEAM_SPAWNS);
                } catch (IncompleteRegionException e) {
                    sendMsg(player, "arenabuilder.race.error.unselected_area", "%alias%", label);
                }
            }
            case PHASE_SET_TEAM_SPAWNS, PHASE_SET_TEAM_SPAWNS_OR_NEXT -> {
                switch (args[0].toLowerCase(Locale.ENGLISH)) {
                    case "next" -> {
                        if (spawnLocations.size() >= 2) {
                            setPhaseRaw(PHASE_SET_SPECTATOR_SPAWN);
                            return;
                        } else {
                            ERR_UNKNOWN_ACTION.send(player, "%alias%", label);
                        }
                    }
                    case "setteamspawn" -> {
                        if (!this.isInside(player.getLocation())) {
                            ERR_OUTSIDE_ARENA.send(player, "%alias%", label);
                            return;
                        }
                        //TODO check color value
                        DyeColor color = DyeColor.valueOf(args[1].toUpperCase());
                        LocationOffset3D loc = LocationOffset3D.fromLocation(player.getLocation().subtract(getArea().getMin()));
                        boolean override = spawnLocations.containsKey(color);
                        spawnLocations.put(color, loc);
                        sendMsg(player, override ? "arenabuilder.race.success.override_team_spawn"
                                : "arenabuilder.race.success.set_team_spawn", "%color%", color.name(), "%alias%", label);
                        if (getPhase() == PHASE_SET_TEAM_SPAWNS && spawnLocations.size() >= 2)
                            setPhaseRaw(PHASE_SET_TEAM_SPAWNS_OR_NEXT);
                        else
                            getRepeatedMessage().send();
                        return;
                    }
                    case "deleteteamspawn" -> {
                        //TODO check color value
                        DyeColor color = DyeColor.valueOf(args[1].toUpperCase());
                        spawnLocations.remove(color);
                        sendMsg(player, "arenabuilder.race.success.deleted_team_spawn",
                                "%color%", color.name(), "%alias%", label);
                        if (getPhase() == PHASE_SET_TEAM_SPAWNS_OR_NEXT && spawnLocations.size() < 2)
                            setPhaseRaw(PHASE_SET_TEAM_SPAWNS);
                        else
                            getRepeatedMessage().send();
                        return;
                    }
                }
                ERR_UNKNOWN_ACTION.send(player, "%alias%", label);
            }
            case PHASE_SET_SPECTATOR_SPAWN, PHASE_SET_SPECTATOR_SPAWN_OR_NEXT -> {
                switch (args[0].toLowerCase(Locale.ENGLISH)) {
                    case "next" -> {
                        if (spectatorsOffset != null) {
                            setPhaseRaw(PHASE_CHECKPOINT_AREA_OR_NEXT);
                            return;
                        }
                    }
                    case "setspectatorspawn" -> {
                        if (!this.isInside(player.getLocation())) {
                            ERR_OUTSIDE_ARENA.send(player, "%alias%", label);
                            return;
                        }
                        boolean override = spectatorsOffset != null;
                        spectatorsOffset = LocationOffset3D.fromLocation(player.getLocation().subtract(getArea().getMin()));
                        sendMsg(player, override ? "arenabuilder.race.success.override_spectators_spawn"
                                : "arenabuilder.race.success.set_spectators_spawn", "%alias%", label);
                        if (getPhase() == PHASE_SET_SPECTATOR_SPAWN)
                            setPhaseRaw(PHASE_SET_SPECTATOR_SPAWN_OR_NEXT);
                        return;
                    }
                }
                ERR_UNKNOWN_ACTION.send(player, "%alias%", label);
            }
            case PHASE_CHECKPOINT_AREA_OR_NEXT -> { //checkpointarea or next
                switch (args[0].toLowerCase(Locale.ENGLISH)) {
                    case "next" -> setPhaseRaw(PHASE_FINISH_AREA);
                    case "clear" -> {
                        checkPoints.clear();
                        checkPointsRespawn.clear();
                        sendMsg(player, "arenabuilder.race.success.cleared_checkpoints",
                                "%amount%", String.valueOf(checkPoints.size()), "%alias%", label);
                    }
                    case "checkpointarea" -> {
                        try {
                            World world = player.getWorld();
                            Region sel = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player))
                                    .getSelection(BukkitAdapter.adapt(world));
                            BoundingBox checkPointArea = new BoundingBox(sel.getMinimumPoint().getX(), sel.getMinimumPoint().getY(),
                                    sel.getMinimumPoint().getZ(), sel.getMaximumPoint().getX() + 1, sel.getMaximumPoint().getY() + 1,
                                    sel.getMaximumPoint().getZ() + 1);
                            if (!checkPointArea.overlaps(getArea())) {
                                ERR_SELECTED_AREA_OUTSIDE_ARENA.send(player, "%alias%", label);

                                return;
                            }
                            checkPointArea.intersection(getArea().clone().expand(0, 1, 0)); //minimize
                            if (this.getArea().getVolume() == checkPointArea.getVolume()) {
                                ERR_SELECTED_AREA_TOO_BIG.send(player, "%alias%", label);
                                return;
                            }
                            for (BoundingBox box : checkPoints)
                                if (checkPointArea.overlaps(box)) {
                                    ERR_SELECTED_AREA_OVERLAPPING_CHECKPOINT.send(player, "%alias%", label);
                                    return;
                                }
                            if (this.getArea().getVolume() <= checkPointArea.getVolume() * 0.5) {
                                WARN_SELECTED_AREA_VERY_BIG.send(player, "%volume%", String.valueOf(checkPointArea.getVolume()),
                                        "%maxvolume%", String.valueOf(getArea().getVolume()), "%alias%", label);
                            }
                            if (checkPointArea.getVolume() <= 4) {
                                WARN_SELECTED_AREA_VERY_SMALL.send(player, "%volume%", String.valueOf(checkPointArea.getVolume()), "%alias%", label);
                            }

                            sendMsg(player, "arenabuilder.race.success.select_checkpoint",
                                    "%world%", getWorld().getName(),
                                    "%x1%", String.valueOf((int) checkPointArea.getMinX()),
                                    "%x2%", String.valueOf((int) checkPointArea.getMaxX()),
                                    "%y1%", String.valueOf((int) checkPointArea.getMinY()),
                                    "%y2%", String.valueOf((int) checkPointArea.getMaxY()),
                                    "%z1%", String.valueOf((int) checkPointArea.getMinZ()),
                                    "%z2%", String.valueOf((int) checkPointArea.getMaxZ()), "%number%", String.valueOf(checkPoints.size()), "%alias%", label);
                            checkPoints.add(checkPointArea);
                            setPhaseRaw(PHASE_CHECKPOINT_SPAWN);
                        } catch (IncompleteRegionException e) {
                            sendMsg(player, "arenabuilder.race.error.unselected_area", "%alias%", label);
                        }
                    }
                    default -> ERR_UNKNOWN_ACTION.send(player, "%alias%", label);
                }

            }
            case PHASE_CHECKPOINT_SPAWN -> { //checkpoint checkpointrespawnloc || undo
                switch (args[0].toLowerCase(Locale.ENGLISH)) {
                    case "undo" -> {
                        sendMsg(player, "race.arenabuilder.success.undo_checkpoint", "%alias%", label);
                        checkPoints.remove(checkPoints.size() - 1);
                        setPhaseRaw(PHASE_CHECKPOINT_AREA_OR_NEXT);
                    }
                    case "checkpointrespawnloc" -> {
                        if (!checkPoints.get(checkPoints.size() - 1).contains(player.getLocation().toVector())) {
                            ERR_OUTSIDE_CHECKPOINT.send(player, "%alias%", label);
                            return;
                        }
                        checkPointsRespawn.add(LocationOffset3D.fromLocation(player.getLocation().subtract(getArea().getMin())));
                        sendMsg(player, "race.arenabuilder.success.set_checkpoint_respawn", "%number%", String.valueOf(checkPoints.size()), "%alias%", label);
                        setPhaseRaw(PHASE_CHECKPOINT_AREA_OR_NEXT);
                    }
                    default -> ERR_UNKNOWN_ACTION.send(player, "%alias%", label);
                }
            }
            case PHASE_FINISH_AREA -> { // finisharea
                switch (args[0].toLowerCase(Locale.ENGLISH)) {
                    case "finisharea" -> {
                        try {
                            World world = player.getWorld();
                            Region sel = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player))
                                    .getSelection(BukkitAdapter.adapt(world));
                            BoundingBox finishArea = new BoundingBox(sel.getMinimumPoint().getX(), sel.getMinimumPoint().getY(),
                                    sel.getMinimumPoint().getZ(), sel.getMaximumPoint().getX() + 1, sel.getMaximumPoint().getY() + 1,
                                    sel.getMaximumPoint().getZ() + 1);
                            if (!finishArea.overlaps(getArea())) {
                                ERR_SELECTED_AREA_OUTSIDE_ARENA.send(player, "%alias%", label);
                                return;
                            }
                            finishArea.intersection(getArea().clone().expand(0, 1, 0)); //minimize
                            if (this.getArea().getVolume() == finishArea.getVolume()) {
                                ERR_SELECTED_AREA_TOO_BIG.send(player, "%alias%", label);
                                return;
                            }
                            for (BoundingBox box : checkPoints)
                                if (finishArea.overlaps(box)) {
                                    ERR_SELECTED_AREA_OVERLAPPING_CHECKPOINT.send(player, "%alias%", label);
                                    return;
                                }
                            if (this.getArea().getVolume() <= finishArea.getVolume() * 0.5) {
                                WARN_SELECTED_AREA_VERY_BIG.send(player, "%volume%", String.valueOf(finishArea.getVolume()),
                                        "%maxvolume%", String.valueOf(getArea().getVolume()), "%alias%", label);
                            }
                            if (finishArea.getVolume() < 2) {
                                WARN_SELECTED_AREA_VERY_SMALL.send(player, "%volume%", String.valueOf(finishArea.getVolume()), "%alias%", label);
                            }
                            sendMsg(player, "arenabuilder.race.success.select_finisharea",
                                    "%world%", getWorld().getName(),
                                    "%x1%", String.valueOf((int) finishArea.getMinX()),
                                    "%x2%", String.valueOf((int) finishArea.getMaxX()),
                                    "%y1%", String.valueOf((int) finishArea.getMinY()),
                                    "%y2%", String.valueOf((int) finishArea.getMaxY()),
                                    "%z1%", String.valueOf((int) finishArea.getMinZ()),
                                    "%z2%", String.valueOf((int) finishArea.getMaxZ()), "%alias%", label);
                            this.endArea = finishArea;
                            setPhaseRaw(PHASE_FALLING_ZONES);
                        } catch (IncompleteRegionException e) {
                            sendMsg(player, "arenabuilder.race.error.unselected_area", "%alias%", label);
                        }
                    }
                    default -> ERR_UNKNOWN_ACTION.send(player, "%alias%", label);
                }
            }
            case PHASE_FALLING_ZONES -> { //zone di caduta
                switch (args[0].toLowerCase(Locale.ENGLISH)) {
                    case "undofinisharea" -> {
                        this.endArea = null;
                        this.fallAreas.clear();
                        this.setPhaseRaw(PHASE_FINISH_AREA);
                    }
                    case "next" -> {
                        ArenaManager.get().onArenaBuilderCompletedArena(this);
                        sendMsg(player, "arenabuilder.race.success.completed",
                                UtilsString.merge(ArenaManager.get().get(getId()).getPlaceholders(), "%alias%", label));
                    }
                    case "fallingzone" -> {
                        try {
                            World world = player.getWorld();
                            Region sel = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player))
                                    .getSelection(BukkitAdapter.adapt(world));
                            BoundingBox fallArea = new BoundingBox(sel.getMinimumPoint().getX(), sel.getMinimumPoint().getY(),
                                    sel.getMinimumPoint().getZ(), sel.getMaximumPoint().getX() + 1, sel.getMaximumPoint().getY() + 1,
                                    sel.getMaximumPoint().getZ() + 1);
                            if (!fallArea.overlaps(getArea())) {
                                ERR_SELECTED_AREA_OUTSIDE_ARENA.send(player, "%alias%", label);
                                return;
                            }
                            fallArea.intersection(getArea().clone().expand(0, 1, 0)); //minimize
                            if (this.getArea().getVolume() == fallArea.getVolume()) {
                                ERR_SELECTED_AREA_TOO_BIG.send(player, "%alias%", label);
                                return;
                            }
                            for (BoundingBox box : checkPoints)
                                if (fallArea.overlaps(box)) {
                                    ERR_SELECTED_AREA_OVERLAPPING_CHECKPOINT.send(player, "%alias%", label);

                                    return;
                                }
                            if (fallArea.overlaps(this.endArea)) {
                                ERR_SELECTED_AREA_OVERLAPPING_FINISH_AREA.send(player, "%alias%", label);
                                return;
                            }
                            fallAreas.add(fallArea);
                            sendMsg(player, "arenabuilder.race.success.select_fallingzone",
                                    "%world%", getWorld().getName(),
                                    "%x1%", String.valueOf((int) fallArea.getMinX()),
                                    "%x2%", String.valueOf((int) fallArea.getMaxX()),
                                    "%y1%", String.valueOf((int) fallArea.getMinY()),
                                    "%y2%", String.valueOf((int) fallArea.getMaxY()),
                                    "%z1%", String.valueOf((int) fallArea.getMinZ()),
                                    "%z2%", String.valueOf((int) fallArea.getMaxZ()), "%number%", String.valueOf(fallAreas.size()), "%alias%", label);
                        } catch (IncompleteRegionException e) {
                            sendMsg(player, "arenabuilder.race.error.unselected_area", "%alias%", label);
                        }
                    }
                    case "undofallingzone" -> {
                        if (fallAreas.size() > 0) {
                            fallAreas.remove(fallAreas.size() - 1);
                            sendMsg(player, "arenabuilder.race.success.deleted_last_fallingzone",
                                    "%alias%", label);
                        }
                    }
                }
            }
            default -> new IllegalStateException().printStackTrace();
        }
    }

    @Override
    public List<String> handleComplete(@NotNull String[] args) {
        return switch (getPhase()) {
            case PHASE_SELECT_AREA -> args.length == 1 ? UtilsCommand.complete(args[0], List.of("selectarea")) : Collections.emptyList();
            case PHASE_SET_TEAM_SPAWNS -> args.length == 1 ? UtilsCommand.complete(args[0], List.of("setteamspawn")) :
                    args.length == 2 ? UtilsCommand.complete(args[1], DyeColor.class, (DyeColor c) -> !spawnLocations.containsKey(c)) : Collections.emptyList();
            case PHASE_SET_TEAM_SPAWNS_OR_NEXT -> {
                if (args.length == 1)
                    yield UtilsCommand.complete(args[0], List.of("setteamspawn", "next"));
                if (args.length == 2)
                    yield UtilsCommand.complete(args[1], DyeColor.class, (DyeColor c) -> !spawnLocations.containsKey(c));
                yield Collections.emptyList();
            }
            case PHASE_SET_SPECTATOR_SPAWN -> UtilsCommand.complete(args[0], List.of("setspectatorspawn"));
            case PHASE_SET_SPECTATOR_SPAWN_OR_NEXT -> UtilsCommand.complete(args[0], List.of("setspectatorspawn", "next"));
            case PHASE_CHECKPOINT_AREA_OR_NEXT -> UtilsCommand.complete(args[0], List.of("checkpointarea", "next", "clear"));
            case PHASE_CHECKPOINT_SPAWN -> UtilsCommand.complete(args[0], List.of("checkpointrespawnloc", "undo"));
            case PHASE_FINISH_AREA -> UtilsCommand.complete(args[0], List.of("finisharea"));
            case PHASE_FALLING_ZONES -> UtilsCommand.complete(args[0], List.of("fallingzone", "next", "undofinisharea", "undofallingzone"));
            default -> throw new IllegalStateException("Unexpected value: " + getPhase());
        };
    }

    @Override
    public RaceArena build() {
        Map<String, Object> map = new LinkedHashMap<>();

        String schemName = Bukkit.getOfflinePlayer(getUser()).getName() + "_" + getId();
        File schemFile = new File(ArenaManager.get().getSchematicsFolder(), schemName);
        String schemNameFinal = schemName;

        //avoid overriding existing schematics
        int counter = 1;
        while (schemFile.exists()) {
            schemNameFinal = schemName + "_" + counter;
            schemFile = new File(ArenaManager.get().getSchematicsFolder(), schemNameFinal);
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

        //optimize fallareas, remove contained inside others
        for (int i = 0; i < fallAreas.size(); i++)
            for (int j = 0; j < fallAreas.size(); j++)
                if (i != j && fallAreas.get(i).overlaps(fallAreas.get(j)) && fallAreas.get(j).contains(fallAreas.get(i))) {
                    fallAreas.remove(i);
                    i--;
                    break;
                }

        //remove offset from boundingboxes
        checkPoints.forEach((box) -> box.shift(getArea().getMin().multiply(-1)));
        fallAreas.forEach((box) -> box.shift(getArea().getMin().multiply(-1)));
        endArea.shift(getArea().getMin().multiply(-1));
        map.put("checkpoints", checkPoints);
        List<String> raw = new ArrayList<>();
        checkPointsRespawn.forEach((loc) -> raw.add(loc.toString()));
        map.put("checkpoints_respawn", raw);
        map.put("end_area", endArea);
        map.put("fall_areas", fallAreas);

        return new RaceArena(map);
    }

    @Override
    public void onTimerCall() {
        Player p = getBuilder();
        if (p == null || !p.isOnline())
            return;
        timerTick++;

        if (timerTick % 180 == 0) { //every 45 seconds
            getRepeatedMessage().send();
        }
        if (timerTick % 2 == 0) { //every 15 game ticks
            if (getPhase() <= PHASE_SELECT_AREA)
                this.spawnParticleWorldEditRegionEdges(p, Particle.COMPOSTER);
            else
                this.spawnParticleBoxEdges(p, Particle.COMPOSTER, getArea().expand(0, 0, 0, 1, 1, 1));
            if (getPhase() <= PHASE_SELECT_AREA)
                return;
            Vector min = getAreaMin();
            if (!getArea().equals(getWorldEditSection(p)))
                spawnParticleWorldEditRegionEdges(p, Particle.WAX_OFF);
            spawnLocations.forEach((k, v) -> spawnParticleCircle(p, Particle.REDSTONE, min.getX() + v.x, min.getY() + v.y, min.getZ() + v.z,
                    0.4, timerTick % 4 == 0, new Particle.DustOptions(k.getColor(), 1F)));
            if (spectatorsOffset != null) {
                spawnParticleCircle(p, Particle.WAX_ON, min.getX() + spectatorsOffset.x, min.getY() + spectatorsOffset.y, min.getZ() + spectatorsOffset.z,
                        0.4, timerTick % 4 == 0);
                if (timerTick % 4 == 0)
                    spawnParticle(p, Particle.SCULK_SOUL, min.getX() + spectatorsOffset.x, min.getY() + spectatorsOffset.y + 1, min.getZ() + spectatorsOffset.z);
            }
            for (int i = 0; i < checkPoints.size(); i++) {
                spawnParticleBoxEdges(p, Particle.REDSTONE, checkPoints.get(i), new Particle.DustOptions(getCheckpointColor(i), 1F));
            }
            for (int j = 0; j < checkPointsRespawn.size(); j++) {
                spawnParticleCircle(p, Particle.REDSTONE, min.getX() + checkPointsRespawn.get(j).x, min.getY() +
                                checkPointsRespawn.get(j).y, min.getZ() + checkPointsRespawn.get(j).z, 0.4,
                        timerTick % 4 == 0, new Particle.DustOptions(getCheckpointColor(j), 0.5F));
                if (timerTick % 4 == 0)
                    for (int i = 0; i <= j; i++)
                        spawnParticle(p, Particle.HEART, min.getX() + checkPointsRespawn.get(j).x, min.getY() +
                                checkPointsRespawn.get(j).y + 0.5 + (0.5 * (i)), min.getZ() + checkPointsRespawn.get(j).z);
            }
            if (endArea != null)
                spawnParticleBoxEdges(p, Particle.REDSTONE, endArea, new Particle.DustOptions(getCheckpointColor(checkPoints.size()), 2F));

            fallAreas.forEach((fallArea) -> spawnParticleBoxFaces(p, timerTick, Particle.FLAME, fallArea, null));
        }
    }

    private static Color getCheckpointColor(int i) {
        return switch (i % 10) {
            case 0 -> Color.fromBGR(Integer.decode("0xeb3434"));
            case 1 -> Color.fromBGR(Integer.decode("0xeb9f34"));
            case 2 -> Color.fromBGR(Integer.decode("0xebe234"));
            case 3 -> Color.fromBGR(Integer.decode("0xb4eb34"));
            case 4 -> Color.fromBGR(Integer.decode("0x34eb56"));
            case 5 -> Color.fromBGR(Integer.decode("0x34aeeb"));
            case 6 -> Color.fromBGR(Integer.decode("0x3440eb"));
            case 7 -> Color.fromBGR(Integer.decode("0x8334eb"));
            case 8 -> Color.fromBGR(Integer.decode("0xc934eb"));
            case 9 -> Color.fromBGR(Integer.decode("0xeb348f"));
            default -> throw new IllegalStateException("Unexpected value: " + i % 10);
        };
    }
}

