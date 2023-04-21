package emanondev.minigames.games.eggwars;

import emanondev.core.ItemBuilder;
import emanondev.core.YMLSection;
import emanondev.core.gui.Gui;
import emanondev.core.gui.GuiButton;
import emanondev.core.gui.MapGui;
import emanondev.core.message.DMessage;
import emanondev.minigames.MinigameTypes;
import emanondev.minigames.Minigames;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GenericShopMenu extends MapGui {
    private final String id;

    public GenericShopMenu(@NotNull String id, @NotNull ShopsMenu parent) {
        super(new DMessage(Minigames.get(), parent.getTargetPlayer()).appendLang(
                        "eggwars.shops." + id + ".title"
                ),
                MinigameTypes.EGGWARS.getSection().getInteger("shops." + id + ".rows", 6), parent.getTargetPlayer(), parent, Minigames.get());
        this.id = id;
        fill();
        this.setButton(this.getInventory().getSize() - 9, getPreviousGui().getButton(18));
        this.setButton(this.getInventory().getSize() - 8, getPreviousGui().getButton(19));
        this.setButton(this.getInventory().getSize() - 7, getPreviousGui().getButton(20));
        this.setButton(this.getInventory().getSize() - 5, getPreviousGui().getBackButton());
        this.setButton(this.getInventory().getSize() - 3, getPreviousGui().getButton(24));
        this.setButton(this.getInventory().getSize() - 2, getPreviousGui().getButton(25));
        this.setButton(this.getInventory().getSize() - 1, getPreviousGui().getButton(26));
    }

    protected void fill() {
        for (String key : MinigameTypes.EGGWARS.getSection().getKeys("shops." + id + ".slots")) {
            try {
                int slot = Integer.parseInt(key);
                this.setButton(slot, new ShopItem(slot));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public @NotNull ShopsMenu getPreviousGui() {
        return (ShopsMenu) super.getPreviousGui();
    }

    protected ItemBuilder craftItem(String path) {
        //TODO enchants
        @NotNull YMLSection section = MinigameTypes.EGGWARS.getSection(); //
        return new ItemBuilder(
                section.getMaterial(path + ".material", Material.STONE))
                .setAmount(section.getInt(path + ".amount", 1))
                .setCustomModelData(section.getInteger(path + ".custom_model", null))
                .setColor(getGame().getTeam(getTargetPlayer()).getColor());
    }

    public EggWarsGame getGame() {
        return getPreviousGui().getGame();
    }

    private class ShopItem implements GuiButton {
        private final int slot;
        private final ItemStack itemBase;
        private final EggWarsGeneratorType priceType;
        private final int priceCoins;
        private final ItemBuilder cacheItem;
        private final boolean allowShift;

        public ShopItem(int slot) {
            if (slot < 0 || slot >= getInventory().getSize() - 9)
                throw new IllegalArgumentException();
            this.slot = slot;
            YMLSection section = MinigameTypes.EGGWARS.getSection();
            cacheItem = craftItem("shops." + id + ".slots." + slot + ".item");
            itemBase = cacheItem.clone().build();
            priceCoins = Math.max(section.getInt("shops." + id + ".slots." + slot + ".price_amount", 1), 1);
            allowShift = section.getBoolean("shops." + id + ".slots." + slot + ".allow_shift", true);
            priceType = MinigameTypes.EGGWARS.getGenerator(section.getString("shops." + id + ".slots." + slot + ".price_type", ""));
            if (priceType == null)
                throw new IllegalStateException();
            cacheItem.setGuiProperty();
        }

        @Override
        public boolean onClick(@NotNull InventoryClickEvent inventoryClickEvent) {
            return false;//TODO
        }

        @Override
        public @Nullable ItemStack getItem() {
            int has = getPreviousGui().getCoins(priceType);
            return cacheItem.setDescription(new DMessage(Minigames.get(), getTargetPlayer()).appendLang("eggwars.shops." + id + ".slots." + slot))
                    .addDescription(new DMessage(Minigames.get(), getTargetPlayer()).appendLang(
                            (allowShift ? "eggwars.shops.items.simple_and_shift" : "eggwars.shops.items.simple"),
                            "%coin_name%",
                            new DMessage(Minigames.get(), getTargetPlayer())
                                    .appendLang("eggwars.generators." + priceType.getType()).toString(),
                            "%coin_price%", String.valueOf(priceCoins),
                            "%coin_color%", String.valueOf(priceType.miniColor()),
                            "%buy_amount%", String.valueOf(itemBase.getAmount()),
                            "%coin_pricemax%", String.valueOf(priceCoins * Math.max(has / priceCoins, 1)),
                            "%buy_amountmax%", String.valueOf(itemBase.getAmount() * Math.max(has / priceCoins, 1)),
                            "%coin_has%", String.valueOf(has),
                            "%canbuy_symbol%", has >= priceCoins ? "✓" : "✗",
                            "%canbuy_color%", has >= priceCoins ? ChatColor.GREEN.toString() : ChatColor.RED.toString()
                    )).addEnchantment(Enchantment.DURABILITY, has >= priceCoins ? 1 : 0).build();
        }

        @Override
        public @NotNull Gui getGui() {
            return GenericShopMenu.this;
        }
    }
}
