package emanondev.minigames.generic;

import emanondev.core.PermissionBuilder;
import emanondev.minigames.Minigames;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class Perms {

    public static final Permission COMMAND_MINIKIT = PermissionBuilder.ofCommand(Minigames.get(), "Minikit").buildAndRegister(Minigames.get());
    public static final Permission COMMAND_MINIFILLER = PermissionBuilder.ofCommand(Minigames.get(), "minifiller").buildAndRegister(Minigames.get());
    public static final Permission COMMAND_MINIOPTION = PermissionBuilder.ofCommand(Minigames.get(), "minioption").buildAndRegister(Minigames.get());
    public static final Permission COMMAND_MINIGAME = PermissionBuilder.ofCommand(Minigames.get(), "MiniGame").buildAndRegister(Minigames.get());
    public static final Permission COMMAND_LEAVE = PermissionBuilder.ofCommand(Minigames.get(), "leave")
            .setAccess(PermissionDefault.TRUE).buildAndRegister(Minigames.get());
    public static final Permission COMMAND_FASTJOIN = PermissionBuilder.ofCommand(Minigames.get(), "fastjoin")
            .setAccess(PermissionDefault.TRUE).buildAndRegister(Minigames.get());
    public static final Permission COMMAND_ARENABUILDER = PermissionBuilder.ofCommand(Minigames.get(), "arenabuilder").buildAndRegister(Minigames.get());
}
