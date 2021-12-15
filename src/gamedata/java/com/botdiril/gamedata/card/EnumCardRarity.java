package com.botdiril.gamedata.card;

public enum EnumCardRarity
{
    BASIC(1, 100, 1.8),
    COMMON(2, 400, 1.9),
    RARE(3, 1500, 1.85),
    LEGACY(4, 1200, 1.95),
    LEGENDARY(5, 8000, 1.75),
    LEGACY_LEGENDARY(6, 16000, 1.7),
    ULTIMATE(7, 40000, 1.585),
    LIMITED(8, 64000, 1.57),
    MYTHIC(9, 80000, 1.525),
    UNIQUE(10, 150000, 1.49);

    private final int level;
    private final long basePrice;
    private final double levelPriceIncrease;

    EnumCardRarity(int level, long basePrice, double levelPriceIncrease)
    {
        this.level = level;
        this.basePrice = basePrice;
        this.levelPriceIncrease = levelPriceIncrease;
    }

    public long getBasePrice()
    {
        return this.basePrice;
    }

    public int getRarityLevel()
    {
        return this.level;
    }

    public double getLevelPriceIncrease()
    {
        return this.levelPriceIncrease;
    }
}
