package emanondev.minigames.generic;

import emanondev.core.gui.Gui;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface MOption extends ConfigurationSerializable, Cloneable, Registrable {
    @Deprecated
    default void openEditor(@NotNull Player who) {
        getEditorGui(who).open(who);
    }

    void setAllowSpectators(boolean allowSpectators);

    void setCollectingPlayersPhaseCooldownMax(int value);

    void setEndPhaseCooldownMax(int value);

    void setPreStartPhaseCooldownMax(int value);

    int getCollectingPlayersPhaseCooldownMax();

    int getEndPhaseCooldownMax();

    int getPreStartPhaseCooldownMax();

    int getTeamMaxSize();

    boolean getAllowSpectators();

    /*
    @NotNull
    List<Kit> getKits();*/

    boolean allowSelectingTeam();

    default String[] getPlaceholders() {
        String name = getClass().getSimpleName();
        return new String[]{
                "%id%", getId(), "%type%", name.endsWith("Option") ? name.substring(0, name.length() - 6) : name
        };
    }

    Gui getEditorGui(Player player, Gui parent);

    default Gui getEditorGui(Player player) {
        return getEditorGui(player, null);
    }

    boolean getShowArenaBorders();

    void setShowArenaBorders(boolean value);
}
