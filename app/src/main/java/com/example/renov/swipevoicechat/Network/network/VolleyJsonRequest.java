package com.example.renov.swipevoicechat.Network.network;

import android.util.Log;

import com.android.volley.Response;
import com.google.gson.Gson;


import java.lang.reflect.Type;

public class VolleyJsonRequest<T> extends VolleyRequest<T> {

    private static final String PROTOCOL_CHARSET = "UTF-8";
    private static final String PROTOCOL_CONTENT_TYPE = String.format("application/json; charset=%s", PROTOCOL_CHARSET);
    private static final String TAG = VolleyJsonRequest.class.getSimpleName();

    private Gson gson = new Gson();
    private Object requestObject;

    public VolleyJsonRequest(int method, String url, Class<T> clazz, Type responseType, VolleyResponseListener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, clazz, responseType, listener, errorListener);
    }

    public void setRequestObject(Object requestObject) {
        this.requestObject = requestObject;
    }

    @Override
    public String getPostBodyContentType() {
        Log.d(TAG,"request : getPostBodyContentType()");
        return getBodyContentType();
    }

    @Override
    public byte[] getPostBody() {
        return getBody();
    }

    @Override
    public String getBodyContentType() {
        return PROTOCOL_CONTENT_TYPE;
    }

    @Override
    public byte[] getBody() {
        try {
            return gson.toJson(requestObject).getBytes(PROTOCOL_CHARSET);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
