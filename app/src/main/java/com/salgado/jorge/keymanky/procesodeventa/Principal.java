package com.salgado.jorge.keymanky.procesodeventa;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.Retrofit.Builder;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.security.AccessController.getContext;

@SuppressLint({"SetJavaScriptEnabled"})
public class Principal extends AppCompatActivity {
    private static final String DEVICE_ADDRESS_END = ")";
    private static final String DEVICE_ADDRESS_START = " (";
    public static final int RC_STORAGE = 55;
    private static final int REQUEST_CODE_ACTION_PICK = 2;
    private static final int REQUEST_CODE_BLUETOOTH = 1;
    JavaScriptInterface Jinterface;
    private final String TAG_REQUEST = "MY_TAG";
    private ArrayAdapter<CharSequence> arrayAdapter;
    private final ArrayList<CharSequence> bondedDevices = new ArrayList();
    public static final String KEY_CONNECTIONS = "KEY_CONNECTIONS";
    private int brightness = 50;
    public WebView browser;
    private int compress = 0;
    private BroadcastReceiver deliveredStatusReceiver;
    private String logicalName;
    private BroadcastReceiver sentStatusReceiver;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestPermissionLocation();
        setContentView((int) R.layout.activity_principal);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (VERSION.SDK_INT >= 19 && (getApplicationInfo().flags & 2) != 0) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        this.browser = (WebView) findViewById(R.id.browser);
        WebSettings webSettings = this.browser.getSettings();
        webSettings.setJavaScriptEnabled(true);
        this.browser.setWebViewClient(new WebViewClient());
        this.Jinterface = new JavaScriptInterface(this, this.browser);
        this.browser.addJavascriptInterface(this.Jinterface, "javascript_obj");
        this.browser.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(Principal.this.browser, url);
                Principal.this.injectFunction();
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                view.loadUrl("/sdcard/htdocs/rutas/error.php");
                Principal.this.startActivity(Principal.this.getPackageManager().getLaunchIntentForPackage("ru.kslabs.ksweb"));
                view.loadUrl("http://localhost:8080/rutas/index.php");
                Principal.this.browser.loadUrl("http://www.websitehere.php");
                view.loadUrl("/sdcard/htdocs/rutas/error.php");
            }
        });
        webSettings.setDomStorageEnabled(true);
        this.browser.setWebChromeClient(new WebChromeClient());
        this.browser.loadUrl("http://localhost:8080/rutas/index.php");
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);
        this.browser.setInitialScale(105);
    }



    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.base) {
            startActivity(getPackageManager().getLaunchIntentForPackage("ru.kslabs.ksweb"));
            return true;
        } else if (id == R.id.menu) {
            this.browser.loadUrl("http://localhost:8080/rutas/menu.php");
            return true;
        } else if (id == R.id.ventas) {
            this.browser.loadUrl("http://localhost:8080/rutas/3.2.php");
            return true;
        } else if (id == R.id.conecta_gps) {
            startActivity(getPackageManager().getLaunchIntentForPackage("fr.herverenault.selfhostedgpstracker"));
            return true;
        } else if (id == R.id.consulta_gps) {
            this.browser.loadUrl("http://localhost:8080/gps/index.php");
            return true;
        } else if (id == R.id.imprimir) {
            startActivity(getPackageManager().getLaunchIntentForPackage("com.bixolon.webprint"));
            return true;
        } else if (id == R.id.blue) {
            startActivityForResult(new Intent("android.settings.BLUETOOTH_SETTINGS"), 1);
            return true;
        } else if (id == R.id.conectar) {
            Intent i = new Intent(this, Imprimir.class);
            i.putExtra("url", this.browser.getUrl().toString());
            startActivity(i);
            return true;
        } else if (id == R.id.actualizar) {
            showToast("");
            this.browser.setDownloadListener(new DownloadListener() {
                public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                    final Request request = new Request(Uri.parse("http://187.176.24.218:88/gps.rar"));
                    request.allowScanningByMediaScanner();
                    request.setMimeType(mimetype);
                    request.addRequestHeader("cookie", CookieManager.getInstance().getCookie("http://187.176.24.218:888/gps.rar"));
                    request.addRequestHeader("User-Agent", userAgent);
                    Principal.this.showToast("");
                    request.setDescription("Downloading file...");
                    request.setTitle(URLUtil.guessFileName("http://187.176.24.218:888/gps.rar", contentDisposition, mimetype));
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(1);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName("http://187.176.24.218:88/gps.rar", contentDisposition, mimetype));
                    final DownloadManager dm = (DownloadManager) Principal.this.getSystemService("download");
                    new Thread("Browser download") {
                        public void run() {
                            dm.enqueue(request);
                        }
                    }.start();
                    Principal.this.showToast("");
                    if (!JavaScriptInterface.longitudeGPS.toString().isEmpty() || !JavaScriptInterface.latitudeGPS.toString().isEmpty() || !JavaScriptInterface.usuario3.isEmpty()) {
                        JSONObject json = new JSONObject();
                        try {
                            json.put("username", "DCP001");
                            json.put("password", "02b2de20e8981444cb9a57a6621acf0bcd95693daab029549b0b0c2ab8248a2c");
                            json.put("useragent", "Android tracker v0.8");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        ((ManagerService) new Builder().baseUrl("https://www.apps-sellcom-dev.com/LALOMA_CENTRAL-web/").addConverterFactory(GsonConverterFactory.create()).build().create(ManagerService.class)).setConfirmar(RequestBody.create(
                                MediaType.parse("application/json; charset=utf-8"), json.toString())).enqueue(new Callback<ResponseBody>() {
                            /* JADX WARNING: inconsistent code. */
                            /* Code decompiled incorrectly, please refer to instructions dump. */
                            public void onResponse(retrofit2.Call<okhttp3.ResponseBody> r19, retrofit2.Response<okhttp3.ResponseBody> r20) {

                                throw new UnsupportedOperationException("Method not decompiled: com.salgado.jorge.keymanky.procesodeventa.Principal.2.2.onResponse(retrofit2.Call, retrofit2.Response):void");
                            }

                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                            }
                        });
                    }
                }
            });
            showToast("");

            String connectionsJSONString = getPreferences(MODE_PRIVATE).getString(KEY_CONNECTIONS, null);
            Type type = new TypeToken< ArrayList < Ruta >>() {}.getType();
            ArrayList < Ruta > rutasObject = new Gson().fromJson(connectionsJSONString, type);

            for (int i=0; i< rutasObject.size(); i++) {
                Ruta ruta = VariablesGlobales.rutas.get(i);
                JSONObject json = new JSONObject();
                try {
                    json.put("clientID", ruta.clienteId);
                    json.put("route", ruta.route);
                    json.put("onDate", ruta.onDate);
                    json.put("endDate", ruta.endDate);
                    json.put("checkinLocation", ruta.checkinLocation);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Retrofit retrofit = new Retrofit.Builder().baseUrl("https://www.apps-sellcom-dev.com/LALOMA_CENTRAL-web/").addConverterFactory(GsonConverterFactory.create()).build();
                ManagerService managerService = retrofit.create(ManagerService.class);
                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());
                managerService.setInsertar("e1c35bf0594ac92177406e4ef28d46f671a00fafb7347b880a7a42450e4bbad0", "3122d44f-3900-45be-9709-0d4d5a05aad2", requestBody).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
            }
            return true;
        } else {
           /* if (id == R.id.lectorCodigo) {
                startActivity(new Intent(this, LectorDeBarras.class));
            }*/
            return super.onOptionsItemSelected(item);
        }
    }

    private void showToast(String msg) {
    }

    private void injectFunction() {
        this.browser.loadUrl("javascript: window.androidObj.notificationA = function(identificador,identificador2) {  javascript_obj.ejecutarGPS(identificador,identificador2) }");
    }

    public void onDestroy() {
        super.onDestroy();
        this.browser.removeJavascriptInterface("javascript_obj");
    }
}