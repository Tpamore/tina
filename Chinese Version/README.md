# Tina

## 特点
1.  构造式编码。
2.  支持链式请求。
3.  支持并发式请求。
4.  注解式request&response。
5.  response数据校验&数据注入。
6.  支持restful风格。
7.  数据本地可强缓存。
8.  请求生命周期可伴随activity。
9.  多配置支持。

## change
### 1.1.2
- 现在可以拓展@AutoModel模式下自定义注解了。
- 增加一个@AutoModel模式下自定义注解@NotNull。
- 增加TinaFormConvert，支持form请求的格式。

### 1.1.1-beta
- 合并HOLD和TARGET的缓存策略，简化使用繁琐度。
- 修复在多线程读写请求缓存时，小几率出现数据错乱的bug。
- 优化了部分代码

## 引用
```groovy
dependencies {
    api 'com.tpa.client:tina:1.1.2-beta'
    annotationProcessor 'com.tpa.client:tina-compiler:1.0.0'

}
```
## 混淆
```java

## request混淆
-keep public class * extends com.tpa.client.tina.model.TinaBaseRequest {
    public void set*(***);
    public *** get*();
    public *** is*();
}

## response混淆
-keep public class * extends ${BaseResponseClass} {
    *;
}

```


## 初始化

```java
public interface TinaConfig {
    /** httpclient配置 **/
    public @NonNull OkHttpClient getOkhttpClient();

    /** mediaType配置 **/
    public @NonNull MediaType getMediaType();

    /** 根地址 **/
    public @NonNull String getHost();

    /** 成功请求过滤 **/
    public @Nonable TinaFilter getTinaFilter();

    /** request数据转换器,一般用来加密请求body数据 **/
    public @Nullable TinaConvert getRequestConvert();
}

Tina.initConfig(tinaConfig);   
```

## 简单请求

#### 流程图示

![lc](../png/1.jpg)


```java
@Post("url")
public class Reqest extends TinaBaseRequest{

   private String name = "tqf";
   private String sex = "man";
        
 }

------------------------------------------------------

Reqest request = new Reqest();
Tina.build()
        .call(request)
        .callBack(new TinaSingleCallBack<Response>() {
            @Override
            public void onSuccess(Response response) {

            }

            @Override
            public void onFail(TinaException e) {

            }
        })
        .request();

```

## 链式请求

#### 流程图示

![lc1](../png/2.jpg)

#### 代码实现
```java
@Post("url")
public class Reqest extends TinaBaseRequest{

   private String name = "tqf";
   private String sex = "man";
        
 }
    
----------------------------------------

Reqest request = new Reqest();
Tina.build(Tina.CHAINS)
        .call(request)
        .call(request)
        .callBack(new TinaChainCallBack<TinaBaseResponse>() {
            @Override
            public Object onSuccess(Object feedbackResult,TinaBaseResponse response) {
                return null;
            }

            @Override
            public void onFail(TinaException e) {

            }
        })
        .callBack(new TinaChainCallBack<TinaBaseResponse>() {
            @Override
            public Object onSuccess(Object feedbackResult, TinaBaseResponse response) {
                return null;
            }

            @Override
            public void onFail(TinaException e) {

            }
        })
        .request();
```


#### TinaChainCallBack
- feedbackResult : 上一个请求传递过来的结果，没有则为null
- response : 本次请求的结果
- return : 传递给下个请求的结果，没有则返回null。返回TinaChain.FUSING则打断链式请求。

#### 注意
1. 链式请求的addCall与addCallBack是通过构造顺序进行匹配的。
2. 链式请求中执行到某一个请求fail时则会熔断请求链。


## 并发式请求
#### 流程图示

![lc2](../png/3.jpg)

#### 代码实现
```java
@Post("url")
public class Reqest extends TinaBaseRequest{

   private String name = "tqf";
   private String sex = "man";
        
 }
 
 ----------------------------------------------------------
    
Reqest request = new Reqest();
Tina.build(Tina.CONCURRENT)
        .call(request)
        .call(request)
        .callBack(new TinaSingleCallBack<TinaBaseResponse>() {
            @Override
            public void onSuccess(TinaBaseResponse response) {
            }

            @Override
            public void onFail(TinaException e) {
            }
        })
        .callBack(new TinaSingleCallBack<TinaBaseResponse>() {
            @Override
            public void onSuccess(TinaBaseResponse response) {
            }

            @Override
            public void onFail(TinaException e) {
            }
        })
        .request();
```

## 统一的开始回调和结束回调
```java
...
.startCallBack(new TinaStartCallBack() {
    @Override
    public void start() {        
    }
})
.endCallBack(new TinaEndCallBack() {
    @Override
    public void end() {            
    }
})
...
```

## 过滤器

```java
Tina.build()
          .filter(new TinaFilter() {
                    @Override
                    public TinaFilterResult filter(TinaBaseRequest request, byte[] body, Class expect) {
                        return null;
                    }
                })
         ...
```
## 通过过滤器定制response类型
```java
         /**
         * 请求数据类型是bitmap类型
         */
        Tina.build()
                .filter(BitmapFilter.build())
                .callBack(request)
                .callBack(new TinaSingleCallBack<Bitmap>() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {

                    }

                    @Override
                    public void onFail(TinaException e) {

                    }
                })
                .request();

```

## deamon
```java
        Tina.build()
                .deamon(activity)
                ...
```
请求会伴随activity的生命周期消亡而取消

## response的@AutoModel模式

#### 用法
```java
@AutoMode
public class AnswererListResponse {
}
```
被@AutoModel注解的resposne会递归遍历整个response model，填充所有空对象。

#### 使用前
```java
if(data != null && data.getData1() != null && data.getData1().getData2 != null){
    do(data.getData1().getData2());
}
else{
    //do somethings
}
```
#### 使用后
```java
do(data.getData1().getData2());
```
#### 注意
- @AutoModel不会注入递归字段。
- @AutoModel不会注入静态字段。
- @AutoModel不会注入java.lang包下的基本类型字段(Float、Integer、Long、Double等)。
- 使用@IgnoreInfate可以忽略字段的注入。
- @AutoModel可以注入集合及集合里的数据(集合嵌套亦支持)。

### 子注解拓展@NumberScale

```java
@AutoMode
public class Response {
    @NumberScale(2)
    private String data;
}
```
- 保留小数位操作，银行家四舍五入算法，支持对string、float、double类型的小数位转换。
- 只在@AutoMote模式下才生效

### 子注解拓展@NotNull
```java
@AutoMode
public class Response {
    @NotNull(message = "data字段不能为空")
    private String data;
}
```
- 如果response被注解的字段为空，则不达成onSuccess回调条件，触发onFail回调。
- 只在@AutoMote模式下才生效

### 自定义子注解

> 具体请参见@NotNull、@NumberScale的实现

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NotNull {
    public String message() default "";
}

public class NotNullHandler implements TinaAnnotationHandler<NotNull>{
    @Override
    public void hanld(NotNull annotation, Object host, Field field) throws TinaDataException{
        try {
            Object o = field.get(host);
            if (o == null) {
                throw new TinaDataException(annotation.message());
            }
        } catch (IllegalAccessException e) {
        }
    }
}
TinaAnnotationManager.getInstance().register(NotNull.class , new NotNullHandler());
```


## request缓存注解@Cache
```java
@Cache(key = "key" , expire = 1000 , unit = TimeUnit.SECONDS)
public class Request extends TinaBaseRequest {

}
Request request = new Request();

// 单回调  命中缓存则不会发起网络请求
Tina.build()
        .call(request)
        .callBack(new TinaSingleCallBack<Response>() {
            @Override
            public void onSuccess(Response response) {
                //response
            }
            @Override
            public void onFail(TinaException exception) {

            }
        })
        .request();

// 双回调 无论命中缓存与否 都会发起网络请求
Tina.build()
        .call(request)
        .callBack(new TinaSingleCacheCallBack<Response>() {
            @Override
            public void onSuccess(Response response) {
                //fresh response
            }
            @Override
            public void onCache(Response response) {
                //cache response
            }
            @Override
            public void onFail(TinaException exception) {

            }
        })
        .request();
```
- key缺省值为 - url(支持Tina restful语法构建)
- expire缺省值为 - 永久
- unit缺省值为 - TimeUnit.SECONDS

## 关于restful
> 支持 `POST`、`GET`、`PUT`、`DELETE`、`PATCH`请求。
> 支持restful语义构建。


### 语法1
```java
    @Delete("/name/{name}/sex/{sex}")
    public class Reqest extends TinaBaseRequest{

        @Path("name")
        private String name = "tqf";

        @Path("sex")
        private String sex = "man";
        
    }
    
    >>> DELETE /name/tqf/sex/man
```

### 语法2
```java
    @Delete("/name/:name/sex/:sex")
    public class Reqest extends TinaBaseRequest{

        private String name = "tqf";

        private String sex = "man";
        
    }
    
    >>> DELETE /name/tqf/sex/man
    
    @Delete("/name/:name/sex/:sex?")
    public class Reqest extends TinaBaseRequest{

        private String name = "tqf";

        private String sex = null;
        
    }
    
    >>> DELETE /name/tqf/sex
```


## 多配置支持

```java
@ConfigId("Pay")
class Config implements TinaConfig{
    ...
}

Tina.addConfig(new Config());


--- gradle clean build ---


/*
* 编译期 自动生成PayTina  
*/
PayTina.build()...

```

## live templates

### 导入设置

首先将settings.jar下载下来保持到本地 [>>>>> settings.jar](../setting/settings.jar)

**打开 AndroidStudio ，选择 Android Studio - File  - Import Setttings**      

选择你刚刚下载的setting.jar  

### abbreviation
- tina_contract : 生成model contract
- tina_singleReq : 生成简单请求
- tina_singleReq2 : 生成带有endCallBack和startCallBack的简单请求
- tina_chainReq : 生成链式请求
- tina_chainReq2 : 生成带有endCallBack和startCallBack的链式请求
- tina_concurrentReq : 生成并发式请求
- tina_concurrentReq2 : 生成带有endCallBack和startCallBack的并发式请求

![lc5](../png/5.gif)
![lc6](../png/6.gif)

> 只提供简单参考，可根据自己的业务需求修改live templates

## 邮箱
736969519@qq.com
