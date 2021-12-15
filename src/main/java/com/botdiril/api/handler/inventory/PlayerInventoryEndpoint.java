package com.botdiril.api.handler.inventory;

import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;

import com.botdiril.BotMain;
import com.botdiril.util.RequestUtil;
import com.botdiril.util.ResponseObject;

public class PlayerInventoryEndpoint
{
    public static void getItemCount(ServerRequest req, ServerResponse res)
    {
        var path = req.path();
        var item = RequestUtil.parseItem(path.param("item"));

        try (var db = BotMain.SQL_MANAGER.getReadOnly())
        {
            var player = RequestUtil.parsePlayer(db, req);
            var ui = player.inventory();
            res.send(ResponseObject.of(ui.howManyOf(item)));
        }

    }
}
