package com.oddsreader.app;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;
    private final int FILE_CHOOSER = 1001;

    public class AndroidBridge {
        @JavascriptInterface
        public String getClipboardText() {
            try {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm.hasPrimaryClip() && cm.getPrimaryClip().getItemCount()>0) {
                    return cm.getPrimaryClip().getItemAt(0).coerceToText(MainActivity.this).toString();
                }
            } catch (Exception e) {}
            return "";
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        webView=new WebView(this);
        setContentView(webView);
        WebSettings s=webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setAllowFileAccessFromFileURLs(true);
        s.setAllowUniversalAccessFromFileURLs(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        webView.addJavascriptInterface(new AndroidBridge(), "Android");
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (filePathCallback!= null) filePathCallback.onReceiveValue(null);
                filePathCallback = callback;
                try {
                    startActivityForResult(params.createIntent(), FILE_CHOOSER);
                } catch (Exception e) {
                    filePathCallback = null;
                    return false;
                }
                return true;
            }
        });
        // The full app. (index.html was a cut-down copy whose tabs did nothing.)
        String url = APP_URL;
        String shared = sharedText(getIntent());
        if (shared != null) url += "?share=" + Uri.encode(shared);
        webView.loadUrl(url);
    }

    private static final String APP_URL = "file:///android_asset/public/Odds-Reader-TEST-12.html";

    /** Text sent to the app with Android's Share menu, or null. */
    private static String sharedText(Intent intent) {
        if (intent == null || !Intent.ACTION_SEND.equals(intent.getAction())) return null;
        CharSequence t = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
        return (t == null || t.length() == 0) ? null : t.toString();
    }

    // App already open and the user shares more text into it
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String shared = sharedText(intent);
        if (shared != null && webView != null) {
            webView.evaluateJavascript(
                "window.OddsReaderShare && window.OddsReaderShare(" + JSONObject.quote(shared) + ")", null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CHOOSER) {
            if (filePathCallback == null) return;
            Uri[] results = null;
            if (resultCode == RESULT_OK && data!= null) {
                String ds = data.getDataString();
                if (ds!= null) results = new Uri[]{Uri.parse(ds)};
            }
            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
        } else super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed(){
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}

