package emanondev.minigames.race.boat;

import emanondev.core.ItemBuilder;
import emanondev.core.gui.FWrapperButton;
import emanondev.core.gui.Gui;
import emanondev.core.gui.ResearchFButton;
import emanondev.core.message.DMessage;
import emanondev.minigames.Minigames;
import emanondev.minigames.race.MountedRaceOption;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SerializableAs(value="BoatRaceOption")
public class BoatRaceOption extends MountedRaceOption {


    public BoatRaceOption() {
        this(new HashMap<>());
    }

    public BoatRaceOption(@NotNull Map<String, Object> map) {
        super(map);
    }


    @Override
    protected @NotNull EntityType getDefaultType() {
        return EntityType.BOAT;
    }

    @Override
    protected boolean isAllowedType(@NotNull EntityType type) {
        return type.getEntityClass() != null && Boat.class.isAssignableFrom(type.getEntityClass());
    }

    @Override
    public Gui getEditorGui(Player target, Gui parent) {
        Gui gui = super.getEditorGui(target, parent);
        gui.addButton(new FWrapperButton(gui, new ResearchFButton<>(gui,
                () -> new ItemBuilder(getBoatTypeMaterial(getBoatType())).setGuiProperty().setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                        "minioption.gui.boat_type_selector", "%selected%", getBoatType() == null ? "-random-" : getBoatType().name().toLowerCase(Locale.ENGLISH)
                )).build(),
                (String base, Boat.Type type) -> type.name().toLowerCase(Locale.ENGLISH).contains(base.toLowerCase(Locale.ENGLISH)),
                (InventoryClickEvent event, Boat.Type type) -> {
                    setBoatType(getBoatType() == type ? null : type);
                    gui.open(event.getWhoClicked());
                    return true;
                },
                (Boat.Type type) -> new ItemBuilder(getBoatTypeMaterial(type)).setGuiProperty().addEnchantment(Enchantment.DURABILITY,
                        type == getBoatType() ? 1 : 0).setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                        "minioption.gui.boat_type_description", "%type%", type.name().toLowerCase(Locale.ENGLISH)
                )).build(),
                () -> List.of(Boat.Type.values())),
                (e) -> getType().getEntityClass() == null || !Boat.class.isAssignableFrom(getType().getEntityClass()),
                () -> getType().getEntityClass() == null || !Boat.class.isAssignableFrom(getType().getEntityClass()),
                (e) -> false, () -> null));
        return gui;
    }

    private static @NotNull Material getBoatTypeMaterial(@Nullable Boat.Type type) {
        if (type == null)
            return Material.OAK_BOAT;
        try {
            return Material.valueOf(type + "_BOAT");
        } catch (Throwable t) {
            return Material.OAK_BOAT;
        }

    }
}
