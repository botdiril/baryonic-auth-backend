package com.botdiril.gamedata.tempstat;

public enum EnumCurse
{
    CURSE_OF_YASUO(0, 60 * 30), //
    WORSE_WOODCUT(1, 60 * 60), //
    DOUBLE_PICKAXE_BREAK_CHANCE(2, 60 * 45), //
    CANT_TAKE_DAILY(3, 60 * 120), //
    HALVED_SELL_VALUE(4, 60 * 35), //
    CRAFTING_MAY_FAIL(5, 60 * 25), //
    CANT_SEE_MINED_STUFF(6, 60 * 35), //
    CANT_WIN_JACKPOT(7, 60 * 120), //
    MAGNETIC(8, 60 * 60), // Your gifts may end up redirected to the bot
    EASIER_TO_ROB(9, 60 * 30); //

    public static final int MAX_CURSES = 32;

    private final int id;
    private final long durationInSeconds;

    EnumCurse(int id, long durationInSeconds)
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
