package emanondev.minigames;

import emanondev.core.PlayerSnapshot;
import emanondev.minigames.generic.ARegistrable;
import emanondev.minigames.generic.Registrable;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Kit extends ARegistrable implements ConfigurationSerializable, Registrable {

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

    public static @NotNull Kit fromPlayer(@NotNull Player player) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        PlayerSnapshot clone = new PlayerSnapshot(player, PlayerSnapshot.FieldType.INVENTORY, PlayerSnapshot.FieldType.ARMOR, PlayerSnapshot.FieldType.EXTRACONTENTS);
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


    public void updateSnapshot(@NotNull Player player) {
        this.snap.loadFrom(player, PlayerSnapshot.FieldType.INVENTORY, PlayerSnapshot.FieldType.ARMOR, PlayerSnapshot.FieldType.EXTRACONTENTS);
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

    public int getPrice() {
        return price;
    }

    public String[] getPlaceholders() {
        return new String[]{
                "%price%", String.valueOf(price), "%id%", getId()
        };
    }

    public void setPrice(int val) {
        this.price = Math.max(0, val);
        KitManager.get().save(this);
    }
}
