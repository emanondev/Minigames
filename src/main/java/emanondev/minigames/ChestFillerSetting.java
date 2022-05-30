package emanondev.minigames;

import emanondev.core.UtilsString;
import emanondev.minigames.generic.Registrable;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class ChestFillerSetting implements InventoryFiller, Registrable {


    @Override
    public void fillInventory(Inventory inv){

    }

    private String id = null;

    public boolean isRegistered() {
        return id != null;
    }

    public void setRegistered(String id) {
        if (!UtilsString.isLowcasedValidID(id))
            throw new IllegalStateException();
        this.id = id;
    }

    public @NotNull String getId() {
        return id;
    }

    public void setUnregister() {
        id = null;
    }
}
