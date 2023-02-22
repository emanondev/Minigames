package emanondev.minigames.generic;

import emanondev.core.ItemBuilder;
import emanondev.core.UtilsString;
import emanondev.core.gui.*;
import emanondev.core.message.DMessage;
import emanondev.minigames.DropGroupManager;
import emanondev.minigames.FillerManager;
import emanondev.minigames.Minigames;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DropsFiller extends ARegistrable implements MFiller {

    private final List<String> dropGroups = new ArrayList<>();
    private final List<Double> chances = new ArrayList<>();

    @Override
    public @NotNull Gui getEditorGui(@NotNull Player player, @Nullable Gui previousGui) {
        PagedMapGui gui = new PagedMapGui(
                new DMessage(Minigames.get(), player).appendLang("minidropsfiller.gui.title", "%id%", getId()).toLegacy(),
                6, player, null, Minigames.get(), false);
        for (int i = 0; i < getSize(); i++)
            gui.addButton(getEditorButton(gui, i));
        gui.addButton(new ResearchDropGroupButton(gui));
        gui.setControlGuiButton(5, new FButton(gui, () -> new ItemBuilder(Material.PAPER).setGuiProperty().setDescription(
                new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList("minidropsfiller.gui.clear")).build(),
                (e) -> {
                    if (clear())
                        getEditorGui(gui.getTargetPlayer(), gui.getPreviousGui()).open(gui.getTargetPlayer());
                    return false;
                }));
        return gui;
    }

    private boolean clear() {
        boolean found = false;
        for (int i = 0; i < getSize(); i++) {
            if (getChance(i) <= 0) {
                removeGroup(i);
                i--;
                found = true;
            }
        }
        return found;
    }

    private class ResearchDropGroupButton extends ResearchFButton<DropGroup> {
        public ResearchDropGroupButton(Gui gui) {
            super(gui, () -> new ItemBuilder(Material.PAPER).setGuiProperty().setDescription(
                            new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList("minidropsfiller.gui.add_group")).build()
                    , (name, group) -> group.getId().toLowerCase(Locale.ENGLISH).contains(name.toLowerCase(Locale.ENGLISH)),
                    (e, dropGroup) -> {
                        addGroup(dropGroup, 1);
                        gui.setButton(DropsFiller.this.getSize() - 1, getEditorButton(gui, DropsFiller.this.getSize() - 1));
                        gui.addButton(new ResearchDropGroupButton(gui));
                        gui.open(e.getWhoClicked());
                        return false;
                    }, (dropGroup) -> new ItemBuilder(Material.CHEST).setGuiProperty().setDescription(
                                    new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList("minidropsfiller.gui.group_selector", dropGroup.getPlaceholders()))
                            .build(), () -> DropGroupManager.get().getAll().values());
        }
    }

    private GuiButton getEditorButton(Gui gui, int slot) {
        return new LongEditorFButton(gui, 10, 1, 100,
                () -> getChancePercent(slot),
                (chance) -> setChancePercent(slot, chance),
                () -> new ItemBuilder(Material.CHEST).build(),
                () -> {
                    DropGroup group = getGroup(slot);
                    return new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList("minidropsfiller.gui.group_info", UtilsString.merge(
                            group.getPlaceholders(),
                            "%chance%", String.valueOf(getChancePercent(slot))
                    )).toStringList();
                });
    }

    public int getSize() {
        return dropGroups.size();
    }

    public void addGroup(@NotNull DropGroup group, double chance) {
        if (chance <= 0 || !group.isRegistered())
            throw new IllegalArgumentException();
        dropGroups.add(group.getId());
        chances.add(chance);
        FillerManager.get().save(this);
    }

    public void removeGroup(int slot) {
        if (slot < 0 || dropGroups.size() <= slot)
            throw new IndexOutOfBoundsException();
        dropGroups.remove(slot);
        chances.remove(slot);
        FillerManager.get().save(this);
    }

    public void setChancePercent(int slot, long val) {
        setChance(slot, ((double) val) / 100);
    }

    public void setChance(int slot, double val) {
        if (slot < 0 || dropGroups.size() <= slot)
            throw new IndexOutOfBoundsException();
        chances.set(slot, Math.max(0, val));
        FillerManager.get().save(this);
    }

    public double getChance(int slot) {
        if (slot < 0 || dropGroups.size() <= slot)
            throw new IndexOutOfBoundsException();
        return chances.get(slot);
    }

    public long getChancePercent(int slot) {
        return (long) (getChance(slot) * 100);
    }

    public DropGroup getGroup(int slot) {
        if (slot < 0 || dropGroups.size() <= slot)
            throw new IndexOutOfBoundsException();
        return DropGroupManager.get().get(dropGroups.get(slot));
    }

    @Override
    public @NotNull List<ItemStack> getDrops() {
        ArrayList<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < dropGroups.size(); i++) {
            if (Math.random() > chances.get(i))
                continue;
            DropGroup group = DropGroupManager.get().get(dropGroups.get(i));
            if (group == null)
                continue;
            list.add(group.getDrop());
        }
        return list;
    }

    @Override
    public String[] getPlaceholders() {
        return new String[]{"%size%", String.valueOf(getSize()), "%id%", getId()};
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("groups", new ArrayList<>(dropGroups));
        map.put("chances", new ArrayList<>(chances));
        return map;
    }

    public static DropsFiller deserialize(Map<String, Object> map) {
        DropsFiller dropContainer = new DropsFiller();
        if (map.containsKey("groups"))
            dropContainer.dropGroups.addAll((Collection<? extends String>) map.get("groups"));
        if (map.containsKey("chances"))
            dropContainer.chances.addAll((Collection<? extends Double>) map.get("chances"));
        if (dropContainer.dropGroups.size() != dropContainer.chances.size())
            throw new IllegalArgumentException();
        return dropContainer;
    }

}
