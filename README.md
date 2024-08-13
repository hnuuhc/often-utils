Java 网络常用工具类
===============

使用方法:
-------

使用终端命令导入Maven仓库:

```
mvn clean install
```

在Maven依赖中添加:

```
<dependency>
    <groupId>io.github.hnuuhc</groupId>
    <artifactId>often-utils</artifactId>
    <version>1.2.23</version>
</dependency>
```

简介:
----

在日常网络操作中,针对经常遇到的一些问题,设计的一些常用类,目的是使代码简洁且高效,重复且复杂的操作尽可能由一段代码完成,并且尽可能地使用Java

文件下载工具类
------------

### 说明:

1.正常情况下,无需额外设置参数  
2.默认多线程模式下载,线程没有限制,但不应该过多,默认即可

### 简单示例:

```
var res = SionDownload.connect(url)   
.retry(true)  // 重试次数,以及重试等待间隔, true为无限重试
.thread(16)  // 多线程下载,无法获取文件大小转为全量下载,默认线程10  
.execute(); // 开始下载
HLSDownload.connect(url).execute(); // m3u8下载
```

## 调用Aria2下载

### 说明:

1.支持aria2 API交互

### 简单示例:

```

Aria2Util.connect("127.0.0.1", 6800)  //地址以及端口  
.addUrl(url)  //添加url,可以添加磁力,种子  
.setToken("12345")  //设置token  
.setProxy() //为所有链接添加代理
.proxy(proxyHost, proxyPort) // 访问RPC接口的代理
// .remove(gid) // 删除指定下载
// .pause(gid) // 暂停指定下载
// .unpause(gid) // 继续指定下载
// .tellStatus(gid) //获取指定下载进度状态等信息
// .session(Aria2Method,gid) // 其它API接口
.post(); // get()
```

网络访问工具类
------------

### 说明:

1.对一些常用的网络工具进行包装,开箱即食,代码风格一致,参数可直接相互使用  
2.在发送网络错误时可以重试  
3.对一些类增加scoks代理支持  
4.其中HtmlUnitUtil可运行JS代码,默认可运行JS最大1秒,由waitJSTime方法修改  
5.默认配置已经可以应对大部分网站,无需设置过多参数

### 网络工具:

#### HttpsUtil - 用于http请求

### 简单示例:

```
Document doc = HttpsUtil.connect("https://www.baidu.com")
.proxy(proxyHost, proxyPort) // or socks() 设置代理  
.retry(MAX_RETRY, MILLISECONDS_SLEEP) // 重试次数，重试等待间隔   
.get().parse(); // post() or execute() or get().json()
```

HTML或XML解析器
---------

### 简单示例:

```
Document doc = Document.parse(String); // 构建解析对象
```

JSON解析器
---------

1. 其逻辑类似于fastjson,可以很快上手
2. 测试64字符1000万次解析,稳定在4500ms

### 简单示例:

```
    var json = JSONObject.parseObject(String); // 构建JSON对象
    var json = JSONArray.parseArray(String); // 构建JSON数组
    json.select(".cc[0]",Class); // 位置索引

    // SpringBoot注册JsonParam注解
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(0, new JsonParamAnnotationResolver());
	}

	@Bean
	@Order(1)
	public FilterRegistrationBean<Filter> inputStreamWrapperFilterRegistration() {
		var registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new InputStreamWrapperFilter());
		registrationBean.setName("inputStreamWrapperFilter");
		registrationBean.addUrlPatterns("/*");
		return registrationBean;
	}
```

文件读写工具类
-----------

### 说明:

1.写操作默认为追加,append(boolean)方法修改   
2.支持randomAccessFile,FileChannel,MappedByteBuffer,但是在实际应用中默认io接口的方法速度反而更快,可能是ssd的原因,大文件应该可以看出差别

### 简单示例:

```
String str = ReadWriteUtil.orgin(filePath).read(); //读取文件文本  

List<String> lists = ReadWriteUtil.orgin(filePath).readAsLine(); //按行读取文件文本  

ReadWriteUtil.orgin(filePath).write(str); //字符串按行写入文本  

ReadWriteUtil.orgin(file).copy(out); // 文件复制
```

文件压缩工具类
-----------

### 说明:

1.支持密码  
2.支持单文件添加删除

### 简单示例:

```
ZipUtil.origin(file).passwd("123456").compress(file); // 文件压缩  

ZipUtil.origin(file).charset("GBK").deCompress(folder); // 文件解压

```

谷歌浏览器工具类
-------------

### 说明:

1.一键获取本地浏览器的数据,理论上chromium内核都支持  
2.home()方法,默认win版edge用户路径,其它浏览器添加参数,路径至User Data目录

### 简单示例:

```
// 获取本地浏览器cookie   
Map<String, String> cookies = LocalCookie.home().getForDomain("pixiv.net");
// 获取LoginData(账号和密码)  
Map<String, String> loginDatas = LocalLoginData.home().getForDomain("pixiv.net");
// 获取 Local Storage  
Map<String, String> storages = LocalStorage.home().getForDomain("pixiv.net");

```

参数化多线程
----------

### 说明:

1.使多线程方法参数化,可直接传入参数,应用范围很广  
2.最多支持传入10个参数

### 简单示例:

```
ExecutorService executor = Executors.newFixedThreadPool(10);
for (int i = 0; i < 10; i++) {	
	executor.execute(new ConsumerThread(i, (index) -> { // 传参执行多线程
		// TODO
	}));
			
	Future<Integer> future = executorService.submit(new FutureTaskThread<>(i, (index) -> { // 传参执行多线程带返回值
		return index + 1;
	}));
}
ThreadUtil.waitEnd(executor); // 等待线程结束
```

常用网盘API
----------

### 说明:

1.支持网盘: 天翼云盘,阿里云盘,蓝奏云盘,123云盘,夸克网盘,和彩云(中国移动云盘)   
2.为保证兼容性,降低复杂度,均以传递JSON数据操作

### 简单示例:

类: TianYiYunPan - 天翼云盘  
类: ALiYunPan - 阿里云盘  
类: LanZouYunPan - 蓝奏云盘  
类: YunPan123 - 123云盘  
类: KuaKeYunPan - 夸克网盘  
类: HeCaiYunPan - 和彩云(中国移动云盘)

```
TianYiYunPan.login(username,password).getInfoAsHome(); // 获取主页文件列表信息

YunPan123.login(auth).getStraight(fileInfo); // 根据JSON配置获取直链
```

## 鸣谢

感谢[**JetBrains**](https://www.jetbrains.com/zh-cn/community/opensource/#support)提供的开源开发许可证，JetBrains 通过为核心项目贡献者免费提供一套一流的开发者工具来支持非商业开源项目。

[<img src=".github/jetbrains-variant-3.png" width="200"/>](https://www.jetbrains.com/zh-cn/community/opensource/#support)
