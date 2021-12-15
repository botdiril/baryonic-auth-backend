package com.botdiril.framework;

import java.util.Objects;

import com.botdiril.framework.sql.connection.ReadDBConnection;
import com.botdiril.gamedata.player.inventory.ReadOnlyInventory;

public class ReadOnlyEntityPlayer
{
    protected ReadOnlyInventory cachedInventory;

    protected final ReadDBConnection db;
    protected final int fkid;

    public ReadOnlyEntityPlayer(ReadDBConnection db, int fkid)
    {
        this.db = db;
        this.fkid = fkid;
    }

    @Override
    public final int hashCode()
    {
        return Objects.hashCode(this.fkid);
    }

    @Override
    public final boolean equals(Object obj)
    {
        if (obj instanceof EntityPlayer entityPlayer)
            return this.fkid == entityPlayer.fkid;

        return false;
    }

    protected ReadOnlyInventory loadInventory()
    {
        return new ReadOnlyInventory(this.db, this.fkid);
    }

    public ReadOnlyInventory inventory()
    {
        if (this.cachedInventory == null)
            this.cachedInventory = this.loadInventory();

        return this.cachedInventory;
    }
}
