package emanondev.minigames.minigames.skywars;

import emanondev.core.ItemBuilder;
import emanondev.core.gui.FButton;
import emanondev.core.gui.NumberEditorFButton;
import emanondev.core.gui.PagedMapGui;
import emanondev.minigames.minigames.Minigames;
import emanondev.minigames.minigames.OptionManager;
import emanondev.minigames.minigames.generic.AbstractMOption;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SkyWarsOption extends AbstractMOption {

    private boolean allowSpectators;
    private int perTeamMaxPlayers;

    public int getTeamMaxSize() {
        return perTeamMaxPlayers;
    }

    public SkyWarsOption() {
        this(new HashMap<>());
    }

    public SkyWarsOption(@NotNull Map<String, Object> map) {
        super(map);
        allowSpectators = (boolean) map.getOrDefault("allowSpectators", true);
        perTeamMaxPlayers = Math.max(1, (int) map.getOrDefault("maxPlayersPerTeam", 1));
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("allowSpectators", allowSpectators);
        map.put("maxPlayersPerTeam", perTeamMaxPlayers);
        return map;
    }

    @Override
    public void openEditor(Player who) {
        PagedMapGui gui = craftEditor(who);
        fillEditor(gui);
        gui.open(who);
    }

    protected void fillEditor(PagedMapGui gui){
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
        gui.addButton(new FButton(gui, () -> new ItemBuilder(Material.VEX_SPAWN_EGG).addEnchantment(Enchantment.DURABILITY, allowSpectators ? 1 : 0).setGuiProperty().setDescription(
                Minigames.get().getLanguageConfig(gui.getTargetPlayer()).loadStringList(
                        "minioption.buttons.allowspectators", new ArrayList<>()), true
                , "%value%", String.valueOf(allowSpectators)
        ).build(), (event) -> {
            allowSpectators = !allowSpectators;
            OptionManager.get().save(SkyWarsOption.this);
            return true;
        }
        ));
    }

    @Override
    public PagedMapGui craftEditor(Player target){
        return new PagedMapGui(Minigames.get().getLanguageConfig(target).loadMessage(
                "skywars.option_gui_title", ""), 6, target, null, Minigames.get());
    }

}
