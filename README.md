# appcentersdk
1.appcentersdk 用于生成应用中心支持的微信H5跳转的链接，本工具已整合微信小程序的跳转链接生成API，使用者需要在config.properties中配置对应微信小程序的appid和secret使可以调用接口生成。需要发布在java 1.8 或以上的运行环境.
wx.appId=<微信小程序的appid>
wx.appSecret=<微信小程序的app secret>
verifyIp=<用,区分的ip地址，不匹配代表允许所有访问，匹配代表只允许配置的IP调用>

2.Swager api 支持 http://localhost:10800/appcsdk/swagger-ui/ 进行访问
3.调用生成api
当启动后可以HTTP Post访问 （可以在nginx之类的web server中配置proxy)
http://serverip:10800/appcsdk/wechat/genURL
postBody
{
"type":"Link",
"path":"",
"query":"",
"number":1
}
type 默认为scheme ,当传入为Link时为http link的微信链接生成
number 默认生成一个链接
path 为小程序链接跳转的页面，默认空为小程序home
query 当有需要为小程序path对应的页面传递参数时可以使用
