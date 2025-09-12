package com.nunucore.corestudio;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.SslErrorHandler;
import android.webkit.SslError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    // Ganti URL ini kalau perlu
    private static final String HOME_URL = "https://corestudio.id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // pastikan activity_main.xml ada dan punya WebView dengan id webView

        webView = findViewById(R.id.webView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setSupportZoom(false);
        webSettings.setAllowFileAccess(true);

        // Allow mixed content (http resources on https pages) — gunakan jika situs punya mixed content
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Cache policy (default). Bisa diubah ke LOAD_NO_CACHE untuk selalu fresh
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setWebViewClient(new WebViewClient() {
            // Handle navigation (API 24+)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(request.getUrl().toString());
            }

            // For older API (<24) if needed, override deprecated method too:
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(url);
            }

            // Handle SSL errors: show dialog, let user decide
            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                // Build a confirmation dialog
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                String message = "SSL Certificate error.";
                switch (error.getPrimaryError()) {
                    case SslError.SSL_UNTRUSTED:
                        message = "The certificate authority is not trusted.";
                        break;
                    case SslError.SSL_EXPIRED:
                        message = "The certificate has expired.";
                        break;
                    case SslError.SSL_IDMISMATCH:
                        message = "The certificate Hostname mismatch.";
                        break;
                    case SslError.SSL_NOTYETVALID:
                        message = "The certificate is not yet valid.";
                        break;
                }
                message += " Do you want to continue anyway?";

                builder.setTitle("SSL Certificate error");
                builder.setMessage(message);
                builder.setPositiveButton("Continue", (DialogInterface dialog, int which) -> {
                    // USER CHOSE to proceed — insecure but sometimes needed for dev
                    handler.proceed();
                });
                builder.setNegativeButton("Cancel", (DialogInterface dialog, int which) -> {
                    handler.cancel();
                });
                final AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        // Load homepage
        webView.loadUrl(HOME_URL);
    }

    /**
     * Handle URLs that should open external apps (mailto:, tel:, whatsapp:, market:, intent:, etc.)
     * Return true if we've handled the URL externally (do not load in webview), false to let WebView load it.
     */
    private boolean handleUrl(String url) {
        if (url == null) return false;

        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();

        if (scheme == null) {
            // no scheme — let WebView handle it
            return false;
        }

        // Allow http(s) to be loaded inside WebView
        if (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) {
            return false; // load in WebView
        }

        // Mailto: open email apps
        if (scheme.equalsIgnoreCase("mailto")) {
            try {
                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No email app found.", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        // Tel: open dialer
        if (scheme.equalsIgnoreCase("tel")) {
            try {
                Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No dialer app found.", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        // sms:
        if (scheme.equalsIgnoreCase("sms")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.putExtra("address", uri.getSchemeSpecificPart());
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No SMS app found.", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        // whatsapp: (whatsapp://send?text=...) or custom schemes
        if (scheme.equalsIgnoreCase("whatsapp") || url.contains("wa.me") || url.contains("api.whatsapp.com")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // fallback: open wa.me link in browser if possible
                try {
                    Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browser);
                } catch (Exception ex) {
                    Toast.makeText(this, "WhatsApp not available.", Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        }

        // market:, intent:, other custom schemes -> try to open external app
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No application can handle this request.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // generic fallback try to open with browser
            try {
                Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browser);
            } catch (Exception ex) {
                Toast.makeText(this, "Cannot open link.", Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
