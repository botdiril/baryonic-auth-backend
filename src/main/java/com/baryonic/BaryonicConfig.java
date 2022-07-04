package com.baryonic;

import com.baryonic.service.UserService;

public class BaryonicConfig
{
    private int port;

    private String redisHost;

    private String sqlHost;

    private String sqlUser;

    private String sqlPass;

    private String discordClientID;

    private String discordClientSecret;

    private String discordRedirectURL;

    private UserService.CloudflareAuth cloudflareImagesAuth;

    public static BaryonicConfig load()
    {
        try
        {
            var config = new BaryonicConfig();
            config.port = Integer.parseInt(System.getenv("BARYON_PORT"));
            config.redisHost = System.getenv("BARYON_REDIS_HOST");
            config.sqlHost = System.getenv("BARYON_SQL_HOST");
            config.sqlUser = System.getenv("BARYON_SQL_USER");
            config.sqlPass = System.getenv("BARYON_SQL_PASS");
            config.discordClientID = System.getenv("BARYON_DISCORD_CLIENT_ID");
            config.discordClientSecret = System.getenv("BARYON_DISCORD_CLIENT_SECRET");
            config.discordRedirectURL = System.getenv("BARYON_DISCORD_REDIRECT_URI");
            config.cloudflareImagesAuth = new UserService.CloudflareAuth(System.getenv("BARYON_CLOUDFLARE_ACCOUNT_ID"), System.getenv("BARYON_CLOUDFLARE_IMAGES_TOKEN"));
            return config;
        }
        catch (Exception e)
        {
            throw new MajorFailureException("Failed to load config.", e);
        }
    }

    public int getPort()
    {
        return this.port;
    }

    public String getRedisHost()
    {
        return this.redisHost;
    }

    public String getSqlHost()
    {
        return this.sqlHost;
    }

    public String getSqlUser()
    {
        return this.sqlUser;
    }

    public String getSqlPass()
    {
        return this.sqlPass;
    }

    public String getDiscordClientID()
    {
        return this.discordClientID;
    }

    public String getDiscordClientSecret()
    {
        return this.discordClientSecret;
    }

    public String getDiscordRedirectURL()
    {
        return this.discordRedirectURL;
    }

    public UserService.CloudflareAuth getCloudflareImagesAuth()
    {
        return this.cloudflareImagesAuth;
    }
}
