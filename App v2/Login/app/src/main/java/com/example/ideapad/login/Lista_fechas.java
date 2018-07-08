package com.example.ideapad.login;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

public class Lista_fechas extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ArrayList<String> lista_fecha;
    ArrayList<String> fechas = new ArrayList<>();
    ListView listaview_F;
    ArrayList<String> lista_informacion;
    String RPi;
    private String IP = "192.168.42.1";
    int ID_tarea;
    int ID_predio;
    String id_ub;
    String placa;
    String SSID;
    String PASS;

    int validar_origen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_lista_fechas);

        Bundle parametro = getIntent().getExtras();
        ID_tarea = parametro.getInt("id_tarea");
        ID_predio = parametro.getInt("id_predio");
        id_ub = parametro.getString("id_ub");
        placa = parametro.getString("placa");
        SSID = parametro.getString("SSID");
        PASS = parametro.getString("PASS");

        validar_origen = parametro.getInt("origen");

        Button boton_B = (Button) findViewById(R.id.btn_volver);
        boton_B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                actualizar_tarea();

            }
        });

        listaview_F = (ListView) findViewById(R.id.lista_F);


        if(validar_origen == 1) {

            if (isOnline() != false) {
                validar_formulario();
            }

        } else {

            consulta_tareas();

        }
    }

    private void consulta_tareas() {

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);

        SQLiteDatabase bd = admin.getWritableDatabase();

        String Id = placa;

        lista_informacion = new ArrayList<String>();

        Cursor fila = bd.rawQuery("select * from L_microcontrolador where Id_placa = ?", new String[] {Id});
        if(fila.moveToFirst()){

            for (fila.moveToFirst(); !fila.isAfterLast(); fila.moveToNext()) {

                buscar_ubicacion(fila.getString(0), fila.getString(1));

                HashSet hs = new HashSet();
                hs.addAll(lista_informacion);
                lista_informacion.clear();
                lista_informacion.addAll(hs);

                HashSet hs2 = new HashSet();
                hs2.addAll(fechas);
                fechas.clear();
                fechas.addAll(hs2);

            }

        }

        bd.close();

        ArrayAdapter adaptador = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,lista_informacion);
        listaview_F.setOnItemClickListener(this);
        listaview_F.setAdapter(adaptador);
        borrar_SSID();
    }

    public void buscar_ubicacion(String placa, String AP){

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);

        SQLiteDatabase bd = admin.getWritableDatabase();

        Cursor fila = bd.rawQuery("select * from L_ubicacion_sensor where id_placa=" + placa, null);

        if(fila.moveToFirst()){

            for (fila.moveToFirst(); !fila.isAfterLast(); fila.moveToNext()) {

                if (fila.getString(0) != null) {

                    buscar_fechas(placa,fila.getString(0), AP);

                }
            }
        }

        bd.close();

    }


    public void buscar_fechas(String placa, String id_ubicacion, String AP){

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);

        SQLiteDatabase bd = admin.getWritableDatabase();

        Cursor fila = bd.rawQuery("select * from L_lectura_humedad where id_us=" + id_ubicacion, null);

        if(fila.moveToFirst()){

            for (fila.moveToFirst(); !fila.isAfterLast(); fila.moveToNext()) {

                RPi = placa;

                    fechas.add(fila.getString(3));
                    lista_informacion.add("Fecha : " + fila.getString(3) + " -- " + "Sensor : " + AP);


            }

        }

        bd.close();

    }

    private void validar_formulario() {

        Thread tr = new Thread() {

            @Override
            public void run() {

                final String resultado = buscar_GET();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        int r = obtenerDatos_json(resultado);
                        if (r > 0) {

                            //Toast.makeText(getApplicationContext(), "si : " + resultado, Toast.LENGTH_SHORT).show();
                            consulta_tareas();

                        }else{

                            Toast.makeText(getApplicationContext(), "no : " + resultado, Toast.LENGTH_SHORT).show();
                        }

                    }

                });
            }


        };

        tr.start();

    }


    public String buscar_GET(){

        URL url = null;
        String linea = "";
        int respuesta = 0;
        StringBuilder resul=null;

        try {

            url = new URL("http://" + IP + "/select_dato.php");

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

    public int obtenerDatos_json(String response){

        int res = 0;

        int largo;
        String id = id_ub;

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        try {

            JSONArray json= new JSONArray(response);
            JSONObject objeto;

            if (json.length()>0){

                largo = json.length();

                for (int contador = 0; contador < largo; contador++){

                    objeto = json.getJSONObject(contador);

                    ContentValues dato = new ContentValues();
                    //dato.put("Id_lectura", objeto.getInt("id"));
                    dato.put("id_us", id);
                    dato.put("valor", objeto.getString("valor"));
                    dato.put("hora", objeto.getString("hora"));
                    dato.put("fecha", objeto.getString("fecha"));


                    bd.insert("L_lectura_humedad", null, dato);
                    res = 1;

                }
            }

            bd.close();

        }catch (Exception e){

        }
        return res;
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
                        //Log.e(TAG, "Error checking internet connection", e);
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        for(int i=0;i<fechas.size();i++)
        {
            if(position == i)
            {
                String fecha = fechas.get(i);

                Intent lista_g = new Intent(this, Grafico.class);
                lista_g.putExtra("fecha", fecha);
                lista_g.putExtra("ub",id_ub);

                lista_g.putExtra("id_tarea", ID_tarea);
                lista_g.putExtra("id_predio", ID_predio);
                lista_g.putExtra("placa", placa);
                lista_g.putExtra("SSID" , SSID);
                lista_g.putExtra("PASS", PASS);

                startActivity(lista_g);

            }

        }

    }

    private void actualizar_tarea() {


        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("realizada","1");

        int update = bd.update("L_ubicacion_sensor",valores,"Id_us=" + id_ub,null);

        if (update == 1)
            Toast.makeText(this, "Se Ingresaron Correctamente los Datos", Toast.LENGTH_SHORT).show();

        borrar_SSID();

        Intent lis = new Intent(getApplicationContext(), Mapa_ubicacionesS.class);
        lis.putExtra("id_tarea", ID_tarea);
        lis.putExtra("id_predio", ID_predio);
        startActivity(lis);

    }

    private void borrar_SSID(){

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int networkId = wifiManager.getConnectionInfo().getNetworkId();
        wifiManager.disconnect();
        wifiManager.removeNetwork(networkId);
        wifiManager.saveConfiguration();


    }

    @Override
    public void onBackPressed() {

    }
}
