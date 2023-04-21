package emanondev.minigames.games.eggwars;

import emanondev.core.ItemBuilder;
import emanondev.core.UtilsInventory;
import emanondev.core.UtilsString;
import emanondev.minigames.ArenaManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.OptionManager;
import emanondev.minigames.games.MArena;
import emanondev.minigames.games.MOption;
import emanondev.minigames.games.MType;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EggWarsType extends MType<EggWarsArena, EggWarsOption> {
    public EggWarsType() {
        super("eggwars", EggWarsArena.class, EggWarsOption.class, Minigames.get());
        ConfigurationSerialization.registerClass(EggWarsArena.class);
        ConfigurationSerialization.registerClass(EggWarsOption.class);
        ConfigurationSerialization.registerClass(EggWarsGame.class);
        reload();
    }

    @Override
    @NotNull
    public EggWarsArenaBuilder getArenaBuilder(@NotNull UUID uuid, @NotNull String id, @NotNull String label) {
        return new EggWarsArenaBuilder(uuid, id, label);
    }

    @Override
    public @NotNull EggWarsOption createDefaultOptions() {
        return new EggWarsOption();
    }

    @Override
    public @NotNull EggWarsGame createGame(@NotNull String arenaId, @NotNull String optionId) {
        MArena arena = ArenaManager.get().get(arenaId);
        if (arena == null || !this.matchType(arena))
            throw new IllegalStateException();
        MOption option = OptionManager.get().get(optionId);
        if (option == null || !this.matchType(option))
            throw new IllegalStateException();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("option", optionId);
        map.put("arena", arenaId);
        return new EggWarsGame(map);
    }

    @Override
    public @NotNull ItemBuilder getGameSelectorBaseItem() {
        return new ItemBuilder(getSection().getMaterial("display.gui.material", Material.BOW))
                .setGuiProperty().setCustomModelData(getSection()
                        .getInteger("display.gui.custommodel", null));
    }

    public double getSnowballPush() {
        return this.getSection().loadDouble("game.snowball_push", 0.5D);
    }

    public double getSnowballVerticalPush() {
        return this.getSection().loadDouble("game.snowball_vertical_push", 0.3D);
    }

    public ItemStack getKillRewardItem() { //TODO description
        return new ItemBuilder(Material.MAGMA_CREAM).setGuiProperty().addEnchantment(Enchantment.DURABILITY, 1).build();
    }

    public double getDefinitiveKillPoints() {
        return getSection().loadDouble("game.kill_points", 1D);
    }

    public double getWinPoints() {
        return getSection().loadDouble("game.win_points", 5D);
    }

    public int getDefinitiveKillExp() {
        return getSection().loadInteger("game.kill_exp", 0);
    }

    public int getWinExp() {
        return getSection().loadInteger("game.win_exp", 5);
    }

    public double getEggBrokenPoints() {
        return getSection().loadDouble("game.egg_broken_points", 3D);
    }

    public int getEggBrokenExp() {
        return getSection().loadInteger("game.egg_broken_exp", 5);
    }

    private final HashMap<String, EggWarsGeneratorType> generatorTypes = new HashMap<>();

    public void reload() {
        generatorTypes.clear();
        for (String key : getSection().getKeys("generators")) {
            try {
                EggWarsGeneratorType type = new EggWarsGeneratorType(key, getSection().loadSection("generators." + key));
                registerType(type);
            } catch (IllegalArgumentException e){
                Minigames.get().logIssue(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (EggWarsGeneratorType type : generatorTypes.values()) {
            type.validateUpgradeCosts(this);
        }
    }

    private void registerType(EggWarsGeneratorType type) {
        if (!UtilsString.isLowcasedValidID(type.getType()))
            throw new IllegalArgumentException("invalid id '" + type.getType() + "'");
        if (generatorTypes.containsKey(type.getType()))
            throw new IllegalArgumentException("duplicated id '" + type.getType() + "'");
        generatorTypes.put(type.getType(), type);
    }

    public @Nullable EggWarsGeneratorType getGenerator(ItemStack item) {
        if (UtilsInventory.isAirOrNull(item))
            return null;
        if (!item.hasItemMeta())
            return null;
        boolean found = false;
        for (EggWarsGeneratorType type : generatorTypes.values())
            if (type.getMaterial() == item.getType()) {
                found = true;
                break;
            }
        if (!found)
            return null;
        String val = item.getItemMeta().getPersistentDataContainer().get(EggWarsGeneratorType.DATA_KEY, PersistentDataType.STRING);
        return val == null ? null : getGenerator(val);
    }

    public @Nullable EggWarsGeneratorType getGenerator(String key) {
        return generatorTypes.get(key.toLowerCase(Locale.ENGLISH));
    }

    public Collection<EggWarsGeneratorType> getGenerators() {
        return generatorTypes.values();
    }
}
