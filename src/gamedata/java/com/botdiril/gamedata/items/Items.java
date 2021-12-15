package com.botdiril.gamedata.items;

import java.util.List;

import com.botdiril.gamedata.EnumCurrency;
import com.botdiril.gamedata.item.*;
import com.botdiril.gamedata.items.cardpack.CardPacks;

public class Items
{
    public static ItemCurrency xp;
    public static ItemCurrency coins;
    public static ItemCurrency keks;
    public static ItemCurrency tokens;
    public static ItemCurrency megakeks;
    public static ItemCurrency dust;
    public static ItemCurrency keys;

    public static Item redGem;
    public static Item greenGem;

    public static Item blueGem;
    public static Item purpleGem;

    public static Item rainbowGem;
    public static Item blackGem;

    public static Item timewarpCrystal;

    public static Item gemdiril;

    public static Item toolBox;
    public static Item ash;
    public static Item prismaticDust;

    public static Item wood;

    public static Item coal;
    public static Item iron;
    public static Item copper;
    public static Item gold;
    public static Item platinum;
    public static Item uranium;
    public static Item kekium;
    public static Item emerald;
    public static Item diamond;

    public static Item strangeMetal;

    public static Item repairKit;

    public static Item oil;
    public static Item goldenOil;
    public static Item prismaticOil;

    public static void load()
    {
        xp = new ItemCurrency(EnumCurrency.XP);
        coins = new ItemCurrency(EnumCurrency.COINS);
        keks = new ItemCurrency(EnumCurrency.KEKS);
        tokens = new ItemCurrency(EnumCurrency.TOKENS);
        dust = new ItemCurrency(EnumCurrency.DUST);
        keys = new ItemCurrency(EnumCurrency.KEYS);

        CardPacks.load();

        redGem = new Item("infernalgem");
        greenGem = new Item("peacegem");

        blueGem = new Item("equlibriumgem");
        purpleGem = new Item("imbalancegem");

        rainbowGem = new Item("ordergem");
        blackGem = new Item("chaosgem");

        timewarpCrystal = new Item("timewarpcrystal");

        strangeMetal = new Item("strangemetal");

        ash = new Item("ash");
        ShopEntries.addDisenchant(ash, 1000);

        prismaticDust = new Item("prismaticdust");

        oil = new Item("oil");
        ShopEntries.addCoinSell(oil, 800);

        goldenOil = new Item("goldenoil");

        prismaticOil = new Item("prismaticoil");
        CraftingEntries.add(new Recipe(List.of(
            goldenOil.ofAmount(1),
            prismaticDust.ofAmount(1)
        ), 1, prismaticOil));

        wood = new Item("wood");
        ShopEntries.addCoinSell(wood, 20);

        coal = new Item("coal");
        ShopEntries.addCoinSell(coal, 3);

        iron = new Item("iron");
        ShopEntries.addCoinSell(iron, 8);

        copper = new Item("copper");
        ShopEntries.addCoinSell(copper, 60);

        gold = new Item("gold");
        ShopEntries.addCoinSell(gold, 1_000);

        platinum = new Item("platinum");
        ShopEntries.addCoinSell(platinum, 4_000);

        uranium = new Item("uranium");
        ShopEntries.addCoinSell(uranium, 500);

        kekium = new Item("kekium");
        ShopEntries.addCoinSell(kekium, 100_000);
        ShopEntries.addTokenBuy(kekium, 80_000);

        emerald = new Item("emerald");
        ShopEntries.addCoinSell(emerald, 1_000_000);

        diamond = new Item("diamond");
        ShopEntries.addCoinSell(diamond, 10_000_000);

        gemdiril = new Item("gemdiril");
        CraftingEntries.add(new Recipe(List.of(
            ItemPair.of(redGem, 256),
            ItemPair.of(greenGem, 256),
            ItemPair.of(blueGem, 192),
            ItemPair.of(purpleGem, 192),
            ItemPair.of(rainbowGem, 128),
            ItemPair.of(blackGem, 128),
            ItemPair.of(kekium, 1_234_567),
            ItemPair.of(prismaticDust, 1)),
            1, gemdiril));

        toolBox = new Item("toolbox");
        ShopEntries.addCoinSell(toolBox, 4_000);
        CraftingEntries.add(new Recipe(List.of(ItemPair.of(wood, 500), ItemPair.of(ash, 10), ItemPair.of(greenGem, 12), ItemPair.of(strangeMetal, 2)), 1, toolBox));

        repairKit = new Item("repairkit");
        CraftingEntries.add(new Recipe(List.of(ItemPair.of(toolBox, 1), ItemPair.of(oil, 1)), 1, repairKit));
    }
}
