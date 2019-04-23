package com.cmtech.android.bledeviceapp.activity;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.cmtech.android.bledeviceapp.R;

/**
  *
  * ClassName:      NewsActivity
  * Description:    新闻Activity
  * Author:         chenm
  * CreateDate:     2019-04-23 15:46
  * UpdateUser:     chenm
  * UpdateDate:     2019-04-23 15:46
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class NewsActivity extends AppCompatActivity {

    private WebView wvNews;

    private ProgressBar pbNewsLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        Toolbar toolbar = findViewById(R.id.tb_open_news);

        setSupportActionBar(toolbar);

        pbNewsLoad = findViewById(R.id.pb_news_loading);

        wvNews = findViewById(R.id.wv_news);

        wvNews.loadUrl("https://www.gdmu.edu.cn");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            wvNews.getSettings().setSafeBrowsingEnabled(false);
        }

        WebSettings settings = wvNews.getSettings();
        settings.setJavaScriptEnabled(true);

        wvNews.setWebViewClient(new WebViewClient() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(String.valueOf(request.getUrl()));
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                pbNewsLoad.setVisibility(View.GONE);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                pbNewsLoad.setVisibility(View.VISIBLE);
            }

        });

        wvNews.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                pbNewsLoad.setProgress(newProgress);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED, null);
                finish();
                break;

        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (wvNews.canGoBack() && keyCode == KeyEvent.KEYCODE_BACK){
            wvNews.goBack();

            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        wvNews.destroy();
        wvNews = null;
    }
}
