package emanondev.minigames.generic;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

public interface MOption extends ConfigurationSerializable, Cloneable, Registrable {
    void openEditor(Player who);

    int getCollectingPlayersPhaseCooldownMax();

    int getEndPhaseCooldownMax();

    int getPreStartPhaseCooldownMax();

    boolean allowSpectators();
}
