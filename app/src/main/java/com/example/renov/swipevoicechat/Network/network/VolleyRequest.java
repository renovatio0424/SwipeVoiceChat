package com.example.renov.swipevoicechat.Network.network;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Map;

public class VolleyRequest<T> extends Request<T> {
    private Class<T> clazz;
    private Type responseType;
    private Map<String, String> headers;
    private Map<String, String> parameters;
    private VolleyResponseListener<T> listener;

    public VolleyRequest(int method, String url, Class<T> clazz, Type responseType, VolleyResponseListener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.clazz = clazz;
        this.responseType = responseType;
        this.listener = listener;
//        Log.e("request", url);
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
//        Map<String, String> headers = new HashMap<>();
//        if (this.headers != null) {
//            headers.putAll(this.headers);
//        }
//        return headers;
        return (headers != null || headers.isEmpty()) ? headers : super.getHeaders();
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return parameters != null ? parameters : super.getParams();
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {


        BufferedReader bufferedReader = null;

        try {
            final Gson gson = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
            bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(response.data)));
//            if(response.statusCode == 400){
//                LogUtil.d("parseNetworkResponse] status code " + response.statusCode);
//                LogUtil.d("parseNetworkResponse] data" + response.data);
//                return null;
//            }else{

//			String charset = HttpHeaderParser.parseCharset(response.headers);
//            String str = new String(response.data, "UTF-8");
//            LogUtil.e("response: ", str);

//            LogUtil.d("parseNetworkResponse] code: " + response.statusCode);
//            LogUtil.d("parseNetworkResponse] data.length: " + response.data.length);

//			T object = getResponseParser().onParse(bufferedReader, clazz, charset);
                T object = null;
                if (clazz == null && responseType != null) {
                    object = gson.fromJson(bufferedReader.readLine(), responseType);
                } else {
//                LogUtil.d("bufferedReader" + bufferedReader.readLine());
                    object = gson.fromJson(bufferedReader.readLine(), clazz);
                }
                listener.onParse(response);
                return Response.success(
                        object,
                        HttpHeaderParser.parseCacheHeaders(response));
//            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error(new ParseError(e));
        } finally {
            if(bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return Response.error(new ParseError(e));
                }
            }

        }
    }

    @Override
    public void deliverError(VolleyError error) {
        super.deliverError(error);
    }
}
