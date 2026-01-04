package com.develophub.roomfinder;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;

public class ChatActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        webView = findViewById(R.id.webViewChat);
        loader = findViewById(R.id.chatLoader);

        // WebView Settings
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Chat ke liye zaroori hai
        webSettings.setDomStorageEnabled(true); // Tawk.to ko storage chahiye hoti hai

        // Page load hone par loader ko hide karne ke liye
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                loader.setVisibility(View.GONE);
            }
        });

        // ⚠️ Apna Tawk.to Direct Chat Link yahan paste karein
        webView.loadUrl("https://tawk.to/chat/6949529ca83a5d19807b4cee/1jd36ldah");
    }

    // Back button dabane par agar chat mein back ja sake to jaye, nahi to activity band ho
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}