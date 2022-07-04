package com.baryonic.api.data.login;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface ILoginInfo
{
    @JsonProperty("type")
    String getType();
}
