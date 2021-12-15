package com.botdiril.gamedata.items.cardpack;

import com.botdiril.framework.EntityPlayer;
import com.botdiril.gamedata.card.Card;
import com.botdiril.gamedata.card.CardDrops;
import com.botdiril.gamedata.item.IOpenable;
import com.botdiril.gamedata.item.Item;
import com.botdiril.gamedata.pools.PoolDrawer;
import com.botdiril.gamedata.tempstat.Curser;
import com.botdiril.gamedata.tempstat.EnumCurse;

public abstract class ItemCardPack extends Item implements IOpenable
{
    private static final int DISPLAY_LIMIT = 15;

    public ItemCardPack(String name)
    {
        super(name);
    }

    public abstract PoolDrawer<Card> getPool(EntityPlayer player);

    public abstract int getNumberOfCards(EntityPlayer player);

    protected void onOpen(EntityPlayer player, long amount)
    {

    }

    @Override
    public void open(EntityPlayer player, long amount)
    {
        var cp = new CardDrops();

        var contents = this.getNumberOfCards(player);
        var pool = this.getPool(player);

        var inventory = player.inventory();
        var po = inventory.getProperties();

        for (int i = 0; i < contents * amount; i++)
        {
            if (Curser.isCursed(po, EnumCurse.CURSE_OF_YASUO))
            {
                cp.addItem(Card.getCardByName("yasuo"));
                continue;
            }

            cp.addItem(pool.draw(), 1);
        }

        var i = 0;

        for (var cardPair : cp)
        {
            var card = cardPair.getCard();
            var amt = cardPair.getAmount();

            inventory.addCard(card, amt);

            i++;
        }

        this.onOpen(player, amount);
    }
}
