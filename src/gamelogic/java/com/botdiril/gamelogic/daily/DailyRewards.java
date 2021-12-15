package com.botdiril.gamelogic.daily;

import com.botdiril.util.BotdirilRnd;

public class DailyRewards
{
    public static DailyResult generateRewards(int level)
    {
        var xp = Math.round(BotdirilRnd.random().nextDouble() *  1000);
        var rdg = BotdirilRnd.rdg();

        var levelScalingCoins = Math.pow(level, 1.6);
        var coins = rdg.nextLong(Math.round(200 + levelScalingCoins * 100), Math.round(300 + levelScalingCoins * 150));

        var levelScalingKeks = Math.pow(level, 1.6);
        var keks = rdg.nextLong(Math.round(2000 + levelScalingKeks * 800), Math.round(10000 + levelScalingKeks * 2000));

        var keys = level > 100 ? 5 : 3;

        return new DailyResult(xp, coins, keks, keys);
    }
}
