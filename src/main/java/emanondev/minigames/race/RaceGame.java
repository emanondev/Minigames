package emanondev.minigames.race;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class RaceGame extends ARaceGame<RaceTeam,RaceOption>{

    public RaceGame(@NotNull Map<String, Object> map) {
        super(map);
    }

    @NotNull
    @Override
    protected RaceTeam craftTeam(@NotNull DyeColor color) {
        return null;
    }

    @Override
    protected void assignTeam(Player p) {

    }

    @Override
    public @NotNull RaceType getMinigameType() {
        return null;
    }

    @Override
    public boolean canSwitchToSpectator(Player player) {
        return false;
    }

    @Override
    public void checkGameEnd() {

    }

    @Override
    public boolean gameCanPreStart() {
        return false;
    }

    @Override
    public boolean gameCanStart() {
        return false;
    }

    @Override
    public void onEntityDeath(@NotNull EntityDeathEvent event) {

    }

    @Override
    public boolean joinGameAsGamer(@NotNull Player player) {
        return false;
    }
}
