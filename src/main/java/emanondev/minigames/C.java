package emanondev.minigames;

public class C {

    private static int gameMinimalDistancing = 272;
    private static int startupGameInitializeDelayTicks = 80;


    public static void reload(){
        gameMinimalDistancing = Math.max(Minigames.get().getConfig().getInt("game_minimal_distancing",17*16),8*16);
        startupGameInitializeDelayTicks = Math.max(Minigames.get().getConfig().getInt("startup_game_initialize_delay_ticks",80),20);
    }

    public static int getGameMinimalDistancing() {
        return gameMinimalDistancing;
    }

    public static int getStartupGameInitializeDelayTicks() {
        return startupGameInitializeDelayTicks;
    }
}
