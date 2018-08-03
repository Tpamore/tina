package com.tpa.client.tina.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author tqf
 */
public class Schedulers {

    /**
     * 适用于I/O操作 eg.读写文件、读写数据库、网络信息交互等
     **/
    public static final int IO = 1001;
    public static ExecutorService cachedThreadPool;

    /**
     * 适用于密集型计算操作 eg.例如图形的计算、加密算法计算、以及各种复杂动画插值计算的的等
     **/
    public static final int COMPUTATION = 1002;
    public static ExecutorService singleThreadExecutor;

    private static Handler handler = new Handler(Looper.getMainLooper());

    /**
     * 调度类型
     **/
    private int mScheduleType = 0;

    /**
     * 调度完成时回调
     **/
    private ScheduleCallBack mCallback;

    public static Schedulers subscribeOn(int scheduleType) {
        Schedulers schedulers = new Schedulers();
        schedulers.setmScheduleType(scheduleType);
        return schedulers;
    }

    public static int io() {
        return IO;
    }

    public static int computation() {
        return COMPUTATION;
    }

    public Schedulers callback(ScheduleCallBack callback) {
        this.mCallback = callback;
        return this;
    }

    public void run(ZbjRunnable runnable) {
        runnable.setCallBack(mCallback);
        switch (mScheduleType) {
            case IO:
                if (cachedThreadPool == null) {
                    cachedThreadPool = new ThreadPoolExecutor(5, 20,
                            100L, TimeUnit.SECONDS,
                            new LinkedBlockingQueue<Runnable>(50)
                            , new RejectedExecutionHandler() {
                        @Override
                        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

                        }
                    });
                }
                cachedThreadPool.execute(runnable);
                break;
            case COMPUTATION:
                if (singleThreadExecutor == null) {
                    singleThreadExecutor = new ThreadPoolExecutor(0, 1,
                            100L, TimeUnit.MILLISECONDS,
                            new LinkedBlockingQueue<Runnable>(50),
                            new RejectedExecutionHandler() {
                                @Override
                                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

                                }
                            });
                }
                singleThreadExecutor.execute(runnable);
                break;
            default:
                break;
        }
    }

    public int getmScheduleType() {
        return mScheduleType;
    }

    public void setmScheduleType(int mScheduleType) {
        this.mScheduleType = mScheduleType;
    }

    public static interface ScheduleCallBack {
        public void onCallBack(Object result);
    }

    public static abstract class SRunnable<T extends Object> extends
            ZbjRunnable {

        @Override
        public void run() {
            long t1 = System.currentTimeMillis();
            final T result = callable();
            if (callBack != null && result != null) {
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        callBack.onCallBack(result);
                    }
                });
            }
        }

        public abstract T callable();
    }

    public static abstract class SNullRunnable extends ZbjRunnable {

        @Override
        public void run() {
            long t1 = System.currentTimeMillis();
            callable();
            if (callBack != null) {
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        callBack.onCallBack(null);
                    }
                });
            }
        }

        @Override
        public void setCallBack(ScheduleCallBack callBack) {
            this.callBack = callBack;
        }

        public abstract void callable();
    }

    private static abstract class ZbjRunnable implements Runnable {

        public ScheduleCallBack callBack;

        public void setCallBack(ScheduleCallBack callBack) {
            this.callBack = callBack;
        }

    }

}
