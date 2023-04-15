package emanondev.minigames;

import emanondev.core.UtilsString;
import emanondev.minigames.games.deathmatch.DeathMatchType;
import emanondev.minigames.games.eggwars.EggWarsType;
import emanondev.minigames.generic.MType;
import emanondev.minigames.games.race.boat.BoatRaceType;
import emanondev.minigames.games.race.elytra.ElytraRaceType;
import emanondev.minigames.games.race.horse.HorseRaceType;
import emanondev.minigames.games.race.running.RunRaceType;
import emanondev.minigames.games.skywars.SkyWarsType;
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
    public static final HorseRaceType HORSE_RACE = new HorseRaceType();
    public static final BoatRaceType BOAT_RACE = new BoatRaceType();
    public static final DeathMatchType DEATH_MATCH = new DeathMatchType();

}
