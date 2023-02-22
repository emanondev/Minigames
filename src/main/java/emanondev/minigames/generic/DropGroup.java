package emanondev.minigames.generic;

import emanondev.core.ItemBuilder;
import emanondev.core.RandomItemContainer;
import emanondev.core.UtilsString;
import emanondev.core.gson.GsonUtil;
import emanondev.core.gui.FButton;
import emanondev.core.gui.Gui;
import emanondev.core.gui.LongEditorFButton;
import emanondev.core.gui.PagedMapGui;
import emanondev.core.message.DMessage;
import emanondev.minigames.Minigames;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DropGroup extends ARegistrable implements ConfigurationSerializable, Registrable {

    private final RandomItemContainer<ItemStack> drops;

    public DropGroup() {
        this(new RandomItemContainer<>());
    }

    private DropGroup(@NotNull RandomItemContainer<ItemStack> drops) {
        this.drops = drops;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        List<ItemStack> items = drops.getItems();
        for (int i = 0; i < items.size(); i++) {
            String key = String.valueOf(drops.getWeights().get(i));
            map.putIfAbsent(key, new ArrayList<String>());
            ((ArrayList<String>) map.get(key)).add(GsonUtil.toJson(items.get(i)));
        }
        return map;
    }

    @NotNull
    public static DropGroup deserialize(Map<String, Object> map) {
        RandomItemContainer<ItemStack> drops = new RandomItemContainer<>();
        for (String key : map.keySet()) {
            try {
                int value = Integer.parseInt(key);
                List<String> items = (List<String>) map.get(key);
                for (String json : items)
                    drops.addItem((ItemStack) GsonUtil.fromJson(json), value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new DropGroup(drops);
    }

    public void addWeight(@NotNull ItemStack stack, int weight) {
        drops.setWeight(stack, weight);
    }

    public void remove(@NotNull ItemStack stack) {
        drops.deleteItem(stack);
    }


    public String[] getPlaceholders() {
        return new String[]{
                "%drops_size%", String.valueOf(drops.getItems().size()), "%id%", getId()
        };
    }

    public Gui getEditorGui(@NotNull Player player) {
        return getEditorGui(player, null);
    }

    public Gui getEditorGui(@NotNull Player player, @Nullable Gui previous) {
        PagedMapGui gui = new PagedMapGui(
                Minigames.get().getLanguageConfig(player).getMessage("minidropgroup.gui.title", null, "%id%", getId()),
                6, player, previous, Minigames.get(), false);
        for (ItemStack item : drops.getItems()) {
            gui.addButton(new LongEditorFButton(gui, 50, 1, 1000,
                    () -> (long) drops.getWeight(item),
                    (weight) -> drops.setWeight(item, weight.intValue()),
                    () -> new ItemBuilder(item).build(),
                    () -> new ItemBuilder(item).getDescription(
                            Minigames.get()).append("").append(
                            Minigames.get().getLanguageConfig(player).getMultiMessage("minidropgroup.gui.chance_info", false, null,
                                    "%chance%", UtilsString.formatForced2Digit(((double) drops.getWeight(item)) / drops.getFullWeight()),
                                    "%weight%", String.valueOf(drops.getWeight(item)))).toStringList()));
        }
        gui.setControlGuiButton(4, new FButton(gui, () -> new ItemBuilder(Material.PAPER).setGuiProperty()
                .setDescription(new DMessage(Minigames.get(), player).appendLangList("minidropgroup.gui.info", "%id%", getId())).build(),
                (e) -> false));
        return gui;
    }

    public ItemStack getDrop() {
        return new ItemStack(drops.getItem());
    }
}
