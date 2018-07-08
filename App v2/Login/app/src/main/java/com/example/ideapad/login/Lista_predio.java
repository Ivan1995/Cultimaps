package com.example.ideapad.login;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

public class Lista_predio extends AppCompatActivity implements ListView.OnItemClickListener {

    private String IP = "192.168.0.9";
    ArrayList<String> lista_informacion;
    ArrayList<Integer> Id_predio;
    ListView listaview_tareas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_predio);

        listaview_tareas = (ListView) findViewById(R.id.lista);

      /*  if (isOnline() != false){

            vaciar_predios();
            vaciar_tareas();
            vaciar_ubicaciones();
            vaciar_cuarteles();
            vaciar_puntos_cuartel();
            cargar_tareas();
            cargar_ubicaciones();
            cargar_cuartel();
            cargar_puntos_cuartel();
            cargar_predio();

        }else{

            consulta_predio();

        }*/

        consulta_predio();

    }

    private void vaciar_predios() {

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        bd.execSQL("DELETE FROM L_predio");
        bd.close();

    }

    private void vaciar_tareas() {

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        bd.execSQL("DELETE FROM L_tareas");
        bd.close();

    }

    private void vaciar_ubicaciones() {

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        bd.execSQL("DELETE FROM L_ubicacion_tarea");
        bd.close();

    }

    private void vaciar_cuarteles(){

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        bd.execSQL("DELETE FROM L_cuartel");
        bd.close();

    }

    private void vaciar_puntos_cuartel(){

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        bd.execSQL("DELETE FROM L_punto_cuartel");
        bd.close();

    }

    //----------------------------------------------------------------------------------------------------

    private void cargar_predio() {

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);

        SQLiteDatabase bd = admin.getWritableDatabase();

        Thread tr = new Thread() {

            @Override
            public void run() {

                final String resultado = buscar_predio();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        int r = obtenerjson_predio(resultado);
                        if (r > 0) {

                            consulta_predio();

                        }
                    }
                });
            }
        };
        tr.start();
    }



    private void cargar_tareas() {

        final AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        final SQLiteDatabase bd = admin.getWritableDatabase();

        Cursor fila = bd.rawQuery("select * from L_sesion", null);

        if(fila.moveToFirst()) {
            if (fila.getString(0) != null) {

                final String us = fila.getString(0);

                Thread tr = new Thread() {

                    @Override
                    public void run() {

                        final String resultado = buscar_tareas(us);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                SQLiteDatabase bd = admin.getWritableDatabase();

                                int r = obtenerjson_tareas(resultado);
                                if (r > 0) {

                                    //consulta_tareas();

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

    private void cargar_ubicaciones() {

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);

        SQLiteDatabase bd = admin.getWritableDatabase();

        Thread tr = new Thread() {

            @Override
            public void run() {

                final String resultado = buscar_ubicaciones();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        int r = obtenerjson_ubicaciones(resultado);
                        if (r > 0) {

                            //consulta_predio();

                        }
                    }
                });
            }
        };
        tr.start();
    }

    private void cargar_cuartel(){

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);

        SQLiteDatabase bd = admin.getWritableDatabase();

        Thread tr = new Thread() {

            @Override
            public void run() {

                final String resultado = buscar_cuartel();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        int r = obtenerjson_cuarteles(resultado);
                        if (r > 0) {

                            //consulta_predio();

                        }
                    }
                });
            }
        };
        tr.start();

    }

    private void cargar_puntos_cuartel(){

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);

        SQLiteDatabase bd = admin.getWritableDatabase();

        Thread tr = new Thread() {

            @Override
            public void run() {

                final String resultado = buscar_puntos_cuartel();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        int r = obtenerjson_puntos_cuarteles(resultado);
                        if (r > 0) {

                            //consulta_predio();

                        }
                    }
                });
            }
        };
        tr.start();


    }


    //--------------------------------------------------------------------------------------------------

    public int obtenerjson_predio(String response){

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
                    predio.put("Id_predio", objeto.getInt("Id_predio"));
                    predio.put("nombre_predio", objeto.getString("nombre_predio"));

                    long insertar = bd.insert("L_predio", null, predio);
                    res = 1;

                }
            }

            bd.close();

        }catch (Exception e){

        }
        return res;
    }

    public int obtenerjson_tareas(String response){

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
                    tarea.put("id_predio", objeto.getString("id_predio"));
                    tarea.put("id_cuartel", objeto.getString("id_cuartel"));
                    tarea.put("fecha_inicio", objeto.getString("fecha_inicio"));
                    tarea.put("fecha_termino", objeto.getString("fecha_termino"));
                    tarea.put("realizada", objeto.getString("realizada"));
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

    public int obtenerjson_ubicaciones(String response){

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
                    tarea.put("Id_ubt", objeto.getInt("Id_ubt"));
                    tarea.put("id_tarea", objeto.getString("id_tarea"));
                    tarea.put("latitud", objeto.getString("latitud"));
                    tarea.put("longitud", objeto.getString("longitud"));
                    tarea.put("n_fruta", objeto.getString("n_fruta"));
                    tarea.put("t_fruta", objeto.getString("t_fruta"));
                    tarea.put("realizada", objeto.getString("realizada"));
                    tarea.put("observacion", objeto.getString("observacion"));



                    long insertar = bd.insert("L_ubicacion_tarea", null, tarea);
                    res = 1;

                }
            }

            bd.close();

        }catch (Exception e){

        }
        return res;
    }

    public int obtenerjson_cuarteles(String response){


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
                    tarea.put("Id_cuartel", objeto.getInt("Id_cuartel"));
                    tarea.put("id_predio", objeto.getString("id_predio"));
                    tarea.put("id_tipo_cultivo", objeto.getString("id_tipo_cultivo"));

                    long insertar = bd.insert("L_cuartel", null, tarea);
                    res = 1;

                }
            }

            bd.close();

        }catch (Exception e){

        }
        return res;

    }

    public int obtenerjson_puntos_cuarteles(String response){


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
                    tarea.put("Id_punto", objeto.getInt("Id_punto"));
                    tarea.put("id_cuartel", objeto.getString("id_cuartel"));
                    tarea.put("latitud", objeto.getString("latitud"));
                    tarea.put("longitud", objeto.getString("longitud"));

                    long insertar = bd.insert("L_punto_cuartel", null, tarea);
                    res = 1;

                }
            }

            bd.close();

        }catch (Exception e){

        }
        return res;

    }

    //-----------------------------------------------------------------------------------

    public String buscar_predio(){

        URL url = null;
        String linea = "";
        int respuesta = 0;
        StringBuilder resul=null;

        try {

            url = new URL("http://" + IP + "/WebServices/obtener_predios.php");

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


    public String buscar_tareas(String id_trabajador_l){

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

    public String buscar_ubicaciones(){

        URL url = null;
        String linea = "";
        int respuesta = 0;
        StringBuilder resul=null;

        try {

            url = new URL("http://" + IP + "/WebServices/obtener_ubicaciones.php");

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

    public String buscar_cuartel(){

        URL url = null;
        String linea = "";
        int respuesta = 0;
        StringBuilder resul=null;

        try {

            url = new URL("http://" + IP + "/WebServices/obtener_cuarteles.php");

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

    public String buscar_puntos_cuartel(){

        URL url = null;
        String linea = "";
        int respuesta = 0;
        StringBuilder resul=null;

        try {

            url = new URL("http://" + IP + "/WebServices/obtener_puntos.php");

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


    private void consulta_predio() {

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);

        SQLiteDatabase bd = admin.getWritableDatabase();

        lista_informacion = new ArrayList<String>();
        Id_predio = new ArrayList<Integer>();

        Cursor fila = bd.rawQuery("select * from L_predio", null);

        if(fila.moveToFirst()){

            for (fila.moveToFirst(); !fila.isAfterLast(); fila.moveToNext()) {

                Id_predio.add(fila.getInt(0));
                lista_informacion.add("Predio : " + fila.getString(1));
            }

        }

        bd.close();

        ArrayAdapter adaptador = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,lista_informacion);
        listaview_tareas.setOnItemClickListener(this);
        listaview_tareas.setAdapter(adaptador);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        for(int i=0;i<Id_predio.size();i++)
        {
            if(position == i)
            {
                int id_predio = Id_predio.get(i);

                    Intent lista_T = new Intent(this, Lista_tareas_m.class);
                    lista_T.putExtra("id_predio", id_predio);
                    startActivity(lista_T);

            }

        }
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

    @Override
    public void onBackPressed() {

        Intent lis = new Intent(getApplicationContext(), Menu.class);
        startActivity(lis);
    }

}
