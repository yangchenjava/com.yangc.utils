com.yangc.utils
===============

### java工具类
作为程序猿一枚，热爱开源，这里是自己在工作中积累的工具类，jdk6环境下均可用，若发现bug，请联系QQ：511636835，感激不尽。

### cache
EhCacheUtils - 基于ehcache的工具类<br />
LruCacheUtils - 基于LinkedHashMap实现LRU缓存的工具类<br />
MemcachedUtils - 基于memcached的工具类<br />
XMemcachedUtils - 基于memcached的工具类（使用XMemcached客户端）<br />
RedisUtils - 基于redis的工具类，与redis的集群配置无缝结合

### db
JdbcUtils - 操作jdbc的工具类<br />
MongodbUtils - 操作mongodb的工具类

### email
EmailUtils - 邮件工具类，支持发送带附件的邮件

### encryption
AesUtils - 实现AES加密解密<br />
Base64Utils - 实现Base64加密解密<br />
DesUtils - 实现DES加密解密<br />
Md5Utils - 获取字符串或文件的md5<br />
RsaUtils - 实现RSA加密解密

### excel
ReadExcel2003 - 以model方式读2003版Excel（大数据）<br />
ReadExcel2007 - 以sax方式读2007版Excel（大数据）<br />
WriteExcel - 写Excel

### image
CaptchaUtils - 生成验证码<br />
ImageUtils - 图片压缩、截图<br />
QRCodeUtils - 生成二维码、解析二维码

### io
SerializeUtils - 序列化、反序列化对象<br />
ZipUtils - 压缩、解压文件

### json
JsonUtils - json格式转换

### lang
BeanUtils - map与object互相转换<br />
CharsetDetectorUtils - 获取文本文件编码格式<br />
ChineseCalendar - 农历日历<br />
ConverterUtils - 高低字节转换<br />
DateUtils - 日期工具类<br />
GPSUtils - GPS工具类<br />
HtmlFilterUtils - 过滤html标签<br />
JsoupUtils - 基于jsoup过滤html标签<br />
MoneyUtils - 获取大写金额<br />
NumberUtils - 数字工具类<br />
PinyinUtils - 汉字转拼音<br />
ZHConverterUtils - 汉字繁简体转换

### media
MediaUtils - 基于ffmpeg，qtfaststart，yamdi的多媒体工具类

### net
AttachmentUtils - HTTP文件下载防止中文乱码<br />
FastDFSUtils - 操作FastDFS的工具类<br />
FtpUtils - 操作FTP的工具类（基于sun自家的包，jdk7以后不建议使用）<br />
FtpUtilsApache - 基于apache操作FTP的工具类<br />
HttpUtils - 发送HTTP请求<br />
IpUtils - 获取IP<br />
SFtpUtils - 操作SFTP的工具类

### prop
PropertiesUtils - 操作properties配置文件
