package com.tpa.client.tina;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.tpa.client.tina.annotation.AutoModelTool;
import com.tpa.client.tina.annotation.AutoMode;
import com.tpa.client.tina.annotation.Cache;
import com.tpa.client.tina.annotation.Delete;
import com.tpa.client.tina.annotation.Get;
import com.tpa.client.tina.annotation.NotNull;
import com.tpa.client.tina.annotation.NotNullHandler;
import com.tpa.client.tina.annotation.NumberScale;
import com.tpa.client.tina.annotation.NumberScaleHandler;
import com.tpa.client.tina.annotation.Patch;
import com.tpa.client.tina.annotation.Post;
import com.tpa.client.tina.annotation.Put;
import com.tpa.client.tina.annotation.TinaAnnotationManager;
import com.tpa.client.tina.callback.TinaChainCallBack;
import com.tpa.client.tina.callback.TinaEndCallBack;
import com.tpa.client.tina.callback.TinaSingleCacheCallBack;
import com.tpa.client.tina.callback.TinaSingleCallBack;
import com.tpa.client.tina.callback.TinaStartCallBack;
import com.tpa.client.tina.enu.FilterCode;
import com.tpa.client.tina.model.TinaBaseRequest;
import com.tpa.client.tina.utils.ACache;
import com.tpa.client.tina.utils.ClassUtils;
import com.tpa.client.tina.utils.JSONHelper;
import com.tpa.client.tina.utils.Schedulers;
import com.tpa.client.tina.utils.UrlUtils;
import com.tpa.comm.ConfigId;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by tangqianfeng on 16/9/21.
 */
public class Tina {

    public static final int SINGLE = 10009;
    public static final int CHAINS = 10010;
    public static final int CONCURRENT = 10011;

    private static Config defaultConfig = new Config();
    private static Map<String, Config> configMap = new HashMap<>();
    private Config targetConfig;

    private TinaFilter tinaFilter;
    private TinaConvert tinaConvert;
    private String targetHost = null;
    private MediaType mMediaType;
    private static Handler handler = new Handler(Looper.getMainLooper());

    //单个请求
    private int requestType;
    private TinaBaseRequest tinaRequest;
    private TinaSingleCallBack callBack;

    //链式请求
    private List<TinaBaseRequest> tinaBaseRequests;
    private List<TinaChainCallBack> tinaChainCallBacks;
    private List<TinaSingleCallBack> tinaConcurrentCallBacks;
    private TinaStartCallBack startCallBack;
    private TinaEndCallBack endCallBack;
    private volatile int hitCount;
    private String tag = null;


    /**
     * 做主项目的同学请求自觉使用initConfig
     *
     * @param config
     */
    public static void initConfig(TinaConfig config) {
        if (config != null) {
            defaultConfig.mClient = config.getOkhttpClient();
            defaultConfig.mMediaType = config.getMediaType();
            defaultConfig.hostUrl = config.getHost();
            defaultConfig.mCodeFilter = config.getTinaFilter();
            if (defaultConfig.mClient.cache() != null && defaultConfig.mClient.cache().directory() != null) {
                defaultConfig.mDiskCache = ACache.get(defaultConfig.mClient.cache().directory());
            }
            defaultConfig.mRequestConvert = config.getRequestConvert();
            TinaAnnotationManager.getInstance().register(NumberScale.class , new NumberScaleHandler());
            TinaAnnotationManager.getInstance().register(NotNull.class , new NotNullHandler());
        } else {
            throw new NullPointerException("init config is null");
        }
    }

    /**
     * 做sdk的同学请自觉使用addConfig
     * 使用addConfig会自动生成一个[ConfigId]Tina 避免和主项目的Tina业务冲突
     *
     * @param config
     */
    public static void addConfig(TinaConfig config) throws Exception {
        if (config != null) {
            ConfigId configId = config.getClass().getAnnotation(ConfigId.class);
            if (configId == null) {
                throw new NullPointerException("config annotation is null");
            }
            String key = configId.value();

            if (TextUtils.isEmpty(key)) {
                throw new NullPointerException("config annotation message is null");
            }

            if (configMap.containsKey(key)) {
                throw new Exception("config annotation key is repeated");
            }

            Config cf = new Config();
            cf.mClient = config.getOkhttpClient();
            cf.mMediaType = config.getMediaType();
            cf.hostUrl = config.getHost();
            cf.mCodeFilter = config.getTinaFilter();
            if (cf.mClient.cache() != null && cf.mClient.cache().directory() != null) {
                cf.mDiskCache = ACache.get(cf.mClient.cache().directory());
            }
            cf.mRequestConvert = config.getRequestConvert();
            configMap.put(key, cf);
            TinaAnnotationManager.getInstance().register(NumberScale.class , new NumberScaleHandler());
            TinaAnnotationManager.getInstance().register(NotNull.class , new NotNullHandler());
        } else {
            throw new NullPointerException("add config is null");
        }
    }

    public Tina(int requestType) {
        this.requestType = requestType;
        if (requestType == CHAINS || requestType == CONCURRENT) {
            tinaBaseRequests = new ArrayList<>();
            tinaChainCallBacks = new ArrayList<>();
            tinaConcurrentCallBacks = new ArrayList<>();
        }
    }

    public static Tina build() {
        return new Tina(SINGLE);
    }

    public static Tina build(@TinaType int requestType) {
        return new Tina(requestType);
    }

    public Tina config(String configId) {
        if (!configMap.containsKey(configId)) {
            throw new IllegalArgumentException("There is no configuration id #" + configId);
        }
        this.targetConfig = configMap.get(configId);
        return this;
    }

    public Tina mediaType(MediaType mediaType) {
        mMediaType = mediaType;
        return this;
    }

    public Tina call(TinaBaseRequest request) {
        request.build();
        if (requestType != CHAINS && requestType != CONCURRENT) {
            this.tinaRequest = request;
        } else {
            tinaBaseRequests.add(request);
        }
        return this;
    }

    public Tina callBack(TinaSingleCallBack<?> callBack) {
        if (requestType == SINGLE) {
            this.callBack = callBack;
        } else if (requestType == CONCURRENT) {
            tinaConcurrentCallBacks.add(callBack);
        } else {
            throw new IllegalArgumentException("Request type don't match with TinaSingleCallBack! ");
        }

        return this;
    }

    public Tina callBack(TinaChainCallBack<?> tinaChainCallBack) {
        if (requestType != CHAINS && requestType != CONCURRENT) {
            throw new IllegalArgumentException("Request type don't match with TinaChainCallBack!");
        }
        tinaChainCallBacks.add(tinaChainCallBack);
        return this;
    }

    public Tina startCallBack(TinaStartCallBack startCallBack) {
        this.startCallBack = startCallBack;
        return this;
    }

    public Tina endCallBack(TinaEndCallBack endCallBack) {
        this.endCallBack = endCallBack;
        return this;
    }

    public Tina filter(TinaFilter filter) {
        tinaFilter = filter;
        return this;
    }

    public Tina convert(TinaConvert requestConvert) {
        this.tinaConvert = requestConvert;
        return this;
    }

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public Tina deamon(Activity activity) {
        tag = activity.getClass().getSimpleName() + activity.hashCode();
        registerCancelEvent(activity);
        return this;
    }

    public Tina host(String host) {
        this.targetHost = host;
        return this;
    }

    /**
     * async request
     *
     * @return
     */
    public Tina request() {
        if (tag == null) {
            tag = hashCode() + "tina";
        }
        if (requestType == CHAINS) {
            chainsRequests();
        } else if (requestType == CONCURRENT) {
            concurrentRequests();
        } else {
            singleRequest(tinaRequest , callBack);
        }
        return this;
    }

    /**
     * sync request
     *
     * @return
     */
    public byte[] syncRequst(TinaBaseRequest syncRequest) {
        if (requestType != SINGLE) {
            throw new IllegalArgumentException("sync request only supprot single request type");
        }

        /**
         * 请求形式确定 url生成
         */
        Post post = null;
        Get get = null;
        Put put = null;
        Patch patch = null;
        Delete delete = null;

        String url = null;
        if ((get = syncRequest.getClass().getAnnotation(Get.class)) != null) {
            String host = targetHost == null ? getConfig().hostUrl : targetHost;
            url = host + UrlUtils.generatePathWithParams(get.value(), syncRequest);
        } else if ((post = syncRequest.getClass().getAnnotation(Post.class)) != null) {
            String host = targetHost == null ? getConfig().hostUrl : targetHost;
            url = host + UrlUtils.generatePathWithoutParams(post.value(), syncRequest);
        } else if ((delete = syncRequest.getClass().getAnnotation(Delete.class)) != null) {
            String host = targetHost == null ? getConfig().hostUrl : targetHost;
            url = host + UrlUtils.generatePathWithParams(delete.value(), syncRequest);
        } else if ((put = syncRequest.getClass().getAnnotation(Put.class)) != null) {
            String host = targetHost == null ? getConfig().hostUrl : targetHost;
            url = host + UrlUtils.generatePathWithoutParams(put.value(), syncRequest);
        } else if ((patch = syncRequest.getClass().getAnnotation(Patch.class)) != null) {
            String host = targetHost == null ? getConfig().hostUrl : targetHost;
            url = host + UrlUtils.generatePathWithoutParams(patch.value(), syncRequest);
        } else {
            throw new IllegalArgumentException("request annotation is not exist");
        }

        Request request = generateRequest(syncRequest, post, get, put, patch, delete, url, convertRequest(JSONHelper.objToJson(syncRequest)));
        try {
            Response syncResponse = getConfig().mClient.newCall(request).execute();
            return syncResponse.body().bytes();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private Config getConfig() {
        if (targetConfig != null) {
            return targetConfig;
        }
        return defaultConfig;
    }

    /**
     * 并发请求
     */
    private void concurrentRequests() {
        if (startCallBack != null) {
            startCallBack.start();
        }

        for (int i = 0; i < tinaBaseRequests.size(); i++) {
            TinaBaseRequest request = tinaBaseRequests.get(i);
            TinaSingleCallBack callback = null;
            if (i < tinaConcurrentCallBacks.size()) {
                callback = tinaConcurrentCallBacks.get(i);
            }
            concurrentRequest(request, callback);
        }
    }

    private void concurrentRequest(final TinaBaseRequest sRequest, final TinaSingleCallBack sCallback) {

        Schedulers.subscribeOn(Schedulers.IO)
                .callback(new Schedulers.ScheduleCallBack() {
                    @Override
                    public void onCallBack(Object result) {
                        Pack pack = (Pack) result;
                        executeConcurrent(sRequest, sCallback, pack.post, pack.get,
                                pack.put, pack.patch, pack.delete, pack.url, pack.cacheKey, pack.cache, pack.body);
                    }
                })
                .run(new Schedulers.SRunnable<Pack>() {
                    @Override
                    public Pack callable() {
                        /**
                         * 请求形式确定 url生成
                         */
                        Post post = null;
                        Get get = null;
                        Put put = null;
                        Patch patch = null;
                        Delete delete = null;

                        String urlTemp = null;
                        if ((get = sRequest.getClass().getAnnotation(Get.class)) != null) {
                            String host = targetHost == null ? getConfig().hostUrl : targetHost;
                            urlTemp = host + UrlUtils.generatePathWithParams(get.value(), sRequest);
                        } else if ((post = sRequest.getClass().getAnnotation(Post.class)) != null) {
                            String host = targetHost == null ? getConfig().hostUrl : targetHost;
                            urlTemp = host + UrlUtils.generatePathWithoutParams(post.value(), sRequest);
                        } else if ((delete = sRequest.getClass().getAnnotation(Delete.class)) != null) {
                            String host = targetHost == null ? getConfig().hostUrl : targetHost;
                            urlTemp = host + UrlUtils.generatePathWithParams(delete.value(), sRequest);
                        } else if ((put = sRequest.getClass().getAnnotation(Put.class)) != null) {
                            String host = targetHost == null ? getConfig().hostUrl : targetHost;
                            urlTemp = host + UrlUtils.generatePathWithoutParams(put.value(), sRequest);
                        } else if ((patch = sRequest.getClass().getAnnotation(Patch.class)) != null) {
                            String host = targetHost == null ? getConfig().hostUrl : targetHost;
                            urlTemp = host + UrlUtils.generatePathWithoutParams(patch.value(), sRequest);
                        } else {
                            throw new IllegalArgumentException("request annotation is not exist");
                        }
                        final String url = urlTemp;

                        final String cacheKey;
                        final Cache cache = sRequest.getClass().getAnnotation(Cache.class);
                        if (cache != null) {
                            if (getConfig().mDiskCache == null) {
                                throw new IllegalArgumentException("please check okhttp cache config is right , #" + sRequest.getClass().getCanonicalName());
                            }
                            String cacheTempKey = null;
                            if ("".equals(cache.key())) {
                                cacheTempKey = url;
                            } else {
                                cacheTempKey = UrlUtils.generatePathWithoutParams(cache.key(), sRequest);
                            }
                            cacheKey = cacheTempKey;
                        } else {
                            cacheKey = null;
                        }

                        /**
                         * 缓存数据返回
                         */
                        if (cache != null && getConfig().mDiskCache != null) {
                            byte[] cacheResponse = getCacheBytes(cacheKey);
                            if (cacheResponse != null) {
                                if (sCallback != null && sCallback instanceof TinaSingleCacheCallBack) {
                                    onHoldCacheReturn(sRequest , cacheResponse , (TinaSingleCacheCallBack) sCallback);
                                } else {
                                    boolean s = mapConcurrentRequest(cacheResponse, sCallback, sRequest , null, cacheKey);
                                    /**
                                     * 如果缓存出现不匹配的问题(不确定的缓存规则改变)，则重新请求
                                     */
                                    if (s) {
                                        hitBoom();
                                        return null;
                                    }
                                }
                            }
                        }

                        return new Pack(post, get, put, patch, delete, url, cacheKey, cache, convertRequest(JSONHelper.objToJson(sRequest)));
                    }
                });


    }

    private synchronized byte[] getCacheBytes(String cacheKey) {
        return getConfig().mDiskCache.getAsBinary(cacheKey);
    }

    private void executeConcurrent(final TinaBaseRequest sRequest, final TinaSingleCallBack sCallback, Post post, Get get, Put put, Patch patch,
                                   Delete delete, String url, final String cacheKey, final Cache cache, String rb) {
        Request request = generateRequest(sRequest, post, get, put, patch, delete, url, rb);
        getConfig().mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                hitBoom();
                try {
                    handler.postAtTime(new Runnable() {
                        @Override
                        public void run() {
                            if (sCallback != null) {
                                sCallback.onFail(new TinaException(TinaException.IOEXCEPTION, e.getMessage()));
                            }
                        }
                    }, tag, SystemClock.uptimeMillis());
                } finally {
                    if (endCallBack != null && concurrentEnd()) {
                        handler.postAtTime(new Runnable() {
                            @Override
                            public void run() {
                                endCallBack.end();
                            }
                        }, tag, SystemClock.uptimeMillis());
                    }
                }
            }

            @Override
            public void onResponse(Call call, final Response response) {
                hitBoom();
                if (!response.isSuccessful()) {
                    handler.postAtTime(new Runnable() {
                        @Override
                        public void run() {
                            if (sCallback != null) {
                                sCallback.onFail(new TinaException(TinaException.OTHER_EXCEPTION, response.code() + " " + response.message()));
                            }
                            if (endCallBack != null && concurrentEnd()) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        endCallBack.end();
                                    }
                                });

                            }
                        }
                    }, tag, SystemClock.uptimeMillis());
                    return;
                }
                byte[] bytes = null;
                try {
                    bytes = response.body().bytes();
                } catch (final IOException e) {
                    handler.postAtTime(new Runnable() {
                        @Override
                        public void run() {
                            if (sCallback != null) {
                                sCallback.onFail(new TinaException(TinaException.IOEXCEPTION, e.getMessage()));
                            }
                            if (endCallBack != null && concurrentEnd()) {
                                handler.postAtTime(new Runnable() {
                                    @Override
                                    public void run() {
                                        endCallBack.end();
                                    }
                                }, tag, SystemClock.uptimeMillis());

                            }
                        }
                    }, tag, SystemClock.uptimeMillis());
                    return;
                }
                mapConcurrentRequest(bytes, sCallback, sRequest, cache, cacheKey);

            }
        });
    }

    private boolean mapConcurrentRequest(byte[] body, final TinaSingleCallBack sCallback, TinaBaseRequest sRequest, Cache cache, String cacheKey) {
        Class clazz;
        if (sCallback != null) {
            clazz = sCallback.getClass();
        } else {
            handler.postAtTime(new Runnable() {
                @Override
                public void run() {
                    if (endCallBack != null && concurrentEnd()) {
                        endCallBack.end();
                    }
                }
            }, tag, SystemClock.uptimeMillis());

            if (cache != null && cacheKey != null) {
                storeRequestCache(body, cache, cacheKey);
            }
            return true;
        }

        final TinaFilterResult filterResult = filter(sRequest, body, ClassUtils.getGenericType(sCallback.getClass()));
        final Object tr = filterResult.response;
        if (filterResult.code == FilterCode.SUCCESS) {
            if (tr != null && tr.getClass().isAnnotationPresent(AutoMode.class)) {
                try {
                    AutoModelTool.inflateClassBean(tr.getClass(), tr);
                } catch (TinaDataException e) {
                    filterResult.code = FilterCode.FAIL;
                    filterResult.errorMsg = e.getMessage();
                    filterResult.errorCode = TinaException.DATA_EXCEPTION;
                    return concurrentFail(sCallback, cache, cacheKey, filterResult);
                }
            }
            handler.postAtTime(new Runnable() {
                @Override
                public void run() {
                    sCallback.onSuccess(tr);
                    if (endCallBack != null && concurrentEnd()) {
                        endCallBack.end();
                    }
                }
            }, tag, SystemClock.uptimeMillis());

            /**
             * 缓存服务端返回的数据
             */
            if (cache != null && cacheKey != null) {
                storeRequestCache(body, cache, cacheKey);
            }
            return true;
        } else {
            return concurrentFail(sCallback, cache, cacheKey, filterResult);
        }

    }

    private boolean concurrentFail(final TinaSingleCallBack sCallback, Cache cache, String cacheKey, final TinaFilterResult filterResult) {
        if (cache == null && cacheKey != null) {
            clearErrorCacheData(cacheKey);
            return false;
        }
        handler.postAtTime(new Runnable() {
            @Override
            public void run() {
                sCallback.onFail(new TinaException(filterResult.errorCode, filterResult.errorMsg));
                if (endCallBack != null && concurrentEnd()) {
                    endCallBack.end();
                }
            }
        }, tag, SystemClock.uptimeMillis());
        return true;
    }

    /**
     * 链式请求
     **/
    private void chainsRequests() {
        Collections.reverse(tinaBaseRequests);
        Collections.reverse(tinaChainCallBacks);
        if (startCallBack != null) {
            startCallBack.start();
        }
        chainRequest(tinaBaseRequests.size() - 1, null);
    }

    private void chainRequest(int index, final Object preResult) {
        final int nextIndex = index - 1;
        final TinaBaseRequest chainRequest = tinaBaseRequests.remove(index);
        final TinaChainCallBack chainCallBack;
        if (index < tinaChainCallBacks.size()) {
            chainCallBack = tinaChainCallBacks.remove(index);
        }
        else {
            chainCallBack = null;
        }

        Schedulers.subscribeOn(Schedulers.IO)
                .callback(new Schedulers.ScheduleCallBack() {
                    @Override
                    public void onCallBack(Object result) {
                        Pack pack = (Pack) result;
                        executeChain(preResult, nextIndex, chainRequest, chainCallBack, pack.post,
                                pack.get, pack.put, pack.patch, pack.delete, pack.url, pack.cacheKey, pack.cache, pack.body);
                    }
                })
                .run(new Schedulers.SRunnable<Pack>() {
                    @Override
                    public Pack callable() {
                        /**
                         * 请求形式确定 url生成
                         */
                        Post post = null;
                        Get get = null;
                        Put put = null;
                        Patch patch = null;
                        Delete delete = null;

                        String url = null;
                        if ((get = chainRequest.getClass().getAnnotation(Get.class)) != null) {
                            String host = targetHost == null ? getConfig().hostUrl : targetHost;
                            url = host + UrlUtils.generatePathWithParams(get.value(), chainRequest);
                        } else if ((post = chainRequest.getClass().getAnnotation(Post.class)) != null) {
                            String host = targetHost == null ? getConfig().hostUrl : targetHost;
                            url = host + UrlUtils.generatePathWithoutParams(post.value(), chainRequest);
                        } else if ((delete = chainRequest.getClass().getAnnotation(Delete.class)) != null) {
                            String host = targetHost == null ? getConfig().hostUrl : targetHost;
                            url = host + UrlUtils.generatePathWithParams(delete.value(), chainRequest);
                        } else if ((put = chainRequest.getClass().getAnnotation(Put.class)) != null) {
                            String host = targetHost == null ? getConfig().hostUrl : targetHost;
                            url = host + UrlUtils.generatePathWithoutParams(put.value(), chainRequest);
                        } else if ((patch = chainRequest.getClass().getAnnotation(Patch.class)) != null) {
                            String host = targetHost == null ? getConfig().hostUrl : targetHost;
                            url = host + UrlUtils.generatePathWithoutParams(patch.value(), chainRequest);
                        } else {
                            throw new IllegalArgumentException("request annotation is not exist");
                        }

                        final String cacheKey;
                        final Cache cache = chainRequest.getClass().getAnnotation(Cache.class);
                        if (cache != null) {
                            if (getConfig().mDiskCache == null) {
                                throw new IllegalArgumentException("please check okhttp cache config is right , #" + chainRequest.getClass().getCanonicalName());
                            }
                            String cacheTempKey = null;
                            if ("".equals(cache.key())) {
                                cacheTempKey = url;
                            } else {
                                cacheTempKey = UrlUtils.generatePathWithoutParams(cache.key(), chainRequest);
                            }
                            cacheKey = cacheTempKey;
                        } else {
                            cacheKey = null;
                        }

                        /**
                         * 缓存数据返回
                         */
                        if (cache != null && getConfig().mDiskCache != null) {
                            byte[] cacheResponse = getCacheBytes(cacheKey);
                            if (cacheResponse != null) {
                                boolean s = mapChainRequest(cacheResponse, chainCallBack, chainRequest, preResult, nextIndex, null, cacheKey);
                                if (s) {
                                    return null;
                                }
                            }
                        }
                        return new Pack(post, get, put, patch, delete, url, cacheKey, cache, convertRequest(JSONHelper.objToJson(chainRequest)));
                    }
                });


    }

    private void executeChain(final Object result, final int nextIndex, final TinaBaseRequest chainRequest, final TinaChainCallBack chainCallBack,
                              Post post, Get get, Put put, Patch patch, Delete delete, String url, final String cacheKey, final Cache cache, String rb) {
        Request request = generateRequest(chainRequest, post, get, put, patch, delete, url, rb);
        getConfig().mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                try {
                    handler.postAtTime(new Runnable() {
                        @Override
                        public void run() {
                            if (chainCallBack != null) {
                                chainCallBack.onFail(new TinaException(TinaException.IOEXCEPTION, e.getMessage()));
                            }
                        }
                    }, tag, SystemClock.uptimeMillis());
                } catch (Exception e1) {

                } finally {
                    if (endCallBack != null) {
                        handler.postAtTime(new Runnable() {
                            @Override
                            public void run() {
                                endCallBack.end();
                            }
                        }, tag, SystemClock.uptimeMillis());
                    }
                }
            }

            @Override
            public void onResponse(Call call, final Response response) {
                if (!response.isSuccessful()) {
                    handler.postAtTime(new Runnable() {
                        @Override
                        public void run() {
                            if (chainCallBack != null) {
                                chainCallBack.onFail(new TinaException(TinaException.OTHER_EXCEPTION, response.code() + " " + response.message()));
                            }
                            if (endCallBack != null) {
                                handler.postAtTime(new Runnable() {
                                    @Override
                                    public void run() {
                                        endCallBack.end();
                                    }
                                }, tag, SystemClock.uptimeMillis());

                            }
                        }
                    }, tag, SystemClock.uptimeMillis());
                    return;
                }
                byte[] body = null;
                try {
                    body = response.body().bytes();
                } catch (final IOException e) {
                    handler.postAtTime(new Runnable() {
                        @Override
                        public void run() {
                            if (chainCallBack != null) {
                                chainCallBack.onFail(new TinaException(TinaException.IOEXCEPTION, e.getMessage()));
                            }
                            if (endCallBack != null) {
                                handler.postAtTime(new Runnable() {
                                    @Override
                                    public void run() {
                                        endCallBack.end();
                                    }
                                }, tag, SystemClock.uptimeMillis());

                            }
                        }
                    }, tag, SystemClock.uptimeMillis());
                    return;
                }
                mapChainRequest(body, chainCallBack, chainRequest, result, nextIndex, cache, cacheKey);
            }
        });
    }

    private boolean mapChainRequest(byte[] body, final TinaChainCallBack chainCallBack,
                                    TinaBaseRequest chainRequest, final Object result, final int nextIndex, Cache cache, String cacheKey) {

        Class clazz;
        if (chainCallBack != null) {
            clazz = chainCallBack.getClass();
        } else {
            handler.postAtTime(new Runnable() {
                @Override
                public void run() {
                    if (tinaBaseRequests.size() <= 0) {
                        if (endCallBack != null) {
                            endCallBack.end();
                        }
                    } else {
                        chainRequest(nextIndex, null);
                    }
                }
            }, tag, SystemClock.uptimeMillis());

            if (cache != null && cacheKey != null) {
                storeRequestCache(body, cache, cacheKey);
            }
            return true;
        }

        final TinaFilterResult filterResult = filter(chainRequest, body, ClassUtils.getGenericType(chainCallBack.getClass()));
        final Object tr = filterResult.response;
        if (filterResult.code == FilterCode.SUCCESS) {
            if (tr != null && tr.getClass().isAnnotationPresent(AutoMode.class)) {
                try {
                    AutoModelTool.inflateClassBean(tr.getClass(), tr);
                } catch (TinaDataException e) {
                    filterResult.code = FilterCode.FAIL;
                    filterResult.errorMsg = e.getMessage();
                    filterResult.errorCode = TinaException.DATA_EXCEPTION;
                    return chainFail(chainCallBack, cache, cacheKey, filterResult);
                }
            }

            handler.postAtTime(new Runnable() {
                @Override
                public void run() {
                    Object chainResult = null;
                    if (chainCallBack != null) {
                        chainResult = chainCallBack.onSuccess(result, tr);
                    }
                    if ((chainResult != null && chainResult instanceof String && TinaChain.FUSING.equals(chainResult)) || tinaBaseRequests.size() <= 0) {
                        if (endCallBack != null) {
                            endCallBack.end();
                        }
                    } else if (tinaBaseRequests.size() > 0) {
                        if (chainResult != null && chainResult instanceof String && TinaChain.CONTINUE.equals(chainResult)) {
                            chainRequest(nextIndex, null);
                        } else {
                            chainRequest(nextIndex, chainResult);
                        }
                    }
                }
            }, tag, SystemClock.uptimeMillis());

            /**
             * 缓存服务端返回的数据
             */
            if (cache != null && cacheKey != null) {
                storeRequestCache(body, cache, cacheKey);
            }
            return true;

        } else {
            return chainFail(chainCallBack, cache, cacheKey, filterResult);
        }
    }

    private boolean chainFail(final TinaChainCallBack chainCallBack, Cache cache, String cacheKey, final TinaFilterResult filterResult) {
        if (cache == null && cacheKey != null) {
            clearErrorCacheData(cacheKey);
            return false;
        }

        handler.postAtTime(new Runnable() {
            @Override
            public void run() {
                if (chainCallBack != null) {
                    chainCallBack.onFail(new TinaException(filterResult.errorCode, filterResult.errorMsg));
                }
                if (endCallBack != null) {
                    endCallBack.end();
                }
            }
        }, tag, SystemClock.uptimeMillis());
        return true;
    }


    /**
     * 单个请求
     **/
    private void singleRequest(final TinaBaseRequest sRequest, final TinaSingleCallBack sCallback) {

        if (startCallBack != null) {
            startCallBack.start();
        }

        Schedulers.subscribeOn(Schedulers.IO)
                .callback(new Schedulers.ScheduleCallBack() {
                    @Override
                    public void onCallBack(Object result) {
                        Pack pack = (Pack) result;
                        executeSingle(pack.post, pack.get, pack.put, pack.patch, pack.delete,
                                pack.url, pack.cacheKey, pack.cache, pack.body);
                    }
                })
                .run(new Schedulers.SRunnable<Pack>() {
                    @Override
                    public Pack callable() {

                        /**
                         *
                         * 请求形式确定 url生成
                         *
                         */
                        Post post = null;
                        Get get = null;
                        Put put = null;
                        Patch patch = null;
                        Delete delete = null;

                        String url = null;
                        if ((get = sRequest.getClass().getAnnotation(Get.class)) != null) {
                            String host = targetHost == null ? getConfig().hostUrl : targetHost;
                            url = host + UrlUtils.generatePathWithParams(get.value(), sRequest);
                        } else if ((post = sRequest.getClass().getAnnotation(Post.class)) != null) {
                            String host = targetHost == null ? getConfig().hostUrl : targetHost;
                            url = host + UrlUtils.generatePathWithoutParams(post.value(), sRequest);
                        } else if ((delete = sRequest.getClass().getAnnotation(Delete.class)) != null) {
                            String host = targetHost == null ? getConfig().hostUrl : targetHost;
                            url = host + UrlUtils.generatePathWithParams(delete.value(), sRequest);
                        } else if ((put = sRequest.getClass().getAnnotation(Put.class)) != null) {
                            String host = targetHost == null ? getConfig().hostUrl : targetHost;
                            url = host + UrlUtils.generatePathWithoutParams(put.value(), sRequest);
                        } else if ((patch = sRequest.getClass().getAnnotation(Patch.class)) != null) {
                            String host = targetHost == null ? getConfig().hostUrl : targetHost;
                            url = host + UrlUtils.generatePathWithoutParams(patch.value(), sRequest);
                        } else {
                            throw new IllegalArgumentException("request annotation is not exist");
                        }
                        final String cacheKey;
                        final Cache cache = sRequest.getClass().getAnnotation(Cache.class);
                        if (cache != null) {
                            if (getConfig().mDiskCache == null) {
                                throw new IllegalArgumentException("please check okhttp cache config is right , #" + sRequest.getClass().getCanonicalName());
                            }
                            String cacheTempKey = null;
                            if (cache == null || "".equals(cache.key())) {
                                cacheTempKey = url;
                            } else {
                                cacheTempKey = UrlUtils.generatePathWithoutParams(cache.key(), sRequest);
                            }
                            cacheKey = cacheTempKey;
                        } else {
                            cacheKey = null;
                        }

                        /**
                         * 缓存数据返回
                         */
                        if (cache != null && getConfig().mDiskCache != null) {
                            byte[] cacheResponse = getCacheBytes(cacheKey);
                            if (cacheResponse != null) {
                                if (sCallback != null && sCallback instanceof TinaSingleCacheCallBack) {
                                    onHoldCacheReturn(sRequest , cacheResponse , (TinaSingleCacheCallBack) sCallback);
                                } else {
                                    boolean s = mapSingleRequest(cacheResponse, null, cacheKey);
                                    /**
                                     * 如果缓存出现不匹配的问题(不确定的缓存规则改变)，则重新请求
                                     */
                                    if (s) {
                                        return null;
                                    }
                                }
                            }
                        }
                        return new Pack(post, get, put, patch, delete, url, cacheKey, cache, convertRequest(JSONHelper.objToJson(sRequest)));
                    }
                });
    }

    private void executeSingle(Post post, Get get, Put put, Patch patch, Delete delete,
                               String url, final String cacheKey, final Cache cache, String rb) {
        Request request = generateRequest(tinaRequest, post, get, put, patch, delete, url, rb);

        getConfig().mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                try {
                    handler.postAtTime(new Runnable() {
                        @Override
                        public void run() {
                            if (callBack != null) {
                                callBack.onFail(new TinaException(TinaException.IOEXCEPTION, e.getMessage()));
                            }
                        }
                    }, tag, SystemClock.uptimeMillis());
                } catch (Exception e1) {

                } finally {
                    if (endCallBack != null) {
                        handler.postAtTime(new Runnable() {
                            @Override
                            public void run() {
                                endCallBack.end();
                            }
                        }, tag, SystemClock.uptimeMillis());
                    }
                }
            }

            @Override
            public void onResponse(Call call, final Response response) {
                if (!response.isSuccessful()) {
                    handler.postAtTime(new Runnable() {
                        @Override
                        public void run() {
                            if (callBack != null) {
                                callBack.onFail(new TinaException(TinaException.OTHER_EXCEPTION, response.code() + " " + response.message()));
                            }
                            if (endCallBack != null) {
                                handler.postAtTime(new Runnable() {
                                    @Override
                                    public void run() {
                                        endCallBack.end();
                                    }
                                }, tag, SystemClock.uptimeMillis());

                            }
                        }
                    }, tag, SystemClock.uptimeMillis());
                    return;
                }
                byte[] body = null;
                try {
                    body = response.body().bytes();
                } catch (final IOException e) {
                    handler.postAtTime(new Runnable() {
                        @Override
                        public void run() {
                            if (callBack != null) {
                                callBack.onFail(new TinaException(TinaException.IOEXCEPTION, e.getMessage()));
                            }
                            if (endCallBack != null) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        endCallBack.end();
                                    }
                                });

                            }
                        }
                    }, tag, SystemClock.uptimeMillis());
                    return;
                }
                mapSingleRequest(body, cache, cacheKey);
            }
        });
    }

    private boolean mapSingleRequest(byte[] bytes, final Cache cache, String cacheKey) {
        Class clazz;
        if (callBack != null) {
            clazz = callBack.getClass();
        } else {
            handler.postAtTime(new Runnable() {
                @Override
                public void run() {
                    if (endCallBack != null) {
                        endCallBack.end();
                    }
                }
            }, tag, SystemClock.uptimeMillis());

            if (cache != null && cacheKey != null) {
                storeRequestCache(bytes, cache, cacheKey);
            }

            return true;
        }

        final TinaFilterResult filterResult = filter(tinaRequest, bytes, ClassUtils.getGenericType(clazz));
        final Object respBody = filterResult.response;
        if (filterResult.code == FilterCode.SUCCESS) {
            if (respBody != null && respBody.getClass().isAnnotationPresent(AutoMode.class)) {
                try {
                    AutoModelTool.inflateClassBean(respBody.getClass(), respBody);
                } catch (TinaDataException e) {
                    filterResult.code = FilterCode.FAIL;
                    filterResult.errorCode = TinaException.DATA_EXCEPTION;
                    filterResult.errorMsg = e.getMessage();
                    return singleFail(cache, cacheKey, filterResult);
                }
            }

            handler.postAtTime(new Runnable() {
                @Override
                public void run() {
                    if (callBack != null) {
                        callBack.onSuccess(respBody);
                    }
                    if (endCallBack != null) {
                        endCallBack.end();
                    }
                }
            }, tag, SystemClock.uptimeMillis());

            if (cache != null && cacheKey != null) {
                storeRequestCache(bytes, cache, cacheKey);
            }

            return true;

        } else {
            return singleFail(cache, cacheKey, filterResult);
        }

    }

    private boolean singleFail(Cache cache, String cacheKey, final TinaFilterResult filterResult) {
        /**
         * 如果缓存数据报错 则清空缓存数据
         */
        if (cache == null && cacheKey != null) {
            clearErrorCacheData(cacheKey);
            return false;
        }
        handler.postAtTime(new Runnable() {
            @Override
            public void run() {
                if (callBack != null) {
                    callBack.onFail(new TinaException(filterResult.errorCode, filterResult.errorMsg));
                }
                if (endCallBack != null) {
                    endCallBack.end();
                }
            }
        }, tag, SystemClock.uptimeMillis());
        return true;
    }

    /**
     * 缓存服务端返回的数据
     *
     * @param bytes
     * @param cache
     * @param cacheKey
     */
    private synchronized void storeRequestCache(byte[] bytes, Cache cache, String cacheKey) {
        int expire = cache.expire();
        TimeUnit unit = cache.unit();
        if (expire > 0) {
            getConfig().mDiskCache.put(cacheKey, bytes, (int) TimeUnit.SECONDS.convert(expire, unit));
        } else {
            getConfig().mDiskCache.put(cacheKey, bytes);
        }

    }

    /**
     * 生成request
     *
     * @param req
     * @param post
     * @param get
     * @param put
     * @param patch
     * @param delete
     * @param url
     * @param rb
     * @return
     */
    @Nullable
    private Request generateRequest(TinaBaseRequest req, Post post, Get get, Put put, Patch patch, Delete delete, String url, String rb) {
        Request request = null;
        if (get != null) {
            request = new Request.Builder()
                    .url(url)
                    .tag(tag)
                    .headers(headersBy(req.headers))
                    .get()
                    .build();
        } else if (post != null) {
            RequestBody body = RequestBody.create(mMediaType == null ? getConfig().mMediaType : mMediaType, rb == null ? "" : rb);
            request = new Request.Builder()
                    .url(url)
                    .tag(tag)
                    .headers(headersBy(req.headers))
                    .post(body)
                    .build();
        } else if (delete != null) {
            request = new Request.Builder()
                    .url(url)
                    .tag(tag)
                    .headers(headersBy(req.headers))
                    .delete()
                    .build();
        } else if (put != null) {
            RequestBody body = RequestBody.create(mMediaType == null ? getConfig().mMediaType : mMediaType, rb == null ? "" : rb);
            request = new Request.Builder()
                    .url(url)
                    .tag(tag)
                    .headers(headersBy(req.headers))
                    .put(body)
                    .build();
        } else if (patch != null) {
            RequestBody body = RequestBody.create(mMediaType == null ? getConfig().mMediaType : mMediaType, rb == null ? "" : rb);
            request = new Request.Builder()
                    .url(url)
                    .tag(tag)
                    .headers(headersBy(req.headers))
                    .patch(body)
                    .build();
        }
        return request;
    }

    private void onHoldCacheReturn(TinaBaseRequest sRequest , byte[] bytes , final TinaSingleCacheCallBack cacheCallBack) {
        final TinaFilterResult filterResult = filter(sRequest, bytes, ClassUtils.getGenericType(cacheCallBack.getClass()));
        final Object respBody = filterResult.response;
        if (filterResult.code == FilterCode.SUCCESS) {
            if (respBody != null && respBody.getClass().isAnnotationPresent(AutoMode.class)) {
                try {
                    AutoModelTool.inflateClassBean(respBody.getClass(), respBody);
                } catch (TinaDataException e) {
                    return;
                }
            }

            handler.postAtTime(new Runnable() {
                @Override
                public void run() {
                    cacheCallBack.onCache(respBody);
                }
            }, tag, SystemClock.uptimeMillis());
        }

    }

    /**
     * @param request
     * @param bytes
     * @return true -> onSuccess
     * false -> onFail
     */
    private TinaFilterResult filter(TinaBaseRequest request, byte[] bytes, Class clazz) {
        if (tinaFilter != null) {
            return tinaFilter.filter(request, bytes, clazz);

        } else if (getConfig().mCodeFilter != null) {
            return getConfig().mCodeFilter.filter(request, bytes, clazz);
        }

        if (bytes != null) {
            Object obj = new String(bytes);
            return new TinaFilterResult(FilterCode.SUCCESS, obj);
        }

        return new TinaFilterResult(FilterCode.SUCCESS, "");
    }

    /**
     * @return get okhttpClient instance
     */
    public OkHttpClient getClient() {
        return targetConfig == null ? defaultConfig.mClient : targetConfig.mClient;
    }

    /**
     * @return get host url
     */
    public String getHostUrl() {
        return targetConfig == null ? defaultConfig.hostUrl : targetConfig.hostUrl;
    }

    /**
     * request data convert.
     * commonly used to encrypt request data.
     *
     * @param source
     * @return
     */
    private String convertRequest(String source) {

        if (tinaConvert != null) {
            return tinaConvert.convert(source);
        }

        if (getConfig().mRequestConvert == null) {
            return source;
        }
        return getConfig().mRequestConvert.convert(source);
    }

    private synchronized void hitBoom() {
        hitCount++;
    }

    private synchronized boolean concurrentEnd() {
        if (hitCount >= tinaBaseRequests.size()) {
            hitCount = 0;
            return true;
        }
        return false;
    }

    private Headers headersBy(Map<String, String> map) {
        Headers.Builder builder = new Headers.Builder();
        for (String key : map.keySet()) {
            builder.add(key, map.get(key));
        }
        return builder.build();
    }

    @IntDef({Tina.CHAINS, Tina.CONCURRENT, Tina.SINGLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TinaType {

    }

    private void clearErrorCacheData(String cacheKey) {
        getConfig().mDiskCache.remove(cacheKey);
    }

    /**
     * cancel request
     */
    public void cancel() {

        if (tag == null) {
            return;
        }

        for (Call call : getConfig().mClient.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : getConfig().mClient.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void registerCancelEvent(final Activity act) {
        final int hashCode = act.hashCode();
        act.getApplication().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                if (activity.hashCode() == hashCode) {
                    handler.removeCallbacksAndMessages(tag);
                    cancel();
                    activity.getApplication().unregisterActivityLifecycleCallbacks(this);
                }
            }
        });
    }

    public static class Pack {
        public Post post;
        public Get get;
        public Put put;
        public Patch patch;
        public Delete delete;
        public String url;
        public String cacheKey;
        public Cache cache;
        public String body;

        public Pack(Post post, Get get, Put put, Patch patch, Delete delete, String url, String cacheKey, Cache cache, String body) {
            this.post = post;
            this.get = get;
            this.put = put;
            this.patch = patch;
            this.delete = delete;
            this.url = url;
            this.cacheKey = cacheKey;
            this.cache = cache;
            this.body = body;
        }
    }

    public static class Config {
        public OkHttpClient mClient;
        public String hostUrl;
        public MediaType mMediaType;
        public TinaFilter mCodeFilter;
        public ACache mDiskCache;
        public TinaConvert mRequestConvert;
    }
}
