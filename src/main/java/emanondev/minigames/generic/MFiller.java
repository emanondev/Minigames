package emanondev.minigames.generic;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.List;

public interface MFiller extends Registrable, ConfigurationSerializable {
    void fillInventory(@NotNull Inventory inv);
    void addItems(@NotNull List<ItemStack> items, @Range(from = 1L, to = Integer.MAX_VALUE) int weight);
}
