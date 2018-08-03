package com.square.renov.swipevoicechat.Activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.square.renov.swipevoicechat.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class WebActivity extends AppCompatActivity{
    @BindView(R.id.web_view)
    WebView webView;
    @BindView(R.id.tv_title_bar)
    TextView titleBar;

    Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        unbinder = ButterKnife.bind(this);

        String name = getIntent().getStringExtra("name");

        String title = "";
        if("privacy".equals(name))
            title = "개인정보 보호 약관";
        else if("location".equals(name))
            title = "위치정보 이용 약관";
        else if("policy".equals(name))
            title = "서비스 이용 약관";
        titleBar.setText(title);

        String url = "http://13.125.253.85/api/terms/" + name;
        webView.setVerticalScrollBarEnabled(false);
        webView.getSettings().setJavaScriptEnabled(true);
        if(Build.VERSION.SDK_INT >= 21) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webView.loadUrl(url);
    }

    @OnClick(R.id.iv_back)
    public void onClickBack(){
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
