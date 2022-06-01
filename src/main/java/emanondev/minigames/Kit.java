package emanondev.minigames;

import emanondev.core.PlayerSnapshot;
import emanondev.core.UtilsString;
import emanondev.minigames.generic.Registrable;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Kit implements ConfigurationSerializable, Registrable {

    private final PlayerSnapshot snap;
    private int price;

    public Kit(@NotNull Map<String, Object> map) {
        this.snap = (PlayerSnapshot) map.get("snap");
        this.price = Math.max(0, (int) map.getOrDefault("price", 0));
    }

    public static @NotNull Kit fromPlayerSnapshot(@NotNull PlayerSnapshot snap) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        PlayerSnapshot clone = new PlayerSnapshot();
        clone.setInventory(snap.getInventory());
        clone.setArmor(snap.getArmor());
        clone.setExtraContents(snap.getExtraContents());
        map.put("snap", clone);
        return new Kit(map);
    }

    public void updateSnapshot(@NotNull PlayerSnapshot snap) {
        List<ItemStack> inv = snap.getInventory();
        List<ItemStack> armor = snap.getArmor();
        List<ItemStack> extra = snap.getExtraContents();

        if (inv == null || armor == null)
            throw new NullPointerException();
        this.snap.setInventory(inv);
        this.snap.setArmor(armor);
        this.snap.setExtraContents(extra);
        KitManager.get().save(this);
    }

    public void apply(Player player) {
        snap.apply(player);
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("snap", this.snap);
        map.put("price", this.price);
        return map;
    }

    private String id = null;

    public boolean isRegistered() {
        return id != null;
    }

    public void setRegistered(@NotNull String id) {
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


    public int getPrice() {
        return price;
    }
}
