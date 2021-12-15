package com.botdiril.gamedata.items.cardpack;

import com.botdiril.gamedata.item.ShopEntries;
import com.botdiril.gamedata.pools.CardPools;
import com.botdiril.gamedata.tempstat.Curser;
import com.botdiril.util.BotdirilRnd;

public class CardPacks
{
    public static ItemCardPack cardPackBasic;
    public static ItemCardPack cardPackNormal;
    public static ItemCardPack cardPackGood;
    public static ItemCardPack cardPackVoid;

    public static void load()
    {
        cardPackBasic = new ItemCardPackSimple("basiccardpack", CardPools.basicOrCommon, 10);
        ShopEntries.addCoinSell(cardPackBasic, 1000);

        cardPackNormal = new ItemCardPackSimple("cardpack", CardPools.basicToLimited, 8);

        cardPackGood = new ItemCardPackSimple("goodcardpack", CardPools.rareOrBetter, 8);

        cardPackVoid = new ItemCardPackSimple("voidcardpack",
            CardPools.rareOrBetterV, 24, (player, amount) -> {
            final double CURSE_CHANCE = 0.2;

            var rdg = BotdirilRnd.rdg();

            for (int i = 0; i < amount; i++)
                if (BotdirilRnd.rollChance(rdg, CURSE_CHANCE))
                    Curser.curse(player);

        });
    }
}
