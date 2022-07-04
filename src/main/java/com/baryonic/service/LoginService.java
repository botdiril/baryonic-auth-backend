package com.baryonic.service;

import com.baryonic.api.data.discord.DiscordBasicUserInfo;
import com.baryonic.api.data.discord.DiscordLoginInfo;
import com.baryonic.api.data.login.JWTUserLoginInfo;
import com.baryonic.api.data.login.OAuth2TokenInfo;
import com.baryonic.api.request.login.DiscordOAuth2Login;
import io.helidon.common.http.FormParams;
import io.helidon.common.http.Http;
import io.helidon.common.http.MediaType;
import io.helidon.common.reactive.Single;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbTransaction;
import io.helidon.media.common.DefaultMediaSupport;
import io.helidon.media.jackson.JacksonSupport;
import io.helidon.security.integration.webserver.WebSecurity;
import io.helidon.webclient.WebClient;
import io.helidon.webserver.*;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import redis.clients.jedis.JedisPool;

import java.util.UUID;

public class LoginService implements Service
{
    private static final WebClient DISCORD_API;
    private static final WebClient DISCORD_TOKEN_API;

    private final PublicJsonWebKey jwk;
    private final JedisPool jedisPool;
    private final DbClient dbClient;

    private final UserService.CloudflareAuth cloudflareAuth;

    private final String discordClientID;
    private final String discordPrivateKey;
    private final String discordRedirectUrl;

    static
    {
        DISCORD_API = WebClient.builder()
                               .baseUri("https://discord.com/api")
                               .addReader(JacksonSupport.reader())
                               .addWriter(JacksonSupport.writer())
                               .addMediaSupport(JacksonSupport.create())
                               .build();

        DISCORD_TOKEN_API = WebClient.builder()
                                     .baseUri("https://discord.com/api")
                                     .addReader(JacksonSupport.reader())
                                     .addWriter(DefaultMediaSupport.formParamWriter())
                                     .addMediaSupport(JacksonSupport.create())
                                     .build();
    }

    public LoginService(PublicJsonWebKey jwk, JedisPool jedisPool, UserService.CloudflareAuth cloudflareAuth, DbClient dbClient, String discordClientID, String discordPrivateKey, String discordRedirectURL)
    {
        this.jwk = jwk;
        this.cloudflareAuth = cloudflareAuth;
        this.jedisPool = jedisPool;
        this.dbClient = dbClient;
        this.discordClientID = discordClientID;
        this.discordPrivateKey = discordPrivateKey;
        this.discordRedirectUrl = discordRedirectURL;
    }

    @Override
    public void update(Routing.Rules rules)
    {
        rules.post("/discord-oauth2", WebSecurity.allowAnonymous(), Handler.create(DiscordOAuth2Login.class, this::loginDiscord));
    }


    public void loginDiscord(ServerRequest req, ServerResponse res, DiscordOAuth2Login loginInfo)
    {
        var options = FormParams.builder()
                                .add("client_id", this.discordClientID)
                                .add("client_secret", this.discordPrivateKey)
                                .add("grant_type", "authorization_code")
                                .add("redirect_uri", this.discordRedirectUrl)
                                .add("scope", "identify")
                                .add("code", loginInfo.code())
                                .build();

        var tokenInfo = DISCORD_TOKEN_API.post()
                                         .path("/oauth2/token")
                                         .accept(MediaType.APPLICATION_JSON)
                                         .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                         .submit(options, OAuth2TokenInfo.class)
                                         .exceptionallyAccept(throwable -> {
                                             throw new BadRequestException("Invalid Discord OAuth2 code or other error.");
                                         })
                                         .await();

        if (tokenInfo == null)
            return;

        var userInfo = DISCORD_API.get()
                                  .path("/users/@me")
                                  .headers(headers -> headers.add("Authorization", tokenInfo.tokenType() + " " + tokenInfo.accessToken()))
                                  .request(DiscordBasicUserInfo.class)
                                  .await();

        if (userInfo.bot())
        {
            throw new BadRequestException("Bot accounts are not supported.");
        }

        // Attempt to retrieve a user by their Discord ID.
        // If the such user account doesn't exist, create it.
        var responseLoginInfo = this.dbClient.inTransaction(txn -> loginOrRegister(txn, userInfo)).await();

        res.status(responseLoginInfo.registered() ? Http.Status.CREATED_201 : Http.Status.OK_200)
           .send(responseLoginInfo.loginInfo());
    }

    record LoginStatus(JWTUserLoginInfo loginInfo, boolean registered)
    {

    }

    private Single<LoginStatus> loginOrRegister(DbTransaction txn, DiscordBasicUserInfo userInfo)
    {
        record UserRow(long id, String username, String discriminator, String email, UUID avatar, boolean wasRegistered)
        {

        }

        var discordID = Long.parseUnsignedLong(userInfo.id());

        return txn.get("""
            SELECT `us_id`, `us_username`, `us_discriminator`, `us_email`, `us_avatar`
            FROM `b50_user`.`discord_users`
            INNER JOIN `b50_user`.`users` ON `discord_users`.`du_us_id` = `users`.`us_id`
            WHERE `du_id` = ?
            """, discordID)
            .flatMapOptional(row -> row)
            .map(row -> new UserRow(
                row.column("us_id").as(Long.class),
                row.column("us_username").as(String.class),
                row.column("us_discriminator").as(String.class),
                row.column("us_email").as(String.class),
                UUID.fromString(row.column("us_avatar").as(String.class)),
                false))
            .defaultIfEmpty(() -> {
                // User not found in database, create a new one.
                         // Pull the user's avatar from the Discord API.
                var avatarUUID = userInfo.avatar() == null ? null : UserService.pullFromDiscord(this.cloudflareAuth, userInfo.id(), userInfo.avatar());
                         // Create a new account
                // UUID handling was broken at the time of writing this, so we're using passing it as a string.
                var base = txn.query("""
                    INSERT INTO `b50_user`.`users` (`us_username`, `us_discriminator`, `us_email`, `us_avatar`)
                    VALUES (?, ?, ?, ?)
                    RETURNING `us_id`
                    """,
                    userInfo.username(), userInfo.discriminator(), userInfo.email(), avatarUUID != null ? avatarUUID.toString() : null)
                    .first()
                    .await();

                var row = new UserRow(
                   base.column("us_id").as(Long.class),
                   userInfo.username(),
                   userInfo.discriminator(),
                   userInfo.email(),
                   avatarUUID,
                   true);

                txn.insert("""
                    INSERT INTO `b50_user`.`discord_users` (`du_id`, `du_us_id`)
                    VALUES (?, ?)
                    """, discordID, row.id())
                   .await();

                return row;
            }).map(acc -> {
                try (var jedis = jedisPool.getResource())
                {
                    var keyRefID = jedis.incr("baryonic:jwt_key_ref_ctr");
                    var token = this.generateTokenSelf(keyRefID);
                    jedis.set("baryonic:jwt:" + keyRefID, Long.toUnsignedString(acc.id()));
                    return new LoginStatus(new JWTUserLoginInfo(
                        acc.id(),
                        token,
                        acc.username(),
                        acc.discriminator(),
                        acc.avatar(),
                        new DiscordLoginInfo(
                            Long.toUnsignedString(discordID)
                        )
                    ), acc.wasRegistered());
                }
                catch (JoseException e)
                {
                    throw new RuntimeException(e);
                }
            });
    }

    private String generateTokenSelf(long keyRefID) throws JoseException
    {
        var claims = new JwtClaims();
        claims.setIssuer("baryonic.auth");
        claims.setAudience("baryonic.auth", "baryonic.api");
        claims.setExpirationTimeMinutesInTheFuture(60 * 24 * 30);
        claims.setGeneratedJwtId();
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(2);
        claims.setSubject(Long.toUnsignedString(keyRefID));
        claims.setClaim("grant_type", "user_oauth2");

        var jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(this.jwk.getPrivateKey());
        jws.setKeyIdHeaderValue(this.jwk.getKeyId());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.ECDSA_USING_P384_CURVE_AND_SHA384);

        return jws.getCompactSerialization();
    }
}
