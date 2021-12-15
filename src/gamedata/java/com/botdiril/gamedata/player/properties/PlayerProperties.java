package com.botdiril.gamedata.player.properties;

import org.apache.commons.lang3.tuple.Pair;

import java.nio.ByteBuffer;

import com.botdiril.MajorFailureException;
import com.botdiril.framework.sql.connection.WriteDBConnection;
import com.botdiril.framework.sql.SqlFoundation;
import com.botdiril.framework.util.PlayerRestrictions;
import com.botdiril.gamedata.player.inventory.InventoryTables;
import com.botdiril.gamedata.stat.EnumStat;
import com.botdiril.gamedata.tempstat.EnumBlessing;
import com.botdiril.gamedata.tempstat.EnumCurse;
import com.botdiril.uss2.properties.USS2PropertyObject;
import com.botdiril.uss2.properties.USS2PropertySchema;

public final class PlayerProperties extends ReadOnlyPlayerProperties
{
    private final WriteDBConnection db;

    public PlayerProperties(WriteDBConnection db, int fid)
    {
        super(db, fid);
        this.db = db;
    }

    public void setUsedAliases(byte aliasList)
    {
        var uss = this.load();
        ALIAS_USED.write(uss, aliasList);
        this.save(uss);
    }

    public void setAlias(int aliasNum, String in, String out)
    {
        var uss = this.load();
        ALIAS_IN[aliasNum].write(uss, in);
        ALIAS_OUT[aliasNum].write(uss, out);
        this.save(uss);
    }

    public void setPreferencesBitfield(long preferences)
    {
        var uss = this.load();
        PREFERENCES_BITFIELD.write(uss, preferences);
        this.save(uss);
    }

    public void setBlessing(EnumBlessing blessing, long time)
    {
        var uss = this.load();
        BLESSINGS[blessing.getID()].write(uss, time);
        this.save(uss);
    }

    public void setCurse(EnumCurse curse, long time)
    {
        var uss = this.load();
        CURSES[curse.getID()].write(uss, time);
        this.save(uss);
    }

    public void extendBlessing(EnumBlessing blessing, long time)
    {
        var uss = this.load();
        var id = blessing.getID();
        var bless = BLESSINGS[id].read(uss);
        BLESSINGS[id].write(uss, bless + time);
        this.save(uss);
    }

    public void extendCurse(EnumCurse curse, long time)
    {
        var uss = this.load();
        var id = curse.getID();
        var bless = CURSES[id].read(uss);
        CURSES[id].write(uss, bless + time);
        this.save(uss);
    }

    public void setStat(EnumStat stat, long value)
    {
        var uss = this.load();
        STATS[stat.getID()].write(uss, value);
        this.save(uss);
    }

    public void addStat(EnumStat stat, long valueToAdd)
    {
        var uss = this.load();
        var id = stat.getID();
        var statValue = STATS[id].read(uss);
        STATS[id].write(uss, statValue + valueToAdd);
        this.save(uss);
    }

    public void incrementStat(EnumStat stat)
    {
        this.addStat(stat, 1);
    }

    public void setJackpot(long pool, long stored)
    {
        var uss = this.load();
        JACKPOT.write(uss, pool);
        JACKPOT_STORED.write(uss, stored);
        this.save(uss);
    }

    private void save(USS2PropertyObject uss)
    {
        if (this.newObj)
        {
            this.db.simpleUpdate("INSERT INTO " + TABLE_USS2 + "(uss_us_id, uss_data) VALUES (?, ?)", this.fid, uss.getDataByteArray());
        }
        else if (uss.isDirty())
        {
            this.db.simpleUpdate("UPDATE " + TABLE_USS2 + " SET uss_data=? WHERE uss_us_id=?", uss.getDataByteArray(), this.fid);
        }
    }
}
