package emanondev.minigames.generic;

import emanondev.core.ItemBuilder;
import emanondev.core.gui.FButton;
import emanondev.core.gui.Gui;
import emanondev.core.gui.NumberEditorFButton;
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

    private boolean allowSpectators;
    private int collectingPlayersPhaseCooldownMax;
    private int endPhaseCooldownMax;
    private int preStartPhaseCooldownMax;

    public AbstractMOption(@NotNull Map<String, Object> map) {
        collectingPlayersPhaseCooldownMax = (int) map.getOrDefault("collectingplayersphasecooldownmax", 27);
        endPhaseCooldownMax = (int) map.getOrDefault("endphasecooldownmax", 10);
        preStartPhaseCooldownMax = (int) map.getOrDefault("prestartphasecooldownmax", 3);
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
    public boolean getAllowSpectators() {
        return allowSpectators;
    }

    protected void fillEditor(@NotNull PagedMapGui gui) {
        gui.addButton(new NumberEditorFButton<>(gui, 1, 1, 10, () -> collectingPlayersPhaseCooldownMax
                , (v) -> {
            collectingPlayersPhaseCooldownMax = Math.max(1, Math.min(32, v));
            OptionManager.get().save(AbstractMOption.this);
        },
                () -> Configurations.getCollectingPlayersPhaseCooldownMaxItem(gui.getTargetPlayer()).setAmount(Math.max(1, Math.min(101, collectingPlayersPhaseCooldownMax))).build(),
                () -> new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                        "minioption.gui.collecting_players_phase_max_cooldown").toStringList(), null
        ));
        gui.addButton(new NumberEditorFButton<>(gui, 1, 1, 10, () -> endPhaseCooldownMax, (v) -> {
            endPhaseCooldownMax = Math.max(1, Math.min(32, v));
            OptionManager.get().save(AbstractMOption.this);
        },
                () -> Configurations.getEndPhaseCooldownMaxItem(gui.getTargetPlayer()).setAmount(Math.max(1, Math.min(101, endPhaseCooldownMax))).build(),
                () ->
                        new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                                "minioption.gui.end_phase_max_cooldown").toStringList(), null
        ));
        gui.addButton(new NumberEditorFButton<>(gui, 1, 1, 10, () -> preStartPhaseCooldownMax
                , (v) -> {
            preStartPhaseCooldownMax = Math.max(1, Math.min(32, v));
            OptionManager.get().save(AbstractMOption.this);
        },
                () -> Configurations.getPreStartPhaseCooldownMaxItem(gui.getTargetPlayer()).setAmount(Math.max(1, Math.min(101, preStartPhaseCooldownMax))).build(),
                () -> new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                        "minioption.gui.pre_start_phase_max_cooldown").toStringList(), null
        ));
        gui.addButton(new FButton(gui, () ->
                new ItemBuilder(Material.VEX_SPAWN_EGG).setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer())
                                .appendLangList("minioption.gui.allow_spectators", "%value%", String.valueOf(allowSpectators)))
                        .addEnchantment(Enchantment.DURABILITY, allowSpectators ? 1 : 0).build(), (event) -> {
            allowSpectators = !allowSpectators;
            OptionManager.get().save(AbstractMOption.this);
            return true;
        }
        ));
    }

    @Override
    public Gui getEditorGui(Player player, Gui parent) {
        PagedMapGui gui = craftEditor(player, parent);
        fillEditor(gui);
        return gui;
    }

    public PagedMapGui craftEditor(Player target, Gui parent) {
        return new PagedMapGui(
                new DMessage(Minigames.get(), target).appendLang("minioption.gui.title").toLegacy(),
                6, target, parent, Minigames.get());
    }

    public String[] getPlaceholders() {
        return new String[]{
                "%id%", getId(), "%type%", getClass().getSimpleName()
        };
    }
}
