package com.salgado.jorge.keymanky.procesodeventa;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import jpos.JposException;
import jpos.POSPrinter;
import jpos.POSPrinterConst;
import jpos.config.JposEntry;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.ClientError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.bxl.BXLConst;
import com.bxl.config.editor.BXLConfigLoader;

import org.json.JSONObject;
import org.w3c.dom.Text;

public class Imprimir extends AppCompatActivity implements
        View.OnClickListener, AdapterView.OnItemClickListener,
        SeekBar.OnSeekBarChangeListener, OnCheckedChangeListener  {
    private static final int REQUEST_SMS = 0;
    public WebView browser;
    private final String TAG_REQUEST = "MY_TAG";
    private static final int REQUEST_CODE_BLUETOOTH = 1;
    private static final int REQUEST_CODE_ACTION_PICK = 2;
    private RequestQueue mVolleyQueue;
    private RequestQueue mVolleyQueue2;
    private static final String DEVICE_ADDRESS_START = " (";
    private static final String DEVICE_ADDRESS_END = ")";
    private int pos_id;
    private String result_msn;
    private String id;

    private final ArrayList<CharSequence> bondedDevices = new ArrayList<>();
    private ArrayAdapter<CharSequence> arrayAdapter;

    private TextView pathTextView;
    private TextView progressTextView;
    private TextView vistaprevia;
    private RadioGroup openRadioGroup;
    private Button openFromDeviceStorageButton;
    private Button btn_imprimir;
    private String texto_a_imprimir;

    private BXLConfigLoader bxlConfigLoader;
    private POSPrinter posPrinter;
    private String logicalName;
    private int brightness = 50;
    //	private int compress = 1;
    private int compress = 0;
    private String url;
    JsonObjectRequest jsonObjRequest;
    private TextView texto_request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imprimir);

        pathTextView = (TextView) findViewById(R.id.textViewPath);
        progressTextView = (TextView) findViewById(R.id.textViewProgress);

        openRadioGroup = (RadioGroup) findViewById(R.id.radioGroupOpen);
        openRadioGroup.setOnCheckedChangeListener(this);

        openFromDeviceStorageButton = (Button) findViewById(R.id.buttonOpenFromDeviceStorage);
        openFromDeviceStorageButton.setOnClickListener(this);

        btn_imprimir = (Button) findViewById(R.id.buttonPrint);
        btn_imprimir.setEnabled(false);
        vistaprevia = (TextView) findViewById(R.id.response);

        findViewById(R.id.buttonOpenPrinter).setOnClickListener(this);
        findViewById(R.id.buttonPrint).setOnClickListener(this);
        findViewById(R.id.buttonClosePrinter).setOnClickListener(this);

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBarBrightness);
        seekBar.setOnSeekBarChangeListener(this);
        vistaprevia.setText("Vista Previa");

        //PARA CONEXION A IMPRESORA BIXLOON
        setBondedDevices();
        arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice, bondedDevices);
        ListView listView = (ListView) findViewById(R.id.listViewPairedDevices);
        listView.setAdapter(arrayAdapter);

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(this);
        listView.clearChoices();
        bxlConfigLoader = new BXLConfigLoader(this);
        try {
            bxlConfigLoader.openFile();
        } catch (Exception e) {
            e.printStackTrace();
            bxlConfigLoader.newFile();
        }
        posPrinter = new POSPrinter(this);

        //PARA OBTNER LOS DATOS DEL TICKET
        mVolleyQueue = Volley.newRequestQueue(this);
        mVolleyQueue2 = Volley.newRequestQueue(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            url = extras.getString("url");
        }
        makeSampleHttpRequest(url);

        //PARA OBTENER LOS DATOS DE MSN
        pos_id = url.indexOf("=");
        if(pos_id == -1){
            id   = "0";
            //showToast(id);
        }else{
            id = url.substring(pos_id+1);
            //showToast(id);
        }
        String url2= "http://localhost:8080/rutas/msn.php?id=" + id;
        //showToast(url2);
        makeSampleHttpRequest2(url2);

        //PARA VISTA PREVIA
        browser=(WebView)findViewById(R.id.wb_imprimir);
        browser.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                view.loadUrl(url); return true;
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                //view.loadUrl("file:///android_asset/myerrorpage.html");
                view.loadUrl("/sdcard/htdocs/rutas/error.php");
            }
        });
        WebSettings webSettings = browser.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        browser.setWebChromeClient(new WebChromeClient());
        browser.loadUrl(url);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);

        //Para motoE 2da generacion
        browser.setInitialScale(80);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            posPrinter.close();
        } catch (JposException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_BLUETOOTH:
                setBondedDevices();
                break;

            case REQUEST_CODE_ACTION_PICK:
                if (data != null) {
                    Uri uri = data.getData();
                    ContentResolver cr = getContentResolver();
                    Cursor c = cr.query(uri,
                            new String[] { MediaStore.Images.Media.DATA }, null,
                            null, null);
                    if (c == null || c.getCount() == 0) {
                        return;
                    }

                    c.moveToFirst();
                    int columnIndex = c
                            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    String text = c.getString(columnIndex);
                    c.close();

                    pathTextView.setText(text);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonOpenFromDeviceStorage:
                openFromDeviceStorage();
                break;

            case R.id.buttonOpenPrinter:
                openPrinter();
                break;

            case R.id.buttonPrint:
                print_texto(texto_a_imprimir);
                Handler handler = new Handler();
                //PAUSA Y VOVLER A IMPRIMIR
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        print_texto(texto_a_imprimir);
                    }
                }, 5000);
                Toast.makeText(this, "Enviando 2da copia ... ", Toast.LENGTH_SHORT).show();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        closePrinter();
                        finish();
                    }
                }, 8000);
                break;

            case R.id.buttonClosePrinter:
                closePrinter();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        String device = ((TextView) view).getText().toString();
        logicalName = device.substring(0, device.indexOf(DEVICE_ADDRESS_START));
        String address = device.substring(device.indexOf(DEVICE_ADDRESS_START)
                        + DEVICE_ADDRESS_START.length(),
                device.indexOf(DEVICE_ADDRESS_END));

        try {
            for (Object entry : bxlConfigLoader.getEntries()) {
                JposEntry jposEntry = (JposEntry) entry;
                bxlConfigLoader.removeEntry(jposEntry.getLogicalName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String strProduce = null;
        if(setProductName(logicalName).length() == 0){
            strProduce = logicalName;
        }else{
            strProduce = setProductName(logicalName);
        }

        try {
            bxlConfigLoader.addEntry(logicalName,
                    BXLConfigLoader.DEVICE_CATEGORY_POS_PRINTER, strProduce,
                    BXLConfigLoader.DEVICE_BUS_BLUETOOTH, address);

            bxlConfigLoader.saveFile();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    openPrinter();
                }
            }, 3000);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "ERROR: " +  e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String setProductName(String name){
        String productName = null;

        if((logicalName.indexOf("SPP-R200II")>=0)){
            productName = BXLConst.SPP_R200II;

            if(logicalName.length()>11){
                productName = BXLConst.SPP_R200III;
            }
        }
        else if((logicalName.indexOf("SPP-R210")>=0)){
            productName = BXLConst.SPP_R210;
        }else if((logicalName.indexOf("SPP-R310")>=0)){
            productName = BXLConst.SPP_R310;
        }else if((logicalName.indexOf("SPP-R300")>=0)){
            productName = BXLConst.SPP_R300;
        }else if((logicalName.indexOf("SPP-R400")>=0)){
            productName = BXLConst.SPP_R400;
        }else{

        }

        return productName;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        progressTextView.setText(Integer.toString(progress));
        brightness = progress;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.radioDeviceStorage:
                openFromDeviceStorageButton.setEnabled(true);
                break;

            case R.id.radioProjectResources:
                openFromDeviceStorageButton.setEnabled(false);
                break;
        }
    }

    private void setBondedDevices() {
        logicalName = null;
        bondedDevices.clear();

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();
        Set<BluetoothDevice> bondedDeviceSet = bluetoothAdapter
                .getBondedDevices();

        for (BluetoothDevice device : bondedDeviceSet) {
            bondedDevices.add(device.getName() + DEVICE_ADDRESS_START
                    + device.getAddress() + DEVICE_ADDRESS_END);
        }

        if (arrayAdapter != null) {
            arrayAdapter.notifyDataSetChanged();
        }
    }

    private void openFromDeviceStorage() {
        String externalStorageState = Environment.getExternalStorageState();

        if (externalStorageState.equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
            startActivityForResult(intent, REQUEST_CODE_ACTION_PICK);
        }
    }

    private void openPrinter() {
        try {
            posPrinter.open(logicalName);
            posPrinter.claim(0);
            posPrinter.setDeviceEnabled(true);
            Toast.makeText(this, "Comunicación Establecida, lista para imprimir", Toast.LENGTH_SHORT).show();
            btn_imprimir.setEnabled(true);
        } catch (JposException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

            try {
                posPrinter.close();
            } catch (JposException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    private void closePrinter() {
        try {
            posPrinter.close();
            btn_imprimir.setEnabled(false);
        } catch (JposException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void print_bitmap() {
        InputStream is = null;
        try {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.put((byte) POSPrinterConst.PTR_S_RECEIPT);
            buffer.put((byte) brightness);
            buffer.put((byte) compress);
            buffer.put((byte) 0x00);

            switch (openRadioGroup.getCheckedRadioButtonId()) {
                case R.id.radioDeviceStorage:
//				posPrinter.printBitmap(buffer.getInt(0), pathTextView.getText().toString(),
//						posPrinter.getRecLineWidth(), POSPrinterConst.PTR_BM_LEFT);
                    posPrinter.printBitmap(buffer.getInt(0), pathTextView.getText().toString(),
                            300, POSPrinterConst.PTR_BM_LEFT);
                    break;

                case R.id.radioProjectResources:
                    is = getResources().openRawResource(R.raw.logo_laloma);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
//				posPrinter.printBitmap(buffer.getInt(0), bitmap,
//						posPrinter.getRecLineWidth(), POSPrinterConst.PTR_BM_LEFT);
                    //posPrinter.printBitmap(buffer.getInt(0), bitmap,300, POSPrinterConst.PTR_BM_LEFT);
                    posPrinter.printBitmap(buffer.getInt(0), screenshot(browser,100), 300, POSPrinterConst.PTR_BM_LEFT);
                    break;
            }
            /*
                    AlertDialog alertDialog = new AlertDialog.Builder(Imprimir.this).create();
                    alertDialog.setTitle("Alert");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "-" + texto_a_imprimir + "-",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    */
            //catch JposException E
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private void print_texto(String texto_a_imprimir) {
        InputStream is = null;
        try {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.put((byte) POSPrinterConst.PTR_S_RECEIPT);
            buffer.put((byte) brightness);
            buffer.put((byte) compress);
            buffer.put((byte) 0x00);
            posPrinter.printNormal(2,texto_a_imprimir);
            //posPrinter.printRawData(texto_a_imprimir);
            //posPrinter.cutPaper(90);
        } catch (JposException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    //BITMPA
    public Bitmap screenshot(WebView webView, float scale11) {
        try {
            float scale = webView.getScale();
            int height = (int) (webView.getContentHeight() * scale + 0.5);
            Bitmap bitmap = Bitmap.createBitmap(webView.getWidth(), height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            webView.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //Guarda la respuesta en la variable
    private void makeSampleHttpRequest(String url) {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                texto_a_imprimir = response.toString();
                //showToast(texto_a_imprimir);
                vistaprevia.setText(texto_a_imprimir);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle your error types accordingly.For Timeout & No connection error, you can show 'retry' button.
                // For AuthFailure, you can re login with user credentials.
                // For ClientError, 400 & 401, Errors happening on client side when sending api request.
                // In this case you can check how client is forming the api and debug accordingly.
                // For ServerError 5xx, you can do retry or handle accordingly.
                if( error instanceof NetworkError) {
                } else if( error instanceof ClientError) {
                    texto_a_imprimir = error.toString();
                } else if( error instanceof ServerError) {
                    texto_a_imprimir = error.toString();
                } else if( error instanceof AuthFailureError) {
                    texto_a_imprimir = error.toString();
                } else if( error instanceof ParseError) {
                    texto_a_imprimir = error.toString();
                } else if( error instanceof NoConnectionError) {
                    texto_a_imprimir = error.toString();
                } else if( error instanceof TimeoutError) {
                    texto_a_imprimir = error.toString();
                }
                showToast(error.getMessage());
            }
        });
        /*
        //Set a retry policy in case of SocketTimeout & ConnectionTimeout Exceptions. Volley does retry for you if you have specified the policy.
        jsonObjRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        jsonObjRequest.setTag(TAG_REQUEST);
        mVolleyQueue.add(jsonObjRequest);
        */
        stringRequest.setShouldCache(true);
        stringRequest.setTag(TAG_REQUEST);
        mVolleyQueue.add(stringRequest);
    }

    /* PETICION PARA ENVIO DE SMS*/
    private void makeSampleHttpRequest2(String url) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                result_msn = response.toString();
                //Enviamos el mensaje de texto
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    int hasSMSPermission = checkSelfPermission(Manifest.permission.SEND_SMS);
                    if (hasSMSPermission != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[] {Manifest.permission.SEND_SMS},0);
                    }
                    sendMySMS();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle your error types accordingly.For Timeout & No connection error, you can show 'retry' button.
                // For AuthFailure, you can re login with user credentials.
                // For ClientError, 400 & 401, Errors happening on client side when sending api request.
                // In this case you can check how client is forming the api and debug accordingly.
                // For ServerError 5xx, you can do retry or handle accordingly.
                if( error instanceof NetworkError) {
                } else if( error instanceof ClientError) {
                    result_msn = error.toString();
                } else if( error instanceof ServerError) {
                    result_msn = error.toString();
                } else if( error instanceof AuthFailureError) {
                    result_msn = error.toString();
                } else if( error instanceof ParseError) {
                    result_msn = error.toString();
                } else if( error instanceof NoConnectionError) {
                    result_msn = error.toString();
                } else if( error instanceof TimeoutError) {
                    result_msn = error.toString();
                }
                showToast(error.getMessage());
            }
        });
        stringRequest.setShouldCache(true);
        stringRequest.setTag(TAG_REQUEST);
        mVolleyQueue2.add(stringRequest);
    }

    /* MOSTRAR MENSAJE AL USUARIO*/
    private void showToast(String msg) {
        Toast.makeText(Imprimir.this, msg, Toast.LENGTH_LONG).show();
    }

    /*ENVIA SMS */
    public void sendMySMS() {
        String phone;
        String message;
        String envio;
        //showToast(result_msn);

        //PARA OBTENER LOS DATOS DE MSN
        int pos_id = result_msn.indexOf("|");
        int pos_id2 = result_msn.indexOf("|",pos_id+1);
        message = result_msn.substring(pos_id+1,pos_id2);
        phone = result_msn.substring(pos_id2+1);

        envio = result_msn.substring(0,1);

        //Check if the mensaje se debe enviar, Check if the phoneNumber is empty
        if (phone.isEmpty() || envio == "0") {

        } else {
            SmsManager sms = SmsManager.getDefault();
            // if message length is too long messages are divided
            List<String> messages = sms.divideMessage(message);
            try{
                for (String msg : messages) {
                    //PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), 0);
                    //PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED"), 0);
                    sms.sendTextMessage(phone, null, msg, null, null);
                    //sms.sendTextMessage("5510818900", null, msg, null, null);
                }
            }catch (Exception e){
                //Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.android.mms");
                //startActivity(launchIntent);
            }
        }
    }
}
