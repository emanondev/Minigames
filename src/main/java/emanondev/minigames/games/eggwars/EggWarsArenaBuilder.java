package emanondev.minigames.games.eggwars;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import emanondev.core.UtilsString;
import emanondev.core.message.DMessage;
import emanondev.core.message.SimpleMessage;
import emanondev.core.util.ReadUtility;
import emanondev.core.util.WorldEditUtility;
import emanondev.minigames.ArenaManager;
import emanondev.minigames.MinigameTypes;
import emanondev.minigames.Minigames;
import emanondev.minigames.games.SchematicArenaBuilder;
import emanondev.minigames.locations.BlockLocationOffset3D;
import emanondev.minigames.locations.LocationOffset3D;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class EggWarsArenaBuilder extends SchematicArenaBuilder {

    /*
    EGGWARS
    Step1
    Seleziona l'area
    Step2
    Seleziona i team
    Step2
    Seleziona quanti utenti per team
    Step3
    Imposta i punti di spawn (1 per player)
    Step4
    Imposta i punti di respawn (1 per team)
    Step8
    Imposta Location Villici
    Step5
    Imposta spawn spettatori
    Step6
    Imposta Uova team
    Step7
    Imposta Generatori <type> <lv>
    Step9
    Imposta aree noBuild
     */

    private static final SimpleMessage ERR_UNKNOWN_ACTION = new SimpleMessage(Minigames.get(), "arenabuilder.eggwars.error.unknown_action");
    private static final SimpleMessage ERR_OUTSIDE_ARENA = new SimpleMessage(Minigames.get(), "arenabuilder.eggwars.error.outside_arena");
    private static final SimpleMessage ERR_SELECTED_AREA_OUTSIDE_ARENA = new SimpleMessage(Minigames.get(), "arenabuilder.eggwars.error.selected_area_outside_arena");
    private static final SimpleMessage ERR_SELECTED_AREA_TOO_BIG = new SimpleMessage(Minigames.get(), "arenabuilder.eggwars.error.selected_area_too_big");

    private static final int PHASE_SELECT_AREA = 1;
    private static final int PHASE_SELECT_TEAMS = 2;
    private static final int PHASE_SELECT_TEAMS_SIZE = 3;
    private static final int PHASE_SET_SPAWN_LOCATIONS = 4;
    private static final int PHASE_SET_RESPAWN_LOCATIONS = 5;
    private static final int PHASE_SET_TEAMS_EGG = 6;
    private static final int PHASE_SET_VILLAGERS = 7;
    private static final int PHASE_SET_SPECTATOR_SPAWN = 8;
    private static final int PHASE_SET_GENERATORS = 9;
    private static final int PHASE_SET_NO_BUILD_AREAS = 10;


    //private final HashMap<DyeColor, LocationOffset3D> spawnLocations = new HashMap<>();
    private final List<DyeColor> teams = new ArrayList<>();
    private DyeColor currentColor = null;
    private final LinkedHashMap<DyeColor, List<LocationOffset3D>> teamsSpawns = new LinkedHashMap<>();
    private final LinkedHashMap<DyeColor, LocationOffset3D> teamsRespawn = new LinkedHashMap<>();
    private final LinkedHashMap<DyeColor, BlockLocationOffset3D> teamsEgg = new LinkedHashMap<>();
    private final List<LocationOffset3D> villagers = new ArrayList<>();
    private final Map<EggWarsGeneratorType, Map<BlockLocationOffset3D, Integer>> generators = new HashMap<>();
    private LocationOffset3D spectatorsOffset;
    private final List<BoundingBox> noBuildAreas = new ArrayList<>();

    private int teamsSize = 0;
    private int timerTick = 0;

    public EggWarsArenaBuilder(@NotNull UUID user, @NotNull String id, @NotNull String label) {
        super(user, id, label, Minigames.get());
    }

    @Override
    protected void onPhaseStart() {
        timerTick = 0;
    }

    @Override
    public @NotNull DMessage getCurrentBossBarMessage() {
        return new DMessage(Minigames.get(), getBuilder()).appendLang("arenabuilder.eggwars.bossbar.phase" + getPhase(), "%alias%", getLabel());
    }

    @Override
    public @NotNull DMessage getRepeatedMessage() {
        return switch (getPhase()) {

            default -> new DMessage(Minigames.get(), getBuilder()).appendLang("arenabuilder.eggwars.repeatmessage.phase" + getPhase(), "%alias%", getLabel());
        };
    }

    @Override
    public void handleCommand(@NotNull Player player, String label, @NotNull String[] args) {
        if (args.length == 0) {
            ERR_UNKNOWN_ACTION.send(player, "%alias%", label);
            return;
        }
        try {
            switch (getPhase()) {
                case PHASE_SELECT_AREA -> {
                    if (!args[0].equalsIgnoreCase("selectarea")) {
                        ERR_UNKNOWN_ACTION.send(player, "%alias%", label);
                        return;
                    }
                    try {
                        setArea(player);
                        sendDMessage(player, "arenabuilder.eggwars.success.select_area",
                                "%world%", getWorld().getName(),
                                "%x1%", String.valueOf((int) getArea().getMinX()),
                                "%x2%", String.valueOf((int) getArea().getMaxX()),
                                "%y1%", String.valueOf((int) getArea().getMinY()),
                                "%y2%", String.valueOf((int) getArea().getMaxY()),
                                "%z1%", String.valueOf((int) getArea().getMinZ()),
                                "%z2%", String.valueOf((int) getArea().getMaxZ()), "%alias%", label);

                        setPhaseRaw(PHASE_SELECT_TEAMS);
                    } catch (IncompleteRegionException e) {
                        sendDMessage(player, "arenabuilder.eggwars.error.unselected_area", "%alias%", label);
                    }
                }
                case PHASE_SELECT_TEAMS -> {
                    switch (args[0].toLowerCase(Locale.ENGLISH)) {
                        case "next" -> {
                            if (teams.size() >= 2) {
                                //MessageUtil.sendMessage(player, "arenabuilder.eggwars.success.next");
                                setPhaseRaw(EggWarsArenaBuilder.PHASE_SET_SPECTATOR_SPAWN);
                                return;
                            }
                        }
                        case "addteam" -> {
                            //TODO check color value
                            DyeColor color = DyeColor.valueOf(args[1].toUpperCase());
                            //LocationOffset3D loc = LocationOffset3D.fromLocation(player.getLocation().subtract(getArea().getMin()));
                            //boolean override = spawnLocations.containsKey(color);
                            //spawnLocations.put(color, loc);
                            if (teams.contains(color))
                                return;
                            teams.add(color);//TODO feedback
                            getRepeatedMessage().send();
                            return;
                        }
                        case "removeteam" -> {
                            //TODO check color value
                            DyeColor color = DyeColor.valueOf(args[1].toUpperCase());
                            if (!teams.contains(color))
                                return;
                            teams.remove(color);//TODO feedback
                            getRepeatedMessage().send();
                            return;
                        }
                    }
                    ERR_UNKNOWN_ACTION.send(player, "%alias%", label);
                }
                case PHASE_SELECT_TEAMS_SIZE -> {
                    if ("size".equals(args[0].toLowerCase(Locale.ENGLISH))) {
                        Integer val = args.length <= 1 ? null : ReadUtility.readIntValue(args[1]);
                        if (val == null) {
                            //TODO
                            return;
                        }
                        if (val <= 0) {
                            //TODO
                            return;
                        }
                        teamsSize = val;
                        //TODO
                        setPhaseRaw(PHASE_SET_SPAWN_LOCATIONS);
                        for (DyeColor color : teams)
                            teamsSpawns.put(color, new ArrayList<>(Collections.nCopies(teamsSize, null)));
                        currentColor = teams.get(0);
                        getRepeatedMessage().send();
                        return;
                    }
                    ERR_UNKNOWN_ACTION.send(player, "%alias%", label);
                }
                case PHASE_SET_SPAWN_LOCATIONS -> {
                    switch (args[0].toLowerCase(Locale.ENGLISH)) {
                        case "clear" -> {
                            teamsSpawns.get(currentColor).clear();
                            //TODO fb
                            getRepeatedMessage().send();
                            return;
                        }
                        case "add" -> {
                            if (!this.isInside(player.getLocation())) {
                                ERR_OUTSIDE_ARENA.send(player, "%alias%", label);
                                return;
                            }
                            if (args.length < 2) {
                                //TODO fb
                                return;
                            }

                            /*Integer val = ReadUtility.readIntValue(args[1]);
                            if (val == null) {
                                //TODO
                                return;
                            }
                            val = val - 1;
                            if (val < 0 || val >= teamsSize) {
                                //TODO
                                return;
                            }*/
                            LocationOffset3D loc = LocationOffset3D.fromLocation(player.getLocation().subtract(getArea().getMin()));
                            loc = new LocationOffset3D(Math.floor(loc.x) + 0.5, Math.floor(loc.y), Math.floor(loc.z) + 0.5, loc.yaw, loc.pitch);
                            //TODO no overlap test
                            //boolean override = teamsSpawns.get(currentColor).get(val) == null;
                            teamsSpawns.get(currentColor).add(loc);
                            //TODO fb
                            if (teamsSpawns.get(currentColor).size() >= teamsSize) {
                                setPhaseRaw(PHASE_SET_RESPAWN_LOCATIONS);
                            }
                            getRepeatedMessage().send();
                            return;
                        }
                    }
                    ERR_UNKNOWN_ACTION.send(player, "%alias%", label);
                }
                case PHASE_SET_RESPAWN_LOCATIONS -> {
                    /*set*/
                    if ("setrespawn".equals(args[0].toLowerCase(Locale.ENGLISH))) {
                        if (!this.isInside(player.getLocation())) {
                            ERR_OUTSIDE_ARENA.send(player, "%alias%", label);
                            return;
                        }
                        teamsRespawn.put(currentColor, LocationOffset3D.fromLocation(player.getLocation().subtract(getArea().getMin())));
                        setPhaseRaw(PHASE_SET_TEAMS_EGG);
                        getRepeatedMessage().send();
                        return;
                    }
                    ERR_UNKNOWN_ACTION.send(player, "%alias%", label);
                }
                case PHASE_SET_TEAMS_EGG -> {
                    if ("setegg".equals(args[0].toLowerCase(Locale.ENGLISH))) {
                        Block b = getBuilder().getTargetBlockExact(10);
                        if (b == null) {
                            //TODO
                            return;
                        }
                        if (b.getType() != Material.DRAGON_EGG) {
                            //TODO
                            return;
                        }
                        if (!this.isInside(b.getLocation())) {
                            ERR_OUTSIDE_ARENA.send(player, "%alias%", label);
                            return;
                        }
                        BlockLocationOffset3D loc = BlockLocationOffset3D.fromLocation(player.getLocation().subtract(getArea().getMin()));
                        if (teamsEgg.containsValue(loc)) {
                            //TODO
                            return;
                        }
                        teamsEgg.put(currentColor, loc);
                        int index = teams.indexOf(currentColor) + 1;
                        if (index < teams.size()) {
                            currentColor = teams.get(index);
                            setPhaseRaw(PHASE_SET_SPAWN_LOCATIONS);
                        }
                        setPhaseRaw(PHASE_SET_VILLAGERS);
                        getRepeatedMessage().send();
                        return;
                    }
                    ERR_UNKNOWN_ACTION.send(player, "%alias%", label);
                }
                case PHASE_SET_VILLAGERS -> {
                    switch (args[0].toLowerCase(Locale.ENGLISH)) {
                        case "setvillager" -> {
                            if (!this.isInside(player.getLocation())) {
                                ERR_OUTSIDE_ARENA.send(player, "%alias%", label);
                                return;
                            }
                            villagers.add(LocationOffset3D.fromLocation(player.getLocation().subtract(getArea().getMin())));
                            //TODO
                            getRepeatedMessage().send();
                            return;
                        }
                        case "clear" -> {
                            villagers.clear();
                            getRepeatedMessage().send();
                            return;
                        }
                        case "next" -> {
                            //TODO check villagers #

                            setPhaseRaw(PHASE_SET_SPECTATOR_SPAWN);
                            getRepeatedMessage().send();
                            return;
                        }
                    }
                    ERR_UNKNOWN_ACTION.send(player, "%alias%", label);
                }
                case PHASE_SET_SPECTATOR_SPAWN -> {
                    switch (args[0].toLowerCase(Locale.ENGLISH)) {
                        case "next" -> {
                            if (spectatorsOffset != null) {
                                setPhaseRaw(PHASE_SET_GENERATORS);
                                getRepeatedMessage().send();
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
                            sendDMessage(player, override ? "arenabuilder.eggwars.success.override_spectators_spawn"
                                    : "arenabuilder.eggwars.success.set_spectators_spawn", "%alias%", label);
                            getRepeatedMessage().send();
                            return;
                        }
                    }
                    ERR_UNKNOWN_ACTION.send(player, "%alias%", label);
                }
                case PHASE_SET_GENERATORS -> {
                    switch (args[0].toLowerCase(Locale.ENGLISH)) {
                        case "clear" -> {
                            EggWarsGeneratorType type;
                            if (args.length <= 1) {
                                //TODO specify generator
                                return;
                            }
                            type = MinigameTypes.EGGWARS.getGenerator(args[1]);
                            if (type == null) {
                                //TODO invalid type
                                return;
                            }
                            generators.remove(type);
                            //TODO feedback
                            getRepeatedMessage().send();
                            return;
                        }
                        case "next" -> {
                            if (generators.isEmpty()) {
                                //TODO no generators!
                                return;
                            }
                            setPhaseRaw(PHASE_SET_NO_BUILD_AREAS);
                            getRepeatedMessage().send();
                            return;
                        }
                        case "add" -> {
                            Block b = getBuilder().getTargetBlockExact(10);
                            if (b == null) {
                                //TODO
                                return;
                            }
                            if (!Tag.WALL_SIGNS.isTagged(b.getType())) {
                                //TODO
                                return;
                            }
                            if (!this.isInside(b.getLocation())) {
                                ERR_OUTSIDE_ARENA.send(player, "%alias%", label);
                                return;
                            }
                            EggWarsGeneratorType type;
                            if (args.length <= 1) {
                                //TODO specify generator
                                return;
                            }
                            type = MinigameTypes.EGGWARS.getGenerator(args[1]);
                            if (type == null) {
                                //TODO invalid type
                                return;
                            }
                            if (args.length <= 2) {
                                //TODO specify level
                                return;
                            }
                            Integer level = ReadUtility.readIntValue(args[2]);
                            if (level == null) {
                                //TODO invalid amount
                                return;
                            }
                            BlockLocationOffset3D loc = BlockLocationOffset3D.fromLocation(player.getLocation().subtract(getArea().getMin()));
                            generators.computeIfAbsent(type, k -> new HashMap<>()).put(loc, level);
                            //TODO feedback (override?)
                            getRepeatedMessage().send();
                            return;
                        }
                    }
                    ERR_UNKNOWN_ACTION.send(player, "%alias%", label);

                }
                case PHASE_SET_NO_BUILD_AREAS -> {
                    switch (args[0].toLowerCase(Locale.ENGLISH)) {
                        case "next" -> {
                            ArenaManager.get().onArenaBuilderCompletedArena(this);
                            sendDMessage(player, "arenabuilder.eggwars.success.completed",
                                    UtilsString.merge(ArenaManager.get().get(getId()).getPlaceholders(), "%alias%", label));
                            return;
                        }
                        case "setarea" -> {
                            try {
                                World world = player.getWorld();
                                Region sel = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player))
                                        .getSelection(BukkitAdapter.adapt(world));
                                BoundingBox noBuildArea = new BoundingBox(sel.getMinimumPoint().getX(), sel.getMinimumPoint().getY(),
                                        sel.getMinimumPoint().getZ(), sel.getMaximumPoint().getX() + 1, sel.getMaximumPoint().getY() + 1,
                                        sel.getMaximumPoint().getZ() + 1);
                                if (!noBuildArea.overlaps(getArea())) {
                                    ERR_SELECTED_AREA_OUTSIDE_ARENA.send(player, "%alias%", label);
                                    return;
                                }
                                noBuildArea.intersection(getArea().clone().expand(0, 1, 0)); //minimize
                                if (this.getArea().getVolume() == noBuildArea.getVolume()) {
                                    ERR_SELECTED_AREA_TOO_BIG.send(player, "%alias%", label);
                                    return;
                                }
                                noBuildAreas.add(noBuildArea);
                                sendDMessage(player, "arenabuilder.race.success.select_nobuildarea",
                                        "%world%", getWorld().getName(),
                                        "%x1%", String.valueOf((int) noBuildArea.getMinX()),
                                        "%x2%", String.valueOf((int) noBuildArea.getMaxX()),
                                        "%y1%", String.valueOf((int) noBuildArea.getMinY()),
                                        "%y2%", String.valueOf((int) noBuildArea.getMaxY()),
                                        "%z1%", String.valueOf((int) noBuildArea.getMinZ()),
                                        "%z2%", String.valueOf((int) noBuildArea.getMaxZ()), "%number%", String.valueOf(noBuildAreas.size()), "%alias%", label);
                            } catch (IncompleteRegionException e) {
                                sendDMessage(player, "arenabuilder.race.error.unselected_area", "%alias%", label);
                            }
                            return;
                        }
                        case "undo" -> {
                            if (noBuildAreas.size() > 0) {
                                noBuildAreas.remove(noBuildAreas.size() - 1);
                                sendDMessage(player, "arenabuilder.race.success.deleted_last_nobuildarea",
                                        "%alias%", label);
                            }
                            return;
                        }
                    }
                    ERR_UNKNOWN_ACTION.send(player, "%alias%", label);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> handleComplete(@NotNull String[] args) {
        return switch (args.length) {
            case 1 -> switch (getPhase()) {
                case PHASE_SELECT_AREA -> complete(args[0], List.of("selectarea"));
                case PHASE_SELECT_TEAMS -> complete(args[0], List.of("next", "addteam", "removeteam"));
                case PHASE_SELECT_TEAMS_SIZE -> complete(args[0], List.of("size"));
                case PHASE_SET_SPAWN_LOCATIONS -> complete(args[0], List.of("clear", "add"));
                case PHASE_SET_RESPAWN_LOCATIONS -> complete(args[0], List.of("setrespawn"));
                case PHASE_SET_TEAMS_EGG -> complete(args[0], List.of("setegg"));
                case PHASE_SET_VILLAGERS -> complete(args[0], List.of("setvillager", "clear", "next"));
                case PHASE_SET_SPECTATOR_SPAWN -> complete(args[0], List.of("setspectatorspawn", "next"));
                case PHASE_SET_GENERATORS -> complete(args[0], List.of("clear", "next", "add"));
                case PHASE_SET_NO_BUILD_AREAS -> complete(args[0], List.of("next", "setarea", "undo"));
                default -> Collections.emptyList();
            };
            case 2 -> switch (getPhase()) {
                case PHASE_SELECT_TEAMS -> switch (args[0].toLowerCase(Locale.ENGLISH)) {
                    case "removeteam" -> complete(args[1], teams, (color -> color.name().toLowerCase()));
                    case "addteam" -> complete(args[1], EnumSet.complementOf(EnumSet.copyOf(teams)), (color -> color.name().toLowerCase()));
                    default -> Collections.emptyList();
                };
                case PHASE_SELECT_TEAMS_SIZE -> Objects.equals(args[0].toLowerCase(Locale.ENGLISH), "size") ?
                        complete(args[1], List.of("1", "2", "3", "4", "5", "6", "7", "8")) : Collections.emptyList();
                case PHASE_SET_GENERATORS -> switch (args[0].toLowerCase(Locale.ENGLISH)) {
                    case "add", "clear" -> complete(args[1], MinigameTypes.EGGWARS.getGenerators(), EggWarsGeneratorType::getType);
                    default -> Collections.emptyList();
                };
                default -> Collections.emptyList();
            };
            case 3 -> switch (getPhase()) {
                case PHASE_SET_GENERATORS -> switch (args[0].toLowerCase(Locale.ENGLISH)) {
                    case "add" -> {
                        EggWarsGeneratorType type = MinigameTypes.EGGWARS.getGenerator(args[1]);
                        if (type == null)
                            yield Collections.emptyList();
                        List<Integer> values = new ArrayList<>();
                        for (int i = 0; i <= type.getMaxLevel(); i++)
                            values.add(i);
                        yield complete(args[2], values, String::valueOf);
                    }
                    default -> Collections.emptyList();
                };
                default -> Collections.emptyList();
            };
            default -> Collections.emptyList();
        };
    }

    @Override
    public EggWarsArena build() {
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

        LinkedHashMap<String, Map<String, Object>> teamsMap = new LinkedHashMap<>();
        for (DyeColor color : teams) {
            HashMap<String, Object> teamMap = new HashMap<>();
            List<String> wrap = new ArrayList<>();
            teamsSpawns.get(color).forEach(loc -> wrap.add(loc.toString()));
            teamMap.put("spawn_loc", wrap);
            teamMap.put("respawn_loc", teamsRespawn.get(color).toString());
            teamMap.put("egg", teamsEgg.get(color).toString());
            teamsMap.put(color.name(), teamMap);
        }
        map.put("teams", teamsMap);
        List<String> wrap = new ArrayList<>();
        villagers.forEach(v -> wrap.add(v.toString()));
        map.put("villagers", wrap);
        HashMap<String, Map<String, Integer>> genMap = new HashMap<>();
        for (EggWarsGeneratorType type : generators.keySet()) {
            HashMap<String, Integer> subMap = new HashMap<>();
            Map<BlockLocationOffset3D, Integer> generatorsOfType = generators.get(type);
            generatorsOfType.forEach((k, v) -> subMap.put(k.toString(), v));
            genMap.put(type.getType(), subMap);
        }
        map.put("generators", genMap);
        noBuildAreas.forEach((box) -> box.shift(getArea().getMin().multiply(-1)));
        map.put("no_build_areas", noBuildAreas);
        map.put("team_size", teamsSize);
        map.put("spectator_spawn_offset", spectatorsOffset.toString());

        WorldEditUtility.save(schemFile,
                WorldEditUtility.copy(getWorld(), getArea(), false, true));


        return new EggWarsArena(map);
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
            teamsSpawns.forEach((color, values) -> {
                int index = 0;
                for (LocationOffset3D v : values) {
                    index++;
                    spawnParticleLine(p, Particle.REDSTONE, min.getX() + v.x, min.getY() + v.y+1.62, min.getZ() + v.z,
                            v.getDirection().multiply(0.25), 3, new Particle.DustOptions(color.getColor(), 1F));
                    spawnParticleCircle(p, Particle.REDSTONE, min.getX() + v.x, min.getY() + v.y, min.getZ() + v.z,
                            0.4, timerTick % 4 == 0, new Particle.DustOptions(color.getColor(), 1F));
                    if (timerTick % 4 == 0)
                        for (int i = 0; i <= index; i++)
                            spawnParticle(p, Particle.HEART, min.getX() + v.x, min.getY() +
                                    v.y + 0.5 + (0.5 * (i)), min.getZ() + v.z);
                }
            });
            teamsRespawn.forEach((color, v) -> {
                spawnParticleCircle(p, Particle.REDSTONE, min.getX() + v.x, min.getY() + v.y, min.getZ() + v.z,
                        0.4, timerTick % 4 == 0, new Particle.DustOptions(color.getColor(), 1F));
                if (timerTick % 4 == 0)
                    spawnParticle(p, Particle.SCULK_SOUL, min.getX() + v.x, min.getY() +
                            v.y + 0.5, min.getZ() + v.z);
            });
            teamsRespawn.forEach((color, v) -> {
                spawnParticleCircle(p, Particle.REDSTONE, min.getX() + v.x, min.getY() + v.y, min.getZ() + v.z,
                        0.4, timerTick % 4 == 0, new Particle.DustOptions(color.getColor(), 1F));
                if (timerTick % 4 == 0)
                    spawnParticle(p, Particle.SCULK_SOUL, min.getX() + v.x, min.getY() +
                            v.y + 0.5, min.getZ() + v.z);
            });
            noBuildAreas.forEach((area) -> spawnParticleBoxFaces(p, timerTick, Particle.FLAME, area, null));
            villagers.forEach(v -> spawnParticleCircle(p, Particle.VILLAGER_HAPPY, min.getX() + v.x, min.getY() + v.y, min.getZ() + v.z,
                    0.4, timerTick % 4 == 0));
            generators.forEach((type, values) -> {
                for (BlockLocationOffset3D v : values.keySet()) {
                    spawnParticleBoxEdges(p, Particle.REDSTONE, new BoundingBox(min.getX() + v.x, min.getY() + v.y, min.getZ() + v.z,
                            min.getX() + v.x + 1, min.getY() + v.y + 1, min.getZ() + v.z + 1), new Particle.DustOptions(type.getColor(), 0.3F));
                    spawnParticleCircle(p, Particle.REDSTONE, min.getX() + v.x + 0.5, min.getY() + v.y + 0.1, min.getZ() + v.z + 0.5,
                            0.2, timerTick % 4 == 0, new Particle.DustOptions(type.getColor(), 0.1F));
                }
            });
            if (spectatorsOffset != null) {
                spawnParticleCircle(p, Particle.WAX_ON, min.getX() + spectatorsOffset.x, min.getY() + spectatorsOffset.y, min.getZ() + spectatorsOffset.z,
                        0.4, timerTick % 4 == 0);
                if (timerTick % 4 == 0)
                    spawnParticle(p, Particle.SCULK_SOUL, min.getX() + spectatorsOffset.x, min.getY() + spectatorsOffset.y + 1, min.getZ() + spectatorsOffset.z);
            }
        }
    }
}

