package emanondev.minigames.games.eggwars;

import emanondev.core.ItemBuilder;
import emanondev.core.YMLSection;
import emanondev.core.gui.Gui;
import emanondev.core.gui.GuiButton;
import emanondev.core.gui.MapGui;
import emanondev.core.message.DMessage;
import emanondev.minigames.MinigameTypes;
import emanondev.minigames.Minigames;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class ShopsMenu extends MapGui {


    private final HashMap<String, Gui> subGuis = new HashMap<>();
    private final EggWarsGame game;
    private final HashMap<EggWarsGeneratorType, Integer> coinsCache = new HashMap<>();
    private BackButton backButton;

    @Deprecated
    public ShopsMenu(@NotNull Player p) {//TODO remove
        this(p, null);
    }

    public ShopsMenu(@NotNull Player p, @NotNull EggWarsGame game) {
        super(new DMessage(Minigames.get(), p).appendLang("eggwars.shops.menu.title"), 3, p, null, Minigames.get());
        this.game = game;
        this.setButton(10, new ShopButton("blocks"));
        this.setButton(11, new ShopButton("weapons"));
        this.setButton(12, new ShopButton("armor"));
        this.setButton(13, new ShopButton("food"));
        this.setButton(14, new ShopButton("tools"));
        this.setButton(15, new ShopButton("ranged"));
        this.setButton(16, new ShopButton("special"));

        this.setButton(18, new FastButton(1));
        this.setButton(19, new FastButton(2));
        this.setButton(20, new FastButton(3));
        this.setButton(21, new FastButton(4));
        this.setButton(22, new CloseButton());
        this.setButton(23, new FastButton(5));
        this.setButton(24, new FastButton(6));
        this.setButton(25, new FastButton(7));
        this.setButton(26, new FastButton(8));
    }

    public @NotNull EggWarsGame getGame() {
        return game;
    }

    public void setFastItem(int slot, GuiButton button) {
        switch (slot) {
            case 1 -> ((FastButton) getButton(18)).link = button;
            case 2 -> ((FastButton) getButton(19)).link = button;
            case 3 -> ((FastButton) getButton(20)).link = button;
            case 4 -> ((FastButton) getButton(21)).link = button;
            case 5 -> ((FastButton) getButton(23)).link = button;
            case 6 -> ((FastButton) getButton(24)).link = button;
            case 7 -> ((FastButton) getButton(25)).link = button;
            case 8 -> ((FastButton) getButton(26)).link = button;
        }
    }

    public GuiButton getBackButton() {
        if (backButton == null)
            backButton = new BackButton();
        return backButton;
    }

    public int getCoins(@NotNull EggWarsGeneratorType type) {
        return coinsCache.get(type);
    }

    @Override
    public void onOpen(@NotNull InventoryOpenEvent event) {
        super.onOpen(event);
        this.recalculateCoins();
    }

    public void recalculateCoins() {
        MinigameTypes.EGGWARS.getGenerators().forEach(g -> coinsCache.put(g, 0));
        getTargetPlayer().getInventory().forEach((item) -> {
            EggWarsGeneratorType g = MinigameTypes.EGGWARS.getGenerator(item);
            if (g != null)
                coinsCache.put(g, coinsCache.get(g) + item.getAmount());
        });
    }

    private ItemBuilder craftItem(String path, String... holders) {
        //TODO enchants
        @NotNull YMLSection section = MinigameTypes.EGGWARS.getSection();
        return new ItemBuilder(
                section.getMaterial(path + ".material", Material.STONE))
                .setAmount(section.getInt(path + ".amount", 1))
                .setCustomModelData(section.getInteger(path + ".custom_model", null))
                .setDescription(new DMessage(Minigames.get(), getTargetPlayer()).appendLang(
                        "eggwars." + path, holders
                )).setGuiProperty();
    }

    private class ShopButton implements GuiButton {

        private final String id;

        public ShopButton(String id) {
            this.id = id;
        }

        @Override
        public boolean onClick(@NotNull InventoryClickEvent inventoryClickEvent) {
            if (subGuis.containsKey(id)) {
                subGuis.get(id).open(getTargetPlayer());
                return false;
            }
            Gui gui = id.equals("armor") ? new ArmorShopMenu(this.getGui()) : new GenericShopMenu(id, this.getGui());
            subGuis.put(id, gui);
            gui.open(getTargetPlayer());
            return false;
        }

        @Override
        public @Nullable ItemStack getItem() {
            return craftItem("shops.menu." + id).build();
        }

        @Override
        public @NotNull ShopsMenu getGui() {
            return ShopsMenu.this;
        }
    }

    private class FastButton implements GuiButton {
        private final int slot;
        private GuiButton link;

        public FastButton(int i) {
            this.slot = i;
        }

        @Override
        public boolean onClick(@NotNull InventoryClickEvent event) {
            return link != null && link.onClick(event);
        }

        @Override
        public @Nullable ItemStack getItem() {
            return link == null ? craftItem("shops.menu.fastslot", "%slot%", String.valueOf(slot)).build() : link.getItem();
        }

        @Override
        public @NotNull Gui getGui() {
            return ShopsMenu.this;
        }
    }

    private class CloseButton implements GuiButton {

        @Override
        public boolean onClick(@NotNull InventoryClickEvent event) {
            event.getWhoClicked().closeInventory();
            return false;
        }

        @Override
        public @Nullable ItemStack getItem() {
            return craftItem("shops.menu.close").build();
        }

        @Override
        public @NotNull Gui getGui() {
            return ShopsMenu.this;
        }
    }

    private class BackButton implements GuiButton {

        @Override
        public boolean onClick(@NotNull InventoryClickEvent event) {
            getGui().open(event.getWhoClicked());
            return false;
        }

        @Override
        public @Nullable ItemStack getItem() {
            return craftItem("shops.menu.back").build();
        }

        @Override
        public @NotNull Gui getGui() {
            return ShopsMenu.this;
        }
    }


}
