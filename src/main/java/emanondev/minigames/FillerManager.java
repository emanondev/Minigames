package emanondev.minigames;

import emanondev.minigames.games.DropsFiller;

public class FillerManager extends Manager<DropsFiller> {

    private static FillerManager instance;

    public FillerManager() {
        super("fillers");
        instance = this;
    }

    public static FillerManager get() {
        return instance;
    }
}
