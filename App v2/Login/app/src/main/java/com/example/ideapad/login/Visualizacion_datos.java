package com.example.ideapad.login;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

public class Visualizacion_datos extends AppCompatActivity {


    /*

    No esta en funcionamiento

     */

    private String IP = "192.168.42.1";
    int ID_tarea;
    int ID_predio;
    String id_ub;
    String placa;
    String SSID;
    String PASS;

    String networkSSID;
    String networkPass;
    WifiConfiguration conf = new WifiConfiguration();

    boolean problema = false;

    private LineChart mChart;
    private TextView tvX;

    ArrayList<String> fechas = new ArrayList<>();
    ArrayList<Integer> valores = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizacion_datos);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Bundle parametro = getIntent().getExtras();
        ID_tarea = parametro.getInt("id_tarea");
        ID_predio = parametro.getInt("id_predio");

        id_ub = parametro.getString("id_ub");
        placa = parametro.getString("placa");
        SSID = parametro.getString("SSID");
        PASS = parametro.getString("PASS");

        if (isOnline() != false){

            //buscar_datos_S2();
            //Toast.makeText(getApplicationContext(), linea, Toast.LENGTH_LONG).show();
            //cargar_datos_S();
            buscar_datosG();
            setData();
            mChart.invalidate();


        } else {

        }


        mChart = (LineChart) findViewById(R.id.chart1);

        // no description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        mChart.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setHighlightPerDragEnabled(true);
        mChart.zoom(5,1,0,0);


        // set an alternative background color
        mChart.setBackgroundColor(Color.WHITE);
        mChart.setViewPortOffsets(80f, 0f, 040f, 60f);

        // add data
        //setData();
        mChart.invalidate();

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        l.setEnabled(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setTypeface(mTfLigt);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);
        xAxis.setTextColor(Color.rgb(255, 192, 56));
        xAxis.setCenterAxisLabels(false);
        xAxis.setGranularity(1f); // one hour
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            private SimpleDateFormat mFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                long millis = TimeUnit.MINUTES.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }
        });

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        //leftAxis.setTypeface(mTfLight);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setAxisMinimum(2f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setYOffset(-9f);
        leftAxis.setTextColor(Color.rgb(255, 192, 56));

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);



        Button boton_TM = (Button) findViewById(R.id.OK_btn);
        boton_TM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(), "Guardando datos", Toast.LENGTH_LONG).show();

                actualizar_tarea();
                borrar_SSID();
            }
        });

    }

    public void format_graph(){

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        l.setEnabled(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setTypeface(mTfLigt);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);
        xAxis.setTextColor(Color.rgb(255, 192, 56));
        xAxis.setCenterAxisLabels(false);
        xAxis.setGranularity(1f); // one hour
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            private SimpleDateFormat mFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                long millis = TimeUnit.MINUTES.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }
        });

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        //leftAxis.setTypeface(mTfLight);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setAxisMinimum(2f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setYOffset(-9f);
        leftAxis.setTextColor(Color.rgb(255, 192, 56));

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);


    }

    public void buscar_datosG(){

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);

        SQLiteDatabase bd = admin.getWritableDatabase();

        Cursor fila = bd.rawQuery("select * from L_lectura_humedad where id_us =" + id_ub, null);

        if(fila.moveToFirst()){

            for (fila.moveToFirst(); !fila.isAfterLast(); fila.moveToNext()) {

                if(fila.getString(1) != null) {
                    valores.add(fila.getInt(2));
                    fechas.add(fila.getString(3) + " " + fila.getString(4));
                } else {

                    Toast.makeText(getApplicationContext(), "no hay datos de humedad", Toast.LENGTH_LONG).show();

                }
            }

        }

        bd.close();

    }



    private void setData() {

        ArrayList<Entry> values = new ArrayList<Entry>();

        for (int i=0; i < fechas.size(); i++) {

            // now in hours
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            String dateInString;
            dateInString = fechas.get(i);
            Date date = null;
            try {
                date = formatter.parse(dateInString);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            long now = TimeUnit.MILLISECONDS.toMinutes(date.getTime());
            float y = valores.get(i);
            values.add(new Entry(now, y)); // add one entry per hour

            // create a dataset and give it a type
            LineDataSet set1 = new LineDataSet(values,"");
            set1.setAxisDependency(YAxis.AxisDependency.LEFT);
            set1.setColor(ColorTemplate.getHoloBlue());
            set1.setValueTextColor(ColorTemplate.getHoloBlue());
            set1.setLineWidth(2.5f);
            set1.setCircleRadius(4.5f);
            set1.setDrawCircles(true);
            set1.setDrawValues(true);
            set1.setFillAlpha(65);
            set1.setFillColor(ColorTemplate.getHoloBlue());
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setDrawCircleHole(true);
            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);

            // create a data object with the datasets
            LineData data = new LineData(set1);
            data.setValueTextColor(Color.BLACK);
            data.setHighlightEnabled(true);
            data.setValueTextSize(15f);

            // set data
            mChart.setData(data);

        }
    }

    private void actualizar_tarea() {


        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("realizada","1");

        int update = bd.update("L_ubicacion_sensor",valores,"Id_ubt=" + id_ub,null);

        if (update == 1)
            Toast.makeText(this, "Se Ingresaron Correctamente los Datos", Toast.LENGTH_SHORT).show();

        Intent lista_T = new Intent(this, Mapa_ubicacionesS.class);
        lista_T.putExtra("id_tarea", ID_tarea);
        lista_T.putExtra("id_predio",ID_predio);
        startActivity(lista_T);

    }

    private void cargar_datos_S() {


            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);

            SQLiteDatabase bd = admin.getWritableDatabase();

            Thread tr = new Thread() {

                @Override
                public void run() {

                    final String resultado = buscar_datos_S();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            int r = obtenerjson_datos_S(resultado);
                            if (r > 0) {

                                //setData();
                                //consulta_predio();

                            }
                        }
                    });
                }
            };
            tr.start();
    }
    public int obtenerjson_datos_S(String response){

        int res = 0;
        int largo;

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        try {

            JSONArray json= new JSONArray(response);
            JSONObject objeto;

            if (json.length()>0){

                largo = json.length();

                for (int contador = 0; contador < largo; contador++){

                    objeto = json.getJSONObject(contador);

                    ContentValues predio = new ContentValues();
                    predio.put("Id_lectura", objeto.getInt("id"));
                    predio.put("id_us", id_ub);
                    predio.put("valor", objeto.getString("valor"));
                    predio.put("fecha", objeto.getString("fecha"));
                    predio.put("hora", objeto.getString("hora"));



                    long insertar = bd.insert("L_lectura_humedad", null, predio);
                    res = 1;

                }
            }

            bd.close();

        }catch (Exception e){

        }
        return res;
    }

    public String buscar_datos_S(){


            URL url = null;
            String linea = "";
            int respuesta = 0;
            StringBuilder resul=null;

            try {

                url = new URL("https://" + IP + "/select.php");

                HttpURLConnection conection = (HttpURLConnection)url.openConnection();

                respuesta=conection.getResponseCode();

                resul = new StringBuilder();

                if (respuesta==HttpURLConnection.HTTP_OK){

                    InputStream in = new BufferedInputStream(conection.getInputStream());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                    while((linea=reader.readLine())!=null){

                        resul.append(linea);

                    }

                }

            } catch (Exception e) {

            }

            return  resul.toString();

        }

    public void borrar_SSID(){

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int networkId = wifiManager.getConnectionInfo().getNetworkId();
        wifiManager.removeNetwork(networkId);
        wifiManager.saveConfiguration();

    }

    public boolean isOnline() {

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

        RunnableFuture<Boolean> futureRun = new FutureTask<Boolean>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if ((networkInfo .isAvailable()) && (networkInfo .isConnected())) {
                    try {
                        HttpURLConnection urlc = (HttpURLConnection) (new URL("http://" + IP + "/index.html").openConnection());
                        urlc.setRequestProperty("User-Agent", "Test");
                        urlc.setRequestProperty("Connection", "close");
                        urlc.setConnectTimeout(1500);
                        urlc.connect();
                        return (urlc.getResponseCode() == 200);


                    } catch (IOException e) {

                    }
                } else {
                    //Log.d(TAG, "No network available!");
                }
                return false;
            }
        });

        new Thread(futureRun).start();


        try {
            return futureRun.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public void onBackPressed() {

    }

}
