package emanondev.minigames.games.eggwars;

import emanondev.core.UtilsInventory;
import emanondev.core.gui.Gui;
import emanondev.core.gui.GuiButton;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArmorShopMenu extends GenericShopMenu {

    public ArmorShopMenu(ShopsMenu parent) {
        super("armor", parent);
        //TODO add stuffs
    }

    protected void fill() {
        this.setButton(9, new ArmorSlot(EquipmentSlot.HEAD));
        this.setButton(18, new ArmorSlot(EquipmentSlot.CHEST));
        this.setButton(27, new ArmorSlot(EquipmentSlot.LEGS));
        this.setButton(36, new ArmorSlot(EquipmentSlot.FEET));

    }

    private class ArmorSlot implements GuiButton {
        private final EquipmentSlot slot;

        public ArmorSlot(EquipmentSlot slot) {
            this.slot = slot;
        }

        @Override
        public boolean onClick(@NotNull InventoryClickEvent inventoryClickEvent) {
            return false;
        }

        @Override
        public @Nullable ItemStack getItem() {
            ItemStack item = getTargetPlayer().getInventory().getItem(slot);
            if (UtilsInventory.isAirOrNull(item))
                return new ItemStack(Material.BARRIER);
            return item;
        }

        @Override
        public @NotNull Gui getGui() {
            return ArmorShopMenu.this;
        }
    }
}
