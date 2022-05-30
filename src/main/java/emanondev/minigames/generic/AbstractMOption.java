package emanondev.minigames.generic;

import emanondev.core.ItemBuilder;
import emanondev.core.UtilsString;
import emanondev.core.gui.FButton;
import emanondev.core.gui.NumberEditorFButton;
import emanondev.core.gui.PagedMapGui;
import emanondev.minigames.Minigames;
import emanondev.minigames.OptionManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractMOption implements MOption {

    private boolean allowSpectators;
    private int collectingPlayersPhaseCooldownMax = 10;
    private int endPhaseCooldownMax = 10;
    private int preStartPhaseCooldownMax = 6;

    public AbstractMOption(@NotNull Map<String, Object> map) {
        collectingPlayersPhaseCooldownMax = (int) map.getOrDefault("collectingplayersphasecooldownmax", 45);
        endPhaseCooldownMax = (int) map.getOrDefault("endphasecooldownmax", 10);
        preStartPhaseCooldownMax = (int) map.getOrDefault("prestartphasecooldownmax", 10);
        allowSpectators = (boolean) map.getOrDefault("allowSpectators", true);
    }

    @Override
    public int getCollectingPlayersPhaseCooldownMax() {
        return collectingPlayersPhaseCooldownMax;
    }

    @Override
    public int getEndPhaseCooldownMax() {
        return endPhaseCooldownMax;
    }

    @Override
    public int getPreStartPhaseCooldownMax() {
        return preStartPhaseCooldownMax;
    }

    private String id = null;

    public boolean isRegistered() {
        return id != null;
    }

    public void setRegistered(@NotNull String id) {
        if (!UtilsString.isLowcasedValidID(id))
            throw new IllegalStateException();
        this.id = id;
    }

    public @NotNull String getId() {
        return id;
    }

    public void setUnregister() {
        id = null;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("collectingplayersphasecooldownmax", collectingPlayersPhaseCooldownMax);
        map.put("endphasecooldownmax", endPhaseCooldownMax);
        map.put("prestartphasecooldownmax", preStartPhaseCooldownMax);
        map.put("allowSpectators", allowSpectators);
        return map;
    }

    @Override
    public boolean allowSpectators() {
        return allowSpectators;
    }

    protected void fillEditor(@NotNull PagedMapGui gui) {
        gui.addButton(new NumberEditorFButton<>(gui, 1, 1, 10, () -> collectingPlayersPhaseCooldownMax
                , (v) -> {
            collectingPlayersPhaseCooldownMax = Math.max(1, Math.min(32, v));
            OptionManager.get().save(AbstractMOption.this);
        },
                () -> new ItemBuilder(Material.CLOCK).setGuiProperty().setAmount(Math.max(1, Math.min(101, collectingPlayersPhaseCooldownMax))).build(),
                () -> Minigames.get().getLanguageConfig(gui.getTargetPlayer()).loadStringList(
                        "minioption.buttons.collectingplayersphasecooldownmax", new ArrayList<>()), null
        ));
        gui.addButton(new NumberEditorFButton<>(gui, 1, 1, 10, () -> endPhaseCooldownMax
                , (v) -> {
            endPhaseCooldownMax = Math.max(1, Math.min(32, v));
            OptionManager.get().save(AbstractMOption.this);
        },
                () -> new ItemBuilder(Material.CLOCK).setGuiProperty().setAmount(Math.max(1, Math.min(101, endPhaseCooldownMax))).build(),
                () -> Minigames.get().getLanguageConfig(gui.getTargetPlayer()).loadStringList(
                        "minioption.buttons.endphasecooldownmax", new ArrayList<>()), null
        ));
        gui.addButton(new NumberEditorFButton<>(gui, 1, 1, 10, () -> preStartPhaseCooldownMax
                , (v) -> {
            preStartPhaseCooldownMax = Math.max(1, Math.min(32, v));
            OptionManager.get().save(AbstractMOption.this);
        },
                () -> new ItemBuilder(Material.CLOCK).setGuiProperty().setAmount(Math.max(1, Math.min(101, preStartPhaseCooldownMax))).build(),
                () -> Minigames.get().getLanguageConfig(gui.getTargetPlayer()).loadStringList(
                        "minioption.buttons.prestartphasecooldownmax", new ArrayList<>()), null
        ));
        gui.addButton(new FButton(gui, () -> new ItemBuilder(Material.VEX_SPAWN_EGG).addEnchantment(Enchantment.DURABILITY, allowSpectators ? 1 : 0).setGuiProperty().setDescription(
                Minigames.get().getLanguageConfig(gui.getTargetPlayer()).loadStringList(
                        "minioption.buttons.allowspectators", new ArrayList<>()), true
                , "%value%", String.valueOf(allowSpectators)
        ).build(), (event) -> {
            allowSpectators = !allowSpectators;
            OptionManager.get().save(AbstractMOption.this);
            return true;
        }
        ));
    }

    public abstract PagedMapGui craftEditor(Player target);
}
