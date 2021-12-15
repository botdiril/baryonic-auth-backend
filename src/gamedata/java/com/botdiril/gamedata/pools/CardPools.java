package com.botdiril.gamedata.pools;

import com.botdiril.gamedata.card.Card;

public class CardPools
{
    public static final LootPool<Card> basic = new LootPool<>();
    public static final LootPool<Card> common = new LootPool<>();
    public static final LootPool<Card> rare = new LootPool<>();
    public static final LootPool<Card> legacy = new LootPool<>();
    public static final LootPool<Card> legendary = new LootPool<>();
    public static final LootPool<Card> legacylegendary = new LootPool<>();
    public static final LootPool<Card> ultimate = new LootPool<>();
    public static final LootPool<Card> limited = new LootPool<>();
    public static final LootPool<Card> mythical = new LootPool<>();
    public static final LootPool<Card> unique = new LootPool<>();

    public static final PoolDrawer<Card> basicOrCommon = new PoolDrawer<Card>()
        .add(5, basic)
        .add(1, common);

    public static final PoolDrawer<Card> basicToLimited = new PoolDrawer<Card>()
        .add(50, basic)
        .add(400, common)
        .add(108, rare)
        .add(54, legacy)
        .add(27, legendary)
        .add(9, legacylegendary)
        .add(3, ultimate)
        .add(1, limited);

    public static final PoolDrawer<Card> rareOrBetter = new PoolDrawer<Card>()
        .add(1024, rare)
        .add(512, legacy)
        .add(256, legendary)
        .add(128, legacylegendary)
        .add(64, ultimate)
        .add(16, limited)
        .add(4, mythical)
        .add(1, unique);

    public static final PoolDrawer<Card> rareOrBetterV = new PoolDrawer<Card>()
        .add(30, rare)
        .add(40, legacy)
        .add(90, legendary)
        .add(48, legacylegendary)
        .add(24, ultimate)
        .add(12, limited)
        .add(6, mythical)
        .add(3, unique);
}
