package com.example.ideapad.login;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import android.database.Cursor;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements OnClickListener {

    // UI references.
    private EditText Rut_t;
    private EditText Contrasena_t;
    private Button Ingresar_t;
    private String IP = "192.168.0.9";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        //da inicio a la tarea asincrona que se encarga de verificar y mandar los datos en el dispositivo
        time time = new time();
        time.execute();

        // agrega los campos del formulario a la activity
        Rut_t = (AutoCompleteTextView) findViewById(R.id.rut_t);
        Contrasena_t = (EditText) findViewById(R.id.contrasena_t);
        Ingresar_t = (Button) findViewById(R.id.ingresar);

        //agrega un evento al boton ingresar
        Ingresar_t.setOnClickListener(this);

        //------------------------------------------------------------------------------------------

        //verifica si hay un usuario loggeado en la aplicacion para redirigirlo al menu principal
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        Cursor fila = bd.rawQuery("select * from L_sesion", null);

        if (fila.moveToFirst()) {

            if (fila.getString(0) != null) {

                Intent lis = new Intent(getApplicationContext(), Menu.class);
                startActivity(lis);

            }
        }
        bd.close();

    }

    //----------------------------------------------------------------------------------------------

    //se encarga de validar los datos del usuario

    @Override
    public void onClick(View v) {

        final AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);

        if (isOnline() != false) {

            Thread tr = new Thread() {

                @Override
                public void run() {

                    final String resultado = enviardatos_GET(Rut_t.getText().toString(), Contrasena_t.getText().toString());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int r = obtenerDatos(resultado);
                            if (r > 0) {

                                Intent i = new Intent(getApplicationContext(), Menu.class);
                                SQLiteDatabase bd = admin.getWritableDatabase();
                                String sesion_rut = Rut_t.getText().toString();
                                ContentValues sesion = new ContentValues();
                                sesion.put("L_rut", sesion_rut);
                                long ind_ses = bd.insert("L_sesion", null, sesion);

                                if (ind_ses == 1) {

                                    //Toast.makeText(getApplicationContext(), "insertado", Toast.LENGTH_LONG).show();
                                } else {

                                    //Toast.makeText(getApplicationContext(), "no insertado", Toast.LENGTH_LONG).show();
                                }

                                bd.close();

                                startActivity(i);

                            } else {

                                Toast.makeText(getApplicationContext(), "Usuario o Password Incorrectos", Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                }
            };

            Toast.makeText(getApplicationContext(), "Cargando.......", Toast.LENGTH_LONG).show();
            tr.start();

        } else {

            Toast.makeText(getApplicationContext(), "no hay internet ", Toast.LENGTH_SHORT).show();

        }


    }

    //----------------------------------------------------------------------------------------------


    public String enviardatos_GET(String Rut_t, String Contrasena_t) {

        URL url = null;
        String linea = "";
        int respuesta = 0;
        StringBuilder resul = null;

        try {
            url = new URL("http://" + IP + "/WebServices/validar_user.php?user_v=" + Rut_t + "&password_v=" + Contrasena_t);

            HttpURLConnection conection = (HttpURLConnection) url.openConnection();

            respuesta = conection.getResponseCode();

            resul = new StringBuilder();

            if (respuesta == HttpURLConnection.HTTP_OK) {

                InputStream in = new BufferedInputStream(conection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                while ((linea = reader.readLine()) != null) {

                    resul.append(linea);

                }

            }

        } catch (Exception e) {
        }


        return resul.toString();

    }

    public int obtenerDatos(String response) {

        int res = 0;

        try {

            JSONArray json = new JSONArray(response);
            if (json.length() > 0) {
                res = 1;
            }

        } catch (Exception e) {

        }
        return res;
    }

    private void eliminar_dato(String id) {


        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();


        int cant = bd.delete("L_datoM", "Id=" + id, null);

        bd.close();

    }

    private void consulta_tareas() {

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);

        final SQLiteDatabase bd = admin.getWritableDatabase();

        final Cursor fila = bd.rawQuery("select * from L_datoM", null);

        if (fila.moveToFirst()) {

            if (fila.getString(0) != null) {

                Thread tr = new Thread() {

                    @Override
                    public void run() {

                        for (fila.moveToFirst(); !fila.isAfterLast(); fila.moveToNext()) {

                            final String resultado = intertar_datos(fila.getString(1), fila.getString(2), fila.getString(3), fila.getString(4), fila.getString(5));

                            eliminar_dato(fila.getString(0));

                        }

                    }
                };

                tr.start();

            } else {

                Toast.makeText(LoginActivity.this, "No Hay Datos manuales para subir", Toast.LENGTH_SHORT).show();

            }

        }

        bd.close();

    }

    public String intertar_datos(String id_tarea, String latitud, String longitud, String n_fruta, String t_fruta) {

        URL url = null;
        String linea = "";
        int respuesta = 0;
        StringBuilder resul = null;

        try {
            url = new URL("http://" + IP + "/WebServices/insertar_datosM.php?id_tarea=" + id_tarea + "&latitud=" +
                    latitud + "&longitud=" + longitud + "&n_fruta=" + n_fruta + "&t_fruta=" + t_fruta);
            HttpURLConnection conection = null;
            conection = (HttpURLConnection) url.openConnection();
            respuesta = conection.getResponseCode();

            resul = new StringBuilder();

            if (respuesta == HttpURLConnection.HTTP_OK) {

                InputStream in = new BufferedInputStream(conection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                while ((linea = reader.readLine()) != null) {

                    resul.append(linea);

                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return resul.toString();

    }


    //----------------------------------------------------------------------------------------------

    //consulta si hay conexion con el servidor principal

    public boolean isOnline() {

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

        RunnableFuture<Boolean> futureRun = new FutureTask<Boolean>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if ((networkInfo.isAvailable()) && (networkInfo.isConnected())) {
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

    //---------------------------------------------------------------------------------------------

    //tarea asyncrona

    public void hilo() {

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void ejecutar() {

        time time = new time();
        time.execute();

    }

    public class time extends AsyncTask<Void, Integer, Boolean> {


        @Override
        protected Boolean doInBackground(Void... params) {

            for (int i = 1; i < 9; i++) {

                hilo();

            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            ejecutar();

            if (isOnline() != false) {

                consulta_tareas();
                consultaT_S();

            } else {

                Toast.makeText(LoginActivity.this, "No hay conexion con el Servidor", Toast.LENGTH_SHORT).show();

            }


        }
    }


    //----------------------------------------------------------------------------------------------

    //datos del sensor
    private void consultaT_S() {

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);

        final SQLiteDatabase bd = admin.getWritableDatabase();

        final Cursor fila = bd.rawQuery("select * from L_datoS", null);

        if (fila.moveToFirst()) {

            if (fila.getString(0) != null) {

                Thread tr = new Thread() {

                    @Override
                    public void run() {

                        for (fila.moveToFirst(); !fila.isAfterLast(); fila.moveToNext()) {

                            final String resultado = intertar_datos_S(fila.getInt(0), fila.getString(1), fila.getString(2), fila.getString(3), fila.getString(4), fila.getString(5), fila.getString(6));

                            eliminar_datoS(fila.getInt(0));

                        }

                    }
                };

                tr.start();



            } else {

                Toast.makeText(LoginActivity.this, "No Hay Datos de sensores para Subir", Toast.LENGTH_SHORT).show();

            }

        }

        bd.close();

    }



    public String intertar_datos_S(int Id, String id_tarea, String sensor, String valor_humedad, String latitud, String longitud, String fecha) {

        URL url = null;
        String linea = "";
        int respuesta = 0;
        StringBuilder resul = null;

        try {
            url = new URL("http://" + IP + "/WebServices/insertar_datosS.php?Id=" + Id + "&id_tarea=" +
                    id_tarea + "&sensor=" + sensor + "&valor_humedad=" + valor_humedad + "&latitud=" + latitud + "&longitud=" + longitud + "&fecha=" + fecha);
            HttpURLConnection conection = null;
            conection = (HttpURLConnection) url.openConnection();
            respuesta = conection.getResponseCode();

            resul = new StringBuilder();

            if (respuesta == HttpURLConnection.HTTP_OK) {

                InputStream in = new BufferedInputStream(conection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                while ((linea = reader.readLine()) != null) {

                    resul.append(linea);

                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return resul.toString();

    }

    private void eliminar_datoS(int id) {


        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        int cant = bd.delete("L_datoS", "Id=" + id, null);

        bd.close();

    }


}

