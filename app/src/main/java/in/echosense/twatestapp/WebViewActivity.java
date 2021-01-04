
package in.echosense.twatestapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

public class WebViewActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "url";
    private WebView mWebView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        mWebView = findViewById(R.id.web_view);

        String url = this.getIntent().getStringExtra(EXTRA_URL);
        if (mWebView.getSettings() != null) mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(url);
    }
}