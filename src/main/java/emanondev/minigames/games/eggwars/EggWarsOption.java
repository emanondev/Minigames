package emanondev.minigames.games.eggwars;

import emanondev.core.ItemBuilder;
import emanondev.core.gui.Gui;
import emanondev.core.gui.NumberEditorFButton;
import emanondev.core.gui.ResearchFButton;
import emanondev.minigames.*;
import emanondev.minigames.games.AbstractMOption;
import emanondev.minigames.games.DropsFiller;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EggWarsOption extends AbstractMOption {

    private int perTeamMaxPlayers;
    private String fillerId;
    private final List<String> kits = new ArrayList<>();

    @Override
    public int getTeamMaxSize() {
        return perTeamMaxPlayers;
    }

    public EggWarsOption() {
        this(new HashMap<>());
    }

    public EggWarsOption(@NotNull Map<String, Object> map) {
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


    @Override
    public Gui getEditorGui(Player target, Gui parent) {
        Gui gui = super.getEditorGui(target, parent);
        gui.addButton(new NumberEditorFButton<>(gui, 1, 1, 10, () -> perTeamMaxPlayers
                , (v) -> {
            perTeamMaxPlayers = Math.max(1, Math.min(32, v));
            OptionManager.get().save(EggWarsOption.this);
        },
                () -> new ItemBuilder(Material.IRON_SWORD).setGuiProperty().setAmount(perTeamMaxPlayers).build(),
                () -> Minigames.get().getLanguageConfig(gui.getTargetPlayer()).loadStringList(
                        "minioption.gui.team_max_players", new ArrayList<>()), null
        ));
        gui.addButton(new ResearchFButton<>(gui,
                () -> new ItemBuilder(Material.CHEST).setGuiProperty().setDescription(Minigames.get().getLanguageConfig(gui.getTargetPlayer()).loadMultiMessage(
                        "minioption.gui.dropsfiller_selector", new ArrayList<>(),
                        "%id%", FillerManager.get().get(fillerId) == null ? "-none-" : fillerId)).build(),
                (String base, DropsFiller filler1) -> filler1.getId().toLowerCase(Locale.ENGLISH).contains(base.toLowerCase(Locale.ENGLISH)),
                (InventoryClickEvent event, DropsFiller filler) -> {
                    fillerId = filler.getId().equals(fillerId) ? null : filler.getId();
                    OptionManager.get().save(EggWarsOption.this);
                    return true;
                },
                (DropsFiller filler) -> new ItemBuilder(Material.PAPER).addEnchantment(Enchantment.DURABILITY,
                        filler.getId().equals(fillerId) ? 1 : 0).setDescription(Minigames.get().getLanguageConfig(gui.getTargetPlayer()).loadMultiMessage(
                        "minioption.gui.dropsfiller_description", new ArrayList<>(), "%id%", filler.getId()
                )).build(),
                () -> FillerManager.get().getAll().values()));

        gui.addButton(new ResearchFButton<>(gui,
                () -> new ItemBuilder(Material.IRON_CHESTPLATE).setGuiProperty().setDescription(Minigames.get().getLanguageConfig(gui.getTargetPlayer()).loadMultiMessage(
                        "minioption.gui.kit_selector", new ArrayList<>(), "%selected%", kits.isEmpty() ? "-none-" : String.join(", ", kits)
                )).build(),
                (String base, Kit kit) -> kit.getId().toLowerCase(Locale.ENGLISH).contains(base.toLowerCase(Locale.ENGLISH)),
                (InventoryClickEvent event, Kit kit) -> {
                    if (kits.contains(kit.getId()))
                        kits.remove(kit.getId());
                    else
                        kits.add(kit.getId());
                    OptionManager.get().save(EggWarsOption.this);
                    return true;
                },
                (Kit kit) -> new ItemBuilder(Material.PAPER).addEnchantment(Enchantment.DURABILITY,
                        kits.contains(kit.getId()) ? 1 : 0).setDescription(Minigames.get().getLanguageConfig(gui.getTargetPlayer()).loadMultiMessage(
                        "minioption.gui.kit_description", new ArrayList<>(), "%id%", kit.getId(), "%price%", kit.getPrice() == 0 ? "free" : String.valueOf(kit.getPrice())

                )).build(),
                () -> KitManager.get().getAll().values()));
        return gui;
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