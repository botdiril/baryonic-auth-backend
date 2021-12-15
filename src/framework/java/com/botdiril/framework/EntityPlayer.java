package com.botdiril.framework;

import com.botdiril.framework.sql.connection.WriteDBConnection;
import com.botdiril.gamedata.player.inventory.Inventory;

public class EntityPlayer extends ReadOnlyEntityPlayer
{
    protected final WriteDBConnection db;

    public EntityPlayer(WriteDBConnection db, int fkid)
    {
        super(db, fkid);
        this.db = db;
    }

    protected Inventory loadInventory()
    {
        return new Inventory(this.db, this.fkid);
    }

    public Inventory inventory()
    {
        if (this.cachedInventory == null)
            this.cachedInventory = this.loadInventory();

        return (Inventory) this.cachedInventory;
    }
}
