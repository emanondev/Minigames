package emanondev.minigames.games.deathmatch;

import com.sk89q.worldedit.IncompleteRegionException;
import emanondev.core.UtilsString;
import emanondev.core.message.DMessage;
import emanondev.core.message.SimpleMessage;
import emanondev.core.util.WorldEditUtility;
import emanondev.minigames.ArenaManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.UtilColor;
import emanondev.minigames.games.SchematicArenaBuilder;
import emanondev.minigames.locations.LocationOffset3D;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class DeathMatchArenaBuilder extends SchematicArenaBuilder {

    private static final SimpleMessage ERR_UNKNOWN_ACTION = new SimpleMessage(Minigames.get(), "arenabuilder.deathmatch.error.unknown_action");
    private static final SimpleMessage ERR_OUTSIDE_ARENA = new SimpleMessage(Minigames.get(), "arenabuilder.deathmatch.error.outside_arena");
    private static final int PHASE_SELECT_AREA = 1;
    private static final int PHASE_SET_TEAM_SPAWNS = 2;
    private static final int PHASE_SET_TEAM_SPAWNS_OR_NEXT = 3;
    private static final int PHASE_SET_SPECTATOR_SPAWN = 4;
    private static final int PHASE_SET_SPECTATOR_SPAWN_OR_NEXT = 5;
    private final HashMap<DyeColor, LocationOffset3D> spawnLocations = new HashMap<>();
    private LocationOffset3D spectatorsOffset;
    private int timerTick = 0;

    public DeathMatchArenaBuilder(@NotNull UUID user, @NotNull String id, @NotNull String label) {
        super(user, id, label, Minigames.get());
    }

    @Override
    protected void onPhaseStart() {
        timerTick = 0;
    }

    @Override
    public @NotNull DMessage getCurrentBossBarMessage() {
        return new DMessage(Minigames.get(), getBuilder()).appendLang("arenabuilder.deathmatch.bossbar.phase" + getPhase(), "%alias%", getLabel());
    }

    @Override
    public @NotNull DMessage getRepeatedMessage() {
        return switch (getPhase()) {
            case PHASE_SET_TEAM_SPAWNS, PHASE_SET_TEAM_SPAWNS_OR_NEXT -> {
                DMessage teamSet = new DMessage(Minigames.get(), getBuilder());
                for (DyeColor color : DyeColor.values())
                    if (!spawnLocations.containsKey(color))
                        teamSet.appendLang("arenabuilder.deathmatch.repeatmessage.setteamcolor", "%color%", color.name(), "%hexa%", UtilColor.getColorHexa(color), "%alias%", getLabel());
                DMessage teamDelete = new DMessage(Minigames.get(), getBuilder());
                for (DyeColor color : spawnLocations.keySet())
                    teamDelete.appendLang("arenabuilder.deathmatch.repeatmessage.deleteteamcolor", "%color%", color.name(), "%hexa%", UtilColor.getColorHexa(color), "%alias%", getLabel());
                yield new DMessage(Minigames.get(), getBuilder()).appendLang("arenabuilder.deathmatch.repeatmessage.phase" + getPhase()
                        , "%setteamsspawn%", teamSet.toString()
                        , "%deleteteamsspawn%", teamDelete.toString(), "%alias%", getLabel());
            }
            default -> new DMessage(Minigames.get(), getBuilder()).appendLang("arenabuilder.deathmatch.repeatmessage.phase" + getPhase(), "%alias%", getLabel());
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
                        sendDMessage(player, "arenabuilder.deathmatch.success.select_area",
                                "%world%", getWorld().getName(),
                                "%x1%", String.valueOf((int) getArea().getMinX()),
                                "%x2%", String.valueOf((int) getArea().getMaxX()),
                                "%y1%", String.valueOf((int) getArea().getMinY()),
                                "%y2%", String.valueOf((int) getArea().getMaxY()),
                                "%z1%", String.valueOf((int) getArea().getMinZ()),
                                "%z2%", String.valueOf((int) getArea().getMaxZ()), "%alias%", label);

                        setPhaseRaw(PHASE_SET_TEAM_SPAWNS);
                    } catch (IncompleteRegionException e) {
                        sendDMessage(player, "arenabuilder.deathmatch.error.unselected_area", "%alias%", label);
                    }
                }
                case PHASE_SET_TEAM_SPAWNS, PHASE_SET_TEAM_SPAWNS_OR_NEXT -> {
                    switch (args[0].toLowerCase(Locale.ENGLISH)) {
                        case "next" -> {
                            if (spawnLocations.size() >= 2) {
                                //MessageUtil.sendMessage(player, "arenabuilder.deathmatch.success.next");
                                setPhaseRaw(DeathMatchArenaBuilder.PHASE_SET_SPECTATOR_SPAWN);
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
                            sendDMessage(player, override ? "arenabuilder.deathmatch.success.override_team_spawn"
                                    : "arenabuilder.deathmatch.success.set_team_spawn", "%color%", color.name(), "%alias%", label);
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
                            sendDMessage(player, "arenabuilder.deathmatch.success.deleted_team_spawn",
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
                                ArenaManager.get().onArenaBuilderCompletedArena(this);
                                sendDMessage(player, "arenabuilder.deathmatch.success.completed",
                                        UtilsString.merge(ArenaManager.get().get(getId()).getPlaceholders(), "%alias%", label));
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
                            sendDMessage(player, override ? "arenabuilder.deathmatch.success.override_spectators_spawn"
                                    : "arenabuilder.deathmatch.success.set_spectators_spawn", "%alias%", label);
                            if (getPhase() == PHASE_SET_SPECTATOR_SPAWN)
                                setPhaseRaw(PHASE_SET_SPECTATOR_SPAWN_OR_NEXT);
                            return;
                        }
                    }
                    ERR_UNKNOWN_ACTION.send(player, "%alias%", label);
                }
                default -> new IllegalStateException().printStackTrace();
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
                case PHASE_SET_TEAM_SPAWNS -> complete(args[0], List.of("setteamspawn", "deleteteamspawn"));
                case PHASE_SET_TEAM_SPAWNS_OR_NEXT -> complete(args[0], List.of("setteamspawn", "deleteteamspawn", "next"));
                case PHASE_SET_SPECTATOR_SPAWN -> complete(args[0], List.of("setspectatorspawn"));
                case PHASE_SET_SPECTATOR_SPAWN_OR_NEXT -> complete(args[0], List.of("setspectatorspawn", "next"));
                default -> Collections.emptyList();
            };
            case 2 -> switch (getPhase()) {
                case PHASE_SET_TEAM_SPAWNS, PHASE_SET_TEAM_SPAWNS_OR_NEXT -> switch (args[0].toLowerCase(Locale.ENGLISH)) {
                    case "setteamspawn" -> complete(args[1], DyeColor.class, (DyeColor c) -> !spawnLocations.containsKey(c));
                    case "deleteteamspawn" -> complete(args[1], DyeColor.class, spawnLocations::containsKey);
                    default -> Collections.emptyList();
                };
                default -> Collections.emptyList();
            };
            default -> Collections.emptyList();
        };
    }

    @Override
    public DeathMatchArena build() {
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
        return new DeathMatchArena(map);
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
        }
    }
}

