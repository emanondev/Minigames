package emanondev.minigames.spleef;

import emanondev.core.message.DMessage;
import emanondev.core.message.SimpleMessage;
import emanondev.minigames.Minigames;
import emanondev.minigames.UtilColor;
import emanondev.minigames.generic.SchematicArenaBuilder;
import emanondev.minigames.locations.LocationOffset3D;
import emanondev.minigames.skywars.SkyWarsArena;
import org.bukkit.DyeColor;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SpleefArenaBuilder extends SchematicArenaBuilder {

    private static final SimpleMessage ERR_UNKNOWN_ACTION = new SimpleMessage(Minigames.get(), "arenabuilder.skywars.error.unknown_action");
    private static final SimpleMessage ERR_OUTSIDE_ARENA = new SimpleMessage(Minigames.get(), "arenabuilder.skywars.error.outside_arena");
    private final HashMap<DyeColor, LocationOffset3D> spawnLocations = new HashMap<>();
    private LocationOffset3D spectatorsOffset;

    private static final int PHASE_SELECT_AREA = 1;
    private static final int PHASE_SET_TEAM_SPAWNS = 2;
    private static final int PHASE_SET_TEAM_SPAWNS_OR_NEXT = 3;
    private static final int PHASE_SET_SPECTATOR_SPAWN = 4;
    private static final int PHASE_SET_SPECTATOR_SPAWN_OR_NEXT = 5;

    public SpleefArenaBuilder(@NotNull UUID user, @NotNull String id, @NotNull String label) {
        super(user, id, label, Minigames.get());
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
    public void handleCommand(Player player, String label, @NotNull String[] args) {
        if (args.length == 0) {
            ERR_UNKNOWN_ACTION.send(player, "%alias%", label);
            return;
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
    public SkyWarsArena build() {
        Map<String, Object> map = new LinkedHashMap<>();
        return null;
    }


    private int timerTick = 0;

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
