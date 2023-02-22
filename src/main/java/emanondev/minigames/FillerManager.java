package emanondev.minigames;

import emanondev.minigames.generic.DropsFiller;

public class FillerManager extends Manager<DropsFiller> {

    private static FillerManager instance;

    public static FillerManager get() {
        return instance;
    }

    public FillerManager() {
        super("fillers");
        instance = this;
    }
}
