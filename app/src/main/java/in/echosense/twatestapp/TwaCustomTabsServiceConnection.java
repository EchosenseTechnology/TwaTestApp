package in.echosense.twatestapp;

import android.content.ComponentName;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsServiceConnection;

import java.util.Arrays;

public class TwaCustomTabsServiceConnection extends CustomTabsServiceConnection {
    private Context mContext;
    private String mCustomTabsProviderPackage;
    private CustomTabsClient customTabsClient;

    private static final String DEV_PACKAGE = "com.chrome.dev";
    private static final String BETA_PACKAGE = "com.chrome.beta";
    private static final String STABLE_PACKAGE = "com.android.chrome";

    public TwaCustomTabsServiceConnection(Context context) {
        this.mContext = context;
        mCustomTabsProviderPackage = CustomTabsClient.getPackageName(context, Arrays.asList(STABLE_PACKAGE, BETA_PACKAGE, DEV_PACKAGE), true);
    }

    public String getCustomTabsProviderPackage() {
        return mCustomTabsProviderPackage;
    }

    public CustomTabsClient getCustomTabsClient() {
        return customTabsClient;
    }

    @Override
    public void onBindingDied(ComponentName name) {
        try {
            CustomTabsClient.bindCustomTabsService(mContext, mCustomTabsProviderPackage, this);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCustomTabsServiceConnected(@NonNull ComponentName componentName, @NonNull CustomTabsClient client) {
        try {
            Toast.makeText(mContext, "Connection established", Toast.LENGTH_SHORT).show();
            customTabsClient = client;
            client.warmup(0);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        customTabsClient = null;
    }
}
