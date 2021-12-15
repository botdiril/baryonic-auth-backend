package com.botdiril;

import io.helidon.common.http.Http;
import io.helidon.media.jackson.JacksonSupport;
import io.helidon.security.Security;
import io.helidon.security.spi.SecurityProvider;
import io.helidon.webserver.BadRequestException;
import io.helidon.webserver.Handler;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.cors.CorsSupport;
import io.helidon.webserver.cors.CrossOriginConfig;

import com.botdiril.api.data.request.DiscordOAuth2Login;
import com.botdiril.api.handler.inventory.PlayerInventoryEndpoint;
import com.botdiril.api.handler.inventory.PlayerInventoryOverviewEndpoint;
import com.botdiril.api.handler.mesondb.TableEndpoint;
import com.botdiril.api.handler.metrics.PlayerMetricsEndpoint;
import com.botdiril.api.handler.user.LoginEndpoint;
import com.botdiril.framework.sql.DBException;
import com.botdiril.internal.BotdirilConfig;
import com.botdiril.util.ResponseObject;

public class Botdiril
{
    public static final String BRANDING = "Meson";
    public static final String REPO_URL = "https://github.com/493msi/botdiril400";

    private final BotdirilConfig config;

    public Botdiril(BotdirilConfig config)
    {
        this.config = config;
    }

    public void start()
    {
        var corsSettings = CrossOriginConfig.builder()
            .allowOrigins("*")
            .allowMethods("PUT", "DELETE", "POST", "GET", "PATCH")
            .build();

        var corsSupport = CorsSupport.builder()
            .addCrossOrigin(corsSettings)
            .build();

        var routing = Routing.builder()
            .register(corsSupport)
            .error(BadRequestException.class, (req, res, ex) -> res.send(ResponseObject.of(Http.Status.BAD_REQUEST_400, "Bad Request")))
            .error(NumberFormatException.class, (req, res, ex) -> res.send(ResponseObject.of(Http.Status.BAD_REQUEST_400, "Bad Request")))
            .error(DBException.class, (req, res, ex) -> {
                ex.printStackTrace();
                res.send(ResponseObject.of(Http.Status.INTERNAL_SERVER_ERROR_500, "Internal Server Error"));
            })
            .error(Exception.class, (req, res, ex) -> {
                ex.printStackTrace();
                res.send(ResponseObject.of(Http.Status.INTERNAL_SERVER_ERROR_500, "Internal Server Error"));
            })
            .get("/players/{fid}/overview", PlayerInventoryOverviewEndpoint::getOverview)
            .get("/players/{fid}/inventory/{item}", PlayerInventoryEndpoint::getItemCount)
            .get("/players/{fid}/metrics/{item}", PlayerMetricsEndpoint::getItemMetrics)
            .post("/login/discord-oauth2", Handler.create(DiscordOAuth2Login.class, LoginEndpoint::loginDiscord))
            .get("/meson/tables/list", TableEndpoint::getTables)
            .build();

        WebServer.builder()
            .routing(routing)
            .port(this.config.getPort())
            .addMediaSupport(JacksonSupport.create())
            .build()
            .start();
    }
}
