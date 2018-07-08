package com.example.ideapad.login;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

public class Conectar_sensor extends AppCompatActivity implements View.OnClickListener {

    private String IP = "192.168.0.10";
    private TextView labeltarea;
    private TextView labelconectado;
    private Button boton_cancelar;
    private Button boton_guardar;
    private String t_n;
    private int valor_C;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conectar_sensor);

        labeltarea = (TextView) findViewById(R.id.labeltarea);
        labelconectado = (TextView) findViewById(R.id.labelconectar);


        valor_C = 0;
        boton_guardar = (Button) findViewById(R.id.btn_guardar);
        boton_cancelar = (Button) findViewById(R.id.btn_cancelar);
        boton_guardar.setOnClickListener(this);
        boton_cancelar.setOnClickListener(this);

        Bundle parametro = getIntent().getExtras();

        if (parametro != null) {

            String string = parametro.getString("tarea");
            String[] parts = string.split("  F");
            String part1 = parts[0];
            //String part2 = parts[1];

            labeltarea.setText(part1);

        }



        Button boton_cn = (Button) findViewById(R.id.btn_conectar);
        boton_cn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isOnline() != false) {

                    labelconectado.setText("Conectado");
                    valor_C = 1;

                }else{

                    labelconectado.setText("Fallo conexión");
                    valor_C = 2;

                }

            }
        });

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

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        try {

            JSONArray json= new JSONArray(response);
            JSONObject objeto;

            if (json.length()>0){

                largo = json.length();

                for (int contador = 0; contador < largo; contador++){

                    objeto = json.getJSONObject(contador);

                    ContentValues tarea = new ContentValues();
                    tarea.put("Id", objeto.getInt("Id"));
                    tarea.put("id_tarea", t_n);
                    tarea.put("sensor", objeto.getString("sensor"));
                    tarea.put("valor_humedad", objeto.getString("valor_humedad"));
                    tarea.put("latitud", "0");
                    tarea.put("longitud","0");
                    tarea.put("fecha", objeto.getString("fecha"));

                    long insertar = bd.insert("L_datoS", null, tarea);
                    res = 1;

                }
            }

            bd.close();

        }catch (Exception e){

        }
        return res;
    }


    //preguntar si hay internet
    public boolean isOnline() {

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

        RunnableFuture<Boolean> futureRun = new FutureTask<Boolean>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if ((networkInfo .isAvailable()) && (networkInfo .isConnected())) {
                    try {
                        HttpURLConnection urlc = (HttpURLConnection) (new URL("http://" + IP).openConnection());
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
    public void onClick(View v) {

        int id;
        id = v.getId();

        String idf = (String) labeltarea.getText();

        String[] parts = idf.split(": ");
        String part1 = parts[0];
        t_n = parts[1];

        switch (id){

            case R.id.btn_cancelar:
                Intent lis = new Intent(getApplicationContext(), ListaT_sensor.class);
                startActivity(lis);
                break;

            case R.id.btn_guardar:

                validar_formulario();

                break;

        }

    }

    private void validar_formulario() {

        if(valor_C != 1) {

            Toast.makeText(getApplicationContext(), "Conectece al sensor primero", Toast.LENGTH_SHORT).show();

        }else{

            Thread tr = new Thread() {

                @Override
                public void run() {

                    final String resultado = buscar_GET();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            int r = obtenerDatos_json(resultado);
                            if (r > 0) {

                                actualizar_tarea(Integer.parseInt(t_n));


                            }

                        }

                    });
                }


            };

            tr.start();


        }
    }

    private void actualizar_tarea(int Tarea) {


        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("realizada","1");

        int update = bd.update("L_tareas",valores,"Id_tarea=" + String.valueOf(Tarea),null);

        if (update == 1)
            Toast.makeText(this, "se guadaron Correctamente los Datos del sensor", Toast.LENGTH_SHORT)
                    .show();

        Intent lis = new Intent(getApplicationContext(), ListaT_sensor.class);
        startActivity(lis);

    }

}
