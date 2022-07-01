package com.baryonic;

public class BaryonicConfig
{
    private int port;

    private String sqlHost;

    private String sqlUser;

    private String sqlPass;

    private String discordClientID;

    private String discordClientSecret;

    private String discordRedirectURL;

    public static BaryonicConfig load()
    {
        try
        {
            var config = new BaryonicConfig();
            config.port = Integer.parseInt(System.getenv("BARYON_PORT"));
            config.sqlHost = System.getenv("BARYON_SQL_HOST");
            config.sqlUser = System.getenv("BARYON_SQL_USER");
            config.sqlPass = System.getenv("BARYON_SQL_PASS");
            config.discordClientID = System.getenv("BARYON_DISCORD_CLIENT_ID");
            config.discordClientSecret = System.getenv("BARYON_DISCORD_CLIENT_SECRET");
            config.discordRedirectURL = System.getenv("BARYON_DISCORD_REDIRECT_URI");
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
}
