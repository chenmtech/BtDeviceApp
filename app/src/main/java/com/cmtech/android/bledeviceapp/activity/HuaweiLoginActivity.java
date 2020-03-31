package com.cmtech.android.bledeviceapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.util.HttpUtils;
import com.vise.log.ViseLog;

public class HuaweiLoginActivity extends AppCompatActivity {
    public static final String HUAWEI_PLAT_NAME = "Huawei";

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
                    ViseLog.e("Huawei login response: " + url);
                    String id = HttpUtils.parseUrl(url).get("open_id");
                    String name = HttpUtils.parseUrl(url).get("displayName");
                    LoginActivity.loginMainActivity(HuaweiLoginActivity.this, HUAWEI_PLAT_NAME, id, name);
                    return true;
                }
                return false;
            }

            @Deprecated
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                ViseLog.e(view.getUrl() + "----" + description);
                Toast.makeText(HuaweiLoginActivity.this, "Huawei login failure", Toast.LENGTH_LONG).show();
                finish();
            }
        });
        final String url = "http://huawei.tighoo.com/home/login";
        webView.loadUrl(url);
    }
}
