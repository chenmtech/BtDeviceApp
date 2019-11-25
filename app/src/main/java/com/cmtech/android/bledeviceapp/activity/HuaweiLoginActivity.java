package com.cmtech.android.bledeviceapp.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.util.HttpUtils;
import com.vise.log.ViseLog;

public class HuaweiLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_huwei_login);
        final WebView webView = findViewById(R.id.wvLoginHuawei);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.requestFocus();
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("open_id=")) {
                    HttpUtils.open_id = HttpUtils.parseUrl(url).get("open_id");
                    AccountManager.getInstance().getAccount().setHuaweiId(HttpUtils.open_id);
                    Intent intent = new Intent(HuaweiLoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

                return false;
            }

            @Deprecated
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                Toast.makeText(HuaweiLoginActivity.this, view.getUrl() + "----" + description, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(HuaweiLoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

        });
        String url = "http://huawei.tighoo.com/home/login";
        webView.loadUrl(url);
    }
}
