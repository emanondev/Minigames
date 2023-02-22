package emanondev.minigames.skywars;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import emanondev.core.UtilsCommand;
import emanondev.core.YMLSection;
import emanondev.core.message.MessageComponent;
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

public class SkyWarsArenaBuilder extends SchematicArenaBuilder {

    private final HashMap<DyeColor, LocationOffset3D> spawnLocations = new HashMap<>();
    private LocationOffset3D spectatorsOffset;

    private static final int PHASE_SELECT_AREA = 1;
    private static final int PHASE_SET_TEAM_SPAWNS = 2;
    private static final int PHASE_SET_TEAM_SPAWNS_OR_NEXT = 3;
    private static final int PHASE_SET_SPECTATOR_SPAWN = 4;
    private static final int PHASE_SET_SPECTATOR_SPAWN_OR_NEXT = 5;

    public SkyWarsArenaBuilder(@NotNull UUID user, @NotNull String id) {
        super(user, id);
    }

    @Override
    protected void onPhaseStart() {

    }

    @Override
    public @NotNull String getCurrentBossBarMessage() {
        return Minigames.get().getLanguageConfig(getBuilder()).getString("skywars.arenabuilder.bossbar.phase" + getPhase(), "");
    }

    @Override
    public @NotNull String getRepeatedMessage() {
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
                yield sect.loadMessage("phase" + getPhase(), "", (CommandSender) null
                        , "%setteamsspawn%", teamSet.toString()
                        , "%deleteteamsspawn%", teamDelete.toString());
            }
            default -> Minigames.get().getLanguageConfig(getBuilder()).getString("skywars.arenabuilder.repeatmessage.phase" + getPhase(), "");
        };
    }

    @Override
    public void handleCommand(@NotNull Player player, @NotNull String[] args) {
        if (args.length == 0) {
            MessageUtil.sendMessage(player, "skywars.arenabuilder.error.unknown_action");
            return;
        }
        try {
            switch (getPhase()) {
                case PHASE_SELECT_AREA -> {
                    if (!args[0].equalsIgnoreCase("selectarea")) {
                        MessageUtil.sendMessage(player, "skywars.arenabuilder.error.unknown_action");
                        return;
                    }
                    try {
                        setArea(player);
                        MessageUtil.sendMessage(player, "skywars.arenabuilder.success.select_area",
                                "%world%", getWorld().getName(),
                                "%x1%", String.valueOf((int) getArea().getMinX()),
                                "%x2%", String.valueOf((int) getArea().getMaxX()),
                                "%y1%", String.valueOf((int) getArea().getMinY()),
                                "%y2%", String.valueOf((int) getArea().getMaxY()),
                                "%z1%", String.valueOf((int) getArea().getMinZ()),
                                "%z2%", String.valueOf((int) getArea().getMaxZ()));

                        setPhaseRaw(PHASE_SET_TEAM_SPAWNS);
                    } catch (IncompleteRegionException e) {
                        MessageUtil.sendMessage(player, "skywars.arenabuilder.error.unselected_area");
                    }
                }
                case PHASE_SET_TEAM_SPAWNS, PHASE_SET_TEAM_SPAWNS_OR_NEXT -> {
                    switch (args[0].toLowerCase(Locale.ENGLISH)) {
                        case "next" -> {
                            if (spawnLocations.size() >= 2) {
                                //MessageUtil.sendMessage(player, "skywars.arenabuilder.success.next");
                                setPhaseRaw(SkyWarsArenaBuilder.PHASE_SET_SPECTATOR_SPAWN);
                                return;
                            } else {
                                MessageUtil.sendMessage(player, "skywars.arenabuilder.error.unknown_action");
                            }
                        }
                        case "setteamspawn" -> {
                            if (!this.isInside(player.getLocation())) {
                                MessageUtil.sendMessage(player, "skywars.arenabuilder.error.outside_area");
                                return;
                            }
                            //TODO check color value
                            DyeColor color = DyeColor.valueOf(args[1].toUpperCase());
                            LocationOffset3D loc = LocationOffset3D.fromLocation(player.getLocation().subtract(getArea().getMin()));
                            boolean override = spawnLocations.containsKey(color);
                            spawnLocations.put(color, loc);
                            MessageUtil.sendMessage(player, override ? "skywars.arenabuilder.success.override_team_spawn"
                                    : "skywars.arenabuilder.success.set_team_spawn", "%color%", color.name());
                            if (getPhase() == PHASE_SET_TEAM_SPAWNS && spawnLocations.size() >= 2)
                                setPhaseRaw(PHASE_SET_TEAM_SPAWNS_OR_NEXT);
                            else
                                new MessageComponent(Minigames.get(), getBuilder()).append(getRepeatedMessage()).send();
                            return;
                        }
                        case "deleteteamspawn" -> {
                            //TODO check color value
                            DyeColor color = DyeColor.valueOf(args[1].toUpperCase());
                            spawnLocations.remove(color);
                            MessageUtil.sendMessage(player, "skywars.arenabuilder.success.deleted_team_spawn",
                                    "%color%", color.name());
                            if (getPhase() == PHASE_SET_TEAM_SPAWNS_OR_NEXT && spawnLocations.size() < 2)
                                setPhaseRaw(PHASE_SET_TEAM_SPAWNS);
                            else
                                new MessageComponent(Minigames.get(), getBuilder()).append(getRepeatedMessage()).send();
                            return;
                        }
                    }
                    MessageUtil.sendMessage(player, "skywars.arenabuilder.error.unknown_action");
                }
                case PHASE_SET_SPECTATOR_SPAWN, PHASE_SET_SPECTATOR_SPAWN_OR_NEXT -> {
                    switch (args[0].toLowerCase(Locale.ENGLISH)) {
                        case "next" -> {
                            if (spectatorsOffset != null) {
                                MessageUtil.sendMessage(player, "skywars.arenabuilder.success.completed",
                                        "%name%", getId());
                                //TODO
                                ArenaManager.get().onArenaBuilderCompletedArena(this);
                                return;
                            }
                        }
                        case "setspectatorspawn" -> {
                            if (!this.isInside(player.getLocation())) {
                                MessageUtil.sendMessage(player, "skywars.arenabuilder.error.outside_area");
                                return;
                            }
                            boolean override = spectatorsOffset != null;
                            spectatorsOffset = LocationOffset3D.fromLocation(player.getLocation().subtract(getArea().getMin()));
                            MessageUtil.sendMessage(player, override ? "skywars.arenabuilder.success.override_spectators_spawn"
                                    : "skywars.arenabuilder.success.set_spectators_spawn");
                            if (getPhase() == PHASE_SET_SPECTATOR_SPAWN)
                                setPhaseRaw(PHASE_SET_SPECTATOR_SPAWN_OR_NEXT);
                            return;
                        }
                    }
                    MessageUtil.sendMessage(player, "skywars.arenabuilder.error.unknown_action");
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
        return new SkyWarsArena(map);
    }


    @Override
    public void onTimerCall(int timerTick) {
        Player p = getBuilder();
        if (p == null || !p.isOnline())
            return;
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
            new MessageComponent(Minigames.get(), getBuilder()).append(getRepeatedMessage()).send();
        }
    }
}

