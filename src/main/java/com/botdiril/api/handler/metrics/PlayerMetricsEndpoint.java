package com.botdiril.api.handler.metrics;

import io.helidon.webserver.BadRequestException;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import org.intellij.lang.annotations.Language;

import java.util.Arrays;

import com.botdiril.BotMain;
import com.botdiril.gamedata.metrics.EnumMetric;
import com.botdiril.util.RequestUtil;
import com.botdiril.util.ResponseObject;

public class PlayerMetricsEndpoint
{
    private static final int LENGTH_LIMIT = 10000;

    public static void getItemMetrics(ServerRequest req, ServerResponse res)
    {
        var path = req.path();
        var metricOpt = EnumMetric.getMetric(path.param("item"));

        if (metricOpt.isEmpty())
            throw new BadRequestException("No such player metric.");

        try (var db = BotMain.SQL_MANAGER.getReadOnly())
        {
            var player = RequestUtil.parsePlayer(db, req);
            var ui = player.inventory();
            var fid = ui.getFID();
            var metric = metricOpt.get();
            var metricKey = metric.getDBKey();

            @Language("MySQL")
            var sql = "SELECT um_commandid, " + metricKey + " FROM (SELECT * FROM `metrics` WHERE fk_us_id = ? ORDER BY um_commandid DESC LIMIT ?) AS m ORDER BY um_commandid";

            var metrics = db.getMap(sql, "um_commandid", Integer.class, metricKey, metric.getValueType(), fid, LENGTH_LIMIT);
            res.send(ResponseObject.of(metrics));
        }

    }
}
