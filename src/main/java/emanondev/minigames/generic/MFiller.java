package emanondev.minigames.generic;

import emanondev.core.gui.Gui;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.List;

public interface MFiller extends Registrable, ConfigurationSerializable {
    void fillInventory(@NotNull Inventory inv);

    void addItems(@NotNull List<ItemStack> items, @Range(from = 1L, to = Integer.MAX_VALUE) int weight);

    @NotNull
    Gui editorGui(@NotNull Player player, @Nullable Gui previousGui);

    int getMinElements();

    int getMaxElements();

    int getElementsAmount();
}
