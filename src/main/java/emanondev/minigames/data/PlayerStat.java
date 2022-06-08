package emanondev.minigames.data;

import emanondev.core.YMLConfig;
import emanondev.minigames.Minigames;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.util.Calendar;
import java.util.Set;
import java.util.UUID;

public class PlayerStat {

    private static final YMLConfig conf = Minigames.get().getConfig("data"+ File.separator+"user_data.yml");
    public final String id;

    public PlayerStat(String id){
        this.id = id.toLowerCase();
    }

    public final static PlayerStat SKYWARS_KILLS = new PlayerStat("SKYWARS_KILLS");
    public final static PlayerStat SKYWARS_VICTORY = new PlayerStat("SKYWARS_VICTORY");
    public final static PlayerStat SKYWARS_PLAYED = new PlayerStat("SKYWARS_PLAYED");
    public final static PlayerStat GAME_PLAYED = new PlayerStat("GAME_PLAYED");



    public void add(OfflinePlayer player, int amount){
        add(player.getUniqueId(),amount);
    }
    public int getTotal(OfflinePlayer player){
        return getTotal(player.getUniqueId());
    }
    public int getToday(OfflinePlayer player){
        return getToday(player.getUniqueId());
    }
    public int getMonth(OfflinePlayer player){
        return getLastThreeMonth(player.getUniqueId());
    }
    public int getLastThreeMonth(OfflinePlayer player){
        return getLastThreeMonth(player.getUniqueId());
    }


    public void add(UUID uuid, int amount){
        String path = uuid+".permanent."+id;
        conf.set(path,conf.getInt(path,0)+amount);
        path = uuid+".temp."+ Calendar.getInstance().get(Calendar.YEAR)+"." +
                Calendar.getInstance().get(Calendar.MONTH)+"." +Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                +"." +id;
        conf.set(path,conf.getInt(path,0)+amount);
    }
    public int getTotal(UUID uuid){
        String path = uuid+".permanent."+id;
        return conf.getInt(path,0);
    }
    public int getToday(UUID uuid){
        String path = uuid + ".temp." + Calendar.getInstance().get(Calendar.YEAR) + "." +
                Calendar.getInstance().get(Calendar.MONTH) + "." + Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                + "." + id;
        if (!conf.contains(path))
            return 0;
        return conf.getInt(path,0);
    }
    public int getMonth(UUID uuid){
        return getMonth(uuid,String.valueOf(Calendar.getInstance().get(Calendar.MONTH)),
                String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
    }
    public int getLastThreeMonth(UUID uuid){
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int val = getMonth(uuid,String.valueOf(month),
                String.valueOf(year));
        if (month==1){
            month = 12;
            year-=1;
        }
        else
            month-=1;
        val += getMonth(uuid,String.valueOf(month),
                String.valueOf(year));
        if (month==1){
            month = 12;
            year-=1;
        }
        else
            month-=1;
        val += getMonth(uuid,String.valueOf(month),
                String.valueOf(year));
        return val;
    }

    private int getMonth(UUID uuid,String month,String year){
        String prePath = uuid + ".temp." + year + "." +
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
