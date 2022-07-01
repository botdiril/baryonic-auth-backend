package com.baryonic;

import com.baryonic.service.LoginService;
import com.baryonic.util.BotdirilLog;
import com.baryonic.util.ResponseObject;
import io.helidon.common.http.Http;
import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import io.helidon.config.spi.ConfigNode;
import io.helidon.dbclient.DbClient;
import io.helidon.media.jackson.JacksonSupport;
import io.helidon.webserver.BadRequestException;
import io.helidon.webserver.NotFoundException;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.cors.CorsSupport;
import io.helidon.webserver.cors.CrossOriginConfig;

import java.util.Locale;

public class Main
{
    public static void main(String[] args)
    {
        BotdirilLog.init();

        BotdirilLog.logger.info("=====================================");
        BotdirilLog.logger.info("####        BARYONIC 500         ####");
        BotdirilLog.logger.info("=====================================");

        Locale.setDefault(Locale.US);


        try
        {
            var baryonicConfig = BaryonicConfig.load();

            var dbConnectionConfig = ConfigNode.ObjectNode.builder()
                .addValue("url", "jdbc:mariadb://" + baryonicConfig.getSqlHost())
                .addValue("username", baryonicConfig.getSqlUser())
                .addValue("password", baryonicConfig.getSqlPass())
                .addValue("poolName", "mysql")
                .build();

            var dbConfig = ConfigNode.ObjectNode.builder()
                                                .addValue("source", "jdbc")
                                                .addNode("connection", dbConnectionConfig)
                                                .addList("statements", ConfigNode.ListNode.builder().build())
                                                .build();

            var dbClient = DbClient.builder(Config.create(ConfigSources.create(dbConfig)))
                                   .build();

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
                                 .error(NotFoundException.class, (req, res, ex) -> res.send(ResponseObject.of(Http.Status.NOT_FOUND_404, "Not Found")))
                                 .error(Exception.class, (req, res, ex) -> {
                                     ex.printStackTrace();
                                     res.send(ResponseObject.of(Http.Status.INTERNAL_SERVER_ERROR_500, "Internal Server Error"));
                                 })
                                 .register("/login", new LoginService(dbClient, baryonicConfig.getDiscordClientID(), baryonicConfig.getDiscordClientSecret(), baryonicConfig.getDiscordRedirectURL()))
                                 .build();

            WebServer.builder()
                     .routing(routing)
                     .port(baryonicConfig.getPort())
                     .addMediaSupport(JacksonSupport.create())
                     .build()
                     .start();
        }
        catch (MajorFailureException e)
        {
            BotdirilLog.logger.fatal("An unrecoverable failure has occurred while initializing the bot.", e);
        }
        catch (Exception e)
        {
            BotdirilLog.logger.fatal("A general unrecoverable failure has occurred while initializing the bot.", e);
        }
    }
}
