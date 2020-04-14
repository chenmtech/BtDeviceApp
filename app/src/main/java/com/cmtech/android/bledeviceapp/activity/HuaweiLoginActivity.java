package com.cmtech.android.bledeviceapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.HttpUtils;
import com.vise.log.ViseLog;

import static com.cmtech.android.bledeviceapp.AppConstant.HW_PLAT_NAME;

public class HuaweiLoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_huawei);
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
                    String icon = HttpUtils.parseUrl(url).get("headPictureURL");

                    Intent intent = new Intent();
                    intent.putExtra("platId", id);
                    intent.putExtra("userName", name);
                    intent.putExtra("icon", icon);
                    setResult(RESULT_OK, intent);
                    finish();
                    return true;
                }
                return false;
            }

            @Deprecated
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                ViseLog.e(view.getUrl() + "----" + description);
                Toast.makeText(HuaweiLoginActivity.this, "Huawei login failure", Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        final String url = "http://huawei.tighoo.com/home/login";
        webView.loadUrl(url);
    }
}
