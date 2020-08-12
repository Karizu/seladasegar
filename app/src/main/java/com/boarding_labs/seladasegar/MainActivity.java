package com.boarding_labs.seladasegar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Geolocation permission request code
    private static final int RP_ACCESS_LOCATION = 1001;
    private static final int FINE_LOCATION_PERMISSION_REQUEST = 1;

    // global variables for the origin for permission and interface used by the your application to set the Geolocation permission state for an origin
    private String mGeolocationOrigin;
    private GeolocationPermissions.Callback mGeolocationCallback;
    private Context mContext = MainActivity.this;

    private static final int REQUEST = 112;

    WebView wb;
    ProgressBar progressBar;
    SwipeRefreshLayout swipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wb = findViewById(R.id.webView1);
        swipe = findViewById(R.id.swipe);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                wb.reload();
            }
        });
        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(100);
        progressBar.setProgress(1);

        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};
            if (!hasPermissions(mContext, PERMISSIONS)) {
                ActivityCompat.requestPermissions((Activity) mContext, PERMISSIONS, REQUEST);
            } else {
                //call get location here
            }
        } else {
            //call get location here
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    FINE_LOCATION_PERMISSION_REQUEST);
        }

        loadWeb();
//        wb.setWebViewClient(new AppWebViewClients(progressBar));
//        wb.setWebChromeClient(new WebChromeClient() {
//            public void onProgressChanged(WebView view, int progress) {
//                progressBar.setProgress(progress);
//            }
//        });
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RP_ACCESS_LOCATION:
                boolean allow = false;
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // user has allowed these permissions
                    allow = true;
                }
                if (mGeolocationCallback != null) {
                    // use stored callback and origin for allowing Geolocation permission for WebView
                    mGeolocationCallback.invoke(mGeolocationOrigin, allow, false);
                }
                break;
            case REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //call get location here
                } else {
                    Toast.makeText(mContext, "The app was not allowed to access your location", Toast.LENGTH_LONG).show();
                }
                break;
            case FINE_LOCATION_PERMISSION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("TAG", "onPermissionMaps");
                }
            }
        }
    }

    @SuppressLint({"ObsoleteSdkInt", "SetJavaScriptEnabled"})
    private void loadWeb() {
        wb.getSettings().setDatabaseEnabled(true);
        wb.getSettings().setGeolocationEnabled(true);
        wb.getSettings().setDomStorageEnabled(true);
        wb.getSettings().setLoadWithOverviewMode(true);
        wb.getSettings().setUseWideViewPort(true);
        wb.getSettings().setAppCacheEnabled(true);
//        wb.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        wb.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        wb.setWebViewClient(new WebViewClient());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // chromium, enable hardware acceleration
            wb.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            wb.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        wb.getSettings().setJavaScriptEnabled(true);
        wb.getSettings().setPluginState(WebSettings.PluginState.ON);

        wb.getSettings().setUseWideViewPort(true);
        wb.getSettings().setLoadWithOverviewMode(true);
        wb.getSettings().setGeolocationDatabasePath(this.getFilesDir().getPath());

        wb.setWebChromeClient(new GeoWebChromeClient());
        wb.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }


            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                wb.loadUrl("file:///android_asset/error.html");
            }

            public void onLoadResource(WebView view, String url) { //Doesn't work
                //swipe.setRefreshing(true);
            }

            public void onPageFinished(WebView view, String url) {
                //Hide the SwipeReefreshLayout
                progressBar.setVisibility(View.GONE);
                swipe.setRefreshing(false);
            }
        });

        wb.loadUrl("http://www.seladasegar.com");
        swipe.setRefreshing(true);
    }

    public class GeoWebChromeClient extends android.webkit.WebChromeClient {
        @Override
        public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
//            final String permission = Manifest.permission.ACCESS_FINE_LOCATION;
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
//                    ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_GRANTED) {
//                // that is you already implement, but it works only
//                // we're on SDK < 23 OR user has ALREADY granted permission
//                callback.invoke(origin, true, false);
//            } else {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {
//                    // user has denied this permission before and selected [/] DON'T ASK ME AGAIN
//                    // TODO Best Practice: show an AlertDialog explaining why the user could allow this permission, then ask again
//                } else {
//                    // store
//                    mGeolocationOrigin = origin;
//                    mGeolocationCallback = callback;
//                    // ask the user for permissions
//                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, RP_ACCESS_LOCATION);
//                }
//            }
            Log.i("TAG", "onGeolocationPermissionsShowPrompt()");

            final boolean remember = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Locations Permission");
            builder.setMessage("Would like to use your Current Location ")
                    .setCancelable(true).setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // origin, allow, remember
                    callback.invoke(origin, true, remember);
                }
            }).setNegativeButton("Don't Allow", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // origin, allow, remember
                    callback.invoke(origin, false, remember);
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }


    public class AppWebViewClients extends WebViewClient {
        private ProgressBar progressBar;

        public AppWebViewClients(ProgressBar progressBar) {
            this.progressBar = progressBar;
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            swipe.setRefreshing(false);
        }
    }

    @Override
    public void onBackPressed() {

        if (wb.canGoBack()) {
            wb.goBack();
        } else {
            finish();
        }
    }
}
