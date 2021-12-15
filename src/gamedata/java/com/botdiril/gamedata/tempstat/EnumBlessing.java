package com.botdiril.gamedata.tempstat;

public enum EnumBlessing
{
    UNBREAKABLE_PICKAXE(0, 60 * 30), //
    BETTER_WOODCUT(1, 60 * 60), //
    CANT_BE_CURSED(2, 60 * 30), //
    BETTER_SELL_PRICES(3, 60 * 5), //
    CHANCE_NOT_TO_CONSUME_KEY(4, 60 * 12), //
    STEAL_IMMUNE(5, 60 * 30), //
    MINE_SURGE(6, 60 * 12), //
    PICKPOCKET(7, 60 * 60 * 2), //
    CRAFTING_SURGE(8, 60 * 4), //
    NUKE_IMMUNE(9, 60 * 60); //

    public static final int MAX_BLESSINGS = 32;

    private final int id;
    private final long durationInSeconds;

     EnumBlessing(int id, long durationInSeconds)
    {
        this.id = id;
        this.durationInSeconds = durationInSeconds;
    }

    public long getDurationInSeconds()
    {
        return this.durationInSeconds;
    }

    public int getID()
    {
        return this.id;
    }
}
