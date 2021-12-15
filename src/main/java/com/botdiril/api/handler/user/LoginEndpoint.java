package com.botdiril.api.handler.user;

import io.helidon.common.http.FormParams;
import io.helidon.common.http.Http;
import io.helidon.common.http.MediaType;
import io.helidon.media.common.DefaultMediaSupport;
import io.helidon.media.jackson.JacksonSupport;
import io.helidon.media.jsonp.JsonpSupport;
import io.helidon.webclient.WebClient;
import io.helidon.webserver.NotFoundException;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;

import java.util.HashMap;

import com.botdiril.BotMain;
import com.botdiril.api.data.discord.DiscordBasicUserInfo;
import com.botdiril.api.data.discord.DiscordLoginInfo;
import com.botdiril.api.data.discord.OAuth2TokenInfo;
import com.botdiril.api.data.request.DiscordOAuth2Login;
import com.botdiril.framework.sql.SqlFoundation;
import com.botdiril.util.ResponseObject;

public class LoginEndpoint
{
    private static final WebClient DISCORD_API;
    private static final WebClient DISCORD_TOKEN_API;

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

    public static void loginDiscord(ServerRequest req, ServerResponse res, DiscordOAuth2Login loginInfo)
    {
        var options = FormParams.builder()
            .add("client_id", BotMain.config.getDiscordClientID())
            .add("client_secret", BotMain.config.getDiscordClientSecret())
            .add("grant_type", "authorization_code")
            .add("redirect_uri", "http://localhost:9000/?action=login&loginMethod=oauth2-discord")
            .add("scope", "identify")
            .add("code", loginInfo.code())
            .build();

        var tokenInfo = DISCORD_TOKEN_API.post()
            .path("/oauth2/token")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .submit(options, OAuth2TokenInfo.class)
            .await();

        var userInfoReq = DISCORD_API.get()
            .path("/users/@me");
        userInfoReq.headers().add("Authorization", tokenInfo.tokenType() + " " + tokenInfo.accessToken());
        var userInfo = userInfoReq.request(DiscordBasicUserInfo.class)
            .await();


        try (var db = BotMain.SQL_MANAGER.getReadOnly())
        {
            var idValue = db.getValue("SELECT `us_id` FROM `users` WHERE `us_userid` = ?", "us_id", Integer.class, userInfo.id());

            if (idValue.isEmpty())
            {
                res.send(ResponseObject.of(Http.Status.NOT_FOUND_404, "No such user!"));
                return;
            }

            res.send(ResponseObject.of(new DiscordLoginInfo(
                idValue.get(),
                userInfo.id(),
                userInfo.username(),
                userInfo.discriminator(),
                userInfo.avatar(),
                userInfo.bot()
            )));
        }
   }
}
