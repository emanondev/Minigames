package emanondev.minigames.games.deathmatch;

import emanondev.core.ItemBuilder;
import emanondev.core.gui.Gui;
import emanondev.core.gui.LongEditorFButton;
import emanondev.core.gui.ResearchFButton;
import emanondev.core.message.DMessage;
import emanondev.minigames.*;
import emanondev.minigames.generic.AbstractMOption;
import emanondev.minigames.generic.DropsFiller;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class DeathMatchOption extends AbstractMOption {

    public void setTeamMaxPlayers(int value) {
        this.perTeamMaxPlayers = Math.max(1, Math.min(32, value));
        OptionManager.get().save(this);
    }

    public void setChestFillerId(@Nullable String chestFillerId) {
        this.chestFillerId = chestFillerId;
        OptionManager.get().save(this);
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
    private String kitId;


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
        kitId = (String) map.get("kit");
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("maxPlayersPerTeam", perTeamMaxPlayers);
        map.put("fillerId", chestFillerId);
        map.put("killRewardfillerId", killKewardFillerId);
        map.put("kit", kitId);
        return map;
    }

    public @Nullable Kit getKit() {
        return kitId == null ? null : KitManager.get().get(kitId);
    }

    @Override
    public Gui getEditorGui(Player target, Gui parent) {
        Gui gui = super.getEditorGui(target, parent);
        gui.addButton(new LongEditorFButton(gui, 1, 1, 10, () -> (long) getTeamMaxSize()
                , (v) -> setTeamMaxPlayers(v.intValue()),
                () -> new ItemBuilder(Material.IRON_SWORD).setGuiProperty().setAmount(getTeamMaxSize())
                        .setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
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
                (String base, DropsFiller filler1) -> filler1.getId().toLowerCase(Locale.ENGLISH).contains(base.toLowerCase(Locale.ENGLISH)),
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
                (String base, DropsFiller filler1) -> filler1.getId().toLowerCase(Locale.ENGLISH).contains(base.toLowerCase(Locale.ENGLISH)),
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
                        "minioption.gui.kit_selector", "%selected%", kitId == null ? "-none-" : kitId
                )).build(),
                (String base, Kit kit) -> kit.getId().toLowerCase(Locale.ENGLISH).contains(base.toLowerCase(Locale.ENGLISH)),
                (InventoryClickEvent event, Kit kit) -> {
                    if (Objects.equals(kitId, kit.getId()))
                        setKit(null);
                    else
                        setKit(kit);
                    return true;
                },
                (Kit kit) -> kit.getGuiSelectorItemRaw().addEnchantment(Enchantment.DURABILITY,
                        Objects.equals(kitId, kit.getId()) ? 1 : 0).setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
                        "minioption.gui.kit_description", "%id%", kit.getId(), "%price%", "free")).build(),
                () -> KitManager.get().getAll().values()));
        return gui;
    }

    public @Nullable DropsFiller getChestsFiller() {
        return this.chestFillerId == null ? null : FillerManager.get().get(this.chestFillerId);
    }

    public @Nullable DropsFiller getKillRewardFiller() {
        return this.killKewardFillerId == null ? null : FillerManager.get().get(this.killKewardFillerId);
    }


    public void setKit(@Nullable Kit kit) {
        kitId = kit == null ? null : kit.getId();
        OptionManager.get().save(this);
    }

    @Override
    public boolean allowSelectingTeam() {
        return getTeamMaxSize() > 1;
    }

}