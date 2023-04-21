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

    private boolean allowPvp;
    private boolean allowPve;
    private boolean allowFallDamage;
    private boolean allowEnvironmentDamage;
    private String kitId;
    private int perTeamMaxPlayers;

    public ARaceOption(@NotNull Map<String, Object> map) {
        super(map);
        perTeamMaxPlayers = Math.max(1, (int) map.getOrDefault("maxPlayersPerTeam", 1));
        kitId = (String) map.get("kit");
        allowPvp = (boolean) map.getOrDefault("allowPvp", false);
        allowPve = (boolean) map.getOrDefault("allowPve", false);
        allowFallDamage = (boolean) map.getOrDefault("allowFallDamage", false);
        allowEnvironmentDamage = (boolean) map.getOrDefault("allowEnvironmentDamage", false);
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("maxPlayersPerTeam", perTeamMaxPlayers);
        map.put("kit", kitId);
        map.put("allowPvp", allowPvp);
        map.put("allowPve", allowPve);
        map.put("allowFallDamage", allowFallDamage);
        map.put("allowEnvironmentDamage", allowEnvironmentDamage);
        return map;
    }

    @Override
    public Gui getEditorGui(Player target, Gui parent) {
        Gui gui = super.getEditorGui(target, parent);
        gui.addButton(new LongEditorFButton(gui, 1, 1, 10, () -> (long) perTeamMaxPlayers
                , (v) -> setTeamMaxSize(v.intValue()),
                () -> new ItemBuilder(Material.IRON_SWORD).setGuiProperty().setAmount(perTeamMaxPlayers)
                        .setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
                                "minioption.gui.team_max_players", "%value%", String.valueOf(perTeamMaxPlayers))).build()));
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
                allowPvp ? 1 : 0).setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
                "minioption.gui.allow_pvp", "%value%", String.valueOf(allowPvp))).build(),
                (e) -> {
                    this.setAllowPvp(!this.getAllowPvp());
                    return true;
                }
        ));
        gui.addButton(new FButton(gui, () -> new ItemBuilder(Material.BOW).setGuiProperty().addEnchantment(Enchantment.DURABILITY,
                allowPve ? 1 : 0).setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
                "minioption.gui.allow_pve", "%value%", String.valueOf(allowPve))).build(),
                (e) -> {
                    this.setAllowPve(!this.getAllowPve());
                    return true;
                }
        ));
        gui.addButton(new FButton(gui, () -> new ItemBuilder(Material.LEATHER_BOOTS).setGuiProperty().addEnchantment(Enchantment.DURABILITY,
                allowFallDamage ? 1 : 0).setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
                "minioption.gui.allow_fall_damage", "%value%", String.valueOf(allowFallDamage))).build(),
                (e) -> {
                    this.setAllowFallDamage(!this.getAllowFallDamage());
                    return true;
                }
        ));
        gui.addButton(new FButton(gui, () -> new ItemBuilder(Material.LAVA_BUCKET).setGuiProperty().addEnchantment(Enchantment.DURABILITY,
                allowEnvironmentDamage ? 1 : 0).setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
                "minioption.gui.allow_environment_damage", "%value%", String.valueOf(allowEnvironmentDamage))).build(),
                (e) -> {
                    this.setAllowEnvironmentDamage(!this.getAllowEnvironmentDamage());
                    return true;
                }
        ));
        return gui;
    }

    public boolean getAllowPvp() {
        return allowPvp;
    }

    public boolean getAllowPve() {
        return allowPve;
    }

    public void setAllowPve(boolean value) {
        this.allowPve = value;
        OptionManager.get().save(this);
    }

    public boolean getAllowFallDamage() {
        return allowFallDamage;
    }

    public void setAllowFallDamage(boolean value) {
        this.allowFallDamage = value;
        OptionManager.get().save(this);
    }

    public boolean getAllowEnvironmentDamage() {
        return allowEnvironmentDamage;
    }

    public void setAllowEnvironmentDamage(boolean value) {
        this.allowEnvironmentDamage = value;
        OptionManager.get().save(this);
    }

    public void setAllowPvp(boolean value) {
        this.allowPvp = value;
        OptionManager.get().save(this);
    }

    public @Nullable Kit getKit() {
        return kitId == null ? null : KitManager.get().get(kitId);
    }

    @Override
    public int getTeamMaxSize() {
        return perTeamMaxPlayers;
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
        perTeamMaxPlayers = Math.max(1, Math.min(32, amount));
        OptionManager.get().save(ARaceOption.this);
    }


}
