package no.handleliste.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.WindowInsets;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;
import android.widget.LinearLayout;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.webkit.WebViewAssetLoader;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Viser Handleliste (HTML/JS i assets) i en WebView.
 * Alt – siden, skriftene og Firebase-biblioteket – ligger inne i appen, og data lagres lokalt på telefonen.
 */
public class MainActivity extends Activity {

    private static final String HOST = "appassets.androidplatform.net";
    private static final String START_URL = "https://" + HOST + "/assets/index.html";
    private static final int REQ_VOICE = 1001;

    private WebView web;
    private View topBar;
    private View bottomBar;
    private int statusColor;
    private int navColor;
    private WebViewAssetLoader assetLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assetLoader = new WebViewAssetLoader.Builder()
                .setDomain(HOST)
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        web = new WebView(this);
        web.setBackgroundColor(getColor(R.color.bg));

        // Kant-til-kant (påkrevd fra Android 15): to fargede felt over og under siden fyller plassen bak system-feltene.
        statusColor = getColor(R.color.bar);
        navColor = getColor(R.color.surface);
        topBar = new View(this);
        bottomBar = new View(this);
        topBar.setBackgroundColor(statusColor);
        bottomBar.setBackgroundColor(navColor);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.addView(topBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0));
        root.addView(web, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(bottomBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0));
        setContentView(root);

        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
            root.setOnApplyWindowInsetsListener((v, insets) -> {
                android.graphics.Insets bars = insets.getInsets(
                        WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                android.graphics.Insets ime = insets.getInsets(WindowInsets.Type.ime());
                root.setPadding(bars.left, 0, bars.right, 0);
                setHeight(topBar, bars.top);
                setHeight(bottomBar, Math.max(bars.bottom, ime.bottom));
                return WindowInsets.CONSUMED;
            });
            root.requestApplyInsets();
        }

        if (Build.VERSION.SDK_INT >= 33) {
            // Tilbake-gest (fungerer med «predictive back» når appen målretter Android 16)
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT, (OnBackInvokedCallback) this::handleBack);
        }

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);          // localStorage = lagring på telefonen
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        if (Build.VERSION.SDK_INT >= 29 && Build.VERSION.SDK_INT < 33) {
            s.setForceDark(WebSettings.FORCE_DARK_OFF);   // siden styrer mørk modus selv
        }

        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (HOST.equals(uri.getHost())) {
                    return false;
                }
                openExternal(uri);   // mailto:, nettlenker osv. åpnes i riktig app
                return true;
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(message)
                        .setPositiveButton("OK", (d, w) -> result.confirm())
                        .setOnCancelListener(d -> result.confirm())
                        .show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(message)
                        .setPositiveButton("OK", (d, w) -> result.confirm())
                        .setNegativeButton("Avbryt", (d, w) -> result.cancel())
                        .setOnCancelListener(d -> result.cancel())
                        .show();
                return true;
            }
        });

        web.addJavascriptInterface(new Bridge(), "AndroidBridge");
        web.loadUrl(startUrlFrom(getIntent()));
    }

    /** Lenker som https://.../Handleliste/?liste=ID åpner den delte listen i appen. */
    private String startUrlFrom(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            String id = intent.getData().getQueryParameter("liste");
            if (id != null && id.matches("[A-Za-z0-9_-]{4,64}")) {
                return START_URL + "?liste=" + id;
            }
        }
        return START_URL;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String url = startUrlFrom(intent);
        if (!START_URL.equals(url)) {
            web.loadUrl(url);
        }
    }

    private void openExternal(Uri uri) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Fant ingen app som kan åpne dette", Toast.LENGTH_SHORT).show();
        }
    }

    private static void setHeight(View v, int h) {
        android.view.ViewGroup.LayoutParams lp = v.getLayoutParams();
        if (lp.height != h) {
            lp.height = h;
            v.setLayoutParams(lp);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        // Android 12 og eldre; nyere versjoner bruker OnBackInvokedCallback over
        handleBack();
    }

    private void handleBack() {
        // La siden lukke vinduer/undersider først. Hvis den ikke gjør det, lukkes appen.
        web.evaluateJavascript("(window.__hlBack ? window.__hlBack() : false)", value -> {
            if (!"true".equals(value)) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_VOICE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                web.evaluateJavascript(
                        "window.__onVoice && window.__onVoice(" + JSONObject.quote(results.get(0)) + ")", null);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        web.onResume();
    }

    @Override
    protected void onPause() {
        web.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        web.destroy();
        super.onDestroy();
    }

    /** Funksjoner siden kan kalle via window.AndroidBridge. */
    public class Bridge {

        @JavascriptInterface
        public void setKeepAwake(final boolean on) {
            runOnUiThread(() -> {
                if (on) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            });
        }

        @JavascriptInterface
        public void setBars(final String statusHex, final String navHex, final boolean darkNav) {
            runOnUiThread(() -> {
                try {
                    statusColor = Color.parseColor(statusHex);
                    navColor = Color.parseColor(navHex);
                    topBar.setBackgroundColor(statusColor);
                    bottomBar.setBackgroundColor(navColor);
                    if (Build.VERSION.SDK_INT < 35) {
                        getWindow().setStatusBarColor(statusColor);
                        getWindow().setNavigationBarColor(navColor);
                    }
                    if (Build.VERSION.SDK_INT >= 30) {
                        android.view.WindowInsetsController c = getWindow().getInsetsController();
                        if (c != null) {
                            int m = android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                            c.setSystemBarsAppearance(darkNav ? 0 : m, m);
                        }
                    } else if (Build.VERSION.SDK_INT >= 26) {
                        View decor = getWindow().getDecorView();
                        int flags = decor.getSystemUiVisibility();
                        if (darkNav) {
                            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                        } else {
                            flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                        }
                        decor.setSystemUiVisibility(flags);
                    }
                } catch (IllegalArgumentException ignored) {
                    // ugyldig fargekode, ignorer
                }
            });
        }

        @JavascriptInterface
        public void share(final String text) {
            runOnUiThread(() -> {
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("text/plain");
                send.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(send, "Del handlelisten"));
            });
        }

        @JavascriptInterface
        public void copy(final String text) {
            runOnUiThread(() -> {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm != null) {
                    cm.setPrimaryClip(ClipData.newPlainText("Handleliste", text));
                }
            });
        }

        @JavascriptInterface
        public void startVoice() {
            runOnUiThread(() -> {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "nb-NO");
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Si varene dine");
                try {
                    startActivityForResult(i, REQ_VOICE);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(MainActivity.this,
                            "Talegjenkjenning er ikke tilgjengelig på denne telefonen", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
