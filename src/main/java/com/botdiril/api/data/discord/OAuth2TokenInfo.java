package com.botdiril.api.data.discord;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OAuth2TokenInfo(
    @JsonProperty("access_token")
    String accessToken,
    @JsonProperty("expires_in")
    int expiresIn,
    @JsonProperty("refresh_token")
    String refreshToken,
    @JsonProperty("scope")
    String scope,
    @JsonProperty("token_type")
    String tokenType
)
{
}
