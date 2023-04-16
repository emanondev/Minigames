package emanondev.minigames;

import emanondev.core.ItemBuilder;
import emanondev.core.PlayerSnapshot;
import emanondev.core.gui.*;
import emanondev.core.message.DMessage;
import emanondev.minigames.games.ARegistrable;
import emanondev.minigames.games.Registrable;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Kit extends ARegistrable implements ConfigurationSerializable, Registrable {

    private final PlayerSnapshot snap;
    private int price;
    private int unlockLevel;
    private ItemStack guiSelectorItem;

    @NotNull
    public String getDisplayName() {
        return displayName != null ? displayName : getId() != null ? getId() : "";
    }

    public void setDisplayName(String displayName) {
        if (displayName.isEmpty())
            this.displayName = null;
        else
            this.displayName = displayName;
    }

    private String displayName;

    public Kit(@NotNull Map<String, Object> map) {
        this.snap = (PlayerSnapshot) map.get("snap");
        this.price = Math.max(0, (int) map.getOrDefault("price", 0));
        this.displayName = (String) map.get("displayName");
        this.guiSelectorItem = (ItemStack) map.get("gui_selector_item");
        this.unlockLevel = Math.max(0, (int) map.getOrDefault("unlock_level", 0));
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
        map.put("displayName", this.displayName);
        map.put("snap", this.snap);
        map.put("price", this.price);
        map.put("gui_selector_item", this.guiSelectorItem);
        map.put("unlock_level", this.unlockLevel);
        return map;
    }

    public int getPrice() {
        return price;
    }

    public String[] getPlaceholders() {
        return new String[]{
                "%price%", price == 0 ? "-free-" : String.valueOf(price), "%id%", getId(),
                "%displayname%", getDisplayName()
        };
    }

    public int getUnlockLevel() {
        return unlockLevel;
    }

    public void setUnlockLevel(int unlockLevel) {
        this.unlockLevel = Math.max(0, unlockLevel);
    }

    public void setPrice(int val) {
        this.price = Math.max(0, val);
        KitManager.get().save(this);
    }

    public Gui getEditorGui(@NotNull Player target, @Nullable Gui parent) {
        PagedMapGui gui = new PagedMapGui(
                new DMessage(Minigames.get(), target).appendLang("minikit.gui.title", getPlaceholders()).toLegacy(),
                6, target, parent, Minigames.get());
        gui.addButton(new StringEditorFButton(gui, this::getDisplayName, this::setDisplayName,
                () -> new ItemBuilder(Material.MOJANG_BANNER_PATTERN).setDescription(
                        new DMessage(Minigames.get(), target)
                                .appendLang("minikit.gui.display_name_editor", getPlaceholders())
                ).setGuiProperty().build(), true));
        gui.addButton(new ItemEditorFButton(gui,
                () -> getGuiSelectorItemRaw().setDescription(
                        new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
                                "minikit.gui.display_item_editor")).build(),
                () -> getGuiSelectorItemRaw().build(),
                this::setGuiSelectorItem, (event) -> gui.open(target)));
        gui.addButton(new LongEditorFButton(gui, 1, 1, 10,
                () -> (long) getPrice(),
                (v) -> {
                    setPrice(v.intValue());
                    KitManager.get().save(Kit.this);
                },
                () -> new ItemBuilder(Material.GOLD_INGOT).setGuiProperty().setDescription(
                        new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
                                "minikit.gui.price", "%value%", String.valueOf(getPrice()))).build()));
        gui.addButton(new LongEditorFButton(gui, 1, 1, 10,
                () -> (long) getUnlockLevel(),
                (v) -> {
                    setUnlockLevel(v.intValue());
                    KitManager.get().save(Kit.this);
                },
                () -> new ItemBuilder(Material.EXPERIENCE_BOTTLE).setGuiProperty().setDescription(
                        new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
                                "minikit.gui.unlock_level", "%value%", String.valueOf(getPrice()))).build()));
        return gui;
    }

    public Gui getEditorGui(@NotNull Player player) {
        return getEditorGui(player, null);
    }

    public ItemBuilder getGuiSelectorItemRaw() {
        return (this.guiSelectorItem == null ? new ItemBuilder(Material.IRON_CHESTPLATE).setGuiProperty() :
                new ItemBuilder(this.guiSelectorItem));
    }

    public ItemBuilder getGuiSelectorItem(Player target) {
        return getGuiSelectorItemRaw().setDescription(new DMessage(Minigames.get(), target)
                .appendLang("generic.gui.kitselector_description",
                        getPlaceholders()));
    }

    public void setGuiSelectorItem(ItemStack item) {
        if (item == null)
            guiSelectorItem = null;
        else
            guiSelectorItem = new ItemBuilder(item).setGuiProperty().build();
        KitManager.get().save(this);
    }
}
