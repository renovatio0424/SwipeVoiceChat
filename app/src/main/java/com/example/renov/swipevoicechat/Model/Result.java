package com.example.renov.swipevoicechat.Model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class Result {
    private String message;
    private boolean error;
    private String code;

    public String getMessage() {
        return message;
    }

    public boolean getError() {
        return error;
    }

    public String getCode() {
        return code;
    }

    public Result setResultFromErrorBody(String ErrorBody){
        String mJsonString = ErrorBody;
        JsonParser parser = new JsonParser();
        JsonElement mJson = parser.parse(mJsonString);
        Gson gson = new Gson();
        Result result = gson.fromJson(mJson, Result.class);
        return result;
    }
}
