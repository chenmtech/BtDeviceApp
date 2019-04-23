package com.cmtech.android.bledeviceapp.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        Toolbar toolbar = findViewById(R.id.tb_open_news);

        setSupportActionBar(toolbar);

        wvNews = findViewById(R.id.wv_news);

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
        });

        wvNews.loadUrl("http://www.gdmu.edu.cn");

    }

}
