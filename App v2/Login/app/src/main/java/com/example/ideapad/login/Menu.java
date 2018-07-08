package com.example.ideapad.login;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.util.AsyncListUtil;
import android.view.View;
import android.widget.Button;
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

public class Menu extends AppCompatActivity {

    //private Button Lista_tareas;
    private String IP = "192.168.0.9";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (isOnline() != false){

            vaciar_predios();
            vaciar_tareas();
            vaciar_ubicaciones();
            vaciar_cuarteles();
            vaciar_puntos_cuartel();
            vaciar_ubicacion_sensor();
            vaciar_microcontrolador();
            cargar_tareas();
            cargar_ubicaciones();
            cargar_cuartel();
            cargar_puntos_cuartel();
            cargar_predio();
            cargar_ubicacion_sensor();
            cargar_microcontrolador();


        }else{

            //consulta_predio();

        }

        Button boton_TM = (Button) findViewById(R.id.lista_tareasM);
        boton_TM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(), "Cargando.......", Toast.LENGTH_LONG).show();

                Intent lis = new Intent(getApplicationContext(), Lista_predio.class);
                startActivity(lis);
            }
        });


        Button boton_TS = (Button) findViewById(R.id.lista_tareasS);
        boton_TS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(), "Cargando.......", Toast.LENGTH_LONG).show();

                Intent lis = new Intent(getApplicationContext(), Lista_predioTS.class);
                startActivity(lis);

            }
        });

        Button boton_F = (Button) findViewById(R.id.fijar_sensor);
        boton_F.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Toast.makeText(getApplicationContext(), "Cargando.......", Toast.LENGTH_LONG).show();

                Intent lis = new Intent(getApplicationContext(), Lista_datos_sensores.class);
                startActivity(lis);

                //vaciar_lectura();



            }
        });


        Button boton_cerrarS = (Button) findViewById(R.id.C_sesion);
        boton_cerrarS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Thread tr = new Thread() {

                    @Override
                    public void run() {

                       Eliminar_sesion();
                        onBackPressed();

                    }
                };

                tr.start();

            }
        });
    }

    //------------------------------------vaciar contenido---------------------------------------------

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

    private void vaciar_ubicacion_sensor() {

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        bd.execSQL("DELETE FROM L_ubicacion_sensor");
        bd.close();

    }

    private void vaciar_microcontrolador(){

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        bd.execSQL("DELETE FROM L_microcontrolador");
        bd.close();

    }

    //----------------------------------------------------------------------------------------------
    //-----------------------------cargar contenido-------------------------------------------------

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

                            //consulta_predio();

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

    private void cargar_ubicacion_sensor(){

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);

        SQLiteDatabase bd = admin.getWritableDatabase();

        Thread tr = new Thread() {

            @Override
            public void run() {

                final String resultado = buscar_ubicacion_sensor();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        int r = obtenerjson_ubicacion_sensor(resultado);
                        if (r > 0) {

                            //consulta_predio();

                        }
                    }
                });
            }
        };
        tr.start();

    }

    private void cargar_microcontrolador(){

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);

        SQLiteDatabase bd = admin.getWritableDatabase();

        Thread tr = new Thread() {

            @Override
            public void run() {

                final String resultado = buscar_microcontrolador();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        int r = obtenerjson_microcontrolador(resultado);
                        if (r > 0) {

                            //consulta_predio();

                        }
                    }
                });
            }
        };
        tr.start();

    }
    //----------------------------------------------------------------------------------------------
    //--------------------------------convertir contenido y guardar---------------------------------

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

    public int obtenerjson_ubicacion_sensor(String response){


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
                    tarea.put("Id_us", objeto.getInt("Id_us"));
                    tarea.put("id_tarea", objeto.getString("id_tarea"));
                    tarea.put("id_placa", objeto.getString("id_placa"));
                    tarea.put("latitud", objeto.getString("latitud"));
                    tarea.put("longitud", objeto.getString("longitud"));
                    tarea.put("observacion", objeto.getString("observacion"));
                    tarea.put("realizada", objeto.getString("realizada"));


                    long insertar = bd.insert("L_ubicacion_sensor", null, tarea);
                    res = 1;

                }
            }

            bd.close();

        }catch (Exception e){

        }
        return res;

    }

    public int obtenerjson_microcontrolador(String response){

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
                    tarea.put("Id_placa", objeto.getInt("Id_placa"));
                    tarea.put("nombre_ap", objeto.getString("nombre_ap"));
                    tarea.put("contrasena", objeto.getString("contrasena"));

                    long insertar = bd.insert("L_microcontrolador", null, tarea);
                    res = 1;

                }
            }

            bd.close();

        }catch (Exception e){

        }
        return res;
    }
    //----------------------------------------------------------------------------------------------
    //----------------------------------buscar contenido en el servidor-----------------------------

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

    public String buscar_ubicacion_sensor(){

        URL url = null;
        String linea = "";
        int respuesta = 0;
        StringBuilder resul=null;

        try {

            url = new URL("http://" + IP + "/WebServices/obtener_ubicacion_sensor.php");

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

    public String buscar_microcontrolador(){

        URL url = null;
        String linea = "";
        int respuesta = 0;
        StringBuilder resul=null;

        try {

            url = new URL("http://" + IP + "/WebServices/obtener_microcontrolador.php");

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

    //----------------------------------------------------------------------------------------------
    //--------------------------------otras funciones-----------------------------------------------

    @Override
    public void onBackPressed() {

        Intent lis = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(lis);
    }

    private void Eliminar_sesion() {

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        bd.execSQL("delete from L_sesion");

        bd.close();

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

                    } catch (java.net.SocketTimeoutException e) {

                        return false;

                    } catch (IOException e) {

                        return false;
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





}
