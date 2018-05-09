package com.salgado.jorge.keymanky.procesodeventa;

import android.content.Context;
import android.text.format.Time;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class JavaScriptInterface {
    public static String fechaestatica;
    public static Double latitudeGPS;
    public static Double longitudeGPS;
    public static String ruta2;
    public static String usuario3;
    GPSTracker gps;
    Context mContext;
    WebView wb;

    JavaScriptInterface(Context c, WebView webView) {
        this.mContext = c;
        this.wb = webView;
    }

    @JavascriptInterface
    public void ejecutarGPS(String usuario, String usuario2) {
        this.gps = new GPSTracker(this.mContext);
        if (this.gps.canGetLocation()) {
            latitudeGPS = Double.valueOf(this.gps.getLatitude());
            longitudeGPS = Double.valueOf(this.gps.getLongitude());
            usuario3 = usuario2;
            //this.gps.showSettingsAlert();
            this.gps.stopUsingGPS();
            inserta(usuario, usuario2);
            //Toast.makeText(mContext,usuario2,Toast.LENGTH_LONG).show();
        }
    }

    private void inserta(String ruta, String usuario2) {
        ruta2 = ruta;
        Time time = new Time(Time.getCurrentTimezone());
        time.setToNow();
        String horafecha = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH).format(new Date(System.currentTimeMillis())) + " - " + time.format("%Y/%m/%d");
        fechaestatica = horafecha;
        //Toast.makeText(this.mContext,ruta.toString(),Toast.LENGTH_LONG).show();
        String texto = ruta2.toString();
        if (texto.equals("inicia ruta")) {
            texto ="inicia ruta";
        }else{
            texto = "modulo";
        }

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://localhost:8080/rutas/insertarGPS.php");
        ArrayList<NameValuePair> param = new ArrayList();
        param.add(new BasicNameValuePair("id", usuario2));
        param.add(new BasicNameValuePair("latitud", latitudeGPS.toString()));
        param.add(new BasicNameValuePair("longitud", longitudeGPS.toString()));
        param.add(new BasicNameValuePair("modulo", texto));
        param.add(new BasicNameValuePair("cliente", usuario2));
        param.add(new BasicNameValuePair("id_ruta", ruta));
        param.add(new BasicNameValuePair("fecha", horafecha));
        param.add(new BasicNameValuePair("hora", "00"));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(param));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpClient.execute(httpPost).getEntity().getContent()));
            String isi = "";
            String str = "";
            while (true) {
                str = bufferedReader.readLine();
                if (str != null) {
                    isi = isi + str;
                } else if (!isi.equals("null")) {
                    return;
                } else {
                    return;
                }
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }
}