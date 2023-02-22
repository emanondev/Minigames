package emanondev.minigames;

public class KitManager extends Manager<Kit> {

    private static KitManager instance;

    public KitManager() {
        super("kits");
        instance = this;
    }

    public static KitManager get() {
        return instance;
    }

}
