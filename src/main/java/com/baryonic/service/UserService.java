package com.baryonic.service;

import com.baryonic.util.BotdirilLog;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.helidon.common.http.Http;
import io.helidon.common.http.MediaType;
import io.helidon.dbclient.DbClient;
import io.helidon.media.jackson.JacksonSupport;
import io.helidon.media.multipart.FileFormParams;
import io.helidon.media.multipart.MultiPartBodyWriter;
import io.helidon.media.multipart.MultiPartSupport;
import io.helidon.security.SecurityContext;
import io.helidon.security.integration.webserver.WebSecurity;
import io.helidon.webclient.WebClient;
import io.helidon.webserver.*;
import redis.clients.jedis.JedisPool;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletionException;

public class UserService implements Service
{
    private static final WebClient CLOUDFLARE_IMAGES_API;

    static
    {
        CLOUDFLARE_IMAGES_API = WebClient.builder()
                                         .baseUri("https://api.cloudflare.com/client/v4")
                                         .addReader(JacksonSupport.reader())
                                         .addWriter(MultiPartBodyWriter.create())
                                         .addMediaSupport(JacksonSupport.create())
                                         .addMediaSupport(MultiPartSupport.create())
                                         .build();
    }

    private final JedisPool pool;
    private final DbClient dbClient;

    public UserService(JedisPool pool, DbClient dbClient)
    {
        this.pool = pool;
        this.dbClient = dbClient;
    }

    @Override
    public void update(Routing.Rules rules)
    {
        rules.any(WebSecurity.rolesAllowed("user"))
             .get("/@self/basic-info", this::getUser)
             .get("/{id}/basic-info", WebSecurity.rolesAllowed("superuser"), this::getUser)
             .delete("/@self/log-out", this::logOut);
    }

    private record BasicUserInfo(
        long id,
        Instant created,
        String username,
        String discriminator,
        String avatar
    )
    {

    }

    private void getUser(ServerRequest req, ServerResponse res)
    {
        var user = req.context()
                      .get(SecurityContext.class)
                      .flatMap(SecurityContext::userPrincipal)
                      .orElseThrow();

        var userID = Long.parseUnsignedLong(user.id());

        var basicInfoOpt = this.dbClient.execute(db -> db.get("""
            SELECT `us_id`, `us_username`, `us_discriminator`, `us_avatar`, `us_created`
            FROM `b50_user`.`users`
            WHERE `us_id` = ?
            """, userID)).await();

        if (basicInfoOpt.isEmpty())
        {
            res.status(Http.Status.NOT_FOUND_404).send("User not found");
            return;
        }

        var basicInfo = basicInfoOpt.get();

        res.send(new BasicUserInfo(
            basicInfo.column("us_id").as(Long.class),
            basicInfo.column("us_created").as(Timestamp.class).toInstant(),
            basicInfo.column("us_username").as(String.class),
            basicInfo.column("us_discriminator").as(String.class),
            basicInfo.column("us_avatar").as(String.class)
        ));
    }

    private void logOut(ServerRequest req, ServerResponse res)
    {
        var keyRefID = (String) req.context()
                               .get(SecurityContext.class)
                               .flatMap(SecurityContext::user)
                               .flatMap(u -> u.abacAttribute("key_ref_id"))
                               .orElseThrow();

        try (var jedis = this.pool.getResource())
        {
            if (jedis.del("baryonic:jwt:" + keyRefID) == 0)
            {
                res.status(Http.Status.NOT_FOUND_404).send("Login token not found.");
                return;
            }
        }

        res.status(Http.Status.RESET_CONTENT_205).send("Logged out.");
    }


    public record CloudflareAuth(
        String accountID,
        String token
    )
    {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CloudflareImageResult(
        UUID id
    )
    {

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CloudflareImageResponse(
        boolean success,
        CloudflareImageResult result
    )
    {

    }

    public static UUID pullFromDiscord(CloudflareAuth cloudflareAuth, String discordUserID, String discordAvatarHash)
    {
        try
        {
            var formParams = FileFormParams.builder()
                                           .add("url", "https://cdn.discordapp.com/avatars/" + discordUserID + "/" + discordAvatarHash + ".webp")
                                           .add("requireSignedURLs", "false")
                                           .build();

            var response = CLOUDFLARE_IMAGES_API.post()
                                                 .path("/accounts/" + cloudflareAuth.accountID() + "/images/v1")
                                                 .contentType(MediaType.MULTIPART_FORM_DATA)
                                                 .accept(MediaType.APPLICATION_JSON)
                                                 .headers(headers -> headers.add("Authorization", "Bearer " + cloudflareAuth.token()))
                                                 .submit(formParams, CloudflareImageResponse.class)
                                                 .await();

            if (!response.success())
            {
                BotdirilLog.logger.error("Failed to pull image from Discord.");
                return null;
            }

            return response.result().id();
        }
        catch (CompletionException e)
        {
            BotdirilLog.logger.error("Failed to pull image from Discord: ", e);
            return null;
        }
    }
}
