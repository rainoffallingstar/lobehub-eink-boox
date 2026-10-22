package dev.local.lobehubeink;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.util.Locale;

public final class MainActivity extends Activity {
    private static final String APP_URL = "https://app.lobehub.com/";
    private static final int FILE_REQUEST = 6001;
    private WebView web;
    private ValueCallback<Uri[]> fileCallback;
    private boolean einkEnabled = true;
    private Button toggle;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(bar, new LinearLayout.LayoutParams(-1, dp(48)));
        button(bar, "主页", v -> web.loadUrl(APP_URL));
        button(bar, "刷新", v -> web.reload());
        toggle = button(bar, "简化:开", v -> {
            einkEnabled = !einkEnabled;
            toggle.setText(einkEnabled ? "简化:开" : "简化:关");
            injectStyle();
        });
        button(bar, "浏览器", v -> external(Uri.parse(APP_URL)));
        web = new WebView(this);
        web.setBackgroundColor(Color.WHITE);
        root.addView(web, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setAllowFileAccess(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, false);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (!request.isForMainFrame()) return false;
                Uri uri = request.getUrl();
                if ("https".equalsIgnoreCase(uri.getScheme()) && allowed(uri.getHost())) return false;
                if ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                    external(uri);
                return true;
            }
            @Override public void onPageFinished(WebView view, String url) {
                Uri uri = Uri.parse(url);
                if ("https".equalsIgnoreCase(uri.getScheme()) && allowed(uri.getHost())) injectStyle();
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView w, ValueCallback<Uri[]> cb, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = cb;
                try {
                    startActivityForResult(params.createIntent(), FILE_REQUEST);
                    return true;
                } catch (ActivityNotFoundException e) {
                    fileCallback = null;
                    Toast.makeText(MainActivity.this, "没有文件选择器", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });
        web.setDownloadListener((url, ua, disposition, type, length) -> {
            Uri uri = Uri.parse(url);
            if ("https".equalsIgnoreCase(uri.getScheme())) external(uri);
        });
        if (state == null || web.restoreState(state) == null) web.loadUrl(APP_URL);
    }

    private boolean allowed(String host) {
        if (host == null) return false;
        String normalized = host.toLowerCase(Locale.ROOT);
        return normalized.equals("lobehub.com") || normalized.endsWith(".lobehub.com");
    }

    private Button button(LinearLayout row, String label, View.OnClickListener action) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(12);
        b.setTextColor(Color.BLACK);
        b.setAllCaps(false);
        b.setPadding(0, 0, 0, 0);
        b.setBackgroundColor(Color.WHITE);
        row.addView(b, new LinearLayout.LayoutParams(0, -1, 1));
        b.setOnClickListener(action);
        return b;
    }

    private void injectStyle() {
        String css = "*,*::before,*::after{animation:none!important;transition:none!important;" +
                "scroll-behavior:auto!important;text-shadow:none!important;}" +
                "html{scroll-behavior:auto!important;}" +
                "img,video,canvas{filter:grayscale(1)!important;}";
        String encoded = android.util.Base64.encodeToString(
                css.getBytes(java.nio.charset.StandardCharsets.UTF_8), android.util.Base64.NO_WRAP);
        String script = "(function(){var prior=document.getElementById('boox-eink-css');" +
                "if(prior)prior.remove();" +
                (einkEnabled ? "var style=document.createElement('style');" +
                "style.id='boox-eink-css';style.textContent=atob('" + encoded + "');" +
                "(document.head||document.documentElement).appendChild(style);" : "") +
                "})();";
        web.evaluateJavascript(script, null);
    }

    private void external(Uri uri) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE));
        } catch (ActivityNotFoundException e) {
            new AlertDialog.Builder(this).setMessage("没有可用浏览器").setPositiveButton("确定", null).show();
        }
    }
    private int dp(int d) { return Math.round(d * getResources().getDisplayMetrics().density); }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == FILE_REQUEST && fileCallback != null) {
            fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result, data));
            fileCallback = null;
        }
    }
    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && web != null) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_PAGE_DOWN) { web.pageDown(false); return true; }
            if (event.getKeyCode() == KeyEvent.KEYCODE_PAGE_UP) { web.pageUp(false); return true; }
        }
        return super.dispatchKeyEvent(event);
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        web.saveState(state);
        super.onSaveInstanceState(state);
    }
    @Override @SuppressWarnings("deprecation") public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }
    @Override protected void onDestroy() {
        if (fileCallback != null) { fileCallback.onReceiveValue(null); fileCallback = null; }
        ViewGroup parent = (ViewGroup) web.getParent();
        if (parent != null) parent.removeView(web);
        web.destroy();
        super.onDestroy();
    }
}
