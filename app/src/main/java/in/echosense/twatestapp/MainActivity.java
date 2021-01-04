package in.echosense.twatestapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsSession;
import androidx.browser.trusted.TrustedWebActivityIntentBuilder;

import java.util.ArrayList;
import java.util.List;

import static androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION;

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
            DisplayInTWA(MainActivity.this, url);
        });

    }

    public void DisplayInTWA(Context context, String url) {
        if (url == null || url.isEmpty()) return;
        try {
            TwaCustomTabsServiceConnection serviceConnection = connection;
            CustomTabsClient client = serviceConnection.getCustomTabsClient();
            if (client != null) {
                TrustedWebActivityIntentBuilder builder = new TrustedWebActivityIntentBuilder(Uri.parse(url));
                CustomTabsSession session = client.newSession(null);
                builder.build(session).launchTrustedWebActivity(this);
            } else {
                /**
                 * Use either DisplayInCustomTab or DisplayInExternalBrowser
                 */
                DisplayInCustomTab(context, url);
            }
        } catch (Exception e) {
            /**
             * Use either DisplayInCustomTab or DisplayInExternalBrowser
             */
            DisplayInCustomTab(context, url);
        }
    }

    /**
     * Description: This method can be used if the url is to be launched in external browser if TWA is not present.
     *
     * @param url string url to be open in External browser
     */
    public void DisplayInExternalBrowser(String url) {
        if (url == null || url.isEmpty()) return;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse(url));
        List<String> browserList = getInstalledBrowserPackageName(this);
        if (browserList != null && !browserList.isEmpty()) {
            intent.setPackage(browserList.get(0));
        }
        startActivity(intent);
    }

    private List<String> getInstalledBrowserPackageName(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://www.google.com"));
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> browserList;

            browserList = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL);
            if (browserList.size() <= 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    browserList = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL);
                } else
                    browserList = pm.queryIntentActivities(intent, 0);
            }

            List<String> browserPackageNameList = new ArrayList<>();
            if (browserList != null && browserList.size() > 0) {
                for (ResolveInfo packageInfo : browserList) {
                    browserPackageNameList.add(packageInfo.activityInfo.packageName);
                }
            }
            return browserPackageNameList;
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return null;
    }

    /**
     * Description: This method is used to launch the url in custom tab if TWA is unavailable. All the failure case are required to handled.
     *
     * @param url string url to be open displayed in custom tab
     */
    public void DisplayInCustomTab(Context context, String url) {
        if (url == null || url.isEmpty())
            return;
        List<String> packageList = getCustomTabsPackages(context);
        String packageName;
        if (packageList == null || packageList.isEmpty()) {
            DisplayInWebView(url);
        } else {
            try {
                packageName = packageList.get(0);
                CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                        .enableUrlBarHiding()
                        .setShowTitle(false)
                        .addDefaultShareMenuItem()
                        .build();
                customTabsIntent.intent.setPackage(packageName);
                customTabsIntent.launchUrl(context, Uri.parse(url));
            } catch (Exception e) {
                DisplayInWebView(url);
            }
        }
    }

    /**
     * Returns a list of packages that support Custom Tabs.
     */
    public static ArrayList<String> getCustomTabsPackages(Context context) {
        PackageManager pm = context.getPackageManager();
        // Get default VIEW intent handler.
        Intent activityIntent = new Intent()
                .setAction(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setData(Uri.fromParts("http", "", null));

        // Get all apps that can handle VIEW intents.
        List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, 0);
        ArrayList<String> packagesSupportingCustomTabs = new ArrayList<>();
        for (ResolveInfo info : resolvedActivityList) {
            Intent serviceIntent = new Intent();
            serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
            serviceIntent.setPackage(info.activityInfo.packageName);
            // Check if this package also resolves the Custom Tabs service.
            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info.activityInfo.packageName);
            }
        }
        return packagesSupportingCustomTabs;
    }

    private void DisplayInWebView(String url) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_URL, url);
        startActivity(intent);
    }

}