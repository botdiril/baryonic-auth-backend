package com.botdiril.api.handler.inventory;

import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;

import com.botdiril.BotMain;
import com.botdiril.util.RequestUtil;
import com.botdiril.util.ResponseObject;

public class PlayerInventoryOverviewEndpoint
{
    public static void getOverview(ServerRequest req, ServerResponse res)
    {
        try (var db = BotMain.SQL_MANAGER.getReadOnly())
        {
            var player = RequestUtil.parsePlayer(db, req);
            var ui = player.inventory();
            var overview = ui.getOverview();

            res.send(ResponseObject.of(overview));
        }
    }
}
