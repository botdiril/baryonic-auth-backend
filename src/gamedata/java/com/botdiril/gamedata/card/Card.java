package com.botdiril.gamedata.card;

import java.util.*;

import com.botdiril.gamedata.IGameObject;
import com.botdiril.gamedata.ItemLookup;
import com.botdiril.gamedata.pools.CardPools;

public class Card implements IGameObject
{
    private static final Map<String, Card> cards = new HashMap<>();

    public static Collection<Card> cards()
    {
        return Collections.unmodifiableCollection(cards.values());
    }

    public static Card getCardByID(int id)
    {
        return cards.get(ItemLookup.getName(id));
    }

    public static Card getCardByName(String name)
    {
        return cards.get(name.toLowerCase(Locale.ROOT));
    }

    public static long getPrice(Card c, int level)
    {
        return Math.round(Math.pow(c.getCardRarity().getLevelPriceIncrease(), level) * c.getCardRarity().getBasePrice());
    }

    private final String name;

    private final CardSet cardSet;

    private final EnumCardRarity cardRarity;

    private final int id;

    public Card(CardSet cardCollection, EnumCardRarity cardRarity, String name)
    {
        this.name = name;
        this.cardSet = cardCollection;
        this.cardRarity = cardRarity;

        this.id = ItemLookup.make(this.name);
        cards.put(this.name, this);

        if (cardCollection.canDrop())
        {
            var pool = switch (this.cardRarity) {
                case BASIC -> CardPools.basic;
                case COMMON -> CardPools.common;
                case RARE -> CardPools.rare;
                case LEGACY -> CardPools.legacy;
                case LEGENDARY -> CardPools.legendary;
                case LEGACY_LEGENDARY -> CardPools.legacylegendary;
                case ULTIMATE -> CardPools.ultimate;
                case LIMITED -> CardPools.limited;
                case MYTHIC -> CardPools.mythical;
                case UNIQUE -> CardPools.unique;
            };

            pool.add(this);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Card it)
        {
            return it.getID() == this.getID();
        }

        return false;
    }

    public EnumCardRarity getCardRarity()
    {
        return this.cardRarity;
    }

    public CardSet getCardSet()
    {
        return this.cardSet;
    }

    @Override
    public int getID()
    {
        return this.id;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public int hashCode()
    {
        return 31 + this.getID();
    }
}
