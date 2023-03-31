package emanondev.minigames.race;

import emanondev.core.ItemBuilder;
import emanondev.core.gui.Gui;
import emanondev.core.gui.LongEditorFButton;
import emanondev.core.message.DMessage;
import emanondev.minigames.ArenaManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.generic.AbstractMColorSchemArena;
import emanondev.minigames.locations.LocationOffset3D;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SerializableAs("RaceArena")
public class RaceArena extends AbstractMColorSchemArena {
    private final Map<DyeColor, LocationOffset3D> spawnLocations = new EnumMap<>(DyeColor.class);
    private final List<BoundingBox> checkpoints = new ArrayList<>();
    private final List<LocationOffset3D> checkpointsRespawn = new ArrayList<>();
    private final BoundingBox finishArea;
    private final List<BoundingBox> fallAreas = new ArrayList<>();

    public int getRewardFirst() {
        return rewardFirst;
    }

    public void setRewardFirst(int rewardFirst) {
        this.rewardFirst = Math.max(0, rewardFirst);
        ArenaManager.get().save(this);
    }

    public int getRewardSecond() {
        return rewardSecond;
    }

    public void setRewardSecond(int rewardSecond) {
        this.rewardSecond = Math.max(0, rewardSecond);
        ArenaManager.get().save(this);
    }

    public int getRewardThird() {
        return rewardThird;
    }

    public void setRewardThird(int rewardThird) {
        this.rewardThird = Math.max(0, rewardThird);
        ArenaManager.get().save(this);
    }

    private int rewardFirst;
    private int rewardSecond;
    private int rewardThird;

    public RaceArena(@NotNull Map<String, Object> map) {
        super(map);
        Map<String, ?> teamMap = (Map<String, ?>) map.get("teams");
        teamMap.forEach((k, v) -> spawnLocations.put(DyeColor.valueOf(k), LocationOffset3D.fromString((String) ((Map<String, ?>) v).get("spawnOffset"))));
        List<BoundingBox> checkpoints = (List<BoundingBox>) map.get("checkpoints");
        if (checkpoints != null)
            this.checkpoints.addAll(checkpoints);
        for (String loc : (List<String>) map.getOrDefault("checkpoints_respawn", Collections.emptyList())) {
            this.checkpointsRespawn.add(LocationOffset3D.fromString(loc));
        }
        this.finishArea = (BoundingBox) map.get("end_area");
        if (finishArea == null)
            throw new IllegalStateException();
        List<BoundingBox> fallAreas = (List<BoundingBox>) map.get("fall_areas");
        if (fallAreas != null)
            this.fallAreas.addAll(fallAreas);
        if (checkpoints.size() != checkpointsRespawn.size())
            throw new IllegalArgumentException();

        rewardFirst = Math.max(0, (Integer) map.getOrDefault("reward_first", 10));
        rewardSecond = Math.max(0, (Integer) map.getOrDefault("reward_second", 5));
        rewardThird = Math.max(0, (Integer) map.getOrDefault("reward_third", 3));
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        LinkedHashMap<String, Object> teams = new LinkedHashMap<>();
        map.put("teams", teams);
        for (DyeColor color : spawnLocations.keySet()) {
            Map<String, Object> teamInfo = new HashMap<>();
            teamInfo.put("spawnOffset", spawnLocations.get(color).toString());
            teams.put(color.name(), teamInfo);
        }
        map.put("checkpoints", checkpoints);
        ArrayList<String> raw = new ArrayList<>();
        checkpointsRespawn.forEach((loc) -> raw.add(loc.toString()));
        map.put("checkpoints_respawn", raw);
        map.put("end_area", finishArea);
        map.put("fall_areas", fallAreas);
        map.put("reward_first", rewardFirst);
        map.put("reward_second", rewardSecond);
        map.put("reward_third", rewardThird);
        return map;
    }

    @NotNull
    public Set<DyeColor> getColors() {
        return Collections.unmodifiableSet(spawnLocations.keySet());
    }

    @NotNull
    public LocationOffset3D getSpawnOffset(@NotNull DyeColor color) {
        if (!spawnLocations.containsKey(color))
            throw new NullPointerException();
        return spawnLocations.get(color);
    }

    @Contract(" -> new")
    public List<BoundingBox> getCheckpoints() {
        List<BoundingBox> list = new ArrayList<>();
        for (BoundingBox box : checkpoints)
            list.add(box.clone());
        return list;
    }

    @Contract(" -> new")
    public List<BoundingBox> getFallAreas() {
        List<BoundingBox> list = new ArrayList<>();
        for (BoundingBox box : fallAreas)
            list.add(box.clone());
        return list;
    }

    @Contract(" -> new")
    public BoundingBox getFinishArea() {
        return finishArea.clone();
    }

    @Contract(" -> new")
    public List<LocationOffset3D> getCheckpointsRespawn() {
        return checkpointsRespawn;
    }

    @Override
    public Gui getEditorGui(Player player) {
        Gui gui = super.getEditorGui(player);
        gui.addButton(new LongEditorFButton(gui, 1, 1, 10000,
                () -> (long) getRewardFirst(),
                (v) -> setRewardFirst(v.intValue()),
                () -> new ItemBuilder(Material.GOLD_INGOT).setGuiProperty().setAmount(Math.max(1, Math.min(101, getMaxDurationEstimation())))
                        .setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                                "miniarena.gui.reward_first", "%value%",
                                String.valueOf(getRewardFirst()))).build()));
        gui.addButton(new LongEditorFButton(gui, 1, 1, 10000,
                () -> (long) getRewardSecond(),
                (v) -> setRewardSecond(v.intValue()),
                () -> new ItemBuilder(Material.IRON_INGOT).setGuiProperty().setAmount(Math.max(1, Math.min(101, getMaxDurationEstimation())))
                        .setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                                "miniarena.gui.reward_second", "%value%",
                                String.valueOf(getRewardSecond()))).build()));
        gui.addButton(new LongEditorFButton(gui, 1, 1, 10000,
                () -> (long) getRewardThird(),
                (v) -> setRewardThird(v.intValue()),
                () -> new ItemBuilder(Material.COPPER_INGOT).setGuiProperty().setAmount(Math.max(1, Math.min(101, getMaxDurationEstimation())))
                        .setDescription(new DMessage(Minigames.get(), gui.getTargetPlayer()).appendLangList(
                                "miniarena.gui.reward_third", "%value%",
                                String.valueOf(getRewardThird()))).build()));
        return gui;
    }
}
