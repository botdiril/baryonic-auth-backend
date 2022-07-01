package com.baryonic.util;

import io.helidon.dbclient.DbClient;

public class RequestUtil
{
    private static int parseFID(DbClient db, String fid)
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
}
