package com.baryonic.api.data.discord;

import com.baryonic.api.data.login.ILoginInfo;

public record DiscordLoginInfo(
    String discordID
) implements ILoginInfo
{
    @Override
    public String getType()
    {
        return "discord";
    }
}
