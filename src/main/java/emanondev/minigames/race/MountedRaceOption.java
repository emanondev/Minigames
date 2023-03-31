package emanondev.minigames.race;

import emanondev.core.ItemBuilder;
import emanondev.core.UtilsString;
import emanondev.core.gui.DoubleEditorFButton;
import emanondev.core.gui.Gui;
import emanondev.core.gui.ResearchFButton;
import emanondev.core.message.DMessage;
import emanondev.minigames.Minigames;
import emanondev.minigames.OptionManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class MountedRaceOption extends ARaceOption {

    private EntityType type;
    private double baseSpeed;
    private Boat.Type boatType;
    private double jumpStrenght;
    private Horse.Color horseColor;
    private Horse.Style horseStyle;

    public MountedRaceOption(@NotNull Map<String, Object> map) {
        super(map);
        type = map.containsKey("entity_type") ? EntityType.valueOf((String) map.get("entity_type")) : null;
        boatType = map.containsKey("boat_type") ? Boat.Type.valueOf((String) map.get("boat_type")) : null;
        baseSpeed = (double) map.getOrDefault("speed", 0.12);
        horseColor = map.containsKey("horse_color") ? Horse.Color.valueOf((String) map.get("horse_color")) : null;
        horseStyle = map.containsKey("horse_style") ? Horse.Style.valueOf((String) map.get("horse_style")) : null;
        jumpStrenght = (double) map.getOrDefault("jump_strenght", 0.7);
    }

    public Entity spawnRide(Location loc) {
        return loc.getWorld().spawn(loc, (type == null ? EntityType.HORSE : type).getEntityClass(), false, (en) -> {
            if (en instanceof Horse horse) {
                horse.setColor(horseColor == null ? Horse.Color.values()[(int) (Math.random() * Horse.Color.values().length)] : horseColor);
                horse.setStyle(horseStyle == null ? Horse.Style.values()[(int) (Math.random() * Horse.Style.values().length)] : horseStyle);
            }
            if (en instanceof AbstractHorse horse) {
                horse.setJumpStrength(jumpStrenght);
                horse.setDomestication(horse.getMaxDomestication());
                horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
            }
            if (en instanceof Boat boat)
                boat.setBoatType(boatType == null ? Boat.Type.values()[(int) (Math.random() * Boat.Type.values().length)] : boatType);
            if (en instanceof Steerable steerable)
                steerable.setSaddle(true);
            if (en instanceof LivingEntity living) {
                living.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(baseSpeed);
                living.setCollidable(false);
            }
            if (en instanceof Mob mob)
                mob.setAware(false);
            if (en instanceof Tameable tm)
                tm.setTamed(true);
        });
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        @NotNull Map<String, Object> map = super.serialize();
        if (type != null)
            map.put("entity_type", type.name());
        if (horseColor != null)
            map.put("horse_color", horseColor.name());
        if (horseStyle != null)
            map.put("horse_style", horseStyle.name());
        map.put("speed", baseSpeed);
        if (boatType != null)
            map.put("boat_type", boatType.name());
        map.put("jump_strenght", jumpStrenght);
        return map;
    }


    public @NotNull EntityType getType() {
        return type == null ? getDefaultType() : type;
    }

    public void setType(@Nullable EntityType type) {
        if (type != null && !isAllowedType(type))
            throw new IllegalStateException();
        this.type = type;
        OptionManager.get().save(this);
    }

    public @Nullable Horse.Color getHorseColor() {
        return horseColor;
    }

    public void setHorseColor(@Nullable Horse.Color horseColor) {
        this.horseColor = horseColor;
        OptionManager.get().save(this);
    }

    public @Nullable Horse.Style getHorseStyle() {
        return horseStyle;
    }

    public void setHorseStyle(@Nullable Horse.Style horseStyle) {
        this.horseStyle = horseStyle;
        OptionManager.get().save(this);
    }

    public double getBaseSpeed() {
        return baseSpeed;
    }

    public void setBaseSpeed(double baseSpeed) {
        this.baseSpeed = Math.max(0.01, baseSpeed);
        OptionManager.get().save(this);
    }

    public @Nullable Boat.Type getBoatType() {
        return boatType;
    }

    public void setBoatType(@Nullable Boat.Type boatType) {
        this.boatType = boatType;
        OptionManager.get().save(this);
    }

    public double getJumpStrenght() {
        return jumpStrenght;
    }

    public void setJumpStrenght(double jumpStrenght) {
        this.jumpStrenght = Math.max(0.01, jumpStrenght);
        OptionManager.get().save(this);
    }

    protected Set<EntityType> getAllowedTypes() {
        SortedSet<EntityType> allowed = new TreeSet<>(Comparator.comparing(Enum::name));
        for (EntityType type : EntityType.values())
            if (isAllowedType(type))
                allowed.add(type);
        return allowed;
    }

    @Override
    public Gui getEditorGui(Player target, Gui parent) {
        Gui gui = super.getEditorGui(target, parent);
        Set<EntityType> allowed = getAllowedTypes();

        gui.addButton(new ResearchFButton<>(gui,
                () -> new ItemBuilder(getEntityTypeMaterial(getType())).setGuiProperty().setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                        "minioption.gui.entitytype_selector", "%selected%", getType().name().toLowerCase(Locale.ENGLISH)
                )).build(),
                (String base, EntityType type) -> type.name().toLowerCase(Locale.ENGLISH).contains(base.toLowerCase(Locale.ENGLISH)),
                (InventoryClickEvent event, EntityType type) -> {
                    setType(type);
                    gui.open(event.getWhoClicked());
                    return true;
                },
                (EntityType type) -> new ItemBuilder(getEntityTypeMaterial(type)).setGuiProperty().addEnchantment(Enchantment.DURABILITY,
                        type == getType() ? 1 : 0).setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                        "minioption.gui.entitytype_description", "%type%", type.name().toLowerCase(Locale.ENGLISH)
                )).build(),
                () -> allowed));
        gui.addButton(new DoubleEditorFButton(gui, 0.1, 0.01, 10, this::getBaseSpeed
                , this::setBaseSpeed,
                () -> new ItemBuilder(Material.LEATHER_BOOTS).setGuiProperty()
                        .setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                                "minioption.gui.mounted_basespeed", "%value%",
                                UtilsString.formatOptional2Digit(getBaseSpeed()))).build()));
        return gui;
    }

    protected abstract @NotNull EntityType getDefaultType();

    protected abstract boolean isAllowedType(@NotNull EntityType type);

    private static @NotNull Material getEntityTypeMaterial(@NotNull EntityType type) {
        return switch (type) {
            case BOAT -> Material.OAK_BOAT;
            case CHEST_BOAT -> Material.OAK_CHEST_BOAT;
            case PIG -> Material.CARROT_ON_A_STICK;
            case STRIDER -> Material.WARPED_FUNGUS_ON_A_STICK;
            case MINECART -> Material.MINECART;
            case CAMEL -> Material.CACTUS;
            case SKELETON_HORSE -> Material.BONE;
            case ZOMBIE_HORSE -> Material.ROTTEN_FLESH;
            case MULE, DONKEY -> Material.CARROT;
            default -> Material.SADDLE;
        };

    }
}
