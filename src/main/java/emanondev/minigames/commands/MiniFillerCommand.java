package emanondev.minigames.commands;

import emanondev.core.*;
import emanondev.minigames.FillerManager;
import emanondev.minigames.Minigames;
import emanondev.minigames.generic.ChestFiller;
import emanondev.minigames.generic.MFiller;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MiniFillerCommand extends CoreCommand {
    public MiniFillerCommand() {
        super("minifiller", Minigames.get(), PermissionBuilder.ofCommand(Minigames.get(), "minifiller").buildAndRegister(Minigames.get()));
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            playerOnlyNotify(sender);
            return;
        }
        if (args.length == 0) {
            onHelp(p, label, args);
            return;
        }
        switch (args[0].toLowerCase()) {
            case "create" -> create(p, label, args);
            case "addall" -> addall(p, label, args);
            case "clear" -> clear(p, label, args);
            case "show" -> show(p, label, args);
            case "list" -> list(p, label, args);
            default -> onHelp(p, label, args);
        }
    }

    private void onHelp(Player p, String label, String[] args) {
        new MessageBuilder(Minigames.get(), p)
                .addText("Non implementato").send();
    }

    //create <id>
    private void create(Player p, String label, String[] args) {
        if (args.length != 2) {
            //arguments
            new MessageBuilder(Minigames.get(), p)
                    .addTextTranslation("minifiller.error.create_arguments_amount", "",
                            "%label%", label).send();
            return;
        }
        String id = args[1].toLowerCase();
        if (!UtilsString.isLowcasedValidID(id)) {
            new MessageBuilder(Minigames.get(), p)
                    .addTextTranslation("minifiller.error.invalid_id", "",
                            "%id%", args[1]).send();
            return;
        }
        if (FillerManager.get().getFiller(id) != null) {
            new MessageBuilder(Minigames.get(), p)
                    .addTextTranslation("minifiller.error.already_used_id", "",
                            "%id%", args[1]).send();
            return;
        }
        ChestFiller filler = new ChestFiller();
        FillerManager.get().registerFiller(id, filler, p);
        FillerManager.get().save(filler);
        new MessageBuilder(Minigames.get(), p)
                .addTextTranslation("minifiller.success.create", "",
                        "%id%", id).send();
    }

    private void clear(Player p, String label, String[] args) {
        new MessageBuilder(Minigames.get(), p)
                .addText("Non implementato").send();
    }

    //addall <id> <weight>
    private void addall(Player p, String label, String[] args) {
        if (args.length != 3) {
            //arguments
            new MessageBuilder(Minigames.get(), p)
                    .addTextTranslation("minifiller.error.addall_arguments_amount", "",
                            "%label%", label).send();
            return;
        }
        String id = args[1].toLowerCase();
        MFiller filler = FillerManager.get().getFiller(id);
        if (filler == null) {
            new MessageBuilder(Minigames.get(), p)
                    .addTextTranslation("minifiller.error.unexisting_id", "",
                            "%id%", args[1]).send();
            return;
        }
        int weight;
        try {
            weight = Integer.parseInt(args[2]);
            if (weight <= 0) {
                new MessageBuilder(Minigames.get(), p)
                        .addTextTranslation("minifiller.error.invalid_weight", "",
                                "%weight%", args[2]).send();
                return;
            }
        } catch (NumberFormatException e) {
            new MessageBuilder(Minigames.get(), p)
                    .addTextTranslation("minifiller.error.invalid_weight", "",
                            "%weight%", args[2]).send();
            return;
        }

        Block b = p.getTargetBlockExact(10);
        if (b == null) {
            new MessageBuilder(Minigames.get(), p)
                    .addTextTranslation("minifiller.error.invalid_target", "",
                            "%weight%", args[2]).send();
            return;
        }
        for (double i = b.getX(); i <= b.getX() + 1; i += 0.2) {
            p.spawnParticle(Particle.COMPOSTER, i, b.getY(), b.getZ(), 1);
            p.spawnParticle(Particle.COMPOSTER, i, b.getY() + 1, b.getZ(), 1);
            p.spawnParticle(Particle.COMPOSTER, i, b.getY(), b.getZ() + 1, 1);
            p.spawnParticle(Particle.COMPOSTER, i, b.getY() + 1, b.getZ() + 1, 1);
        }
        for (double i = b.getY(); i <= b.getY() + 1; i += 0.2) {
            p.spawnParticle(Particle.COMPOSTER, b.getX(), i, b.getZ(), 1);
            p.spawnParticle(Particle.COMPOSTER, b.getX() + 1, i, b.getZ(), 1);
            p.spawnParticle(Particle.COMPOSTER, b.getX(), i, b.getZ() + 1, 1);
            p.spawnParticle(Particle.COMPOSTER, b.getX() + 1, i, b.getZ() + 1, 1);
        }
        for (double i = b.getZ(); i <= b.getZ() + 1; i += 0.2) {
            p.spawnParticle(Particle.COMPOSTER, b.getX(), b.getY(), i, 1);
            p.spawnParticle(Particle.COMPOSTER, b.getX() + 1, b.getY(), i, 1);
            p.spawnParticle(Particle.COMPOSTER, b.getX(), b.getY() + 1, i, 1);
            p.spawnParticle(Particle.COMPOSTER, b.getX() + 1, b.getY() + 1, i, 1);
        }

        BlockState state = b.getState();
        if (!(state instanceof Container container)) {
            new MessageBuilder(Minigames.get(), p)
                    .addTextTranslation("minifiller.error.invalid_block", "",
                            "%weight%", args[2]).send();
            return;
        }
        ItemStack[] contents = container.getInventory().getStorageContents();
        ArrayList<ItemStack> contentsList = new ArrayList<>();
        for (ItemStack item : contents)
            if (!UtilsInventory.isAirOrNull(item))
                contentsList.add(item);
        if (contentsList.isEmpty()) {
            new MessageBuilder(Minigames.get(), p)
                    .addTextTranslation("minifiller.error.empty_container", "",
                            "%weight%", args[2]).send();
            return;
        }
        filler.addItems(contentsList, weight);
        new MessageBuilder(Minigames.get(), p)
                .addTextTranslation("minifiller.success.addall", "",
                        "%weight%", args[2], "%id%", id, "%amount%", String.valueOf(contentsList.size())).send();
        return;
    }

    //show <id>
    private void show(Player p, String label, String[] args) {
        if (args.length != 2) {
            //arguments
            new MessageBuilder(Minigames.get(), p)
                    .addTextTranslation("minifiller.error.show_arguments_amount", "",
                            "%label%", label).send();
            return;
        }
        String id = args[1].toLowerCase();
        MFiller filler = FillerManager.get().getFiller(id);
        if (filler == null) {
            new MessageBuilder(Minigames.get(), p)
                    .addTextTranslation("minifiller.error.unexisting_id", "",
                            "%id%", args[1]).send();
            return;
        }
        filler.editorGui(p, null).open(p);
    }

    private void list(Player p, String label, String[] args) {
        new MessageBuilder(Minigames.get(), p)
                .addText("Non implementato").send();
    }

    @Override
    public List<String> onComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @Nullable Location location) {
        return switch (args.length) {
            case 1 -> this.complete(args[0], List.of("create", "addall", "clear",
                    "show", "list"));
            case 2 -> switch (args[0].toLowerCase()) {
                case "addall", "clear", "show" -> this.complete(args[1], FillerManager.get().getFillers().keySet());
                default -> Collections.emptyList();
            };
            case 3 -> switch (args[0].toLowerCase()) {
                case "addall" -> this.complete(args[2], List.of("10", "50", "100"));
                default -> Collections.emptyList();
            };
            default -> Collections.emptyList();
        };
    }
}