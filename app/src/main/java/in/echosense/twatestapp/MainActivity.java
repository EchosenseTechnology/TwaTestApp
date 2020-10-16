package in.echosense.twatestapp;

import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsSession;
import androidx.browser.trusted.TrustedWebActivityIntentBuilder;

import saschpe.android.customtabs.CustomTabsHelper;
import saschpe.android.customtabs.WebViewFallback;

public class MainActivity extends AppCompatActivity {

    private TwaCustomTabsServiceConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            connection = new TwaCustomTabsServiceConnection(this);
            CustomTabsClient.connectAndInitialize(this, connection.getCustomTabsProviderPackage());
            CustomTabsClient.bindCustomTabsService(this, connection.getCustomTabsProviderPackage(), connection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        EditText editText = findViewById(R.id.editText);
        findViewById(R.id.button).setOnClickListener(view -> {
            String url = editText.getText().toString();
            DisplayInTWA(url);
        });

    }

    public void DisplayInTWA(String url) {
        if (url == null || url.isEmpty()) return;
        try {
            TwaCustomTabsServiceConnection serviceConnection = connection;
            CustomTabsClient client = serviceConnection.getCustomTabsClient();
            if (client != null) {
                TrustedWebActivityIntentBuilder builder = new TrustedWebActivityIntentBuilder(Uri.parse(url));
                CustomTabsSession session = client.newSession(null);
                builder.build(session).launchTrustedWebActivity(this);
            } else {
                DisplayInChromeTab(url);
                Toast.makeText(this, "Failed to display TWA.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            DisplayInChromeTab(url);
        }
    }

    public void DisplayInChromeTab(String url) {
        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                .addDefaultShareMenuItem()
                .setShowTitle(true)
                .build();

        CustomTabsHelper.addKeepAliveExtra(this, customTabsIntent.intent);

        CustomTabsHelper.openCustomTab(this, customTabsIntent,
                Uri.parse(url),
                new WebViewFallback());
    }

}