package com.savor.ads.utils;

import android.text.TextUtils;

import com.savor.ads.core.ApiRequestListener;
import com.savor.ads.core.AppApi;
import com.savor.ads.core.AppServiceOk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 回调曝光链接的实现类
 */
public class RetryHandler {

    private static volatile boolean mIsRunning = false;
    private static List<RequestWrap> mRequestWraps;

    /**
     * 加入将要曝光的链接和截止时间
     * @param url       要曝光的链接
     * @param endTime   截止时间
     */
    public static void enqueue(String url, int endTime) {
        if (mRequestWraps == null) {
            mRequestWraps = Collections.synchronizedList(new ArrayList<RequestWrap>());
        }

        mRequestWraps.add(new RequestWrap(url, -1, endTime));

        doRequest();
    }

    private static void doRequest() {
        if (!mIsRunning) {
            mIsRunning = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (mIsRunning && mRequestWraps != null) {
                        LogUtils.d("开始执行请求，数量：" + mRequestWraps.size());

                        for (final RequestWrap requestWrap : mRequestWraps) {
                            if (requestWrap != null && (requestWrap.state == 0) &&
                                    !TextUtils.isEmpty(requestWrap.url) &&
                                    requestWrap.endTime > System.currentTimeMillis() / 1000) {
                                requestWrap.state = 1;
                                new AppServiceOk(null, new ApiRequestListener() {
                                    @Override
                                    public void onSuccess(AppApi.Action method, Object obj) {
                                        LogUtils.d("请求成功！" + requestWrap.url);
                                        requestWrap.retryCount--;
                                        requestWrap.state = 2;
                                    }

                                    @Override
                                    public void onError(AppApi.Action method, Object obj) {
                                        LogUtils.d("请求失败！" + requestWrap.url);
                                        requestWrap.retryCount--;
                                        requestWrap.state = 0;
                                    }

                                    @Override
                                    public void onNetworkFailed(AppApi.Action method) {
                                        LogUtils.d("请求失败！" + requestWrap.url);
                                        requestWrap.retryCount--;
                                        requestWrap.state = 0;
                                    }
                                }).simpleGet(requestWrap.url);
                            }
                        }

                        Iterator<RequestWrap> interator = mRequestWraps.iterator();
                        while (interator.hasNext()) {
                            if (interator.next().state == 2) {
                                interator.remove();
                            }
                        }

                        if (mRequestWraps.isEmpty()) {
                            mIsRunning = false;
                            break;
                        } else {
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }).start();
        }
    }

    private static class RequestWrap {
        String url;
        int retryCount;
        int endTime;
        /**
         * 请求状态
         * 0：待请求；
         * 1：请求中；
         * 2：请求完成
         */
        int state;

        private RequestWrap(String url, int retryCount, int endTime) {
            this.url = url;
            this.retryCount = retryCount;
            this.endTime = endTime;
        }
    }
}
