package emanondev.minigames.games;

import emanondev.core.ItemBuilder;
import emanondev.core.RandomItemContainer;
import emanondev.core.UtilsString;
import emanondev.core.gson.GsonUtil;
import emanondev.core.gui.FButton;
import emanondev.core.gui.Gui;
import emanondev.core.gui.LongEditorFButton;
import emanondev.core.gui.PagedMapGui;
import emanondev.core.message.DMessage;
import emanondev.minigames.DropGroupManager;
import emanondev.minigames.Minigames;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SerializableAs("DropGroup")
public class DropGroup extends ARegistrable implements ConfigurationSerializable, Registrable {

    private final RandomItemContainer<ItemStack> drops;

    public DropGroup() {
        this(new RandomItemContainer<>());
    }

    private DropGroup(@NotNull RandomItemContainer<ItemStack> drops) {
        this.drops = drops;
    }

    @NotNull
    public static DropGroup deserialize(Map<String, Object> map) {
        RandomItemContainer<ItemStack> drops = new RandomItemContainer<>();
        for (String key : map.keySet()) {
            if (!key.equals("=="))
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

    public void remove(@NotNull ItemStack stack) {
        drops.deleteItem(stack);
        DropGroupManager.get().save(this);
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
                new DMessage(Minigames.get(), player).appendLang("minidropgroup.gui.title", "%id%", getId()).toLegacy(),
                6, player, previous, Minigames.get(), false);
        for (ItemStack item : drops.getItems()) {
            gui.addButton(new LongEditorFButton(gui, 10, 1, 1000,
                    () -> (long) getWeight(item),
                    (weight) -> addWeight(item, weight.intValue()),
                    () -> new ItemBuilder(item).addDescription(new DMessage(
                            gui.getPlugin(), gui.getTargetPlayer()).append("\n").appendLang("minidropgroup.gui.chance_info",
                            "%chance%", UtilsString.formatForced2Digit(100 * ((double) getWeight(item)) / drops.getFullWeight()),
                            "%weight%", String.valueOf(getWeight(item)))).build()));
        }
        gui.setControlGuiButton(4, new FButton(gui, () -> new ItemBuilder(Material.PAPER).setGuiProperty()
                .setDescription(new DMessage(Minigames.get(), player).appendLang("minidropgroup.gui.info", "%id%", getId())).build(),
                (e) -> false));
        return gui;
    }

    public int getWeight(@NotNull ItemStack stack) {
        return drops.getWeight(stack);
    }

    public void addWeight(@NotNull ItemStack stack, int weight) {
        drops.addItem(stack, weight);
        DropGroupManager.get().save(this);
    }

    public ItemStack getDrop() {
        return new ItemStack(drops.getItem());
    }
}
