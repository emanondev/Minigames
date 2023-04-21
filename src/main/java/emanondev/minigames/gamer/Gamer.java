package emanondev.minigames.gamer;

import emanondev.core.YMLSection;
import emanondev.minigames.GamerManager;
import emanondev.minigames.event.GamerExperienceGainEvent;
import emanondev.minigames.event.GamerLevelUpEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.UUID;

public class Gamer {

    private final UUID user;
    private long xp;
    private int lv;

    public Gamer(@NotNull UUID user) {
        this.user = user;
        YMLSection section = GamerManager.get().getDatabaseSection(this);
        try {
            lv = Math.max(1, section.getInt("lv", 1));
        } catch (Exception e) {
            e.printStackTrace();
            lv = 1;
        }
        try {
            xp = Math.max(0, section.getLong("xp", 0));
        } catch (Exception e) {
            e.printStackTrace();
            xp = 0;
        }
    }

    public @NotNull UUID getUniqueId() {
        return user;
    }

    public @NotNull OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(user);
    }

    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(user);
    }

    public @Range(from = 0, to = Long.MAX_VALUE) long getExperienceToLevelUp() {
        return Math.max(0, getLevelUpExperience() - getExperience());
    }

    public @Range(from = 0, to = Long.MAX_VALUE) long getLevelUpExperience() {
        return getManager().getLevelUpExperience(getLevel());
    }

    public @Range(from = 0, to = Long.MAX_VALUE) long getExperience() {
        return xp;
    }

    public void setExperience(@Range(from = 0, to = Long.MAX_VALUE) long exp) {
        if (exp == this.xp)
            return;
        if (exp > this.xp) {
            addExperience(exp - this.xp);
        } else {
            this.xp = exp;
            recalculateLevel();
        }
    }

    private GamerManager getManager() {
        return GamerManager.get();
    }

    public @Range(from = 1, to = Integer.MAX_VALUE) int getLevel() {
        return lv;
    }

    public void setLevel(@Range(from = 1, to = Integer.MAX_VALUE) int lv) {
        if (this.lv == lv)
            return;
        if (this.lv < lv)
            this.xp = 0;
        this.lv = lv;

    }

    public void addExperience(@Range(from = 0, to = Long.MAX_VALUE) long exp) {
        if (exp == 0)
            return;
        GamerExperienceGainEvent event = new GamerExperienceGainEvent(this, lv + 1);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() || event.getExperienceGain() <= 0)
            return;
        this.xp += event.getExperienceGain();
        recalculateLevel();
    }

    public void save() {
        YMLSection section = GamerManager.get().getDatabaseSection(this);
        section.set("lv", lv <= 1 ? null : lv);
        section.set("xp", xp <= 0 ? null : xp);
    }

    private void recalculateLevel() {
        if (lv >= getManager().getMaxLevel())
            return;
        long nextXp = getManager().getLevelUpExperience(lv);
        while (lv < getManager().getMaxLevel() && nextXp <= xp) {
            GamerLevelUpEvent event = new GamerLevelUpEvent(this, lv + 1);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                xp = Math.min(xp, nextXp - 1);
                return;
            }
            xp -= nextXp;
            lv++;
            nextXp = getManager().getLevelUpExperience(lv);
        }
        save();
    }

    public void reset() {
        this.setLevel(1);
        this.setExperience(0);
    }
}
