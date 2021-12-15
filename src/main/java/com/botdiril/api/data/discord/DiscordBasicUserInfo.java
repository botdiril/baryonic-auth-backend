package com.botdiril.api.data.discord;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DiscordBasicUserInfo(
    @JsonProperty("id")
    String id,
    @JsonProperty("username")
    String username,
    @JsonProperty("discriminator")
    String discriminator,
    @JsonProperty("avatar")
    String avatar,
    @JsonProperty("bot")
    boolean bot
)
{
}
