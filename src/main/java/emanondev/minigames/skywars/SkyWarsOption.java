package emanondev.minigames.skywars;

import emanondev.core.ItemBuilder;
import emanondev.core.gui.LongEditorFButton;
import emanondev.core.gui.PagedMapGui;
import emanondev.core.gui.ResearchFButton;
import emanondev.core.message.DMessage;
import emanondev.minigames.*;
import emanondev.minigames.generic.AbstractMOption;
import emanondev.minigames.generic.DropsFiller;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SkyWarsOption extends AbstractMOption {

    private int perTeamMaxPlayers;
    private String fillerId;
    private final List<String> kits = new ArrayList<>();

    @Override
    public int getTeamMaxSize() {
        return perTeamMaxPlayers;
    }

    public SkyWarsOption() {
        this(new HashMap<>());
    }

    public SkyWarsOption(@NotNull Map<String, Object> map) {
        super(map);
        perTeamMaxPlayers = Math.max(1, (int) map.getOrDefault("maxPlayersPerTeam", 1));
        fillerId = (String) map.get("fillerId");
        kits.addAll((List<String>) map.getOrDefault("kits", Collections.emptyList()));
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("maxPlayersPerTeam", perTeamMaxPlayers);
        map.put("fillerId", fillerId);
        map.put("kits", kits);
        return map;
    }

    protected void fillEditor(@NotNull PagedMapGui gui) {
        super.fillEditor(gui);
        gui.addButton(new LongEditorFButton(gui, 1, 1, 10, () -> (long) perTeamMaxPlayers
                , (v) -> {
            perTeamMaxPlayers = (int) Math.max(1, Math.min(32, v));
            OptionManager.get().save(SkyWarsOption.this);
        },
                () -> new ItemBuilder(Material.IRON_SWORD).setGuiProperty().setAmount(perTeamMaxPlayers)
                        .setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                                "minioption.gui.team_max_players", "%value%", String.valueOf(perTeamMaxPlayers))).build()));

        gui.addButton(new ResearchFButton<>(gui,
                () -> {
                    DropsFiller filler = fillerId == null ? null : FillerManager.get().get(fillerId);
                    return new ItemBuilder(Material.CHEST).setGuiProperty().setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                            "minioption.gui.dropsfiller_selector",
                            "%id%", filler == null ? "-none-" : fillerId,
                            "%size%", filler == null ? "-" : String.valueOf(filler.getSize())
                    )).build();
                },
                (String base, DropsFiller filler1) -> filler1.getId().toLowerCase(Locale.ENGLISH).contains(base.toLowerCase(Locale.ENGLISH)),
                (InventoryClickEvent event, DropsFiller filler) -> {
                    fillerId = filler.getId().equals(fillerId) ? null : filler.getId();
                    OptionManager.get().save(SkyWarsOption.this);
                    return true;
                },
                (DropsFiller filler) -> new ItemBuilder(Material.PAPER).setGuiProperty().addEnchantment(Enchantment.DURABILITY,
                        filler.getId().equals(fillerId) ? 1 : 0).setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                        "minioption.gui.dropsfiller_description", filler.getPlaceholders()
                )).build(),
                () -> FillerManager.get().getAll().values()));

        gui.addButton(new ResearchFButton<>(gui,
                () -> new ItemBuilder(Material.IRON_CHESTPLATE).setGuiProperty().setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                        "minioption.gui.kit_selector", "%selected%", kits.isEmpty() ? "-none-" : String.join(", ", kits)
                )).build(),
                (String base, Kit kit) -> kit.getId().toLowerCase(Locale.ENGLISH).contains(base.toLowerCase(Locale.ENGLISH)),
                (InventoryClickEvent event, Kit kit) -> {
                    if (kits.contains(kit.getId()))
                        kits.remove(kit.getId());
                    else
                        kits.add(kit.getId());
                    OptionManager.get().save(SkyWarsOption.this);
                    return true;
                },
                (Kit kit) -> new ItemBuilder(Material.PAPER).setGuiProperty().addEnchantment(Enchantment.DURABILITY,
                        kits.contains(kit.getId()) ? 1 : 0).setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                        "minioption.gui.kit_description", "%id%", kit.getId(), "%price%", kit.getPrice() == 0 ? "free" : String.valueOf(kit.getPrice())
                )).build(),
                () -> KitManager.get().getAll().values()));
    }

    public @Nullable DropsFiller getFiller() {
        return this.fillerId == null ? null : FillerManager.get().get(this.fillerId);
    }

    public @NotNull List<Kit> getKits() {
        List<Kit> list = new ArrayList<>();
        for (String key : kits) {
            Kit kit = KitManager.get().get(key);
            if (kit != null)
                list.add(kit);
        }
        list.sort((k1, k2) -> k1.getPrice() == k2.getPrice() ? k1.getId().compareToIgnoreCase(k2.getId()) : k2.getPrice() - k1.getPrice());
        return list;
    }

    @Override
    public boolean allowSelectingTeam() {
        return getTeamMaxSize() > 1;
    }

}
