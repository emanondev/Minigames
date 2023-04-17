package emanondev.minigames.games.deathmatch;

import emanondev.core.ItemBuilder;
import emanondev.core.gui.Gui;
import emanondev.core.gui.LongEditorFButton;
import emanondev.core.gui.ResearchFButton;
import emanondev.core.message.DMessage;
import emanondev.minigames.*;
import emanondev.minigames.games.AbstractMOption;
import emanondev.minigames.games.DropsFiller;
import emanondev.minigames.games.MOptionWithKitsChoice;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SerializableAs("DeathMatchOption")
public class DeathMatchOption extends AbstractMOption implements MOptionWithKitsChoice {

    public void setTeamMaxPlayers(int value) {
        this.perTeamMaxPlayers = Math.max(1, Math.min(32, value));
        OptionManager.get().save(this);
    }

    public void setChestFillerId(@Nullable String chestFillerId) {
        this.chestFillerId = chestFillerId;
        OptionManager.get().save(this);
    }

    public boolean hasKitId(String kit) {
        return kit != null && kits.contains(kit);
    }

    public void addKitId(@Nullable String kitId) {
        if (kitId != null && !kits.contains(kitId)) {
            this.kits.add(kitId);
            OptionManager.get().save(this);
        }
    }

    public void removeKitId(@Nullable String kitId) {
        if (kitId != null && kits.contains(kitId)) {
            this.kits.remove(kitId);
            OptionManager.get().save(this);
        }
    }

    public void toggleKitId(@Nullable String kitId) {
        if (this.kits.contains(kitId))
            removeKitId(kitId);
        else
            addKitId(kitId);
    }

    public void setKillKewardFillerId(@Nullable String killKewardFillerId) {
        this.killKewardFillerId = killKewardFillerId;
        OptionManager.get().save(this);
    }

    public @Nullable String getChestFillerId() {
        return chestFillerId;
    }

    public @Nullable String getKillKewardFillerId() {
        return killKewardFillerId;
    }

    private int perTeamMaxPlayers;
    private String chestFillerId;
    private String killKewardFillerId;
    private final List<String> kits = new ArrayList<>();

    @Override
    public int getTeamMaxSize() {
        return perTeamMaxPlayers;
    }

    public DeathMatchOption() {
        this(new HashMap<>());
    }

    public DeathMatchOption(@NotNull Map<String, Object> map) {
        super(map);
        perTeamMaxPlayers = Math.max(1, (int) map.getOrDefault("maxPlayersPerTeam", 1));
        chestFillerId = (String) map.get("fillerId");
        killKewardFillerId = (String) map.get("killRewardfillerId");
        kits.addAll((List<String>) map.getOrDefault("kits", Collections.emptyList()));
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("maxPlayersPerTeam", perTeamMaxPlayers);
        map.put("fillerId", chestFillerId);
        map.put("killRewardfillerId", killKewardFillerId);
        map.put("kits", kits);
        return map;
    }


    @Override
    public Gui getEditorGui(Player target, Gui parent) {
        Gui gui = super.getEditorGui(target, parent);
        gui.addButton(new LongEditorFButton(gui, 1, 1, 10, () -> (long) getTeamMaxSize()
                , (v) -> setTeamMaxPlayers(v.intValue()),
                () -> new ItemBuilder(Material.IRON_SWORD).setGuiProperty().setAmount(getTeamMaxSize())
                        .setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                                "minioption.gui.team_max_players", "%value%", String.valueOf(getTeamMaxSize()))).build()));
        gui.addButton(new ResearchFButton<>(gui,
                () -> {
                    DropsFiller filler = getChestFillerId() == null ? null : FillerManager.get().get(getChestFillerId());
                    return new ItemBuilder(Material.CHEST).setGuiProperty().setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
                            "minioption.gui.chestsfiller_selector",
                            "%id%", filler == null ? "-none-" : getChestFillerId(),
                            "%size%", filler == null ? "-" : String.valueOf(filler.getSize())
                    )).build();
                },
                (String base, DropsFiller filler1) -> filler1.getId().contains(base.toLowerCase(Locale.ENGLISH)),
                (InventoryClickEvent event, DropsFiller filler) -> {
                    setChestFillerId(filler.getId().equals(getChestFillerId()) ? null : filler.getId());
                    return true;
                },
                (DropsFiller filler) -> new ItemBuilder(Material.PAPER).setGuiProperty().addEnchantment(Enchantment.DURABILITY,
                        filler.getId().equals(getChestFillerId()) ? 1 : 0).setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
                        "minioption.gui.chestsfiller_description", filler.getPlaceholders()
                )).build(),
                () -> FillerManager.get().getAll().values()));
        gui.addButton(new ResearchFButton<>(gui,
                () -> {
                    DropsFiller filler = getKillKewardFillerId() == null ? null : FillerManager.get().get(getKillKewardFillerId());
                    return new ItemBuilder(Material.GOLD_INGOT).setGuiProperty().setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
                            "minioption.gui.killrewardfiller_selector",
                            "%id%", filler == null ? "-none-" : getKillKewardFillerId(),
                            "%size%", filler == null ? "-" : String.valueOf(filler.getSize())
                    )).build();
                },
                (String base, DropsFiller filler1) -> filler1.getId().contains(base.toLowerCase(Locale.ENGLISH)),
                (InventoryClickEvent event, DropsFiller filler) -> {
                    setKillKewardFillerId(filler.getId().equals(getKillKewardFillerId()) ? null : filler.getId());
                    return true;
                },
                (DropsFiller filler) -> new ItemBuilder(Material.PAPER).setGuiProperty().addEnchantment(Enchantment.DURABILITY,
                        filler.getId().equals(getKillKewardFillerId()) ? 1 : 0).setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
                        "minioption.gui.killrewardfiller_description", filler.getPlaceholders()
                )).build(),
                () -> FillerManager.get().getAll().values()));
        gui.addButton(new ResearchFButton<>(gui,
                () -> new ItemBuilder(Material.IRON_CHESTPLATE).setGuiProperty().setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
                        "minioption.gui.kits_selector", "%selected%", kits.isEmpty() ? "-none-" : String.join(", ", getKitsId())
                )).build(),
                (String base, Kit kit) -> kit.getId().contains(base.toLowerCase(Locale.ENGLISH)),
                (InventoryClickEvent event, Kit kit) -> {
                    toggleKit(kit);
                    return true;
                },
                (Kit kit) -> new ItemBuilder(Material.PAPER).setGuiProperty().addEnchantment(Enchantment.DURABILITY,
                        hasKit(kit) ? 1 : 0).setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
                        "minioption.gui.kit_description", "%id%", kit.getId(), "%price%", kit.getPrice() == 0 ? "free" : String.valueOf(kit.getPrice())
                )).build(),
                () -> KitManager.get().getAll().values()));
        return gui;
    }

    public @Nullable DropsFiller getChestsFiller() {
        return this.chestFillerId == null ? null : FillerManager.get().get(this.chestFillerId);
    }

    public @Nullable DropsFiller getKillRewardFiller() {
        return this.killKewardFillerId == null ? null : FillerManager.get().get(this.killKewardFillerId);
    }

    public @NotNull List<Kit> getKits() {
        List<Kit> list = new ArrayList<>();
        for (String key : kits) {
            Kit kit = KitManager.get().get(key);
            if (kit != null)
                list.add(kit);
        }
        list.sort((k1, k2) -> k1.getPrice() == k2.getPrice() ? k1.getId().compareTo(k2.getId()) : k2.getPrice() - k1.getPrice()); //TODO ? sort by price,name
        return list;
    }

    public @NotNull List<String> getKitsId() {
        return Collections.unmodifiableList(kits);
    }

    @Override
    public boolean allowSelectingTeam() {
        return getTeamMaxSize() > 1;
    }

}
