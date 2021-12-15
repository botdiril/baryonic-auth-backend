package com.botdiril.gamedata.tempstat;

import com.botdiril.framework.EntityPlayer;
import com.botdiril.framework.ReadOnlyEntityPlayer;
import com.botdiril.gamedata.player.properties.PlayerProperties;
import com.botdiril.gamedata.player.properties.ReadOnlyPlayerProperties;
import com.botdiril.gamedata.timers.TimerUtil;
import com.botdiril.util.BotdirilRnd;

public class Curser
{

    public static void bless(EntityPlayer player)
    {
        var blessings = EnumBlessing.values();
        var blessing = BotdirilRnd.choose(blessings);

        bless(player, blessing);
    }

    public static void curse(EntityPlayer player)
    {
        var curses = EnumCurse.values();
        var curse = BotdirilRnd.choose(curses);

        curse(player, curse);
    }

    public static void bless(EntityPlayer player, EnumBlessing blessing)
    {
        var millis = blessing.getDurationInSeconds() * 1000;

        var target = player.inventory();
        var properties = target.getProperties();

        if (isBlessed(properties, blessing))
        {
            properties.extendBlessing(blessing, millis);
        }
        else
        {
            properties.setBlessing(blessing, System.currentTimeMillis() + millis);
        }
    }

    public static void curse(EntityPlayer player, EnumCurse curse)
    {
        var target = player.inventory();
        var properties = target.getProperties();

        if (isBlessed(properties, EnumBlessing.CANT_BE_CURSED))
        {
            return;
        }

        var millis = curse.getDurationInSeconds() * 1000;

        if (isCursed(properties, curse))
        {
            properties.extendCurse(curse, millis);
        }
        else
        {
            properties.setCurse(curse, System.currentTimeMillis() + millis);
        }
    }

    public static boolean isBlessed(ReadOnlyPlayerProperties po, EnumBlessing blessing)
    {
        return po.getBlessing(blessing) > System.currentTimeMillis();
    }

    public static boolean isCursed(ReadOnlyPlayerProperties po, EnumCurse curse)
    {
        return po.getCurse(curse) > System.currentTimeMillis();
    }

    public static boolean isBlessed(EntityPlayer player, EnumBlessing blessing)
    {
        var target = player.inventory();
        var properties = target.getProperties();

        return properties.getBlessing(blessing) > System.currentTimeMillis();
    }

    public static boolean isCursed(ReadOnlyEntityPlayer player, EnumCurse curse)
    {
        var target = player.inventory();
        var properties = target.getProperties();

        return properties.getCurse(curse) > System.currentTimeMillis();
    }

    public static void clear(PlayerProperties po, EnumCurse curse)
    {
        po.setCurse(curse, TimerUtil.TIMER_OFF_COOLDOWN);
    }

    public static void clear(PlayerProperties po, EnumBlessing blessing)
    {
        po.setBlessing(blessing, TimerUtil.TIMER_OFF_COOLDOWN);
    }
}
