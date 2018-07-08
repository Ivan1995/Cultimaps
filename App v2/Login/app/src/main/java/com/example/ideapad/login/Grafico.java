package com.example.ideapad.login;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Grafico extends AppCompatActivity {

    String fecha;
    String ub;

    int ID_tarea;
    int ID_predio;
    String id_ub;
    String placa;
    String SSID;
    String PASS;

    ArrayList<String> fechas = new ArrayList<>();
    ArrayList<Integer> valores = new ArrayList<>();
    ArrayList<String> idb = new ArrayList<>();

    ArrayList<Entry> values;

    private LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_grafico);

        Bundle parametro = getIntent().getExtras();
        fecha = parametro.getString("fecha");
        ub = parametro.getString("ub");

        ID_tarea = parametro.getInt("id_tarea");
        ID_predio = parametro.getInt("id_predio");
        id_ub = parametro.getString("ub");
        placa = parametro.getString("placa");
        SSID = parametro.getString("SSID");
        PASS = parametro.getString("PASS");


        Button boton_B = (Button) findViewById(R.id.OK_btn3);
        boton_B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent lis = new Intent(getApplicationContext(), Lista_fechas.class);
                lis.putExtra("id_tarea", ID_tarea);
                lis.putExtra("id_predio", ID_predio);
                lis.putExtra("id_ub", id_ub);
                lis.putExtra("placa", placa);
                lis.putExtra("SSID" , SSID);
                lis.putExtra("PASS", PASS);
                lis.putExtra("origen", 0);
                startActivity(lis);

            }
        });

        mChart = (LineChart) findViewById(R.id.chart2);

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
        cargar_fechas();
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


    }

    private void setData() {

        values = new ArrayList<Entry>();

        for (int i=0; i < fechas.size(); i++) {

            String f = fechas.get(i);
            if (f.toLowerCase().contains(fecha.toLowerCase())) {

                //Log.d("ID : ",id_ub);

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
    }

    public void buscar_fechas(){

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);

        SQLiteDatabase bd = admin.getWritableDatabase();


        final Cursor fila = bd.rawQuery("select * from L_lectura_humedad where id_us = ?", new String[] {id_ub});

        if(fila.moveToFirst()){

                Thread tr = new Thread() {

                    @Override
                    public void run() {

                        for (fila.moveToFirst(); !fila.isAfterLast(); fila.moveToNext()) {

                                idb.add(fila.getString(1));
                                valores.add(fila.getInt(2));
                                fechas.add(fila.getString(3) + " " + fila.getString(4));


                        }
                    }
                };

                tr.start();
        }
        bd.close();

    }

    private void cargar_fechas() {

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);

        SQLiteDatabase bd = admin.getWritableDatabase();

        Thread tr = new Thread() {

            @Override
            public void run() {

             buscar_fechas();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        setData();

                    }
                });

            }
        };
        tr.start();
    }

    @Override
    public void onBackPressed() {

    }

}
