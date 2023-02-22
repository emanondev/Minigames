package emanondev.minigames.generic;

import emanondev.core.ItemBuilder;
import emanondev.core.RandomItemContainer;
import emanondev.core.UtilsString;
import emanondev.core.gui.FButton;
import emanondev.core.gui.Gui;
import emanondev.core.gui.NumberEditorFButton;
import emanondev.core.gui.PagedListGui;
import emanondev.minigames.Configurations;
import emanondev.minigames.FillerManager;
import emanondev.minigames.MessageUtil;
import emanondev.minigames.Minigames;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class ChestFiller implements MFiller {

    // private static final int MAX_FAILS = 3;

    private final RandomItemContainer<ItemStack> items = new RandomItemContainer<>();
    private int amountMin;
    private int amountMax = 3;

    public ChestFiller() {
        this(new LinkedHashMap<>());
    }

    public ChestFiller(@NotNull Map<String, Object> map) {
        Map<String, Object> subMap = (Map<String, Object>) map.get("items");
        setAmountMin((int) map.getOrDefault("minAmount", 3));
        setAmountMax((int) map.getOrDefault("maxAmount", 3));
        if (subMap == null)
            return;
        subMap.forEach((key, v) -> items.addItems((List<ItemStack>) v, Integer.parseInt(key)));
    }

    public void setAmountMin(int value) {
        amountMin = Math.max(0, value);
        amountMax = Math.max(amountMax, amountMin);
    }

    public void setAmountMax(int value) {
        amountMax = Math.max(amountMin, Math.max(1, value));
    }

    public void addItems(@NotNull List<ItemStack> items, int weight) {
        this.items.addItems(items, weight);
        if (isRegistered())
            FillerManager.get().save(this);
    }

    @Override
    public @NotNull Gui getEditorGui(@NotNull Player player, @Nullable Gui previousGui) {
        //TODO edit the core
        final boolean[] showItem = {true};

        PagedListGui<Integer> gui = new PagedListGui<>(getId(), 6, player, previousGui, Minigames.get(), false, 1) {

            @Override
            public PagedListGui<Integer>.ContainerButton getContainer(Integer integer) {
                return new ContainerButton(integer) {

                    @Override
                    public boolean onClick(@NotNull InventoryClickEvent inventoryClickEvent) {
                        return false;
                    }

                    @Override
                    public @Nullable ItemStack getItem() {
                        if (showItem[0])
                            try {
                                return items.getItems().get(getValue());
                            } catch (Exception e) {
                                return null;
                            }
                        try {
                            return new ItemBuilder(items.getItems().get(getValue()))
                                    .setDescription(
                                            MessageUtil.getMultiMessage(getTargetPlayer(), "minifiller.buttons.object_info",
                                                    "%weight%", String.valueOf(items.getWeights().get(getValue())),
                                                    "%multiplier%", UtilsString.formatOptional2Digit(((double) amountMin + amountMax) / 2),
                                                    "%multiplier-chance%", UtilsString.formatForced2Digit(((double) amountMin + amountMax) / 2 * (double) items.getWeights().get(getValue()) * 100 / items.getFullWeight()),
                                                    "%chance%", UtilsString.formatForced2Digit((double) items.getWeights().get(getValue()) * 100 / items.getFullWeight()))
                                    ).setGuiProperty().build();
                        } catch (Exception e) {
                            return null;
                        }
                    }
                };
            }
        };
        for (int i = 0; i < items.getItems().size(); i++)
            gui.addElement(i);
        gui.setControlGuiButton(4, new FButton(gui, () ->
                Configurations.getFillerToggleViewItem(gui.getTargetPlayer()),
                (event -> {
                    showItem[0] = !showItem[0];
                    return true;
                })));
        gui.setControlGuiButton(2, new NumberEditorFButton<>(gui, 1, 1, 10, () -> amountMin,
                (val) -> {
                    setAmountMin(val);
                    FillerManager.get().save(ChestFiller.this);
                }, null,
                () -> MessageUtil.getMultiMessage(player, "minifiller.gui.amountmin_desc")));
        gui.setControlGuiButton(3, new NumberEditorFButton<>(gui, 1, 1, 10, () -> amountMax,
                (val) -> {
                    setAmountMax(val);
                    FillerManager.get().save(ChestFiller.this);
                }, null,
                () -> MessageUtil.getMultiMessage(player, "minifiller.gui.amountmax_desc")));
        return gui;
    }

    @Override
    public List<ItemStack> getDrops() {
        ArrayList<ItemStack> list = new ArrayList<>();
        int itemAttemps = amountMin + ((amountMax == amountMin) ? 0 : (int) (Math.random() * (amountMax - amountMin + 1)));
        for (int i = 0; i < itemAttemps; i++)
            list.add(items.getItem());
        return list;
    }

    public int getMinElements() {
        return amountMin;
    }

    public int getMaxElements() {
        return amountMax;
    }

    public int getElementsAmount() {
        return items.getItems().size();
    }


    /*
    @Override
    public void fillInventory(@NotNull Inventory inv) {
        int itemAttemps = amountMin + ((amountMax == amountMin) ? 0 : (int) (Math.random() * (amountMax - amountMin + 1)));
        for (int i = 0; i < itemAttemps && i < inv.getSize(); i++)
            inv.setItem(i, items.getItem());
        //TODO flush ?
    }*/


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
        map.put("minAmount", amountMin);
        map.put("maxAmount", amountMax);
        List<ItemStack> itemList = items.getItems();
        List<Integer> weightList = items.getWeights();
        LinkedHashMap<String, List<ItemStack>> subMap = new LinkedHashMap<>();
        for (int i = 0; i < itemList.size(); i++) {
            if (!subMap.containsKey(String.valueOf(weightList.get(i)))) {
                subMap.put(String.valueOf(weightList.get(i)), new ArrayList<>());
            }
            subMap.get(String.valueOf(weightList.get(i))).add(itemList.get(i));
        }
        map.put("items", subMap);
        return map;
    }
}
