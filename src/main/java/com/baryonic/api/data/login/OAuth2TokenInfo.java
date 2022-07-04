package com.baryonic.api.data.login;

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
