package hm.orz.chaos114.android.samplehybridandroid;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // set javascript interface
        webView.addJavascriptInterface(new WebAppInterface(this), "MyApp");

        webView.setWebViewClient(new MyWebViewClient());

        // set cookie
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setCookie("https://noboru-i.github.io/", "foo=bar; max-age=3600");

        // set user agent
        String userAgent = webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(userAgent + " Sample-Android-App/v1.0.0");

        webView.loadUrl("https://noboru-i.github.io/sample-html/webview.html");
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    private static class WebAppInterface {
        private Context mContext;

        private WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void sendMessage(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "shouldOverrideUrlLoading with String.");
            return handleLoading(url);
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.d(TAG, "shouldOverrideUrlLoading with WebResourceRequest.");
            String url = request.getUrl().toString();
            return handleLoading(url);
        }

        private boolean handleLoading(String url) {
            printCookie("https://noboru-i.github.io/");

            if (url.equals("sample://update_cookie")) {
                return true;
            }
            if (Uri.parse(url).getHost().equals("noboru-i.github.io")) {
                // in target domain, load normally.
                return false;
            }

            // in other domain, execute by other app.
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            // ignore ssl error for using Charles.
            handler.proceed();
        }
    }

    private void printCookie(String url) {
        String cookieString = CookieManager.getInstance().getCookie(url);
        if (cookieString == null) {
            Log.d(TAG, "cookie is empty.");
            return;
        }
        Log.d(TAG, "cookie: " + cookieString);
    }
}
