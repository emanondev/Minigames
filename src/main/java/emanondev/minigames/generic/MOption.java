package emanondev.minigames.generic;

import emanondev.minigames.Kit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface MOption extends ConfigurationSerializable, Cloneable, Registrable {
    void openEditor(@NotNull Player who);

    int getCollectingPlayersPhaseCooldownMax();

    int getEndPhaseCooldownMax();

    int getPreStartPhaseCooldownMax();

    int getTeamMaxSize();

    boolean allowSpectators();

    @NotNull
    List<Kit> getKits();

    boolean allowSelectingTeam();
}
