package com.xx.easysdk.dto;

public class GenURLReq {
    String type;
    String path;
    String query;
    int number=1;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "GenURLReq{" +
                "type='" + type + '\'' +
                ", path='" + path + '\'' +
                ", query='" + query + '\'' +
                ", number=" + number +
                '}';
    }
}
