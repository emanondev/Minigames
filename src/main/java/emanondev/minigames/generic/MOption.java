package emanondev.minigames.generic;

import emanondev.core.gui.Gui;
import emanondev.minigames.Kit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface MOption extends ConfigurationSerializable, Cloneable, Registrable {
    @Deprecated
    default void openEditor(@NotNull Player who) {
        getEditorGui(who).open(who);
    }

    int getCollectingPlayersPhaseCooldownMax();

    int getEndPhaseCooldownMax();

    int getPreStartPhaseCooldownMax();

    int getTeamMaxSize();

    boolean getAllowSpectators();

    @NotNull
    List<Kit> getKits();

    boolean allowSelectingTeam();

    String[] getPlaceholders();

    Gui getEditorGui(Player player, Gui parent);

    default Gui getEditorGui(Player player) {
        return getEditorGui(player, null);
    }
}
