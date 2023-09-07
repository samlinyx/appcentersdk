package com.xx.easysdk.controller;

import com.xx.easysdk.dto.BatchURL;
import com.xx.easysdk.dto.GenURLReq;
import com.xx.easysdk.service.DynamicWechetService;
import com.xx.easysdk.view.ResponseJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Map;

/**
 * the controller providing api for external service to generating
 * wx mini program invoke linkage
 */
@RestController
public class WXSDKController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private DynamicWechetService dynamicWechetService;
    @PostMapping("/wechat/genURL")
    public ResponseJson genURL(
            HttpServletRequest request,
            @RequestHeader Map<String, String> header,
            @RequestBody GenURLReq genReq) throws Exception {
        String ipaddress = getIpAddr(request);
        logger.debug("wechat genURL request ={}, ip={}",genReq,ipaddress);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,30);
        long expiredTime = cal.getTimeInMillis();
        int type = 0;
        if(genReq.getType().equals("Link"))
            type=1;
        int number = genReq.getNumber();
        String path = genReq.getPath();
        String query = genReq.getQuery();
        BatchURL batResult = this.dynamicWechetService.batchURL(type,number,path,query,ipaddress,expiredTime,true);
        return ResponseJson.buildOnData(batResult);
    }

    public static String getIpAddr(HttpServletRequest request) {
        String ipAddress = null;
        try {
            ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if (ipAddress.equals("127.0.0.1")) {
                    // 根据网卡取本机配置的IP
                    InetAddress inet = null;
                    try {
                        inet = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    ipAddress = inet.getHostAddress();
                }
            }
            // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length()
                // = 15
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
        } catch (Exception e) {
            ipAddress="";
        }
        // ipAddress = this.getRequest().getRemoteAddr();

        return ipAddress;
    }

}
