package com.example.ideapad.login;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

public class Lista extends AppCompatActivity implements ListView.OnItemClickListener {

    ListView listaview_tareas;
    private String IP = "192.168.0.9";
    ArrayList<String> lista_informacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        listaview_tareas = (ListView) findViewById(R.id.lista_tareas);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mapa = new Intent(getApplicationContext(), ubicacion.class);
                startActivity(mapa);
            }
        });


    //______________________________________________________________________________________________

        if (isOnline() != false){

            vaciar_tareas();
            cargar_datos();

        }else{

             consulta_tareas();

        }
    }

    //______________________________________________________________________________________________


    //preguntar si hay internet
    public boolean isOnline() {

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

        RunnableFuture<Boolean> futureRun = new FutureTask<Boolean>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if ((networkInfo .isAvailable()) && (networkInfo .isConnected())) {
                    try {
                        HttpURLConnection urlc = (HttpURLConnection) (new URL("http://" + IP + "/WebServices/").openConnection());
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


    //______________________________________________________________________________________________


    //vacia las tareas para evitar que se repitan
    private void vaciar_tareas() {

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        bd.execSQL("delete from L_tareas");
        bd.close();

    }


    //______________________________________________________________________________________________


    private void cargar_datos() {



        final AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        final SQLiteDatabase bd = admin.getWritableDatabase();

        Cursor fila = bd.rawQuery("select * from L_sesion", null);

        if(fila.moveToFirst()) {
            if (fila.getString(0) != null) {

                final String us = fila.getString(0);

                Thread tr = new Thread() {

                    @Override
                    public void run() {

                        final String resultado = buscar_GET(us);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                SQLiteDatabase bd = admin.getWritableDatabase();

                                int r = obtenerDatos_json(resultado);
                                if (r > 0) {

                                    consulta_tareas();

                                }

                            }

                        });
                    }


                };

                tr.start();
            }
        }
        bd.close();
    }


    //______________________________________________________________________________________________


    private void consulta_tareas() {

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);

        SQLiteDatabase bd = admin.getWritableDatabase();

        lista_informacion = new ArrayList<String>();

        Cursor fila = bd.rawQuery("select * from L_tareas where realizada = 0 and id_tipo = 2", null);

        if(fila.moveToFirst()){

            for (fila.moveToFirst(); !fila.isAfterLast(); fila.moveToNext()) {

                lista_informacion.add("Tarea N°: " + fila.getString(0) + "  Fecha: " + fila.getString(4));

            }

        }

        bd.close();

        ArrayAdapter adaptador = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,lista_informacion);
        listaview_tareas.setOnItemClickListener(this);
        listaview_tareas.setAdapter(adaptador);

    }


    //______________________________________________________________________________________________


    public String buscar_GET(String id_trabajador_l){

        URL url = null;
        String linea = "";
        int respuesta = 0;
        StringBuilder resul=null;

        try {

            url = new URL("http://" + IP + "/WebServices/obtener_tareas.php?id_trabajador="+id_trabajador_l);

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


    //______________________________________________________________________________________________


    //continuar con la lectura json

    public int obtenerDatos_json(String response){

        int res = 0;

        int largo;

      /*  int Id_tarea;
        String id_tipo;
        String latitud;
        String longitud;
        String fecha;
        String realizada;
        String observacion;
        String id_trabajador;*/

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
                    tarea.put("Id_tarea", objeto.getInt("Id_tarea"));
                    tarea.put("id_tipo", objeto.getString("id_tipo"));
                    tarea.put("latitud", objeto.getString("latitud"));
                    tarea.put("longitud", objeto.getString("longitud"));
                    tarea.put("fecha", objeto.getString("fecha"));
                    tarea.put("realizada", objeto.getString("realizada"));
                    tarea.put("observacion", objeto.getString("observacion"));
                    tarea.put("id_trabajador", objeto.getString("id_trabajador"));

                    long insertar = bd.insert("L_tareas", null, tarea);
                    res = 1;

                }
            }

            bd.close();

        }catch (Exception e){

        }
        return res;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String valor = (String) parent.getItemAtPosition(position);

        Intent nuevo_formulario = new Intent(Lista.this, Formulario.class);
        nuevo_formulario.putExtra("tarea", valor);
        startActivity(nuevo_formulario);

    }

    //______________________________________________________________________________________________

    @Override
    public void onBackPressed() {

        Intent lis = new Intent(getApplicationContext(), Menu.class);
        startActivity(lis);
    }



}
