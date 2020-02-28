package com.savor.ads.activity;

import android.content.Intent;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.savor.ads.R;
import com.savor.ads.core.ApiRequestListener;
import com.savor.ads.core.AppApi;
import com.savor.ads.customview.CircleProgressBar;

import java.security.spec.ECField;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.savor.ads.utils.ShowMessage.*;

public class WebviewGameActivity extends BaseActivity{

    private WebView mWebView;
    private String MONKEY_URL = "http://games.littlehotspot.com/g/MonkeysClimbTrees/r/room001";
    private boolean isSuccess = false;
    private boolean isError = false;
    private android.widget.ImageView mLoadingIv;
    private CircleProgressBar mProgressBar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_game);
        mWebView = findViewById(R.id.web_view);
        mProgressBar = findViewById(R.id.pb_image);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true); // 关键点
        webSettings.setAllowFileAccess(true); // 允许访问文件
        webSettings.setSupportZoom(true); // 支持缩放
        webSettings.setDatabaseEnabled(true);
//        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        //自适应屏幕
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 不加载缓存内容
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webSettings.setMediaPlaybackRequiresUserGesture(false);
        }

        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override public boolean onJsAlert(WebView view, String url, String message, JsResult result){
                return super.onJsAlert(view,url,message,result);
            }
        });
        String url = getIntent().getStringExtra("url");
        mWebView.loadUrl(url);

    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            //返回false，意味着请求过程里，不管有多少次的跳转请求（即新的请求地址），均交给webView自己处理，这也是此方法的默认处理
            //返回true，说明你自己想根据url，做新的跳转，比如在判断url符合条件的情况下，我想让webView加载http://ask.csdn.net/questions/178242
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (!isError) {
                isSuccess = true;
                //回调成功后的相关操作
                mProgressBar.setVisibility(GONE);
            }
            isError = false;
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, android.webkit.WebResourceError error){
            isSuccess = false;
            isError = true;
            showToast(mContext, "加载失败即将退出");
            exitWebview();
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }


    }

    @Override
    protected void onResume() {
        try{
            if (mWebView!=null){
                mWebView.onResume();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        super.onResume();


    }

    @Override
    protected void onPause() {
        try{
            if (mWebView!=null){
                mWebView.onPause();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            if (mWebView!=null){
                mWebView.destroy();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void exitWebview(){
        AppApi.postLogoutGameH5(mContext,apiRequestListener);
        finish();
    }

    ApiRequestListener apiRequestListener = new ApiRequestListener() {
        @Override
        public void onSuccess(AppApi.Action method, Object obj) {

        }

        @Override
        public void onError(AppApi.Action method, Object obj) {

        }

        @Override
        public void onNetworkFailed(AppApi.Action method) {

        }
    };
}
