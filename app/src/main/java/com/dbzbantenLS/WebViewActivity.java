package com.dbzbantenLS;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.WindowManager;

public class WebViewActivity extends Activity {
    
    private WebView webView;
    private Button btnStar, btnRefresh, btnStop;
    private static final String BASE_URL = "https://script.google.com/macros/s/AKfycbzZExAchskYfYZwj1w0KV_0ZQuAFrR_RzTTbzWNVuy7sRad3mhwJ-FoTrmW4POZwKON/exec";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set portrait orientation & size
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setLayout(720, WindowManager.LayoutParams.WRAP_CONTENT);
        
        // Create layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        
        // Button Layout
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        
        // Star Button (Open URL)
        btnStar = new Button(this);
        btnStar.setText("★ Star");
        btnStar.setOnClickListener(v -> {
            webView.loadUrl(BASE_URL);
            Toast.makeText(this, "Opening URL...", Toast.LENGTH_SHORT).show();
        });
        
        // Refresh/Random Button
        btnRefresh = new Button(this);
        btnRefresh.setText("↻ Refresh");
        btnRefresh.setOnClickListener(v -> {
            // Reload with random parameter to bypass cache
            String randomUrl = BASE_URL + "?t=" + System.currentTimeMillis();
            webView.loadUrl(randomUrl);
        });
        
        // Stop Button
        btnStop = new Button(this);
        btnStop.setText("■ Stop");
        btnStop.setOnClickListener(v -> {
            webView.stopLoading();
            webView.loadUrl("about:blank");
            Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show();
        });
        
        buttonLayout.addView(btnStar);
        buttonLayout.addView(btnRefresh);
        buttonLayout.addView(btnStop);
        
        // WebView Setup
        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36");
        
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        
        // Add views
        layout.addView(buttonLayout);
        layout.addView(webView, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            1280
        ));
        
        setContentView(layout);
        
        // Load initial URL
        webView.loadUrl(BASE_URL);
    }
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
