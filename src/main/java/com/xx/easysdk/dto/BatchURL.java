package com.xx.easysdk.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * wrapper of wx url result
 *
 */
public class BatchURL {
   List<BatchURLDTL> urls = new ArrayList<>();

    public List<BatchURLDTL> getUrls() {
        return urls;
    }

    public void setUrls(List<BatchURLDTL> urls) {
        this.urls = urls;
    }

    public long getTotalValidCount() {
        return urls.stream().filter(BatchURLDTL::isSuccess).count();
    }

    public long getErrorCount() {
        return urls.size()-urls.stream().filter(BatchURLDTL::isSuccess).count();
    }

    @Override
    public String toString() {
        return "BatchURL{" +
                "urls=" + urls +
                '}';
    }
}
