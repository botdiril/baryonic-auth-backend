package com.botdiril.gamedata.achievement;

import com.botdiril.framework.EntityPlayer;

public class AchievementActivator
{
    public static boolean fire(EntityPlayer player, Achievement achievement)
    {
        var ui = player.inventory();

        if (!ui.hasAchievement(achievement))
        {
            ui.fireAchievement(achievement);

            return true;
        }

        return false;
    }
}
