package com.example.webviewdemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.webkit.WebChromeClient.FileChooserParams;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "WebViewDemo";
    private static final int FILE_CHOOSER_REQUEST_CODE = 1;
    private WebView webView;
    private LinearLayout formLayout;
    private TextInputEditText accessTokenInput;
    private TextInputEditText dataRequestIdInput;
    private Button loadWidgetButton;
    private ValueCallback<Uri[]> filePathCallback;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        webView = findViewById(R.id.webview);
        formLayout = findViewById(R.id.formLayout);
        accessTokenInput = findViewById(R.id.accessTokenInput);
        dataRequestIdInput = findViewById(R.id.dataRequestIdInput);
        loadWidgetButton = findViewById(R.id.loadWidgetButton);

        // Set up button click listener
        loadWidgetButton.setOnClickListener(v -> loadWidgetWithConfig());

        // Configure WebView settings
        setupWebView();
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);
        webSettings.setDefaultTextEncodingName("utf-8");

        // Add JavaScript interface for communication
        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        // Set WebViewClient to handle page loads
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "Page loaded: " + url);
                
                // Inject the configuration after page loads
                injectConfiguration();
            }
        });

        // Set WebChromeClient to handle JavaScript alerts and console logs
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(android.webkit.ConsoleMessage consoleMessage) {
                Log.d(TAG, "Console: " + consoleMessage.message() + " -- From line " + 
                        consoleMessage.lineNumber() + " of " + consoleMessage.sourceId());
                return true;
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> callback, FileChooserParams fileChooserParams) {
                if (filePathCallback != null) {
                    filePathCallback.onReceiveValue(null);
                }
                filePathCallback = callback;
                openFileChooser();
                return true;
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "File Chooser"), FILE_CHOOSER_REQUEST_CODE);
    }

    private void loadWidgetWithConfig() {
        String accessToken = accessTokenInput.getText().toString().trim();
        String dataRequestId = dataRequestIdInput.getText().toString().trim();

        // Validate inputs
        if (accessToken.isEmpty() || accessToken.equals("YOUR_ACCESS_TOKEN_HERE")) {
            Toast.makeText(this, "Please enter a valid access token", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dataRequestId.isEmpty()) {
            Toast.makeText(this, "Please enter a data request ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hide form and show WebView
        formLayout.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);

        // Load the HTML file
        loadLocalHtml();
    }

    private void loadLocalHtml() {
        try {
            webView.loadUrl("file:///android_asset/m1_widget.html");
        } catch (Exception e) {
            Log.e(TAG, "Error loading HTML file", e);
            Toast.makeText(this, "Error loading HTML file", Toast.LENGTH_SHORT).show();
        }
    }

    private void injectConfiguration() {
        String accessToken = accessTokenInput.getText().toString().trim();
        String dataRequestId = dataRequestIdInput.getText().toString().trim();

        // Create the configuration object
        String configScript = String.format(
            "javascript:(function() {" +
            "   var config = {" +
            "       access_key: '%s'," +
            "       host_name: 'api-stg.measureone.com'," +
            "       datarequest_id: '%s'," +
            "       branding: {" +
            "           styles: {" +
            "               primary_dark: '#186793'," +
            "               primary_light: '#2e9ccb'," +
            "               secondary_color: '#ffffff'," +
            "               min_height: '700px'" +
            "           }" +
            "       }," +
            "       options: {" +
            "           'display_profile': false" +
            "       }" +
            "   };" +
            "   " +
            "   var m1_widget = document.querySelector('m1-link');" +
            "   if (m1_widget) {" +
            "       m1_widget.setAttribute('config', JSON.stringify(config));" +
            "       console.log('Configuration injected:', config);" +
            "       Android.onConfigInjected(JSON.stringify(config));" +
            "   } else {" +
            "       console.error('m1-link element not found');" +
            "       Android.onConfigError('m1-link element not found');" +
            "   }" +
            "})();",
            accessToken, dataRequestId
        );

        webView.evaluateJavascript(configScript, null);
    }

    // JavaScript interface for communication between WebView and Android
    public class WebAppInterface {
        @JavascriptInterface
        public void onConfigInjected(String config) {
            Log.d(TAG, "Configuration injected: " + config);
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Widget configuration loaded successfully", Toast.LENGTH_SHORT).show();
            });
        }

        @JavascriptInterface
        public void onConfigError(String error) {
            Log.e(TAG, "Configuration error: " + error);
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
            });
        }

        @JavascriptInterface
        public void onMessageReceived(String message) {
            Log.d(TAG, "Message from WebView: " + message);
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "WebView Message: " + message, Toast.LENGTH_SHORT).show();
            });
        }

        @JavascriptInterface
        public void onDatasourceConnected(String eventData) {
            Log.d(TAG, "Datasource connected: " + eventData);
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "M1 Widget connected successfully!", Toast.LENGTH_LONG).show();
            });
        }

        @JavascriptInterface
        public void showToast(String message) {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.getVisibility() == View.VISIBLE && webView.canGoBack()) {
            webView.goBack();
        } else if (webView.getVisibility() == View.VISIBLE) {
            // Show form again
            webView.setVisibility(View.GONE);
            formLayout.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (filePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }

            Uri[] results = null;
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }

            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
} 