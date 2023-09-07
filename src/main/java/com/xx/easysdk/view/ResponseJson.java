package com.xx.easysdk.view;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class ResponseJson {

    protected int state;

    protected String message;

    protected Object data;

    protected long systemTime;

    protected ResponseJson(int state, String msg, long systemTime, Object data){
        this.state = state;
        this.message = msg;
        this.systemTime = systemTime;
        this.data = data;
    }

    @JsonProperty("state")
    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @JsonProperty("msg")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("data")
    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @JsonProperty("systemTime")
    public long getSystemTime() {
        return systemTime;
    }

    public void setSystemTime(long systemTime) {
        this.systemTime = systemTime;
    }

    public static ResponseJson buildOnError(String error) {
        return new ResponseJson(0, error, (new Date()).getTime(), null);
    }

    public static ResponseJson buildOnData(Object data){
        return new ResponseJson(1, null,(new Date()).getTime(), data);
    }
}
