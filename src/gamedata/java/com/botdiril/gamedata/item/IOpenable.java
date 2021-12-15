package com.botdiril.gamedata.item;

import com.botdiril.framework.EntityPlayer;

public interface IOpenable
{
    void open(EntityPlayer player, long amount);

    default boolean requiresKey()
    {
        return false;
    }
}
