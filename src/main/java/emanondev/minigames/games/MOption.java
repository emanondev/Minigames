package emanondev.minigames.games;

import emanondev.core.gui.Gui;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface MOption extends ConfigurationSerializable, Cloneable, Registrable {
    @Deprecated
    default void openEditor(@NotNull Player who) {
        getEditorGui(who).open(who);
    }

    default Gui getEditorGui(Player player) {
        return getEditorGui(player, null);
    }

    Gui getEditorGui(Player player, Gui parent);

    int getCollectingPlayersPhaseCooldownMax();

    void setCollectingPlayersPhaseCooldownMax(int value);

    int getEndPhaseCooldownMax();

    void setEndPhaseCooldownMax(int value);

    int getPreStartPhaseCooldownMax();

    void setPreStartPhaseCooldownMax(int value);

    int getTeamMaxSize();

    /*
    @NotNull
    List<Kit> getKits();*/

    boolean getAllowSpectators();

    void setAllowSpectators(boolean allowSpectators);

    boolean allowSelectingTeam();

    default String[] getPlaceholders() {
        String name = getClass().getSimpleName();
        return new String[]{
                "%id%", getId(), "%type%", name.endsWith("Option") ? name.substring(0, name.length() - 6) : name
        };
    }

    boolean getShowArenaBorders();

    void setShowArenaBorders(boolean value);
}
