package com.botdiril.gamedata.timers;

import com.botdiril.gamedata.player.inventory.Inventory;

public class TimerUtil
{
    public static final long TIMER_OFF_COOLDOWN = -1;

    public static boolean tryConsume(Inventory ui, EnumTimer timer)
    {
        return ui.useTimer(timer) == TIMER_OFF_COOLDOWN;
    }
}
