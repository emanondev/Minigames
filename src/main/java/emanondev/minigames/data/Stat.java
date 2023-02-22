package emanondev.minigames.data;

import emanondev.core.UtilsString;
import emanondev.core.YMLConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Locale;
import java.util.Set;

public abstract class Stat<T> {

    public final String id;

    public Stat(@NotNull String id) {
        UtilsString.isValidID(id);
        this.id = id.toLowerCase(Locale.ENGLISH);
    }

    protected abstract @NotNull YMLConfig getConfig();

    protected abstract @NotNull String getId(@NotNull T target);

    public void add(@NotNull T target, int amount) {
        add(getId(target), amount);
    }

    public void add(@NotNull T target, @NotNull Calendar day, int amount) {
        add(getId(target), day, amount);
    }

    public int getTotal(@NotNull T target) {
        return getTotal(getId(target));
    }

    public int getToday(@NotNull T target) {
        return getToday(getId(target));
    }

    public int getYesterday(@NotNull T target) {
        return getYesterday(getId(target));
    }

    public int getCurrentWeek(@NotNull T target) {
        return getCurrentWeek(getId(target));
    }

    public int getLastWeek(@NotNull T target) {
        return getLastWeek(getId(target));
    }

    public int getCurrentMonth(@NotNull T target) {
        return getCurrentMonth(getId(target));
    }

    public int getLastMonth(@NotNull T target) {
        return getLastMonth(getId(target));
    }

    public int getLastThreeMonths(@NotNull T target) {
        return getLastThreeMonths(getId(target));
    }

    public void add(@NotNull String id, int amount) {
        add(id, Calendar.getInstance(), amount);
    }

    public void add(@NotNull String id, @NotNull Calendar day, int amount) {
        String path = id + ".permanent." + id;
        getConfig().set(path, getConfig().getInt(path, 0) + amount);
        path = id + ".temp." + day.get(Calendar.YEAR) + "." +
                day.get(Calendar.MONTH) + "." + day.get(Calendar.DAY_OF_MONTH)
                + "." + id;
        getConfig().set(path, getConfig().getInt(path, 0) + amount);
    }

    private int getTotal(@NotNull String id) {
        String path = id + ".permanent." + id;
        return getConfig().getInt(path, 0);
    }

    private int getToday(@NotNull String id) {
        return getDay(id, Calendar.getInstance());
    }

    private int getYesterday(@NotNull String id) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        return getDay(id, yesterday);
    }

    private int getDay(@NotNull String id, @NotNull Calendar day) {
        String path = id + ".temp." + day.get(Calendar.YEAR) + "." +
                day.get(Calendar.MONTH) + "." + day.get(Calendar.DAY_OF_MONTH)
                + "." + id;
        if (!getConfig().contains(path))
            return 0;
        return getConfig().getInt(path, 0);
    }


    private int getCurrentWeek(@NotNull String id) {
        Calendar day = Calendar.getInstance();
        int week = day.getWeekYear();
        int counter = 0;
        while (day.getWeekYear() == week) {
            counter += getDay(id, day);
            day.add(Calendar.DAY_OF_MONTH, -1);
        }
        return counter;
    }

    private int getLastWeek(@NotNull String id) {
        Calendar day = Calendar.getInstance();
        int week = day.getWeekYear();
        while (day.getWeekYear() == week) {
            day.add(Calendar.DAY_OF_YEAR, -1);
        }
        week = day.getWeekYear();
        int counter = 0;
        while (day.getWeekYear() == week) {
            counter += getDay(id, day);
            day.add(Calendar.DAY_OF_MONTH, -1);
        }
        return counter;
    }

    private int getCurrentMonth(@NotNull String id) {
        return getMonth(id, Calendar.getInstance());
    }

    private int getLastMonth(@NotNull String id) {
        Calendar lastMonth = Calendar.getInstance();
        lastMonth.add(Calendar.MONTH, -1);
        return getMonth(id, lastMonth);
    }

    private int getLastThreeMonths(@NotNull String id) {
        Calendar m1 = Calendar.getInstance();
        m1.add(Calendar.MONTH, -1);
        Calendar m2 = Calendar.getInstance();
        m2.add(Calendar.MONTH, -2);
        return getMonth(id, Calendar.getInstance()) + getMonth(id, m1) + getMonth(id, m2);
    }

    private int getMonth(@NotNull String id, @NotNull Calendar calendar) {
        String prePath = id + ".temp." + calendar.get(Calendar.YEAR) + "." +
                calendar.get(Calendar.MONTH);
        if (!getConfig().contains(prePath))
            return 0;
        Set<String> days = getConfig().getKeys(prePath);
        int val = 0;
        for (String day : days)
            if (getConfig().contains(prePath + "." + day + "." + id))
                val += getConfig().getInt(prePath + "." + day + "." + id, 0);
        return val;
    }
}
