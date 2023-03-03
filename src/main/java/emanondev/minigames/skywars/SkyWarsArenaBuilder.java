package emanondev.minigames.skywars;

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

public class SkyWarsArenaBuilder extends SchematicArenaBuilder {

    private static final SimpleMessage ERR_UNKNOWN_ACTION = new SimpleMessage(Minigames.get(), "arenabuilder.skywars.error.unknown_action");
    private static final SimpleMessage ERR_OUTSIDE_ARENA = new SimpleMessage(Minigames.get(), "arenabuilder.skywars.error.outside_arena");
    private final HashMap<DyeColor, LocationOffset3D> spawnLocations = new HashMap<>();
    private LocationOffset3D spectatorsOffset;

    private static final int PHASE_SELECT_AREA = 1;
    private static final int PHASE_SET_TEAM_SPAWNS = 2;
    private static final int PHASE_SET_TEAM_SPAWNS_OR_NEXT = 3;
    private static final int PHASE_SET_SPECTATOR_SPAWN = 4;
    private static final int PHASE_SET_SPECTATOR_SPAWN_OR_NEXT = 5;

    public SkyWarsArenaBuilder(@NotNull UUID user, @NotNull String id, @NotNull String label) {
        super(user, id, label);
    }

    @Override
    protected void onPhaseStart() {
        timerTick = 0;
    }

    @Override
    public @NotNull DMessage getCurrentBossBarMessage() {
        return new DMessage(Minigames.get(), getBuilder()).appendLang("arenabuilder.skywars.bossbar.phase" + getPhase(), "%alias%", getLabel());
    }

    @Override
    public @NotNull DMessage getRepeatedMessage() {
        return switch (getPhase()) {
            case PHASE_SET_TEAM_SPAWNS, PHASE_SET_TEAM_SPAWNS_OR_NEXT -> {
                DMessage teamSet = new DMessage(Minigames.get(), getBuilder());
                for (DyeColor color : DyeColor.values())
                    if (!spawnLocations.containsKey(color))
                        teamSet.appendLang("arenabuilder.skywars.repeatmessage.setteamcolor", "%color%", color.name(), "%hexa%", UtilColor.getColorHexa(color), "%alias%", getLabel());
                DMessage teamDelete = new DMessage(Minigames.get(), getBuilder());
                for (DyeColor color : spawnLocations.keySet())
                    teamDelete.appendLang("arenabuilder.skywars.repeatmessage.deleteteamcolor", "%color%", color.name(), "%hexa%", UtilColor.getColorHexa(color), "%alias%", getLabel());
                yield new DMessage(Minigames.get(), getBuilder()).appendLang("arenabuilder.skywars.repeatmessage.phase" + getPhase()
                        , "%setteamsspawn%", teamSet.toString()
                        , "%deleteteamsspawn%", teamDelete.toString(), "%alias%", getLabel());
            }
            default -> new DMessage(Minigames.get(), getBuilder()).appendLang("arenabuilder.skywars.repeatmessage.phase" + getPhase(), "%alias%", getLabel());
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
                        sendMsg(player, "arenabuilder.skywars.success.select_area",
                                "%world%", getWorld().getName(),
                                "%x1%", String.valueOf((int) getArea().getMinX()),
                                "%x2%", String.valueOf((int) getArea().getMaxX()),
                                "%y1%", String.valueOf((int) getArea().getMinY()),
                                "%y2%", String.valueOf((int) getArea().getMaxY()),
                                "%z1%", String.valueOf((int) getArea().getMinZ()),
                                "%z2%", String.valueOf((int) getArea().getMaxZ()), "%alias%", label);

                        setPhaseRaw(PHASE_SET_TEAM_SPAWNS);
                    } catch (IncompleteRegionException e) {
                        sendMsg(player, "arenabuilder.skywars.error.unselected_area", "%alias%", label);
                    }
                }
                case PHASE_SET_TEAM_SPAWNS, PHASE_SET_TEAM_SPAWNS_OR_NEXT -> {
                    switch (args[0].toLowerCase(Locale.ENGLISH)) {
                        case "next" -> {
                            if (spawnLocations.size() >= 2) {
                                //MessageUtil.sendMessage(player, "arenabuilder.skywars.success.next");
                                setPhaseRaw(SkyWarsArenaBuilder.PHASE_SET_SPECTATOR_SPAWN);
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
                            sendMsg(player, override ? "arenabuilder.skywars.success.override_team_spawn"
                                    : "arenabuilder.skywars.success.set_team_spawn", "%color%", color.name(), "%alias%", label);
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
                            sendMsg(player, "arenabuilder.skywars.success.deleted_team_spawn",
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
                                sendMsg(player, "arenabuilder.skywars.success.completed",
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
                            sendMsg(player, override ? "arenabuilder.skywars.success.override_spectators_spawn"
                                    : "arenabuilder.skywars.success.set_spectators_spawn", "%alias%", label);
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
                case PHASE_SELECT_AREA -> UtilsCommand.complete(args[0], List.of("selectarea"));
                case PHASE_SET_TEAM_SPAWNS -> UtilsCommand.complete(args[0], List.of("setteamspawn", "deleteteamspawn"));
                case PHASE_SET_TEAM_SPAWNS_OR_NEXT -> UtilsCommand.complete(args[0], List.of("setteamspawn", "deleteteamspawn", "next"));
                case PHASE_SET_SPECTATOR_SPAWN -> UtilsCommand.complete(args[0], List.of("setspectatorspawn"));
                case PHASE_SET_SPECTATOR_SPAWN_OR_NEXT -> UtilsCommand.complete(args[0], List.of("setspectatorspawn", "next"));
                default -> Collections.emptyList();
            };
            case 2 -> switch (getPhase()) {
                case PHASE_SET_TEAM_SPAWNS, PHASE_SET_TEAM_SPAWNS_OR_NEXT -> switch (args[0].toLowerCase(Locale.ENGLISH)) {
                    case "setteamspawn" -> UtilsCommand.complete(args[1], DyeColor.class, (DyeColor c) -> !spawnLocations.containsKey(c));
                    case "deleteteamspawn" -> UtilsCommand.complete(args[1], DyeColor.class, spawnLocations::containsKey);
                    default -> Collections.emptyList();
                };
                default -> Collections.emptyList();
            };
            default -> Collections.emptyList();
        };
    }

    @Override
    public SkyWarsArena build() {
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
        return new SkyWarsArena(map);
    }

    private int timerTick = 0;

    @Override
    public void onTimerCall() {
        Player p = getBuilder();
        if (p == null || !p.isOnline())
            return;
        timerTick++;
        if (timerTick % 2 == 0) { //every 15 game ticks
            Vector min = null;
            Vector max = null;

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
                } catch (IncompleteRegionException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            if (min != null) {
                markBox(min, max, p);
                markSpawns(min, p, timerTick % 4 == 0);
            }
        }
        if (timerTick % 180 == 0) { //every 45 seconds
            getRepeatedMessage().send();
        }
    }

    private void markBox(Vector min, Vector max, Player p) {
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
    }

    private void markSpawns(Vector offset, Player p, boolean even) {
        double yOffset = offset.getY() + 0.05D;
        for (int i = 0; i < 8; i++) {
            double degree = (even ? 0 : 0.5 + i) * Math.PI / 4;
            double xOffset = offset.getX() + 0.4 * Math.sin(degree);
            double zOffset = offset.getZ() + 0.4 * Math.cos(degree);
            spawnLocations.forEach((k, v) ->
                    p.spawnParticle(Particle.REDSTONE, v.x + xOffset, v.y + yOffset,
                            v.z + zOffset, 1, new Particle.DustOptions(k.getColor(), 1F)));
            if (spectatorsOffset != null)
                p.spawnParticle(Particle.WAX_ON, spectatorsOffset.x + xOffset,
                        spectatorsOffset.y + yOffset, spectatorsOffset.z + zOffset, 1);
        }
    }

    private void sendMsg(CommandSender target, String path, String... holders) {
        new DMessage(Minigames.get(), target).appendLang(path, holders).send();
    }

    private void sendMsgList(CommandSender target, String path, String... holders) {
        new DMessage(Minigames.get(), target).appendLangList(path, holders).send();
    }
}

