package emanondev.minigames.race;

import emanondev.core.ItemBuilder;
import emanondev.core.UtilsString;
import emanondev.core.gui.DoubleEditorFButton;
import emanondev.core.gui.FWrapperButton;
import emanondev.core.gui.PagedMapGui;
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

public class MountedRaceOption extends ARaceOption {

    private EntityType type;
    private Horse.Color horseColor;
    private Horse.Style horseStyle;
    //private boolean baby; //future custom wasd control
    private double baseSpeed;
    private Boat.Type boatType;
    private double jumpStrenght;
    //chested
    //armoured (type)

    public MountedRaceOption() {
        this(new HashMap<>());
    }

    public MountedRaceOption(@NotNull Map<String, Object> map) {
        super(map);
        type = map.containsKey("entity_type")?EntityType.valueOf((String) map.get("entity_type")):null;
        horseColor = map.containsKey("horse_color")?Horse.Color.valueOf((String) map.get("horse_color")):null;
        horseStyle = map.containsKey("horse_style")?Horse.Style.valueOf((String) map.get("horse_style")):null;
        boatType = map.containsKey("boat_type")?Boat.Type.valueOf((String) map.get("boat_type")):null;
        //baby = (boolean) map.getOrDefault("baby",false);
        baseSpeed = (double) map.getOrDefault("speed",0.12);
        jumpStrenght = (double) map.getOrDefault("jump_strenght",0.7);
    }

    public Entity spawnRide(Location loc) {
        return loc.getWorld().spawn(loc, (type == null ? EntityType.HORSE : type).getEntityClass(), false,(en)-> {
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
            /*if (en instanceof Ageable ageable && baby)
                ageable.setBaby();*/
            if (en instanceof LivingEntity living)
                living.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(baseSpeed);
            if (en instanceof Mob mob)
                mob.setAware(false);
            if (en instanceof Tameable tm)
                tm.setTamed(true);
        });
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        @NotNull Map<String, Object> map = super.serialize();
        map.put("entity_type",type==null?null: type.name());
        map.put("horse_color", horseColor==null?null:horseColor.name());
        map.put("horse_style",horseStyle==null?null: horseStyle.name());
        //map.put("baby", baby);
        map.put("speed", baseSpeed);
        map.put("boat_type",boatType==null?null: boatType.name());
        map.put("jump_strenght", jumpStrenght);
        return map;
    }


    public @NotNull EntityType getType() {
        return type == null ? EntityType.HORSE : type;
    }

    public void setType(@Nullable EntityType type) {
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
/*
    public boolean isBaby() {
        return baby;
    }

    public void setBaby(boolean baby) {
        this.baby = baby;
        OptionManager.get().save(this);
    }*/

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

    protected void fillEditor(@NotNull PagedMapGui gui) {
        super.fillEditor(gui);

        List<EntityType> allowed = new ArrayList<>();
        for (EntityType type : EntityType.values()) //TODO add more with custom spawned controllable mobs
            if (type.getEntityClass() != null && Vehicle.class.isAssignableFrom(type.getEntityClass()))
                if (!type.name().contains("MINECART_") && !type.name().contains("LLAMA"))
                    allowed.add(type);
        allowed.sort(Comparator.comparing(Enum::name));

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
        gui.addButton(new DoubleEditorFButton(gui, 1, 0.01, 10, this::getBaseSpeed
                , this::setBaseSpeed,
                () -> new ItemBuilder(Material.LEATHER_BOOTS).setGuiProperty()
                        .setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                                "minioption.gui.mounted_basespeed", "%value%",
                                UtilsString.formatOptional2Digit(getBaseSpeed()))).build()));
        gui.addButton(new FWrapperButton(gui, new DoubleEditorFButton(gui, 1, 0.01, 10, this::getJumpStrenght
                , this::setJumpStrenght,
                () -> new ItemBuilder(Material.RABBIT_FOOT).setGuiProperty()
                        .setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                                "minioption.gui.mounted_jumpstrenght", "%value%",
                                UtilsString.formatOptional2Digit(getJumpStrenght()))).build()),
                (e) -> getType().getEntityClass()==null||!AbstractHorse.class.isAssignableFrom(getType().getEntityClass()),
                () -> getType().getEntityClass()==null||!AbstractHorse.class.isAssignableFrom(getType().getEntityClass()), (e) -> false, () -> null));


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
                (e) -> getType().getEntityClass()==null||!Boat.class.isAssignableFrom(getType().getEntityClass()),
                () -> getType().getEntityClass()==null||!Boat.class.isAssignableFrom(getType().getEntityClass()),
                (e) -> false, () -> null));


        //TODO future custom wasd control
        /*gui.addButton(new FButton(gui, () ->
                new ItemBuilder(Material.VEX_SPAWN_EGG).setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer())
                                .appendLangList("minioption.gui.allow_spectators", "%value%", String.valueOf(isBaby())))
                        .setGuiProperty().addEnchantment(Enchantment.DURABILITY, isBaby() ? 1 : 0).build(), (event) -> {
            setBaby(!isBaby());
            return true;
        }));*/

    }

    private static Material getBoatTypeMaterial(Boat.Type type){
        if (type==null)
            return Material.OAK_BOAT;
        try{
            return Material.valueOf(type+"_BOAT");
        }catch (Throwable t){
            return Material.OAK_BOAT;
        }

    }
    private static Material getEntityTypeMaterial(EntityType type){
        return switch (type){
            case BOAT -> Material.OAK_BOAT;
            case CHEST_BOAT -> Material.OAK_CHEST_BOAT;
            case PIG -> Material.CARROT_ON_A_STICK;
            case STRIDER -> Material.WARPED_FUNGUS_ON_A_STICK;
            case CAMEL -> Material.CACTUS;
            default -> Material.SADDLE;
        };

    }
}
