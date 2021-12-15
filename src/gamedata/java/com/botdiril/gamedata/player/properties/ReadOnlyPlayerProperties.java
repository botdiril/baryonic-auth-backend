package com.botdiril.gamedata.player.properties;

import org.apache.commons.lang3.tuple.Pair;

import java.nio.ByteBuffer;

import com.botdiril.MajorFailureException;
import com.botdiril.framework.sql.SqlFoundation;
import com.botdiril.framework.sql.connection.ReadDBConnection;
import com.botdiril.framework.sql.connection.WriteDBConnection;
import com.botdiril.framework.util.PlayerRestrictions;
import com.botdiril.gamedata.player.inventory.InventoryTables;
import com.botdiril.gamedata.stat.EnumStat;
import com.botdiril.gamedata.tempstat.EnumBlessing;
import com.botdiril.gamedata.tempstat.EnumCurse;
import com.botdiril.uss2.properties.USS2PropertyObject;
import com.botdiril.uss2.properties.USS2PropertySchema;

public class ReadOnlyPlayerProperties
{
    public static final String TABLE_USS2 = "uss2";
    public static final int USS_SIZE = 4096;

    public static final byte PO_VERSION_0 = 0;
    public static final byte PO_VERSION_1 = 1;

    protected static USS2PropertySchema.USS2Byte ALIAS_USED;
    protected static USS2PropertySchema.USS2String[] ALIAS_IN;
    protected static USS2PropertySchema.USS2String[] ALIAS_OUT;
    protected static USS2PropertySchema.USS2Long PREFERENCES_BITFIELD;

    protected static USS2PropertySchema.USS2Long[] BLESSINGS;
    protected static USS2PropertySchema.USS2Long[] CURSES;
    protected static USS2PropertySchema.USS2Long[] STATS;

    protected static USS2PropertySchema.USS2Long JACKPOT;
    protected static USS2PropertySchema.USS2Long JACKPOT_STORED;

    protected static USS2PropertySchema schema;

    protected final int fid;
    protected final ReadDBConnection db;
    protected boolean newObj;

    public ReadOnlyPlayerProperties(ReadDBConnection db, int fid)
    {
        this.db = db;
        this.fid = fid;
    }

    public static void init(WriteDBConnection db)
    {
        schema = new USS2PropertySchema(USS_SIZE)
        {{
            ALIAS_USED = this.declareByte(PO_VERSION_0);

            ALIAS_IN = new USS2String[Byte.SIZE];
            for (int i = 0; i < ALIAS_IN.length; i++)
                ALIAS_IN[i] = this.declareString(PO_VERSION_0, (byte) (Character.BYTES * PlayerRestrictions.ALIAS_IN_MAX_LENGTH));

            ALIAS_OUT = new USS2String[Byte.SIZE];
            for (int i = 0; i < ALIAS_OUT.length; i++)
                ALIAS_OUT[i] = this.declareString(PO_VERSION_0, (byte) (Character.BYTES * PlayerRestrictions.ALIAS_OUT_MAX_LENGTH));

            PREFERENCES_BITFIELD = this.declareLong(PO_VERSION_0);

            BLESSINGS = new USS2Long[EnumBlessing.MAX_BLESSINGS];
            for (int i = 0; i < BLESSINGS.length; i++)
                BLESSINGS[i] = this.declareLong(PO_VERSION_0);

            CURSES = new USS2Long[EnumCurse.MAX_CURSES];
            for (int i = 0; i < CURSES.length; i++)
                CURSES[i] = this.declareLong(PO_VERSION_0);

            STATS = new USS2Long[EnumStat.MAX_STATS];
            for (int i = 0; i < STATS.length; i++)
                STATS[i] = this.declareLong(PO_VERSION_0);

            JACKPOT = this.declareLong(PO_VERSION_1);
            JACKPOT_STORED = this.declareLong(PO_VERSION_1);
        }};

        schema.printInfo();

        try
        {
            if (!SqlFoundation.checkTableExists(db, TABLE_USS2))
            {
                db.simpleExecute("CREATE TABLE " + TABLE_USS2 + " (" +
                                 "uss_us_id INT NOT NULL PRIMARY KEY, " +
                                 "uss_data BLOB(" + USS_SIZE + "), " +
                                 "FOREIGN KEY (uss_us_id) REFERENCES " + InventoryTables.TABLE_USER + "(us_id)" +
                                 ")");
            }
        }
        catch (Exception e)
        {
            throw new MajorFailureException("Failed to init the property object table!", e);
        }
    }

    public Pair<String, String> getAlias(int aliasNum)
    {
        var uss = this.load();
        return Pair.of(ALIAS_IN[aliasNum].read(uss), ALIAS_OUT[aliasNum].read(uss));
    }

    public long getPreferencesBitfield()
    {
        var uss = this.load();
        return PREFERENCES_BITFIELD.read(uss);
    }

    public byte getUsedAliases()
    {
        var uss = this.load();
        return ALIAS_USED.read(uss);
    }

    public long getStat(EnumStat stat)
    {
        var uss = this.load();
        return STATS[stat.getID()].read(uss);
    }

    public long getJackpot()
    {
        var uss = this.load();
        return JACKPOT.read(uss);
    }

    public long getBlessing(EnumBlessing blessing)
    {
        var uss = this.load();
        return BLESSINGS[blessing.getID()].read(uss);
    }

    public long getCurse(EnumCurse curse)
    {
        var uss = this.load();
        return CURSES[curse.getID()].read(uss);
    }

    public long getJackpotStored()
    {
        var uss = this.load();
        return JACKPOT_STORED.read(uss);
    }

    protected USS2PropertyObject load()
    {
        var data = db.getValue("SELECT uss_data FROM " + TABLE_USS2 + " WHERE uss_us_id=?", "uss_data", byte[].class, this.fid);
        this.newObj = data.isEmpty();
        return this.newObj ? USS2PropertyObject.create(schema) : USS2PropertyObject.from(ByteBuffer.wrap(data.get()), schema);
    }

    public final int getFID()
    {
        return this.fid;
    }

}
