package com.baryonic.util;

import io.helidon.common.http.Http;

public class ResponseObject<T>
{
    private int status;
    private T responseData;

    public static <T> ResponseObject<T> of(T obj)
    {
        return of(Http.Status.OK_200, obj);
    }

    public static <T> ResponseObject<T> of(Http.Status status, T obj)
    {
        var response = new ResponseObject<T>();

        response.responseData = obj;
        response.status = status.code();

        return response;
    }

    public int getStatus()
    {
        return this.status;
    }

    public T getResponseData()
    {
        return this.responseData;
    }
}
