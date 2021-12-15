package com.botdiril.api.handler.mesondb;

import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;

import com.botdiril.BotMain;
import com.botdiril.framework.sql.SqlFoundation;

public class TableEndpoint
{
    public static void getTables(ServerRequest req, ServerResponse res)
    {
        try (var db = BotMain.SQL_MANAGER.getReadOnly())
        {
            var tables = db.getList("SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = ?", "TABLE_NAME", String.class, SqlFoundation.SCHEMA);
            res.send(tables);
        }

    }
}
