package emanondev.minigames.games.eggwars;

import emanondev.core.ItemBuilder;
import emanondev.core.UtilsString;
import emanondev.core.YMLSection;
import emanondev.core.message.DMessage;
import emanondev.minigames.Minigames;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Range;

import java.util.HashMap;

public class EggWarsGeneratorType {
    public static final NamespacedKey DATA_KEY = new NamespacedKey(Minigames.get(), "eggwars_coin");
    private final String id;
    private final HashMap<Integer, Double> dropRateo5ticks = new HashMap<>();
    private final int maxLevel;
    private final HashMap<Integer, Integer> levelupPriceAmount = new HashMap<>();
    private final HashMap<Integer, EggWarsGeneratorType> levelupPriceType = new HashMap<>();
    private final Material material;
    private final Integer customModelData;
    private final String hexColor;
    private HashMap<Integer, String> levelupPriceTypeRaw = new HashMap<>();


    /*
    <id>:
        item:
            desc: ? on language
            material: material
            custommodeldata: =none

        levels:
            0:
                drop_amount: =1 (double)
                drop_frequency_ticks: =1 (int)
                capacity: =20
                upgrade_cost:
                    <id_x>: amount
                    <id_y>: amount
            1:
            2:
            3:


     */
    public EggWarsGeneratorType(String key, YMLSection section) throws IllegalStateException {
        if (!UtilsString.isLowcasedValidID(key))
            throw new IllegalStateException("Eggwars Generator has invalid id &e" + key + " &fon &e/types/eggwars.yml &f-> &egenerators:");
        this.id = key;
        this.material = section.loadMaterial("item.material", null);
        if (material == null || material.isAir() || material.isBlock()) {
            throw new IllegalStateException("Eggwars Generator &e" + id + " &fhas invalid material &e"
                    + material + " &fon &e/types/eggwars.yml &f-> &egenerators." + id + ".item.material: &fNote the item must be an unplaceable item");
        }
        this.customModelData = section.loadInteger("item.custom_model", null);
        int maxLevel = 0;
        for (String pathLevel : section.getKeys("levels")) {
            int level;
            try {
                level = Integer.parseInt(pathLevel);
            } catch (Exception e) {
                throw new IllegalStateException("Eggwars Generator &e" + id + " &fhas invalid level &e"
                        + pathLevel + " &fon &e/types/eggwars.yml &f-> &egenerators." + id + ".levels:");
            }
            if (level < 0)
                throw new IllegalStateException("Eggwars Generator &e" + id + " &fhas invalid level &e"
                        + level + " &fon &e/types/eggwars.yml &f-> &egenerators." + id + ".levels:");

            maxLevel = Math.max(maxLevel, level);
            YMLSection levelSection = section.loadSection("levels." + level);
            if (level != 0) {
                double drop_amount = levelSection.loadDouble("drop_amount", 1D);
                double drop_ticks = levelSection.loadInteger("drop_frequency_ticks", 20);
                if (drop_amount <= 0)
                    throw new IllegalStateException("Eggwars Generator &e" + id + " &fhas invalid drop amount &e"
                            + level + " &fon &e/types/eggwars.yml &f-> &egenerators." + id + ".levels." + level + ".drop_amount:");

                if (drop_ticks <= 0)
                    throw new IllegalStateException("Eggwars Generator &e" + id + " &fhas invalid drop frequency &e"
                            + level + " &fon &e/types/eggwars.yml &f-> &egenerators." + id + ".levels." + level + ".drop_frequency_ticks:");
                dropRateo5ticks.put(level, drop_amount * 5 / drop_ticks);
            }
            String upType = levelSection.getString("upgrade_cost_type", getType());
            int upCost = levelSection.getInt("upgrade_cost_amount", 1);
            if (upCost <= 0)
                throw new IllegalStateException("Eggwars Generator &e" + id + " &fhas invalid drop frequency &e"
                        + level + " &fon &e/types/eggwars.yml &f-> &egenerators." + id + ".levels." + level + ".upgrade_cost_amount:");

            levelupPriceAmount.put(level, upCost);
            levelupPriceTypeRaw.put(level, upType);
        }
        dropRateo5ticks.put(0, 0D);

        this.maxLevel = maxLevel;
        if (maxLevel == 0)
            throw new IllegalStateException("Eggwars Generator &e" + id + " &fhas no valid levels on &e/types/eggwars.yml &f-> &egenerators." + id + ".levels:");
        //first check

        for (int i = 0; i <= maxLevel; i++) {
            if (!dropRateo5ticks.containsKey(i))
                throw new IllegalStateException("Eggwars Generator &e" + id + " &fhas no valid drop rate settings for level &e" + i
                        + "&f on &e/types/eggwars.yml &f-> &egenerators." + id + ".levels:");
            if (i != maxLevel) {
                if (!levelupPriceAmount.containsKey(i))
                    throw new IllegalStateException("Eggwars Generator &e" + id + " &fhas no valid drop rate settings for level &e" + i
                            + "&f on &e/types/eggwars.yml &f-> &egenerators." + id + ".levels:");
            }
        }

        String hexColorT = section.getString("color", "aaaaaa"); //it's gray
        if (hexColorT.startsWith("#"))
            hexColor = hexColorT.substring(1);
        else
            hexColor = hexColorT;
        try {
            int value = Integer.parseInt(hexColor, 16);
            if (hexColor.length() != 6 || value >= 256 * 256 * 256 || value < 0)
                throw new IllegalStateException("Eggwars Generator &e" + id +
                        " &fhas no valid hex color on &e/types/eggwars.yml &f-> &egenerators." + id + ".color:");
        } catch (Exception e) {
            throw new IllegalStateException("Eggwars Generator &e" + id +
                    " &fhas no valid hex color on &e/types/eggwars.yml &f-> &egenerators." + id + ".color:");
        }
    }

    public String getType() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    void validateUpgradeCosts(EggWarsType mType) {
        for (Integer level : levelupPriceTypeRaw.keySet()) {
            EggWarsGeneratorType type = mType.getGenerator(levelupPriceTypeRaw.get(level));
            if (type == null) {
                type = this;
                Minigames.get().logIssue("Eggwars Generator &e" + id + " &fhas invalid cost type &e" + levelupPriceTypeRaw.get(level) +
                        " &fon &e/types/eggwars.yml &f-> &egenerators." + id + ".levels." + level + ".upgrade_cost_type:");
            }
            levelupPriceType.put(level, type);
        }
        levelupPriceTypeRaw = null; //trashed
    }

    public ItemStack getGeneratedItem(Player target, @Range(from = 1, to = Integer.MAX_VALUE) int amount) {
        return new ItemBuilder(material).setAmount(amount).addNamespacedKey(DATA_KEY, PersistentDataType.STRING, getType())
                .setGuiProperty().setCustomModelData(customModelData).setDescription(new DMessage(Minigames.get(), target)
                        .appendLang("eggwars.generators." + getType())).build();
    }

    public boolean canLevelUp(int currentLevel) {
        return currentLevel < maxLevel;
    }

    public Integer getLevelUpCostsAmount(int currentLevel) {
        if (currentLevel < 0 || currentLevel >= maxLevel)
            throw new IllegalArgumentException();
        return levelupPriceAmount.get(currentLevel);
    }

    public EggWarsGeneratorType getLevelUpCostsType(int currentLevel) {
        if (currentLevel < 0 || currentLevel >= maxLevel)
            throw new IllegalArgumentException();
        return levelupPriceType.get(currentLevel);
    }

    public double getDropSum5Ticks(int currentLevel) {
        if (currentLevel < 0 || currentLevel > maxLevel)
            throw new IllegalArgumentException();
        return dropRateo5ticks.get(currentLevel);
    }

    public String miniColor() {
        return "<#" + hexColor() + ">";
    }

    public String hexColor() {
        return hexColor;
    }

    public String miniColorEnd() {
        return "</#" + hexColor() + ">";
    }
}
