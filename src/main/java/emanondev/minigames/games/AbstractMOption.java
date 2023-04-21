package emanondev.minigames.games;

import emanondev.core.ItemBuilder;
import emanondev.core.gui.FButton;
import emanondev.core.gui.Gui;
import emanondev.core.gui.LongEditorFButton;
import emanondev.core.gui.PagedMapGui;
import emanondev.core.message.DMessage;
import emanondev.minigames.Configurations;
import emanondev.minigames.Minigames;
import emanondev.minigames.OptionManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractMOption extends ARegistrable implements MOption {

    private boolean showArenaBorders;
    private boolean allowSpectators;
    private int collectingPlayersPhaseCooldownMax;
    private int endPhaseCooldownMax;
    private int preStartPhaseCooldownMax;

    public AbstractMOption(@NotNull Map<String, Object> map) {
        collectingPlayersPhaseCooldownMax = (int) map.getOrDefault("collectingplayersphasecooldownmax", 27);
        endPhaseCooldownMax = (int) map.getOrDefault("endphasecooldownmax", 10);
        preStartPhaseCooldownMax = (int) map.getOrDefault("prestartphasecooldownmax", 3);
        allowSpectators = (boolean) map.getOrDefault("allowSpectators", true);
        showArenaBorders = (boolean) map.getOrDefault("showArenaBorders", true);
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("collectingplayersphasecooldownmax", collectingPlayersPhaseCooldownMax);
        map.put("endphasecooldownmax", endPhaseCooldownMax);
        map.put("prestartphasecooldownmax", preStartPhaseCooldownMax);
        map.put("allowSpectators", allowSpectators);
        map.put("showArenaBorders", showArenaBorders);
        return map;
    }

    @Override
    public Gui getEditorGui(Player target, Gui parent) {
        PagedMapGui gui = new PagedMapGui(
                new DMessage(Minigames.get(), target).appendLang("minioption.gui.title", getPlaceholders()),
                6, target, parent, Minigames.get());
        gui.addButton(new LongEditorFButton(gui, 1, 1, 10,
                () -> (long) getCollectingPlayersPhaseCooldownMax(),
                (v) -> setCollectingPlayersPhaseCooldownMax(v.intValue()),
                () -> Configurations.getCollectingPlayersPhaseCooldownMaxItem(gui.getTargetPlayer()).setAmount(Math.max(1, Math.min(101, getCollectingPlayersPhaseCooldownMax())))
                        .setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
                                "minioption.gui.collecting_players_phase_max_cooldown", "%value%", String.valueOf(getCollectingPlayersPhaseCooldownMax()))).build()));
        gui.addButton(new LongEditorFButton(gui, 1, 1, 10, () -> (long) getEndPhaseCooldownMax(),
                (v) -> setEndPhaseCooldownMax(v.intValue()),
                () -> Configurations.getEndPhaseCooldownMaxItem(gui.getTargetPlayer()).setAmount(Math.max(1, Math.min(101, getEndPhaseCooldownMax())))
                        .setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
                                "minioption.gui.end_phase_max_cooldown", "%value%", String.valueOf(getEndPhaseCooldownMax()))).build()));
        gui.addButton(new LongEditorFButton(gui, 1, 1, 10, () -> (long) getPreStartPhaseCooldownMax()
                , (v) -> setPreStartPhaseCooldownMax(v.intValue()),
                () -> Configurations.getPreStartPhaseCooldownMaxItem(gui.getTargetPlayer()).setAmount(Math.max(1, Math.min(101, getPreStartPhaseCooldownMax())))
                        .setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLang(
                                "minioption.gui.pre_start_phase_max_cooldown", "%value%", String.valueOf(getPreStartPhaseCooldownMax()))).build()));
        gui.addButton(new FButton(gui, () ->
                new ItemBuilder(Material.VEX_SPAWN_EGG).setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer())
                                .appendLang("minioption.gui.allow_spectators", "%value%", String.valueOf(getAllowSpectators())))
                        .setGuiProperty().addEnchantment(Enchantment.DURABILITY, getAllowSpectators() ? 1 : 0).build(), (event) -> {
            setAllowSpectators(!getAllowSpectators());
            return true;
        }
        ));
        gui.addButton(new FButton(gui, () ->
                new ItemBuilder(Material.BARRIER).setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer())
                                .appendLang("minioption.gui.show_arena_borders", "%value%",
                                        String.valueOf(getAllowSpectators())))
                        .setGuiProperty().addEnchantment(Enchantment.DURABILITY, getShowArenaBorders() ? 1 : 0).build(), (event) -> {
            setShowArenaBorders(!getShowArenaBorders());
            return true;
        }
        ));
        return gui;
    }

    @Override
    public int getCollectingPlayersPhaseCooldownMax() {
        return collectingPlayersPhaseCooldownMax;
    }

    @Override
    public void setCollectingPlayersPhaseCooldownMax(int value) {
        collectingPlayersPhaseCooldownMax = Math.max(1, Math.min(180, value));
        OptionManager.get().save(this);
    }

    @Override
    public int getEndPhaseCooldownMax() {
        return endPhaseCooldownMax;
    }

    @Override
    public void setEndPhaseCooldownMax(int value) {
        endPhaseCooldownMax = Math.max(1, Math.min(60, value));
        OptionManager.get().save(this);
    }

    @Override
    public int getPreStartPhaseCooldownMax() {
        return preStartPhaseCooldownMax;
    }

    @Override
    public void setPreStartPhaseCooldownMax(int value) {
        this.preStartPhaseCooldownMax = Math.max(1, Math.min(60, value));
        OptionManager.get().save(this);
    }

    @Override
    public boolean getAllowSpectators() {
        return allowSpectators;
    }

    @Override
    public void setAllowSpectators(boolean allowSpectators) {
        this.allowSpectators = allowSpectators;
        OptionManager.get().save(this);
    }

    @Override
    public boolean getShowArenaBorders() {
        return showArenaBorders;
    }

    @Override
    public void setShowArenaBorders(boolean value) {
        showArenaBorders = value;
        OptionManager.get().save(this);
    }

}
