
package com.oddsreader.app;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import java.net.URLEncoder;
public class MainActivity extends Activity {
    private WebView webView;
    @SuppressLint({"SetJavaScriptEnabled"})
    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        webView=new WebView(this); setContentView(webView);
        WebSettings s=webView.getSettings();
        s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true); s.setAllowContentAccess(true);
        s.setAllowFileAccessFromFileURLs(true); s.setAllowUniversalAccessFromFileURLs(true);
        webView.setWebViewClient(new WebViewClient());
        Intent intent=getIntent();
        String shared=intent!=null?intent.getStringExtra(Intent.EXTRA_TEXT):null;
        String url="file:///android_asset/public/index.html";
        if(shared!=null && !shared.isEmpty()){
            try{ url+="?text="+URLEncoder.encode(shared,"UTF-8"); }catch(Exception e){}
        }
        webView.loadUrl(url);
    }
    @Override public void onBackPressed(){ if(webView.canGoBack()) webView.goBack(); else super.onBackPressed(); }
}
