package emanondev.minigames.generic;

import emanondev.core.RandomItemContainer;
import emanondev.core.UtilsString;
import emanondev.minigames.FillerManager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChestFiller implements MFiller {

    private static final int MAX_FAILS = 3;

    private final RandomItemContainer<ItemStack> items = new RandomItemContainer<>();
    private int amountMin;
    private int amountMax;

    public ChestFiller() {
        this(new LinkedHashMap<>());
    }

    public ChestFiller(@NotNull Map<String, Object> map) {
        Map<String, Object> subMap = (Map<String, Object>) map.get("items");
        amountMin = Math.max(0, (int) map.getOrDefault("minAmount", 3));
        amountMax = Math.max(amountMin, Math.max(1, (int) map.getOrDefault("maxAmount", 3)));
        if (subMap == null)
            return;
        subMap.forEach((key, v) -> items.addItems((List<ItemStack>) v, Integer.parseInt(key)));
    }

    public void addItems(@NotNull List<ItemStack> items, int weight) {
        this.items.addItems(items, weight);
        if (isRegistered())
            FillerManager.get().save(this);
    }


    @Override
    public void fillInventory(@NotNull Inventory inv) {
        //int failedAttemps = 0;
        int itemAttemps = amountMin + ((amountMax == amountMin) ? 0 : (int) (Math.random() * (amountMax - amountMin + 1)));
        for (int i = 0; i < itemAttemps && i < inv.getSize(); i++) {
            int slot = (int) (Math.random() * inv.getSize());//may override some previusly added items...
            inv.setItem(slot, items.getItem());
        }
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

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("minAmount", 3);
        map.put("maxAmount", 3);
        List<ItemStack> itemList = items.getItems();
        List<Integer> weightList = items.getWeights();
        LinkedHashMap<String, List<ItemStack>> subMap = new LinkedHashMap<>();
        for (int i = 0; i < itemList.size(); i++) {
            if (!subMap.containsKey(String.valueOf(weightList.get(i)))) {
                subMap.put(String.valueOf(weightList.get(i)), new ArrayList<>());
            }
            subMap.get(String.valueOf(weightList.get(i))).add(itemList.get(i));
        }
        return map;
    }
}
