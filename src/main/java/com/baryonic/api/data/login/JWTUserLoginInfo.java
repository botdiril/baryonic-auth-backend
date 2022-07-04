package com.baryonic.api.data.login;

import java.util.UUID;

public record JWTUserLoginInfo(
    long id,
    String token,
    String username,
    String discriminator,
    UUID avatar,
    ILoginInfo loginInfo
)
{
}
