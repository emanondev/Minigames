package emanondev.minigames;

import emanondev.core.MessageBuilder;
import emanondev.core.UtilsCommand;
import emanondev.core.UtilsString;
import emanondev.core.YMLConfig;
import emanondev.minigames.generic.MArena;
import emanondev.minigames.generic.MArenaBuilder;
import emanondev.minigames.generic.MType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class ArenaManager {

    private static ArenaManager instance;

    public static ArenaManager get() {
        return instance;
    }


    public @NotNull File getArenasFolder() {
        return new File(Minigames.get().getDataFolder(), "arenas");
    }

    public @NotNull YMLConfig getArenaConfig(String fileName) {
        return Minigames.get().getConfig("arenas" + File.separator + fileName);
    }


    private final HashMap<UUID, MArenaBuilder> builders = new HashMap<>();
    private final HashMap<String, MArena> arenas = new HashMap<>();
    private final HashMap<String, YMLConfig> arenasFile = new HashMap<>();

    public ArenaManager() {
        instance = this;
    }

    public void reload() {
        arenas.clear();
        arenasFile.clear();
        File arenasFolder = getArenasFolder();
        if (arenasFolder.isDirectory()) {
            File[] files = arenasFolder.listFiles((f, n) -> (n.endsWith(".yml")));
            if (files != null)
                for (File file : files) {
                    YMLConfig config = getArenaConfig(file.getName());
                    for (String key : config.getKeys(false)) {
                        try {
                            if (!UtilsString.isLowcasedValidID(key))
                                throw new IllegalStateException("invalid id");
                            Object value = config.get(key);
                            if (value instanceof MArena arena)
                                registerArena(key, arena, config);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        }
    }

    public @Nullable MArena getArena(@NotNull String id) {
        return arenas.get(id.toLowerCase());
    }

    public @NotNull Map<String, MArena> getArenas() {
        return Collections.unmodifiableMap(arenas);
    }


    private void registerArena(@NotNull String id, @NotNull MArena game, @NotNull OfflinePlayer player) {
        registerArena(id, game, getArenaConfig(player.getName()));
    }

    public void registerArena(@NotNull String id, @NotNull MArena arena, @NotNull YMLConfig config) {
        if (!UtilsString.isLowcasedValidID(id) || arenas.containsValue(id))
            throw new IllegalStateException("invalid id");
        if (arena.isRegistered())
            throw new IllegalStateException();
        arena.setRegistered(id);
        arenas.put(id, arena);
        arenasFile.put(id, config);
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "D Registered Arena &e" + id);
    }

    public void save(@NotNull MArena mArena) {
        if (!mArena.isRegistered())
            throw new IllegalStateException();
        if (arenas.get(mArena.getId()) != mArena)
            throw new IllegalStateException();
        arenasFile.get(mArena.getId()).set(mArena.getId(), mArena);
        arenasFile.get(mArena.getId()).save();
        Minigames.get().logTetraStar(ChatColor.DARK_RED, "D Updated Arena &e" + mArena.getId());
    }

    public <A extends MArena> @NotNull Map<String, A> getCompatibleArenas(@NotNull MType<A, ?> type) {
        HashMap<String, A> map = new HashMap<>();
        arenas.forEach((k, v) -> {
            if (type.matchType(v)) map.put(k, (A) v);
        });
        return map;
    }

    private void registerBuilder(@NotNull UUID who, @NotNull String id, @NotNull MType<?, ?> type) {
        if (!UtilsString.isLowcasedValidID(id)) {
            //TODO
            return;
        }
        if (getArena(id) != null) {
            //TODO
            return;
        }
        if (builders.containsKey(who)) {
            //TODO
            return;
        }
        builders.put(who, type.getArenaBuilder(who, id));
    }

    private void unregisterBuilder(@NotNull UUID who) {
        builders.remove(who);
    }

    public void onArenaBuilderExecute(@NotNull Player who, @NotNull String label, @NotNull String[] args) {
        //TODO args.length==0
        if (!builders.containsKey(who.getUniqueId())) {
            if (args[0].equalsIgnoreCase("create")) {
                if (args.length != 3) {
                    //arguments
                    new MessageBuilder(Minigames.get(), who)
                            .addTextTranslation("generic.arenabuilder.error.arguments_amount", "",
                                    "%label%", label).send();
                    return;
                }
                MType type = MinigameTypes.get().getType(args[1]);
                if (type == null) {
                    new MessageBuilder(Minigames.get(), who)
                            .addTextTranslation("generic.arenabuilder.error.invalid_type", "",
                                    "%type%", args[1]).send();
                    return;
                }
                String id = args[2].toLowerCase();
                if (!UtilsString.isLowcasedValidID(id)) {
                    new MessageBuilder(Minigames.get(), who)
                            .addTextTranslation("generic.arenabuilder.error.invalid_id", "",
                                    "%id%", args[2]).send();
                    return;
                }
                if (getArena(id) != null) {
                    new MessageBuilder(Minigames.get(), who)
                            .addTextTranslation("generic.arenabuilder.error.already_used_id", "",
                                    "%id%", args[2]).send();
                    return;
                }

                registerBuilder(who.getUniqueId(), id, type);
                new MessageBuilder(Minigames.get(), who)
                        .addTextTranslation("generic.arenabuilder.success.started", "").send();
                return;
            }
            if (args[0].equalsIgnoreCase("abort")) {
                new MessageBuilder(Minigames.get(), who)
                        .addTextTranslation("generic.arenabuilder.error.create_already_started", "").send();
                return;
            }

            new MessageBuilder(Minigames.get(), who)
                    .addTextTranslation("generic.arenabuilder.error.pre_create_invalid_action", "", "%label%", label).send();
            return;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("abort")) {
            builders.get(who.getUniqueId()).abort();
            unregisterBuilder(who.getUniqueId());
            new MessageBuilder(Minigames.get(), who)
                    .addTextTranslation("generic.arenabuilder.success.aborted", "").send();
            return;
        }
        if (args[0].equalsIgnoreCase("create")) {
            new MessageBuilder(Minigames.get(), who)
                    .addTextTranslation("generic.arenabuilder.error.create_already_started", "").send();
            return;
        }
        builders.get(who.getUniqueId()).handleCommand(who, args);
    }

    public List<String> onArenaBuilderComplete(@NotNull Player who, @NotNull String label, @NotNull String[] args) {
        if (!builders.containsKey(who.getUniqueId()))
            return switch (args.length) {
                case 1 -> UtilsCommand.complete(args[0], List.of("create"));
                case 2 -> switch (args[0].toLowerCase()) {
                    case "create" -> UtilsCommand.complete(args[1], MinigameTypes.get().getTypes(), (t) -> t.getType(), (t) -> true);
                    //case "edit" -> UtilsCommand.complete(args[1], MinigameManager.get().getOptions().keySet());
                    default -> Collections.emptyList();
                };
                default -> Collections.emptyList();
            };
        return builders.get(who.getUniqueId()).handleComplete(args);
    }

    public void onArenaBuilderCompletedArena(@NotNull MArenaBuilder builder) {
        MArena arena = builder.build();
        registerArena(builder.getId(), arena, Bukkit.getOfflinePlayer(builder.getUser()));
        save(arena);
        builders.remove(builder.getUser());
        builder.abort();
    }
}
