package emanondev.minigames;

import emanondev.minigames.generic.MFiller;

public class FillerManager extends Manager<MFiller> {

    private static FillerManager instance;

    public static FillerManager get() {
        return instance;
    }

    public FillerManager() {
        super("fillers");
        instance = this;
    }
}
