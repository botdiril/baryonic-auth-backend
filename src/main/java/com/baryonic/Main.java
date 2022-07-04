package com.baryonic;

import com.baryonic.auth.JWTAuthProvider;
import com.baryonic.service.LoginService;
import com.baryonic.service.UserService;
import com.baryonic.util.BotdirilLog;
import io.helidon.common.http.Http;
import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import io.helidon.config.spi.ConfigNode;
import io.helidon.dbclient.DbClient;
import io.helidon.media.jackson.JacksonSupport;
import io.helidon.security.Security;
import io.helidon.security.integration.webserver.WebSecurity;
import io.helidon.webserver.BadRequestException;
import io.helidon.webserver.NotFoundException;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.cors.CorsSupport;
import io.helidon.webserver.cors.CrossOriginConfig;
import org.jose4j.jwk.EcJwkGenerator;
import org.jose4j.keys.EcKeyUtil;
import org.jose4j.keys.EllipticCurves;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.params.SetParams;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

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

            var keyID = UUID.randomUUID().toString();

            var jwk = EcJwkGenerator.generateJwk(EllipticCurves.P384);
            jwk.setKeyId(keyID);

            var jedisPool = new JedisPool(baryonicConfig.getRedisHost(), Protocol.DEFAULT_PORT);

            try (var jedis = jedisPool.getResource())
            {
                var key = "baroynic:public_keys:" + keyID;
                jedis.set(key, EcKeyUtil.pemEncode(jwk.getPublicKey()), SetParams.setParams().ex(60L * 60L * 24L * 90L));
            }

            var dbClient = DbClient.builder(Config.create(ConfigSources.create(dbConfig)))
                                   .build();

            var corsSettings = CrossOriginConfig.builder()
                                                .allowOrigins("*")
                                                .allowMethods("PUT", "DELETE", "POST", "GET", "PATCH")
                                                .build();

            var corsSupport = CorsSupport.builder()
                                         .addCrossOrigin(corsSettings)
                                         .build();

            var authProvider = new JWTAuthProvider(jedisPool);

            var security = Security.builder()
                .addAuthenticationProvider(authProvider)
                .addAuthorizationProvider(authProvider)
                .build();

            var routing = Routing.builder()
                                 .register(corsSupport)
                                 .register(WebSecurity.create(security).securityDefaults(WebSecurity.authorize()))
                                 .error(BadRequestException.class, (req, res, ex) -> res.status(Http.Status.BAD_REQUEST_400).send(Objects.requireNonNullElse(ex.getMessage(), "Bad Request")))
                                 .error(NumberFormatException.class, (req, res, ex) -> res.status(Http.Status.BAD_REQUEST_400).send("Bad Request"))
                                 .error(NotFoundException.class, (req, res, ex) -> res.status(Http.Status.NOT_FOUND_404).send("Not Found"))
                                 .error(Exception.class, (req, res, ex) -> {
                                     ex.printStackTrace();
                                     res.status(Http.Status.INTERNAL_SERVER_ERROR_500).send("Internal Server Error");
                                 })
                                 .register("/users", new UserService(jedisPool, dbClient))
                                 .register("/login", new LoginService(jwk, jedisPool, baryonicConfig.getCloudflareImagesAuth(), dbClient, baryonicConfig.getDiscordClientID(), baryonicConfig.getDiscordClientSecret(), baryonicConfig.getDiscordRedirectURL()))
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
