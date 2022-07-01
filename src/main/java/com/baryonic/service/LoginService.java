package com.baryonic.service;

import com.baryonic.api.data.discord.DiscordBasicUserInfo;
import com.baryonic.api.data.discord.DiscordLoginInfo;
import com.baryonic.api.data.discord.OAuth2TokenInfo;
import com.baryonic.api.data.request.DiscordOAuth2Login;
import com.baryonic.util.ResponseObject;
import io.helidon.common.http.FormParams;
import io.helidon.common.http.Http;
import io.helidon.common.http.MediaType;
import io.helidon.dbclient.DbClient;
import io.helidon.media.common.DefaultMediaSupport;
import io.helidon.media.jackson.JacksonSupport;
import io.helidon.webclient.WebClient;
import io.helidon.webserver.*;

public class LoginService implements Service
{
    private static final WebClient DISCORD_API;
    private static final WebClient DISCORD_TOKEN_API;

    private final DbClient dbClient;

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

    public LoginService(DbClient dbClient, String discordClientID, String discordPrivateKey, String discordRedirectURL)
    {
        this.dbClient = dbClient;
        this.discordClientID = discordClientID;
        this.discordPrivateKey = discordPrivateKey;
        this.discordRedirectUrl = discordRedirectURL;
    }

    @Override
    public void update(Routing.Rules rules)
    {
        rules.post("/discord-oauth2", Handler.create(DiscordOAuth2Login.class, this::loginDiscord));
    }


    public void loginDiscord(ServerRequest req, ServerResponse res, DiscordOAuth2Login loginInfo)
    {
        var options = FormParams.builder()
                                .add("client_id", this.discordClientID)
                                .add("client_secret", this.discordPrivateKey)
                                .add("grant_type", "authorization_code")
                                .add("redirect_uri", this.discordRedirectUrl + "/?action=login&loginMethod=oauth2-discord")
                                .add("scope", "identify")
                                .add("code", loginInfo.code())
                                .build();

        var tokenInfo = DISCORD_TOKEN_API.post()
                                         .path("/oauth2/token")
                                         .accept(MediaType.APPLICATION_JSON)
                                         .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                         .submit(options, OAuth2TokenInfo.class)
                                         .exceptionallyAccept(throwable -> {
                                             res.send(ResponseObject.of(Http.Status.BAD_REQUEST_400, "Invalid Discord OAuth2 code or other error."));
                                         })
                                         .await();

        if (tokenInfo == null)
            return;

        var userInfoReq = DISCORD_API.get()
                                     .path("/users/@me");
        userInfoReq.headers().add("Authorization", tokenInfo.tokenType() + " " + tokenInfo.accessToken());
        var userInfo = userInfoReq.request(DiscordBasicUserInfo.class)
                                  .await();

        var row = this.dbClient.execute(exec -> exec.get("SELECT `du` FROM `b50_user`.`discord_users` WHERE `du_userid` = ?", Long.parseUnsignedLong(userInfo.id())))
                               .await();

        if (row.isEmpty())
        {
            res.send(ResponseObject.of(Http.Status.NOT_FOUND_404, "No such user!"));
            return;
        }

        var rowData = row.get();

        res.send(ResponseObject.of(new DiscordLoginInfo(
            rowData.column("du").as(int.class),
            userInfo.id(),
            userInfo.username(),
            userInfo.discriminator(),
            userInfo.avatar(),
            userInfo.bot()
        )));
    }
}
