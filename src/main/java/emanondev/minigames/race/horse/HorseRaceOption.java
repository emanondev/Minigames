package emanondev.minigames.race.horse;

import emanondev.core.ItemBuilder;
import emanondev.core.UtilsString;
import emanondev.core.gui.DoubleEditorFButton;
import emanondev.core.gui.FWrapperButton;
import emanondev.core.gui.Gui;
import emanondev.core.gui.ResearchFButton;
import emanondev.core.message.DMessage;
import emanondev.minigames.Minigames;
import emanondev.minigames.race.MountedRaceOption;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


@SerializableAs(value="HorseRaceOption")
public class HorseRaceOption extends MountedRaceOption {

    public HorseRaceOption() {
        this(new HashMap<>());
    }

    public HorseRaceOption(@NotNull Map<String, Object> map) {
        super(map);
    }

    @Override
    protected @NotNull EntityType getDefaultType() {
        return EntityType.HORSE;
    }

    @Override
    protected boolean isAllowedType(@NotNull EntityType type) {
        return type.getEntityClass() != null && AbstractHorse.class.isAssignableFrom(type.getEntityClass()) && !type.name().contains("LLAMA");
    }


    @Override
    public Gui getEditorGui(Player target, Gui parent) {
        Gui gui = super.getEditorGui(target, parent);
        gui.addButton(new FWrapperButton(gui, new DoubleEditorFButton(gui, 0.1, 0.01, 10, this::getJumpStrenght
                , this::setJumpStrenght,
                () -> new ItemBuilder(Material.RABBIT_FOOT).setGuiProperty()
                        .setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                                "minioption.gui.mounted_jumpstrenght", "%value%",
                                UtilsString.formatOptional2Digit(getJumpStrenght()))).build()),
                (e) -> getType().getEntityClass() == null || !AbstractHorse.class.isAssignableFrom(getType().getEntityClass()),
                () -> getType().getEntityClass() == null || !AbstractHorse.class.isAssignableFrom(getType().getEntityClass()), (e) -> false, () -> null));
        gui.addButton(new FWrapperButton(gui, new ResearchFButton<>(gui,
                () -> new ItemBuilder(Material.BROWN_DYE).setGuiProperty().setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                        "minioption.gui.horse_color_selector", "%selected%", getHorseColor() == null ? "-random-" : getHorseColor().name().toLowerCase(Locale.ENGLISH)
                )).build(),
                (String base, Horse.Color color) -> color.name().toLowerCase(Locale.ENGLISH).contains(base.toLowerCase(Locale.ENGLISH)),
                (InventoryClickEvent event, Horse.Color color) -> {
                    setHorseColor(getHorseColor() == color ? null : color);
                    gui.open(event.getWhoClicked());
                    return true;
                },
                (Horse.Color color) -> new ItemBuilder(Material.MOJANG_BANNER_PATTERN).setGuiProperty().addEnchantment(Enchantment.DURABILITY,
                        color == getHorseColor() ? 1 : 0).setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                        "minioption.gui.horse_color_description", "%color%", color.name().toLowerCase(Locale.ENGLISH)
                )).build(),
                () -> List.of(Horse.Color.values())), (e) -> getType() != EntityType.HORSE, () -> getType() != EntityType.HORSE, (e) -> false, () -> null));
        gui.addButton(new FWrapperButton(gui, new ResearchFButton<>(gui,
                () -> new ItemBuilder(Material.SADDLE).setGuiProperty().setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                        "minioption.gui.horse_style_selector", "%selected%", getHorseStyle() == null ? "-random-" : getHorseStyle().name().toLowerCase(Locale.ENGLISH)
                )).build(),
                (String base, Horse.Style style) -> style.name().toLowerCase(Locale.ENGLISH).contains(base.toLowerCase(Locale.ENGLISH)),
                (InventoryClickEvent event, Horse.Style style) -> {
                    setHorseStyle(getHorseStyle() == style ? null : style);
                    gui.open(event.getWhoClicked());
                    return true;
                },
                (Horse.Style style) -> new ItemBuilder(Material.MOJANG_BANNER_PATTERN).setGuiProperty().addEnchantment(Enchantment.DURABILITY,
                        style == getHorseStyle() ? 1 : 0).setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                        "minioption.gui.horse_style_description", "%style%", style.name().toLowerCase(Locale.ENGLISH)
                )).build(),
                () -> List.of(Horse.Style.values())), (e) -> getType() != EntityType.HORSE, () -> getType() != EntityType.HORSE, (e) -> false, () -> null));
        return gui;
    }

}
