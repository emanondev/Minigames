package emanondev.minigames;

import emanondev.core.UtilsString;
import emanondev.minigames.deathmatch.DeathMatchType;
import emanondev.minigames.eggwars.EggWarsType;
import emanondev.minigames.generic.MType;
import emanondev.minigames.race.TimeMountedRaceType;
import emanondev.minigames.race.elytra.ElytraRaceType;
import emanondev.minigames.race.running.RunRaceType;
import emanondev.minigames.race.boat.BoatRaceType;
import emanondev.minigames.race.horse.HorseRaceType;
import emanondev.minigames.race.timedrace.TimeRaceType;
import emanondev.minigames.skywars.SkyWarsType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MinigameTypes {



    public MinigameTypes() {
        instance = this;
        register(SKYWARS);
        register(RUN_RACE);
        register(BOAT_RACE);
        register(ELYTRA_RACE);
        //register(MOUNTED_RACE);
        //register(TIME_RACE);
        //register(TIME_MOUNTED_RACE);
        register(HORSE_RACE);
        register(DEATH_MATCH);
    }

    @SuppressWarnings("rawtypes")
    private void register(@NotNull MType type) {
        if (!UtilsString.isLowcasedValidID(type.getType()))
            throw new IllegalArgumentException("invalid id '" + type.getType() + "'");
        if (types.containsKey(type.getType()))
            throw new IllegalArgumentException("duplicated id '" + type.getType() + "'");
        types.put(type.getType(), type);
    }

    private static MinigameTypes instance;

    public static MinigameTypes get() {
        return instance;
    }

    @SuppressWarnings("rawtypes")
    private final HashMap<String, MType> types = new HashMap<>();

    @SuppressWarnings("rawtypes")
    public final @NotNull Collection<MType> getTypes() {
        return Collections.unmodifiableCollection(types.values());
    }

    public final @NotNull Set<String> getTypesId() {
        return Collections.unmodifiableSet(types.keySet());
    }

    @SuppressWarnings("rawtypes")
    public @Nullable MType getType(String id) {
        return types.get(id.toLowerCase(Locale.ENGLISH));
    }

    public static final SkyWarsType SKYWARS = new SkyWarsType();
    public static final EggWarsType EGGWARS = new EggWarsType();
    public static final RunRaceType RUN_RACE = new RunRaceType();
    public static final ElytraRaceType ELYTRA_RACE = new ElytraRaceType();
    public static final TimeRaceType TIME_RACE = new TimeRaceType();
    public static final TimeMountedRaceType TIME_MOUNTED_RACE = new TimeMountedRaceType();
    //public static final MountedRaceType MOUNTED_RACE = new MountedRaceType();
    public static final HorseRaceType HORSE_RACE = new HorseRaceType();
    public static final BoatRaceType BOAT_RACE = new BoatRaceType();
    public static final DeathMatchType DEATH_MATCH = new DeathMatchType();

}
