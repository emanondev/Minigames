package emanondev.minigames;

import emanondev.minigames.games.DropGroup;

public class DropGroupManager extends Manager<DropGroup> {

    private static DropGroupManager instance;

    public DropGroupManager() {
        super("dropGroups");
        instance = this;
    }

    public static DropGroupManager get() {
        return instance;
    }
}
