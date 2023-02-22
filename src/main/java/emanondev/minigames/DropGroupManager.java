package emanondev.minigames;

import emanondev.minigames.generic.DropGroup;

public class DropGroupManager extends Manager<DropGroup> {

    private static DropGroupManager instance;

    public static DropGroupManager get() {
        return instance;
    }

    public DropGroupManager() {
        super("dropGroups");
        instance = this;
    }
}
