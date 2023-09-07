package com.xx.easysdk.dto;

public class BatchURLDTL {
    private String url;
    private boolean success;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "BatchURLDTL{" +
                "url='" + url + '\'' +
                ", success=" + success +
                '}';
    }
}
