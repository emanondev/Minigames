package emanondev.minigames.data;

import emanondev.core.UtilsString;
import emanondev.core.YMLConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Locale;
import java.util.Set;

public abstract class Stat<T> {

    public final String pathEnd;

    public Stat(@NotNull String id) {
        UtilsString.isValidID(id);
        this.pathEnd = id.toLowerCase(Locale.ENGLISH);
    }

    public void add(@NotNull T target, int amount) {
        add(getId(target), amount);
    }

    public void add(@NotNull String targetId, int amount) {
        add(targetId, Calendar.getInstance(), amount);
    }

    protected abstract @NotNull String getId(@NotNull T target);

    public void add(@NotNull String targetId, @NotNull Calendar day, int amount) {
        String path = targetId + ".permanent." + pathEnd;
        getConfig().set(path, getConfig().getInt(path, 0) + amount);
        path = targetId + ".temp." + day.get(Calendar.YEAR) + "." +
                day.get(Calendar.MONTH) + "." + day.get(Calendar.DAY_OF_MONTH)
                + "." + this.pathEnd;
        getConfig().set(path, getConfig().getInt(path, 0) + amount);
    }

    protected abstract @NotNull YMLConfig getConfig();

    public void add(@NotNull T target, @NotNull Calendar day, int amount) {
        add(getId(target), day, amount);
    }

    public int getTotal(@NotNull T target) {
        return getTotal(getId(target));
    }

    private int getTotal(@NotNull String targetId) {
        String path = targetId + ".permanent." + pathEnd;
        return getConfig().getInt(path, 0);
    }

    public int getToday(@NotNull T target) {
        return getToday(getId(target));
    }

    private int getToday(@NotNull String targetId) {
        return getDay(targetId, Calendar.getInstance());
    }

    private int getDay(@NotNull String targetId, @NotNull Calendar day) {
        String path = targetId + ".temp." + day.get(Calendar.YEAR) + "." +
                day.get(Calendar.MONTH) + "." + day.get(Calendar.DAY_OF_MONTH)
                + "." + pathEnd;
        if (!getConfig().contains(path))
            return 0;
        return getConfig().getInt(path, 0);
    }

    public int getYesterday(@NotNull T target) {
        return getYesterday(getId(target));
    }

    private int getYesterday(@NotNull String id) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        return getDay(id, yesterday);
    }

    public int getCurrentWeek(@NotNull T target) {
        return getCurrentWeek(getId(target));
    }

    private int getCurrentWeek(@NotNull String targetId) {
        Calendar day = Calendar.getInstance();
        int week = day.getWeekYear();
        int counter = 0;
        while (day.getWeekYear() == week) {
            counter += getDay(targetId, day);
            day.add(Calendar.DAY_OF_MONTH, -1);
        }
        return counter;
    }

    public int getLastWeek(@NotNull T target) {
        return getLastWeek(getId(target));
    }

    private int getLastWeek(@NotNull String targetId) {
        Calendar day = Calendar.getInstance();
        int week = day.getWeekYear();
        while (day.getWeekYear() == week) {
            day.add(Calendar.DAY_OF_YEAR, -1);
        }
        week = day.getWeekYear();
        int counter = 0;
        while (day.getWeekYear() == week) {
            counter += getDay(targetId, day);
            day.add(Calendar.DAY_OF_MONTH, -1);
        }
        return counter;
    }

    public int getCurrentMonth(@NotNull T target) {
        return getCurrentMonth(getId(target));
    }

    private int getCurrentMonth(@NotNull String targetId) {
        return getMonth(targetId, Calendar.getInstance());
    }

    private int getMonth(@NotNull String targetId, @NotNull Calendar calendar) {
        String prePath = targetId + ".temp." + calendar.get(Calendar.YEAR) + "." +
                calendar.get(Calendar.MONTH);
        if (!getConfig().contains(prePath))
            return 0;
        Set<String> days = getConfig().getKeys(prePath);
        int val = 0;
        for (String day : days)
            if (getConfig().contains(prePath + "." + day + "." + pathEnd))
                val += getConfig().getInt(prePath + "." + day + "." + pathEnd, 0);
        return val;
    }

    public int getLastMonth(@NotNull T target) {
        return getLastMonth(getId(target));
    }

    private int getLastMonth(@NotNull String targetId) {
        Calendar lastMonth = Calendar.getInstance();
        lastMonth.add(Calendar.MONTH, -1);
        return getMonth(targetId, lastMonth);
    }

    public int getLastThreeMonths(@NotNull T target) {
        return getLastThreeMonths(getId(target));
    }

    private int getLastThreeMonths(@NotNull String targetId) {
        Calendar m1 = Calendar.getInstance();
        m1.add(Calendar.MONTH, -1);
        Calendar m2 = Calendar.getInstance();
        m2.add(Calendar.MONTH, -2);
        return getMonth(targetId, Calendar.getInstance()) + getMonth(targetId, m1) + getMonth(targetId, m2);
    }
}
