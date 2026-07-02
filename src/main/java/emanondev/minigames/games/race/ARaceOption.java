package emanondev.minigames.games.race;

import emanondev.core.ItemBuilder;
import emanondev.core.gui.FButton;
import emanondev.core.gui.Gui;
import emanondev.core.gui.LongEditorFButton;
import emanondev.core.gui.ResearchFButton;
import emanondev.core.message.DMessage;
import emanondev.minigames.Kit;
import emanondev.minigames.KitManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.OptionManager;
import emanondev.minigames.games.AbstractMOption;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ARaceOption extends AbstractMOption {

    @Getter
    private boolean allowedPvp;
    @Getter
    private boolean allowedPve;
    @Getter
    private boolean allowedFallDamage;
    @Getter
    private boolean allowedEnvironmentDamage;
    private String kitId;
    @Getter
    private int teamMaxSize;

    public ARaceOption(@NotNull Map<String, Object> map) {
        super(map);
        teamMaxSize = Math.max(1, (int) map.getOrDefault("maxPlayersPerTeam", 1));
        kitId = (String) map.get("kit");
        allowedPvp = (boolean) map.getOrDefault("allowPvp", false);
        allowedPve = (boolean) map.getOrDefault("allowPve", false);
        allowedFallDamage = (boolean) map.getOrDefault("allowFallDamage", false);
        allowedEnvironmentDamage = (boolean) map.getOrDefault("allowEnvironmentDamage", false);
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("maxPlayersPerTeam", teamMaxSize);
        map.put("kit", kitId);
        map.put("allowPvp", allowedPvp);
        map.put("allowPve", allowedPve);
        map.put("allowFallDamage", allowedFallDamage);
        map.put("allowEnvironmentDamage", allowedEnvironmentDamage);
        return map;
    }

    @Override
    public Gui getEditorGui(Player target, Gui parent) {
        Gui gui = super.getEditorGui(target, parent);
        gui.addButton(new LongEditorFButton(gui, 1, 1, 10, () -> (long) teamMaxSize
                , (v) -> setTeamMaxSize(v.intValue()),
                () -> new ItemBuilder(Material.IRON_SWORD).setGuiProperty().setAmount(teamMaxSize)
                        .setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
                                "minioption.gui.team_max_players", "%value%", String.valueOf(teamMaxSize))).build()));
        gui.addButton(new ResearchFButton<>(gui,
                () -> new ItemBuilder(Material.IRON_CHESTPLATE).setGuiProperty().setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
                        "minioption.gui.kit_selector", "%selected%", kitId == null ? "-none-" : kitId
                )).build(),
                (String base, Kit kit) -> kit.getId().contains(base.toLowerCase(Locale.ENGLISH)),
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
        gui.addButton(new FButton(gui, () -> new ItemBuilder(Material.IRON_SWORD).setGuiProperty().addEnchantment(Enchantment.DURABILITY,
                allowedPvp ? 1 : 0).setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
                "minioption.gui.allow_pvp", "%value%", String.valueOf(allowedPvp))).build(),
                (e) -> {
                    this.setAllowedPvp(!this.isAllowedPvp());
                    return true;
                }
        ));
        gui.addButton(new FButton(gui, () -> new ItemBuilder(Material.BOW).setGuiProperty().addEnchantment(Enchantment.DURABILITY,
                allowedPve ? 1 : 0).setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
                "minioption.gui.allow_pve", "%value%", String.valueOf(allowedPve))).build(),
                (e) -> {
                    this.setAllowedPve(!this.isAllowedPve());
                    return true;
                }
        ));
        gui.addButton(new FButton(gui, () -> new ItemBuilder(Material.LEATHER_BOOTS).setGuiProperty().addEnchantment(Enchantment.DURABILITY,
                allowedFallDamage ? 1 : 0).setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
                "minioption.gui.allow_fall_damage", "%value%", String.valueOf(allowedFallDamage))).build(),
                (e) -> {
                    this.setAllowedFallDamage(!this.isAllowedFallDamage());
                    return true;
                }
        ));
        gui.addButton(new FButton(gui, () -> new ItemBuilder(Material.LAVA_BUCKET).setGuiProperty().addEnchantment(Enchantment.DURABILITY,
                allowedEnvironmentDamage ? 1 : 0).setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
                "minioption.gui.allow_environment_damage", "%value%", String.valueOf(allowedEnvironmentDamage))).build(),
                (e) -> {
                    this.setAllowedEnvironmentDamage(!this.isAllowedEnvironmentDamage());
                    return true;
                }
        ));
        return gui;
    }

    public void setAllowedPve(boolean value) {
        this.allowedPve = value;
        OptionManager.get().save(this);
    }

    public void setAllowedFallDamage(boolean value) {
        this.allowedFallDamage = value;
        OptionManager.get().save(this);
    }

    public void setAllowedEnvironmentDamage(boolean value) {
        this.allowedEnvironmentDamage = value;
        OptionManager.get().save(this);
    }

    public void setAllowedPvp(boolean value) {
        this.allowedPvp = value;
        OptionManager.get().save(this);
    }

    public @Nullable Kit getKit() {
        return kitId == null ? null : KitManager.get().get(kitId);
    }

    public void setKit(@Nullable Kit kit) {
        kitId = kit == null ? null : kit.getId();
        OptionManager.get().save(this);
    }

    @Override
    public boolean allowSelectingTeam() {
        return getTeamMaxSize() > 1;
    }


    public void setTeamMaxSize(int amount) {
        teamMaxSize = Math.max(1, Math.min(32, amount));
        OptionManager.get().save(ARaceOption.this);
    }


}
