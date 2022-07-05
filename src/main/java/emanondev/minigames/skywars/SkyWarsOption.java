package emanondev.minigames.skywars;

import emanondev.core.ItemBuilder;
import emanondev.core.gui.NumberEditorFButton;
import emanondev.core.gui.PagedMapGui;
import emanondev.core.gui.ResearchFButton;
import emanondev.minigames.*;
import emanondev.minigames.generic.AbstractMOption;
import emanondev.minigames.generic.MFiller;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
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

    @Override
    public void openEditor(@NotNull Player who) {
        PagedMapGui gui = craftEditor(who);
        fillEditor(gui);
        gui.open(who);
    }

    protected void fillEditor(@NotNull PagedMapGui gui) {
        super.fillEditor(gui);
        gui.addButton(new NumberEditorFButton<>(gui, 1, 1, 10, () -> perTeamMaxPlayers
                , (v) -> {
            perTeamMaxPlayers = Math.max(1, Math.min(32, v));
            OptionManager.get().save(SkyWarsOption.this);
        },
                () -> new ItemBuilder(Material.IRON_SWORD).setGuiProperty().setAmount(perTeamMaxPlayers).build(),
                () -> Minigames.get().getLanguageConfig(gui.getTargetPlayer()).loadStringList(
                        "minioption.buttons.teammaxplayers", new ArrayList<>()), null
        ));
        gui.addButton(new ResearchFButton<>(gui,
                () -> new ItemBuilder(Material.CHEST).setGuiProperty().setDescription(Minigames.get().getLanguageConfig(gui.getTargetPlayer()).loadMultiMessage(
                        "minioption.buttons.fillerselector", new ArrayList<>(), "%id%", FillerManager.get().getFiller(fillerId) == null ? "-none-" : fillerId,
                        "%size%", FillerManager.get().getFiller(fillerId) == null ? "-" : String.valueOf(FillerManager.get().getFiller(fillerId).getElementsAmount()),
                        "%min%", FillerManager.get().getFiller(fillerId) == null ? "-" : String.valueOf(FillerManager.get().getFiller(fillerId).getMinElements()),
                        "%max%", FillerManager.get().getFiller(fillerId) == null ? "-" : String.valueOf(FillerManager.get().getFiller(fillerId).getMaxElements()))).build(),
                (String base, MFiller filler1) -> filler1.getId().toLowerCase().contains(base.toLowerCase()),
                (InventoryClickEvent event, MFiller filler) -> {
                    fillerId = filler.getId().equals(fillerId) ? null : filler.getId();
                    OptionManager.get().save(SkyWarsOption.this);
                    return true;
                },
                (MFiller filler) -> new ItemBuilder(Material.PAPER).addEnchantment(Enchantment.DURABILITY,
                        filler.getId().equals(fillerId) ? 1 : 0).setDescription(Minigames.get().getLanguageConfig(gui.getTargetPlayer()).loadMultiMessage(
                        "minioption.buttons.fillerdescription", new ArrayList<>(), "%id%", filler.getId(), "%size%", String.valueOf(filler.getElementsAmount()),
                        "%min%", String.valueOf(filler.getMinElements()), "%max%", String.valueOf(filler.getMaxElements())
                )).build(),
                () -> FillerManager.get().getFillers().values()));

        gui.addButton(new ResearchFButton<>(gui,
                () -> new ItemBuilder(Material.IRON_CHESTPLATE).setGuiProperty().setDescription(Minigames.get().getLanguageConfig(gui.getTargetPlayer()).loadMultiMessage(
                        "minioption.buttons.kitselector", new ArrayList<>(), "%selected%", kits.isEmpty() ? "-none-" : String.join(", ", kits)
                )).build(),
                (String base, Kit kit) -> kit.getId().toLowerCase().contains(base.toLowerCase()),
                (InventoryClickEvent event, Kit kit) -> {
                    if (kits.contains(kit.getId()))
                        kits.remove(kit.getId());
                    else
                        kits.add(kit.getId());
                    OptionManager.get().save(SkyWarsOption.this);
                    return true;
                },
                (Kit kit) -> new ItemBuilder(Material.PAPER).addEnchantment(Enchantment.DURABILITY,
                        kits.contains(kit.getId()) ? 1 : 0).setDescription(Minigames.get().getLanguageConfig(gui.getTargetPlayer()).loadMultiMessage(
                        "minioption.buttons.kitdescription", new ArrayList<>(), "%id%", kit.getId(), "%price%", kit.getPrice() == 0 ? "free" : String.valueOf(kit.getPrice())

                )).build(),
                () -> KitManager.get().getKits().values()));
    }

    @Override
    public PagedMapGui craftEditor(@NotNull Player target) {
        return new PagedMapGui(Minigames.get().getLanguageConfig(target).loadMessage(
                "skywars.option_gui_title", ""), 6, target, null, Minigames.get());
    }

    public @Nullable MFiller getFiller() {
        return this.fillerId == null ? null : FillerManager.get().getFiller(this.fillerId);
    }

    public @NotNull List<Kit> getKits() {
        List<Kit> list = new ArrayList<>();
        for (String key : kits) {
            Kit kit = KitManager.get().getKit(key);
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
