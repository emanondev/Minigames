package emanondev.minigames.data;

import emanondev.core.YMLConfig;
import emanondev.minigames.Minigames;
import emanondev.minigames.generic.MGame;

import java.io.File;
import java.util.Calendar;
import java.util.Set;

public class GameStat {

    private static final YMLConfig conf = Minigames.get().getConfig("data"+ File.separator+"game_data.yml");

    public final String id;

    public GameStat(String id){
        this.id = id.toLowerCase();
    }

    public final static GameStat PLAY_TIMES = new GameStat("PLAY_TIMES");
    public final static GameStat TOTAL_TIME_MS = new GameStat("TOTAL_TIME_MS");

    public void add(@SuppressWarnings("rawtypes") MGame game,int amount){
        String path = game.getId()+".permanent."+id;
        conf.set(path,conf.getInt(path,0)+amount);
        path = game.getId()+".temp."+ Calendar.getInstance().get(Calendar.YEAR)+"." +
                Calendar.getInstance().get(Calendar.MONTH)+"." +Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                +"." +id;
        conf.set(path,conf.getInt(path,0)+amount);
    }
    public int getTotal(@SuppressWarnings("rawtypes") MGame game){
        String path = game.getId()+".permanent."+id;
        return conf.getInt(path,0);
    }
    public int getToday(@SuppressWarnings("rawtypes") MGame game){
        String path = game.getId() + ".temp." + Calendar.getInstance().get(Calendar.YEAR) + "." +
                Calendar.getInstance().get(Calendar.MONTH) + "." + Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                + "." + id;
        if (!conf.contains(path))
            return 0;
        return conf.getInt(path,0);
    }
    public int getMonth(@SuppressWarnings("rawtypes") MGame game){
        return getMonth(game,String.valueOf(Calendar.getInstance().get(Calendar.MONTH)),
                String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
    }
    public int getLastThreeMonth(@SuppressWarnings("rawtypes") MGame game){
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int val = getMonth(game,String.valueOf(month),
                String.valueOf(year));
        if (month==1){
            month = 12;
            year-=1;
        }
        else
            month-=1;
        val += getMonth(game,String.valueOf(month),
                String.valueOf(year));
        if (month==1){
            month = 12;
            year-=1;
        }
        else
            month-=1;
        val += getMonth(game,String.valueOf(month),
                String.valueOf(year));
        return val;
    }

    private int getMonth(@SuppressWarnings("rawtypes") MGame game,String month,String year){
        String prePath = game.getId() + ".temp." + year + "." +
                month;
        if (!conf.contains(prePath))
            return 0;
        Set<String> days = conf.getKeys(prePath);
        int val = 0;
        for (String day:days)
            if (conf.contains(prePath+"."+day+"."+id))
                val+=conf.getInt(prePath+"."+day+"."+id,0);
        return val;
    }


}
