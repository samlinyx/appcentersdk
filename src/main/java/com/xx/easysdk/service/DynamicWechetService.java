package com.xx.easysdk.service;

import com.alibaba.fastjson.JSONObject;
import com.xx.easysdk.dto.BatchURL;
import com.xx.easysdk.dto.BatchURLDTL;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Api for interact with wechat apis
 */
@Service
public class DynamicWechetService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    RestTemplate restTemplate;
    @Value("${wechat.msgTemplateEnv}")
    private String wechatMsgEnv="trial";
    private boolean mockMode = false;
    ExecutorService executor = Executors.newFixedThreadPool(3);
    private Properties wechatProp;
    public static String APPIDKEY="wx.appId";
    public static String APPSECRETKEY="wx.appSecret";
    public static String PATHKEY="wx.homepage";
    public static String VERIFYIPKEY="verifyIp";
    @PreDestroy
    public void shutdown(){
        executor.shutdown();
    }
    /**
     * get back a valid access token
     * @param appId
     * @param appSecret
     * @return
     */
    protected String extractedToken(String appId, String appSecret) {
        String accessTokenUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appId + "&secret=" + appSecret;
        JSONObject json = restTemplate.getForEntity(accessTokenUrl, JSONObject.class).getBody();
        logger.debug("get back response from wechat for access_token {}", json.toJSONString());
        String accessToken = json.getString("access_token");
        if (StringUtils.isNotEmpty(accessToken)) {
            accessToken = accessToken.trim();
        } else {
            // not able to get back access token
            logger.error("Not able to get bacck the access_token from we chat. but got resp {}", json.toJSONString());
        }
        return accessToken;
    }

    /**
     * https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/url-scheme/urlscheme.generate.html
     * get an open url scheme and then return the code
     * POST https://api.weixin.qq.com/wxa/generatescheme?access_token=ACCESS_TOKEN
     *
     * 获取小程序scheme码，适用于短信、邮件、外部网页等拉起小程序的业务场景。通过该接口，可以选择生成到期失效和永久有效的小程序码，目前仅针对国内非个人主体的小程序开放，详见获取URL scheme码。
     *
     * {
     *     "jump_wxa":
     *                {
     *          "path": "/pages/publishHomework/publishHomework",
     *          "query": ""
     *        },
     *     "is_expire":true,
     *     "expire_time":1606737600
     * }
     * @return
     */
    public JSONObject genUrlScheme(String path, long expireTime, boolean isExpire, String query, String accessToken){
        //using http post
        String sendUrl = "https://api.weixin.qq.com/wxa/generatescheme?access_token=" + accessToken;
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        JSONObject jump_wxa = new JSONObject();
//        jump_wxa.put("path","/pages/mall/goods_detail/goods_detail");
//        jump_wxa.put("query","goodsId=7b6817b514b34ee3b66aea3ca0279a85");
        jump_wxa.put("path",path);
        jump_wxa.put("query",query);
        jump_wxa.put("env_version", wechatMsgEnv);
        JSONObject msg = new JSONObject();
        msg.put("is_expire",isExpire);
        msg.put("expire_time",expireTime);
        msg.put("jump_wxa",jump_wxa);
        HttpEntity<String> formEntity = new HttpEntity<String>(msg.toJSONString(), headers);
        JSONObject jsonResult = null;
        if (mockMode) {
            logger.debug("As is mock mode just print out the param and mock a response");
            jsonResult = new JSONObject();
            jsonResult.put("errcode", 0);
            jsonResult.put("errmsg", "ok");
            jsonResult.put("openlink", System.currentTimeMillis());
            //just for testing
        } else {
            String result = restTemplate.postForObject(sendUrl, formEntity, String.class);
            logger.debug("After call wexin to send out message={} got respo {}", msg, result);
            jsonResult = JSONObject.parseObject(result);
        }
        return jsonResult;
    }

    /**
     * POST https://api.weixin.qq.com/wxa/generate_urllink?access_token=ACCESS_TOKEN
     * 请求参数
     属性	类型	默认值	必填	说明
     access_token / cloudbase_access_token	string		是	接口调用凭证
     path	string		否	通过 URL Link 进入的小程序页面路径，必须是已经发布的小程序存在的页面，不可携带 query 。path 为空时会跳转小程序主页
     query	string		否	通过 URL Link 进入小程序时的query，最大1024个字符，只支持数字，大小写英文以及部分特殊字符：!#$&'()*+,/:;=?@-._~%
     env_version	string	"release"	否	要打开的小程序版本。正式版为"release"，体验版为"trial"，开发版为"develop"，仅在微信外打开时生效。
     is_expire	boolean	false	否	生成的 URL Link 类型，到期失效：true，永久有效：false。注意，永久有效 Link 和有效时间超过180天的到期失效 Link 的总数上限为10万个，详见获取 URL Link，生成 Link 前请仔细确认。
     expire_type	number	0	否	小程序 URL Link 失效类型，失效时间：0，失效间隔天数：1
     expire_time	number		否	到期失效的 URL Link 的失效时间，为 Unix 时间戳。生成的到期失效 URL Link 在该时间前有效。最长有效期为1年。expire_type 为 0 必填
     expire_interval	number		否	到期失效的URL Link的失效间隔天数。生成的到期失效URL Link在该间隔时间到达前有效。最长间隔天数为365天。expire_type 为 1 必填
     cloud_base	Object		否	云开发静态网站自定义 H5 配置参数，可配置中转的云开发 H5 页面。不填默认用官方 H5 页面
     cloud_base 的结构

     属性	类型	默认值	必填	说明
     env	string		是	云开发环境
     domain	string		否	静态网站自定义域名，不填则使用默认域名
     path	string	/	否	云开发静态网站 H5 页面路径，不可携带 query
     query	string		否	云开发静态网站 H5 页面 query 参数，最大 1024 个字符，只支持数字，大小写英文以及部分特殊字符：!#$&'()*+,/:;=?@-._~%
     resource_appid	string		否	第三方批量代云开发时必填，表示创建该 env 的 appid （小程序/第三方平台）
     * @return
     */
    public JSONObject genUrllink(String path,long expireTime,boolean isExpire,String query,String accessToken){
        //using http post
        String sendUrl = "https://api.weixin.qq.com/wxa/generate_urllink?access_token=" + accessToken;
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        JSONObject msg = new JSONObject();
        msg.put("path",path);
        msg.put("query",query);
        msg.put("env_version", wechatMsgEnv);

        msg.put("is_expire",isExpire);
        msg.put("expire_time",expireTime);
        HttpEntity<String> formEntity = new HttpEntity<String>(msg.toJSONString(), headers);
        JSONObject jsonResult = null;
        if (mockMode) {
            logger.debug("As is mock mode just print out the param and mock a response");
            jsonResult = new JSONObject();
            jsonResult.put("errcode", 0);
            jsonResult.put("errmsg", "ok");
            jsonResult.put("openlink", System.currentTimeMillis());
            //just for testing
        } else {
            String result = restTemplate.postForObject(sendUrl, formEntity, String.class);
            logger.debug("After call wexin to send out message={} got respo {}", msg, result);
            jsonResult = JSONObject.parseObject(result);
        }
        return jsonResult;
    }

    /**
     * CALL WECHAT TO GENERATE ACCESS LINKAGES
     * @param type
     * @param number
     * @param path
     * @param query
     * @param ipAddress
     * @param expireTime
     * @param isExpire
     * @return
     */
    public BatchURL batchURL(int type,int number,String path,String query, String ipAddress, long expireTime, boolean isExpire){
        String appid = wechatProp.getProperty(APPIDKEY);
        String secret = wechatProp.getProperty(APPSECRETKEY);
        BatchURL batchURL = new BatchURL();
        if(StringUtils.isEmpty(path)){
            path = wechatProp.getProperty(PATHKEY);
        }
        String verifyIp = wechatProp.getProperty(VERIFYIPKEY);
        boolean executeCall = true;
        if(StringUtils.isNotEmpty(verifyIp)){
            // check whether contain
            String[] ips = verifyIp.split(",");
            boolean result = Arrays.asList(ips).contains(ipAddress);
            logger.debug("ipaddress={}, request address={}, contain={}",ips,ipAddress,result);
            executeCall = result;
        }
        if(executeCall){
            //call
            batchURL = batchGenUrl(type,number,path,expireTime,isExpire,query,appid,secret);
        }
        return batchURL;
    }
    /**
     * this method for batching generate the scheme or url from wechat
     * @param type 0 for url schema  1 for url link
     * @param number no of linkage need to gen
     * @param  path the micro program's access path
     * @param  expireTime default it long of 30 days
     * @param isExpire default true
     * @param query the query params to that path
     * @param appid
     * @param secret
     * @return
     */
    protected BatchURL batchGenUrl(int type, int number, String path, long expireTime, boolean isExpire, String query, String appid, String secret){
        String accessToken = this.extractedToken(appid,secret);
        BatchURL batchURL = new BatchURL();
        List<BatchURLDTL> linkageResult = new ArrayList<>();
        if(StringUtils.isEmpty(accessToken)){
            logger.error("not able to generate accesstoken for appid={}",appid);
            return batchURL;
        }
        Callable<BatchURLDTL> callableTask = ()->{
            BatchURLDTL batchURLDTL = new BatchURLDTL();
            String linkage = null;
            int errorCode =0;
            try {
                JSONObject jsonResult = null;
                if(type ==0){
                    jsonResult = this.genUrlScheme(path,expireTime,isExpire,query,accessToken);
                    linkage = jsonResult.getString("openlink");
                    errorCode = jsonResult.getInteger("errcode");
                }else{
                    jsonResult = this.genUrllink(path,expireTime,isExpire,query,accessToken);
                    linkage = jsonResult.getString("url_link");
                    errorCode = jsonResult.getInteger("errcode");
                }
                batchURLDTL.setUrl(linkage);
                batchURLDTL.setSuccess(errorCode==0);
            } catch (Exception e) {
                e.printStackTrace();
                linkage = "";
                batchURLDTL.setUrl(linkage);
                batchURLDTL.setSuccess(false);
            }finally {
                //countDownLatch.countDown();
                return batchURLDTL;
            }
        };
        List<Future<BatchURLDTL>> futures = new ArrayList<Future<BatchURLDTL>>();
        for(int i=0;i<number;i++){
            futures.add(this.executor.submit(callableTask));
        }
        for(Future<BatchURLDTL> ft:futures){
            try {
                linkageResult.add(ft.get());
            } catch (Exception e) {
                linkageResult.add(new BatchURLDTL());
                logger.error("not able to gen wx linkage",e);
            }
        }
        batchURL.setUrls(linkageResult);
        return batchURL;
    }

    @PostConstruct
    public void init(){
        String path = System.getProperty("user.dir");
        String configPath = path+ File.separator+"config.properties";
        logger.debug("The project path ={},will load properties={}",path,configPath);
        Properties prop = new Properties();
        try(InputStream fis = new FileInputStream(configPath)) {
            prop.load(fis);
            this.wechatProp = prop;
        }
        catch(Exception e) {
            System.out.println("Unable to find the specified properties file");
            e.printStackTrace();
            return;
        }
        logger.debug("after load prop , get back appid={}",prop.get(APPIDKEY));
    }
    public static void main(String[] args){
        // load default configuration file

    }
}
