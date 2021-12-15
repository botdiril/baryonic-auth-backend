package com.botdiril.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.botdiril.Botdiril;
import com.botdiril.MajorFailureException;
import com.botdiril.util.BotdirilLog;

public class BotdirilConfig
{
    @JsonProperty("port")
    private int port;

    @JsonProperty("mysql_host")
    private String sqlHost;

    @JsonProperty("mysql_user")
    private String sqlUser;

    @JsonProperty("mysql_pass")
    private String sqlPass;

    @JsonProperty("discord_oauth2_client_id")
    private String discordClientID;

    @JsonProperty("discord_oauth2_client_secret")
    private String discordClientSecret;

    private static final Path CONFIG_FILE = Path.of("settings.json");

    public static BotdirilConfig load() throws IOException
    {
        if (!Files.isRegularFile(CONFIG_FILE))
        {
            if (Files.exists(CONFIG_FILE))
            {
                throw new MajorFailureException("%s exists, but is not a regular file!".formatted(CONFIG_FILE));
            }

            var cfg = new BotdirilConfig();
            cfg.sqlHost = "<insert MySQL hostname here>";
            cfg.sqlUser = "<insert MySQL username here>";
            cfg.sqlPass = "<insert MySQL password here>";
            cfg.port = 8080;

            var mapper = new ObjectMapper().writerWithDefaultPrettyPrinter();

            Files.writeString(CONFIG_FILE, mapper.writeValueAsString(cfg));

            BotdirilLog.logger.error("Could not find %s, aborting.".formatted(CONFIG_FILE.toAbsolutePath()));
            BotdirilLog.logger.error("You need to set up the settings.json file I've just created.");
            BotdirilLog.logger.error("It's just some basic stuff like the API key.");

            throw new MajorFailureException("Unitialized config file!");
        }

        try (var reader = Files.newBufferedReader(CONFIG_FILE))
        {
            var mapper = new ObjectMapper();
            return mapper.readValue(reader, BotdirilConfig.class);
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
}
