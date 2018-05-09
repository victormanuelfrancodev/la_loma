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
import java.util.ArrayList;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
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
                    final Request request = new Request(Uri.parse("/gps.rar"));
                    request.allowScanningByMediaScanner();
                    request.setMimeType(mimetype);
                    request.addRequestHeader("cookie", CookieManager.getInstance().getCookie("http://localhost:8080/gps.rar"));
                    request.addRequestHeader("User-Agent", userAgent);
                    Principal.this.showToast("");
                    request.setDescription("Downloading file...");
                    request.setTitle(URLUtil.guessFileName("http://localhost:8080/gps.rar", contentDisposition, mimetype));
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(1);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName("http://localhost:8080/gps.rar", contentDisposition, mimetype));
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
                        ((ManagerService) new Builder().baseUrl("https://www.apps-sellcom-dev.com/LALOMA_CENTRAL-web/").addConverterFactory(GsonConverterFactory.create()).build().create(ManagerService.class)).setConfirmar(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())).enqueue(new Callback<ResponseBody>() {
                            /* JADX WARNING: inconsistent code. */
                            /* Code decompiled incorrectly, please refer to instructions dump. */
                            public void onResponse(retrofit2.Call<okhttp3.ResponseBody> r19, retrofit2.Response<okhttp3.ResponseBody> r20) {
                                /*
                                r18 = this;
                                r10 = new org.json.JSONObject;	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r16 = r20.body();	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r16 = (okhttp3.ResponseBody) r16;	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r16 = r16.string();	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r0 = r16;
                                r10.<init>(r0);	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r16 = "respuesta";
                                r17 = "apiKey";
                                r0 = r17;
                                r17 = r10.getString(r0);	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                android.util.Log.d(r16, r17);	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r16 = "respuesta";
                                r17 = "authToken";
                                r0 = r17;
                                r17 = r10.getString(r0);	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                android.util.Log.d(r16, r17);	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r14 = new android.text.format.Time;	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r16 = android.text.format.Time.getCurrentTimezone();	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r0 = r16;
                                r14.<init>(r0);	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r14.setToNow();	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r16 = "%Y/%m/%d";
                                r0 = r16;
                                r5 = r14.format(r0);	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r4 = new java.util.Date;	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r16 = java.lang.System.currentTimeMillis();	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r0 = r16;
                                r4.<init>(r0);	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r6 = new java.text.SimpleDateFormat;	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r16 = "hh:mm aa";
                                r17 = java.util.Locale.ENGLISH;	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r0 = r16;
                                r1 = r17;
                                r6.<init>(r0, r1);	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r15 = r6.format(r4);	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r16 = new java.lang.StringBuilder;	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r16.<init>();	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r0 = r16;
                                r16 = r0.append(r15);	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r17 = " - ";
                                r16 = r16.append(r17);	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r0 = r16;
                                r16 = r0.append(r5);	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r8 = r16.toString();	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r9 = new org.json.JSONObject;	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r9.<init>();	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r16 = "clientId";
                                r17 = com.salgado.jorge.keymanky.procesodeventa.JavaScriptInterface.usuario3;	 Catch:{ JSONException -> 0x0129, IOException -> 0x0133 }
                                r0 = r16;
                                r1 = r17;
                                r10.put(r0, r1);	 Catch:{ JSONException -> 0x0129, IOException -> 0x0133 }
                                r16 = "route";
                                r17 = com.salgado.jorge.keymanky.procesodeventa.JavaScriptInterface.ruta2;	 Catch:{ JSONException -> 0x0129, IOException -> 0x0133 }
                                r0 = r16;
                                r1 = r17;
                                r10.put(r0, r1);	 Catch:{ JSONException -> 0x0129, IOException -> 0x0133 }
                                r16 = "onDate";
                                r17 = com.salgado.jorge.keymanky.procesodeventa.JavaScriptInterface.fechaestatica;	 Catch:{ JSONException -> 0x0129, IOException -> 0x0133 }
                                r0 = r16;
                                r1 = r17;
                                r10.put(r0, r1);	 Catch:{ JSONException -> 0x0129, IOException -> 0x0133 }
                                r16 = "endDate";
                                r0 = r16;
                                r10.put(r0, r8);	 Catch:{ JSONException -> 0x0129, IOException -> 0x0133 }
                                r16 = new java.lang.StringBuilder;	 Catch:{ JSONException -> 0x0129, IOException -> 0x0133 }
                                r16.<init>();	 Catch:{ JSONException -> 0x0129, IOException -> 0x0133 }
                                r17 = "{ lat: ";
                                r16 = r16.append(r17);	 Catch:{ JSONException -> 0x0129, IOException -> 0x0133 }
                                r17 = com.salgado.jorge.keymanky.procesodeventa.JavaScriptInterface.latitudeGPS;	 Catch:{ JSONException -> 0x0129, IOException -> 0x0133 }
                                r16 = r16.append(r17);	 Catch:{ JSONException -> 0x0129, IOException -> 0x0133 }
                                r17 = " long: ";
                                r16 = r16.append(r17);	 Catch:{ JSONException -> 0x0129, IOException -> 0x0133 }
                                r17 = com.salgado.jorge.keymanky.procesodeventa.JavaScriptInterface.longitudeGPS;	 Catch:{ JSONException -> 0x0129, IOException -> 0x0133 }
                                r16 = r16.append(r17);	 Catch:{ JSONException -> 0x0129, IOException -> 0x0133 }
                                r17 = " }";
                                r16 = r16.append(r17);	 Catch:{ JSONException -> 0x0129, IOException -> 0x0133 }
                                r11 = r16.toString();	 Catch:{ JSONException -> 0x0129, IOException -> 0x0133 }
                                r16 = "checkinLocation";
                                r0 = r16;
                                r10.put(r0, r11);	 Catch:{ JSONException -> 0x0129, IOException -> 0x0133 }
                            L_0x00d3:
                                r16 = "application/json; charset=utf-8";
                                r16 = okhttp3.MediaType.parse(r16);	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r17 = r10.toString();	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r3 = okhttp3.RequestBody.create(r16, r17);	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r16 = new retrofit2.Retrofit$Builder;	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r16.<init>();	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r17 = "https://www.apps-sellcom-dev.com/LALOMA_CENTRAL-web/";
                                r16 = r16.baseUrl(r17);	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r17 = retrofit2.converter.gson.GsonConverterFactory.create();	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r16 = r16.addConverterFactory(r17);	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r12 = r16.build();	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r16 = com.salgado.jorge.keymanky.procesodeventa.ManagerService.class;
                                r0 = r16;
                                r13 = r12.create(r0);	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r13 = (com.salgado.jorge.keymanky.procesodeventa.ManagerService) r13;	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r16 = "apiKey";
                                r0 = r16;
                                r16 = r10.getString(r0);	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r17 = "authToken";
                                r0 = r17;
                                r17 = r10.getString(r0);	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r0 = r16;
                                r1 = r17;
                                r2 = r13.setInsertar(r0, r1, r3);	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r16 = new com.salgado.jorge.keymanky.procesodeventa.Principal$2$2$1;	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r0 = r16;
                                r1 = r18;
                                r0.<init>();	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                r0 = r16;
                                r2.enqueue(r0);	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                            L_0x0128:
                                return;
                            L_0x0129:
                                r7 = move-exception;
                                r7.printStackTrace();	 Catch:{ JSONException -> 0x012e, IOException -> 0x0133 }
                                goto L_0x00d3;
                            L_0x012e:
                                r7 = move-exception;
                                r7.printStackTrace();
                                goto L_0x0128;
                            L_0x0133:
                                r7 = move-exception;
                                r7.printStackTrace();
                                goto L_0x0128;
                                */
                                throw new UnsupportedOperationException("Method not decompiled: com.salgado.jorge.keymanky.procesodeventa.Principal.2.2.onResponse(retrofit2.Call, retrofit2.Response):void");
                            }

                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                            }
                        });
                    }
                }
            });
            showToast("");
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