package com.botdiril.gamedata.item;

import com.botdiril.gamedata.EnumCurrency;

public class ItemCurrency extends Item
{
    private final EnumCurrency currency;

    public ItemCurrency(EnumCurrency currency)
    {
        super(currency.getName());

        this.currency = currency;
    }

    public EnumCurrency getCurrency()
    {
        return currency;
    }
}
