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
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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

    private void inserta(final String ruta, final String usuario2) {
        ruta2 = ruta;
        Time time = new Time(Time.getCurrentTimezone());
        time.setToNow();
        final String horafecha = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH).format(new Date(System.currentTimeMillis())) + " - " + time.format("%Y/%m/%d");
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



        /*Inserta en servicio si hay internet */
        JSONObject json = new JSONObject();
        try {
            json.put("clientID",Integer.parseInt(usuario2));
            json.put("route",ruta);
            json.put("onDate",horafecha);
            json.put("endDate",horafecha);
            json.put("checkinLocation","{ln:"+latitudeGPS.toString()+",lt:"+longitudeGPS.toString()+"}");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://www.apps-sellcom-dev.com/LALOMA_CENTRAL-web/").addConverterFactory(GsonConverterFactory.create()).build();
        ManagerService managerService = retrofit.create(ManagerService.class);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),json.toString());
        managerService.setInsertar("e1c35bf0594ac92177406e4ef28d46f671a00fafb7347b880a7a42450e4bbad0","3122d44f-3900-45be-9709-0d4d5a05aad2",requestBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                VariablesGlobales.rutas.add(new Ruta(Integer.parseInt(usuario2),ruta,horafecha,horafecha,"{ln:"+latitudeGPS.toString()+",lt:"+longitudeGPS.toString()+"}"));
            }
        });

        /*Fin de inserción*/

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