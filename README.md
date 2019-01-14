# Tina
一个基于okhttp封装的网络库

## 特点
1.  构造式编码。
2.  支持链式请求。
3.  支持并发式请求。
4.  注解式request。
5.  response数自动校验&逻辑处理&数据注入。
6.  支持restful风格。
7.  请求本地缓存。
8. 请求生命周期可伴随activity。

## 初始化

简单使用：
```java
public interface TinaConfig {
    /** httpclient配置 **/
    public @NonNull OkHttpClient getOkhttpClient();

    /** mediaType配置 **/
    public @NonNull MediaType getMediaType();

    /** 根地址 **/
    public @NonNull String getHost();

    /** 成功请求过滤 **/
    public @Nullable TinaFilter getTinaFilter();

    /** request数据转换器 **/
    public @Nullable TinaConvert getRequestConvert();
}

Tina.initConfig(tinaConfig);   
```

## 简单请求

#### 流程图示


```
graph LR
start --> A
A-->End
```



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
                // TODO Auto-generated method stub
            }

            @Override
            public void onFail(TinaException e) {
                // TODO Auto-generated method stub
            }
        })
        .request();

```

## 链式请求

#### 流程图示


```
graph LR
start-->A
A-->B
B-->C
C-->End
```



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
3. 链式请求通过改变build参数（Tina.CONCURRENT）就可以转换成并发请求，但是返回的feedbackResult值都会变为null。


## 并发式请求
#### 流程图示


```
graph LR
start --> A
start --> B
A --> End
B--> End
```



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

## 混淆
```java

-keep public class * extends com.tpa.client.tina.model.TinaBaseRequest {
    public void set*(***);
    public *** get*();
    public *** is*();
}

-keep public class * extends ${BaseResponseClass} {
    *;
}

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
## 其他数据类型支持
```java
         /**
         * 请求数据类型是bitmap类型
         */
        Tina.build()
                .filter(new TinaFilter() {
                    @Override
                    public TinaFilterResult filter(TinaBaseRequest tinaBaseRequest, byte[] bytes, Class aClass) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        return new TinaFilterResult(FilterCode.SUCCESS , bitmap);
                    }
                })
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

## response注解拓展@AutoModel
#### 用法
```java
@AutoMode
public class AnswererListResponse extends TinaBaseResponse {
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

## response注解拓展@NumberScale
#### 使用
```java
@AutoMode
public class AnswererListResponse extends TinaBaseResponse {
    @NumberScale(2)
    private String data;
}
```
- 保留小数位操作，银行家四舍五入算法，支持对string、float、double类型的小数位转换。
- 只在@AutoMote模式下才生效
- 支持对集合里的model操作。

## request缓存注解@Cache
```java
@Cache(key = "key" ，type = CacheType.TARGET , expire = 1000 , unit = TimeUnit.SECONDS)
public class InitRequest extends TinaBaseRequest {

}
key缺省值为 - url(支持Tina restful语法构建)
type缺省值为 - CacheType.TARGET
expire缺省值为 - 永久
unit缺省值为 - TimeUnit.SECONDS
```
### 缓存双回调
type = CacheType.HOLDER类缓存支持双回调
```java
Request request = new Request();
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
  
