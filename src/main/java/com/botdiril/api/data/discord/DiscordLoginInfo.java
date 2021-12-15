package com.botdiril.api.data.discord;

public record DiscordLoginInfo(
    int id,
    String discordID,
    String username,
    String discriminator,
    String avatar,
    boolean bot
)
{
}
