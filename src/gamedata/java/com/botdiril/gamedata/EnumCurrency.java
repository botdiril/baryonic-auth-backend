package com.botdiril.gamedata;

public enum EnumCurrency
{
    XP("xp"),
    COINS("coin"),
    KEKS("kek"),
    TOKENS("kektoken"),
    DUST("dust"),
    KEYS("key");

    private final String name;

    EnumCurrency(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
