package com.savor.ads.core;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessageV3;
import com.savor.ads.bean.JsonBean;
import com.savor.ads.bean.RtbRequest;
import com.savor.ads.okhttp.OkHttpUtils;
import com.savor.ads.okhttp.callback.Callback;
import com.savor.ads.okhttp.callback.FileDownProgress;
import com.savor.ads.okhttp.coreProgress.ProgressHelper;
import com.savor.ads.okhttp.coreProgress.download.UIProgressResponseListener;
import com.savor.ads.okhttp.coreProgress.upload.UIProgressRequestListener;
import com.savor.ads.okhttp.request.GetRequest;
import com.savor.ads.okhttp.request.PostFormRequest;
import com.savor.ads.okhttp.request.PostProtoBufferRequest;
import com.savor.ads.okhttp.request.PostStringRequest;
import com.savor.ads.okhttp.request.RequestCall;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;

import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tianshu.ui.api.ZmtAPI;

public class AppServiceOk {
    public static final String TAG = AppServiceOk.class.getSimpleName();
    private Context mContext;
    private AppApi.Action action;
    private ApiRequestListener handler;
    private Object mParameter;
    private OkHttpUtils okHttpUtils;
    private OkHttpClient client;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private boolean isNeedUpdateUI = true;
    /**
     * 应用Session
     */
    protected Session appSession;
    private String req_id;

    private static long cacheSize = 1024 * 1024 * 5;

    private static int CONNTIMEOUT = 60*1000;
    private static int READTIMEOUT = 5*60*1000;
    private static int WRITETIMEOUT = 5*60*1000;

    public AppServiceOk(Context context, ApiRequestListener handler) {
        this(context, null, handler);
    }

    public AppServiceOk(Context context, AppApi.Action action, ApiRequestListener handler) {
        this(context, action, handler, null);
    }

    public AppServiceOk(Context context, AppApi.Action action, ApiRequestListener handler, Object params) {
        this.mContext = context;
        this.action = action;
        this.handler = handler;
        this.mParameter = params;
        this.appSession = Session.get(context);
        this.okHttpUtils = OkHttpUtils.getInstance();
        this.client = okHttpUtils.getOkHttpClient();
    }
    public AppServiceOk(Context context, AppApi.Action action, ApiRequestListener handler, Object params,String reqid) {
        this.mContext = context;
        this.action = action;
        this.handler = handler;
        this.mParameter = params;
        this.req_id = reqid;
        this.appSession = Session.get(context);
        this.okHttpUtils = OkHttpUtils.getInstance();
        this.client = okHttpUtils.getOkHttpClient();
    }
    public void cancelTag(Object tag) {
        okHttpUtils.cancelTag(tag);

    }

    public void cancelByAction() {
        okHttpUtils.cancelTag(this.action);

    }

    public synchronized boolean isNeedUpdateUI() {
        return isNeedUpdateUI;
    }

    public synchronized void setNeedUpdateUI(boolean isNeedUpdateUI) {
        this.isNeedUpdateUI = isNeedUpdateUI;
    }

    public void post() {
        post(false, false, false, false);
    }

    /**
     * @param isCache         是否需要缓存
     * @param isGzip          是否需要服务端返回的数据Gzip
     * @param isDes           是否需要返回的数据进行Des加密
     * @param isNeedUserAgent 是否需要设置User-Agent请求头
     */
    public void post(final boolean isCache, boolean isGzip, boolean isDes, boolean isNeedUserAgent) {
        final String requestUrl;
        try {
            final Object obj;
            if (action.name().endsWith("PLAIN")) {
                obj = mParameter;
            } else {
                /** 序列化请求包体json */
                obj = ApiRequestFactory.getRequestEntity(action, mParameter, appSession);
            }
            requestUrl = AppApi.API_URLS.get(action);

            final Map<String, String> headers = new HashMap<>();
            headers.put("traceinfo", appSession.getDeviceInfo());
            LogUtils.d("url-->" + requestUrl);
            LogUtils.d("traceinfo-->" + appSession.getDeviceInfo());
            headers.put("boxMac", appSession.getEthernetMac());
            headers.put("hotelId", appSession.getBoiteId());
            if (isGzip) {
                headers.put("Accept-Encoding", "gzip");
            }
            if (isNeedUserAgent) {
                headers.put("User-Agent", "tcphone");// 添加请求头
            }
            if (isDes) {
                headers.put("des", "true");
            }


            /**
             * 1.通过一个requrest构造方法将参数传入
             * 2.
             */
            Callback<Object> callback = new Callback<Object>() {

                @Override
                public Object parseNetworkResponse(Response response)
                        throws Exception {
//					try {
                    try {
                        System.err.println(response.cacheResponse().body().string());
                    } catch (Exception e) {
                    }

                    Object object = ApiResponseFactory.getResponse(mContext, action, response, "", req_id);

                    LogUtils.d(object.toString() + "");
                    response.close();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
                    return object;
                }

                @Override
                public void onError(Call call, Exception e) {
                    if (handler != null) {
                        handler.onNetworkFailed(action);
                    }
                }

                @Override
                public void onResponse(Object response) {
                    if (handler != null) {
                        if (response instanceof ResponseErrorMessage) {
                            handler.onError(action, response);
                        } else {
                            handler.onSuccess(action, response);
                        }
                    }
                }

            };
            if (requestUrl.contains("getConfig")){
                Log.d("测试netty启动",requestUrl);
            }
            PostStringRequest stringRequest = new PostStringRequest(requestUrl, action, null, headers, obj.toString(), MediaType.parse("application/json;charset=utf-8"));
            RequestCall requestCall = new RequestCall(stringRequest);
            requestCall.connTimeOut(CONNTIMEOUT);
            requestCall.readTimeOut(READTIMEOUT);
            requestCall.writeTimeOut(WRITETIMEOUT);
            requestCall.execute(callback);


        } catch (Exception e) {
            LogUtils.d(e.toString());
        }
    }

    /**
     * 获取netty服务器IP和端口专用方法
     */
    public void postByAsynWithForm() {
        try {
            Map<String, String> params = null;
            if (mParameter instanceof HashMap) {
                params = (Map<String, String>) mParameter;
            }
            String requestUrl = AppApi.API_URLS.get(action);

            Log.d("测试netty启动",requestUrl);
            Log.d("测试netty启动","paramsMap"+params.toString());

            final Map<String, String> headers = new HashMap<>();
            headers.put("traceinfo", appSession.getDeviceInfo());
            LogUtils.d("url-->" + requestUrl);
            headers.put("boxMac", appSession.getEthernetMac());
            headers.put("hotelId", appSession.getBoiteId());
            headers.put("Connection", "keep-alive");
            headers.put("platform", "2");
            headers.put("phoneModel", Build.MODEL);
            headers.put("systemVersion", Build.VERSION.RELEASE);
            headers.put("appVersion", "3.2.0");

            Callback<Object> callback = new Callback<Object>() {

                @Override
                public Object parseNetworkResponse(Response response) {
                    Object object = null;
                    try {
                        object = ApiResponseFactory.getResponse(mContext, action, response, "", req_id);
                        LogUtils.d(object.toString() + "");
                        response.close();
                    } catch (Exception e) {
                    }
                    return object;
                }

                @Override
                public void onError(Call call, Exception e) {
                    Log.d("测试netty启动异常",action+e.getMessage());
                    if (handler != null) {
                        handler.onNetworkFailed(action);
                    }
                }

                @Override
                public void onResponse(Object response) {
                    Log.d("测试netty启动","action="+action+"response="+response);
                    if (handler != null) {
                        if (response instanceof ResponseErrorMessage) {
                            handler.onError(action, response);
                        } else {
                            handler.onSuccess(action, response);
                        }
                    }
                }

            };
            PostFormRequest formRequest = new PostFormRequest(requestUrl, action, params, headers);
            RequestCall requestCall = new RequestCall(formRequest);
            requestCall.connTimeOut(CONNTIMEOUT);
            requestCall.readTimeOut(READTIMEOUT);
            requestCall.writeTimeOut(WRITETIMEOUT);
            requestCall.execute(callback);
        } catch (Exception e) {
            LogUtils.d(e.toString());
        }
    }

    public void requestPostByAsynWithForm(HashMap<String, String> paramsMap) {
        try {
            String requestUrl = AppApi.API_URLS.get(action);

            FormBody.Builder builder = new FormBody.Builder();
            for (String key : paramsMap.keySet()) {
                builder.add(key, paramsMap.get(key));
            }
            RequestBody formBody = builder.build();
            final Request request = addHeaders().url(requestUrl).post(formBody).build();
            final Call call = client.newCall(request);
            call.enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    handler.onNetworkFailed(action);
                }

                @Override
                public void onResponse(Call call, Response response){

                    Object object = ApiResponseFactory.getResponse(mContext, action, response, "", req_id);
                    if (handler != null) {
                        if (object instanceof ResponseErrorMessage) {
                            handler.onError(action, object);
                        } else {
                            handler.onSuccess(action, object);
                        }
                    }
                }
            });

        } catch (Exception e) {
            Log.e("AppServiceOk", e.toString());
        }
    }
    /**
     * 统一为请求添加头信息
     * @return
     */
    private Request.Builder addHeaders() {
        Request.Builder builder = new Request.Builder()
                .addHeader("Connection", "keep-alive")
                .addHeader("platform", "2")
                .addHeader("phoneModel", Build.MODEL)
                .addHeader("systemVersion", Build.VERSION.RELEASE)
                .addHeader("appVersion", "3.2.0");
        return builder;
    }

    /**
     * get请求，一般是需要baseURL的
     */

    public void get() {
        get(false, false, false, false);
    }

    /**
     * @param isCache         是否需要缓存
     * @param isGzip          是否需要服务端返回的数据Gzip
     * @param isDes           是否需要返回的数据进行Des加密
     * @param isNeedUserAgent 是否需要设置User-Agent请求头
     */
    public void get(final boolean isCache, boolean isGzip, boolean isDes, boolean isNeedUserAgent) {
        String requestUrl = AppApi.API_URLS.get(action);
        Map<String, String> requestParams = null;
        if (mParameter instanceof HashMap) {
            requestParams = (HashMap<String, String>) mParameter;
        }
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("traceinfo", appSession.getDeviceInfo());
        LogUtils.d("traceinfo-->" + appSession.getDeviceInfo());
        headers.put("boxMac", appSession.getEthernetMac());
        headers.put("hotelId", appSession.getBoiteId());
        headers.put("X-VERSION",appSession.getVersionCode()+"");
        Callback<Object> callback = new Callback<Object>() {

            @Override
            public Object parseNetworkResponse(Response response){
                Object object = ApiResponseFactory.getResponse(mContext, action, response, "", "");

                LogUtils.d(object.toString() + "");
                response.close();
                return object;
            }

            @Override
            public void onError(Call call, Exception e) {
                if (handler != null) {
                    handler.onNetworkFailed(action);
                }
            }

            @Override
            public void onResponse(Object response) {
                if (handler != null) {
                    if (response instanceof ResponseErrorMessage) {
                        handler.onError(action, response);
                    } else {
                        handler.onSuccess(action, response);
                    }
                }
            }

            @Override
            public void inProgress(float progress) {
                super.inProgress(progress);
            }

        };

        requestUrl = ApiRequestFactory.getUrlRequest(requestUrl, action, mParameter, appSession);
        LogUtils.d("requestUrl-->" + requestUrl);
        GetRequest getRequest = new GetRequest(requestUrl, action, requestParams, headers);
        RequestCall requestCall = new RequestCall(getRequest);
        requestCall.connTimeOut(CONNTIMEOUT);
        requestCall.readTimeOut(READTIMEOUT);
        requestCall.writeTimeOut(WRITETIMEOUT);
        requestCall.execute(callback);
    }

    public void simpleGet(String requestUrl) {
        Callback<Object> callback = new Callback<Object>() {

            @Override
            public Object parseNetworkResponse(Response response) {
                Object object = null;
                try {
                    object = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                response.close();
                return object;
            }

            @Override
            public void onError(Call call, Exception e) {
                if (handler != null) {
                    handler.onNetworkFailed(action);
                }
            }

            @Override
            public void onResponse(Object response) {
                if (handler != null) {
                    if (response instanceof ResponseErrorMessage) {
                        handler.onError(action, response);
                    } else {
                        handler.onSuccess(action, response);
                    }
                }
            }

            @Override
            public void inProgress(float progress) {
                super.inProgress(progress);
            }

        };

        GetRequest getRequest = new GetRequest(requestUrl, null, null, null);
        RequestCall requestCall = new RequestCall(getRequest);
        requestCall.execute(callback);
    }

    public JsonBean syncGet() throws IOException {
        String requestUrl = AppApi.API_URLS.get(action);

        requestUrl = ApiRequestFactory.getUrlRequest(requestUrl, action, mParameter, appSession);
        LogUtils.d("url-->" + requestUrl);
        Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader("traceinfo", appSession.getDeviceInfo())
                .addHeader("boxMac", appSession.getEthernetMac())
                .addHeader("hotelId", appSession.getBoiteId())
                .addHeader("X-VERSION",appSession.getVersionCode()+"")
                .build();

        Response response = okHttpUtils.getOkHttpClient().newCall(request).execute();
        String body = response.body().string();
        String smallType = response.header("X-SMALL-TYPE");
        JsonBean jsonBean = new JsonBean();
        jsonBean.setConfigJson(body);
        if (!TextUtils.isEmpty(smallType)){
            jsonBean.setSmallType(smallType);
        }
        response.close();
        return jsonBean;
    }

    /**
     * 下载文件
     *
     * @param url
     * @param targetFile
     */
    public void downLoad(String url, final String targetFile) {
        LogFileUtil.writeDownloadLog("下载文件----fileName="+targetFile+",fileUrl="+url);
        Map<String, String> requestParams = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        int read = 0;
        //这个是ui线程回调，可直接操作UI
        final UIProgressResponseListener uiProgressResponseListener = new UIProgressResponseListener() {
            @Override
            public void onPreExecute(long contentLength) {

            }

            @Override
            public void FailedDownload() {

            }

            @Override
            public void onUIResponseProgress(long bytesRead, long contentLength, boolean done) {
                LogUtils.e("TAG123" + "bytesRead:" + bytesRead);
                LogUtils.e("TAG123" + "contentLength:" + contentLength);
                LogUtils.e("TAG123" + "done:" + done);
                System.out.format("%d%% done\n", (100 * bytesRead) / contentLength);
                if (contentLength != -1) {
                    //长度未知的情况下回返回-1
                    LogUtils.e("TAG123" + (100 * bytesRead) / contentLength + "% done");
                }
                //ui层回调

                if (handler != null) {
                    FileDownProgress fileDownProgress = new FileDownProgress();
                    fileDownProgress.setTotal(contentLength);
                    fileDownProgress.setNow(bytesRead);
                    fileDownProgress.setLoading(done);
                    handler.onSuccess(action, fileDownProgress);
                }
            }
        };


        okhttp3.Callback callback2 = new okhttp3.Callback() {
            @Override
            public void onFailure(Call var1, IOException e) {
                LogUtils.e("下载失败", e);
            }

            @Override
            public void onResponse(Call var1, Response response){
                //将返回结果转化为流，并写入文件
                InputStream inputStream = null;
                FileOutputStream fileOutputStream = null;
                try {
                    int len;
                    byte[] buf = new byte[1024*1024*5];
                    inputStream = response.body().byteStream();
                    //可以在这里自定义路径
                    final File file = new File(targetFile);
                    fileOutputStream = new FileOutputStream(file);

                    while ((len = inputStream.read(buf)) != -1) {
                        fileOutputStream.write(buf, 0, len);
                    }

                    LogUtils.d("下载进度完成关闭进度条");
                    if (handler != null) {
                        handler.onSuccess(action, file);
                    }
                } catch (Exception e) {
                    LogUtils.e("下载文件写入异常", e);
                    if (handler!=null){
                        handler.onError(action,null);
                    }
                } finally {
                    IOUtils.closeQuietly(fileOutputStream);
                    IOUtils.closeQuietly(inputStream);
                    response.close();
                }

            }
        };
        //封装请求
        Request request = new Request.Builder()
                //下载地址
                .url(url)
                .build();
        ProgressHelper.addProgressResponseListener(client, uiProgressResponseListener,targetFile).newCall(request).enqueue(callback2);
    }

    public void postProto(ZmtAPI.ZmAdRequest message) {
        try {
            String requestUrl = AppApi.API_URLS.get(action);
            /**
             * 1.通过一个requrest构造方法将参数传入
             * 2.
             */
                Callback<Object> callback = new Callback<Object>() {

                @Override
                public Object parseNetworkResponse(Response response) {

                    Object object = ApiResponseFactory.getResponse(mContext, action, response, "", "");
                    response.close();
                    return object;
                }

                @Override
                public void onError(Call call, Exception e) {
                    if (handler != null) {
                        handler.onNetworkFailed(action);
                    }
                }

                @Override
                public void onResponse(Object response) {
                    if (handler != null) {
                        handler.onSuccess(action, response);
                    }
                }

            };
            PostProtoBufferRequest stringRequest = new PostProtoBufferRequest(requestUrl, action, null, null, null,message.toByteArray());
            RequestCall requestCall = new RequestCall(stringRequest);
            requestCall.execute(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
