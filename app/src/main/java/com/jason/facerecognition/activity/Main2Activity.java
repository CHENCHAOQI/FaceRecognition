package com.jason.facerecognition.activity;

import android.app.Activity;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;

import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.jason.facerecognition.R;
import com.jason.facerecognition.helper.WebAppInterface;

public class Main2Activity extends Activity {
    private WebView mWebView ;
    private static final String ContentURL =
//            "https://konatasick.github.io/test_simulator/summer2.html";
            "https://baidu.com";
    private static final String info =
//            "请开始你的表演！";
            "其实这是个浏览器！";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toast.makeText(Main2Activity.this,info, Toast.LENGTH_LONG).show();
        //获取webview控件
        mWebView = (WebView) findViewById(R.id.webView);
        preSetting();

        //将WebAppInterface于javascript绑定
        mWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
        mWebView.loadUrl(ContentURL);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        //  return true;
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }


    private void preSetting(){
        //加载服务器上的页面
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBlockNetworkImage(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebView.getSettings().setMixedContentMode(
                    WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view,
                                           SslErrorHandler handler, SslError error) {
                // TODO Auto-generated method stub
                // handler.cancel();// Android默认的处理方式
                handler.proceed();// 接受所有网站的证书
                // handleMessage(Message msg);// 进行其他处理
            }
        });
        //加载本地中的html
        //myWebView.loadUrl("file:///android_asset/www/test2.html");
        //加上下面这段代码可以使网页中的链接不以浏览器的方式打开
        mWebView.setWebViewClient(new WebViewClient());
        //允许使用javascript
        mWebView.getSettings().setJavaScriptEnabled(true);
    }
}
