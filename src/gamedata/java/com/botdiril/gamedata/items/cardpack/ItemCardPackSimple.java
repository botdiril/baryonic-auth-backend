package com.botdiril.gamedata.items.cardpack;

import java.util.function.ObjLongConsumer;

import com.botdiril.framework.EntityPlayer;
import com.botdiril.gamedata.card.Card;
import com.botdiril.gamedata.pools.PoolDrawer;

public class ItemCardPackSimple extends ItemCardPack
{
    protected final PoolDrawer<Card> pool;
    protected final int contents;
    protected final ObjLongConsumer<EntityPlayer> openHandler;

    public ItemCardPackSimple(String name, PoolDrawer<Card> pool, int contents)
    {
        super(name);

        this.pool = pool;
        this.contents = contents;
        this.openHandler = (callObj, amount) -> {};
    }

    public ItemCardPackSimple(String name, PoolDrawer<Card> pool, int contents, ObjLongConsumer<EntityPlayer> openHandler)
    {
        super(name);

        this.pool = pool;
        this.contents = contents;
        this.openHandler = openHandler;
    }

    @Override
    protected void onOpen(EntityPlayer player, long amount)
    {
        this.openHandler.accept(player, amount);
    }

    @Override
    public PoolDrawer<Card> getPool(EntityPlayer player)
    {
        return this.pool;
    }

    @Override
    public int getNumberOfCards(EntityPlayer player)
    {
        return this.contents;
    }
}
