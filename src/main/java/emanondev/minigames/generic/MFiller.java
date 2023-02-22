package emanondev.minigames.generic;

import emanondev.core.gui.Gui;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MFiller extends Registrable, ConfigurationSerializable {
    default void fillInventory(@NotNull Inventory inv) {
        inv.addItem(getDrops().toArray(new ItemStack[0]));
    }

    //void addItems(@NotNull List<ItemStack> items, @Range(from = 1L, to = Integer.MAX_VALUE) int weight);

    @NotNull
    default Gui getEditorGui(@NotNull Player player) {
        return getEditorGui(player, null);
    }

    @NotNull
    Gui getEditorGui(@NotNull Player player, @Nullable Gui previousGui);

    @NotNull
    List<ItemStack> getDrops();

    String[] getPlaceholders();
}
