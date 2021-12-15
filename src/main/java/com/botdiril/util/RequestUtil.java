package com.botdiril.util;

import io.helidon.webserver.BadRequestException;
import io.helidon.webserver.ServerRequest;

import com.botdiril.framework.EntityPlayer;
import com.botdiril.framework.ReadOnlyEntityPlayer;
import com.botdiril.framework.sql.connection.ReadDBConnection;
import com.botdiril.framework.sql.connection.WriteDBConnection;
import com.botdiril.gamedata.item.Item;

public class RequestUtil
{
    private static int parseFID(ReadDBConnection db, String fid)
    {
        int fkid;

        /*
        if ("@self".equals(fid))
        {
            // TODO
        }
        else*/
        {
            fkid = Integer.parseInt(fid);
        }

        return fkid;
    }

    public static ReadOnlyEntityPlayer parsePlayer(ReadDBConnection db, ServerRequest req)
    {
        var path = req.path();
        var fid = path.param("fid");
        int fkid = parseFID(db, fid);

        return new ReadOnlyEntityPlayer(db, fkid);
    }

    public static EntityPlayer parsePlayer(WriteDBConnection db, ServerRequest req)
    {
        var path = req.path();
        var fid = path.param("fid");
        int fkid = parseFID(db, fid);

        return new EntityPlayer(db, fkid);
    }

    public static Item parseItem(String name)
    {
        var item = Item.getItemByName(name);

        if (item == null)
            throw new BadRequestException("Item not found.");

        return item;
    }
}
