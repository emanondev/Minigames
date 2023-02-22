package emanondev.minigames;

import emanondev.core.MessageBuilder;
import emanondev.core.UtilsCommand;
import emanondev.core.UtilsString;
import emanondev.minigames.generic.MArena;
import emanondev.minigames.generic.MArenaBuilder;
import emanondev.minigames.generic.MType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ArenaManager extends Manager<MArena> {

    private static ArenaManager instance;

    public static ArenaManager get() {
        return instance;
    }

    private final HashMap<UUID, MArenaBuilder> builders = new HashMap<>();

    public ArenaManager() {
        super("arenas");
        instance = this;
    }

    public <A extends MArena> @NotNull Map<String, A> getCompatibleArenas(@NotNull MType<A, ?> type) {
        HashMap<String, A> map = new HashMap<>();
        getAll().forEach((k, v) -> {
            if (type.matchType(v)) map.put(k, (A) v);
        });
        return map;
    }

    private void registerBuilder(@NotNull UUID who, @NotNull String id, @NotNull MType<?, ?> type) {
        if (!UtilsString.isLowcasedValidID(id)) {
            //TODO
            return;
        }
        if (get(id) != null) {
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
                String id = args[2].toLowerCase(Locale.ENGLISH);
                if (!UtilsString.isLowcasedValidID(id)) {
                    new MessageBuilder(Minigames.get(), who)
                            .addTextTranslation("generic.arenabuilder.error.invalid_id", "",
                                    "%id%", args[2]).send();
                    return;
                }
                if (get(id) != null) {
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
                case 2 -> switch (args[0].toLowerCase(Locale.ENGLISH)) {
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
        register(builder.getId(), arena, Bukkit.getOfflinePlayer(builder.getUser()));
        save(arena);
        builders.remove(builder.getUser());
        builder.abort();
    }
}
