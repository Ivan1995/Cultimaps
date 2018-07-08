package com.example.ideapad.login;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Formulario extends AppCompatActivity implements LocationListener, View.OnClickListener {

    /*

    No esta en funcionamiento

     */
    private TextView labelform;
    private TextView txt_latitud;
    private TextView txt_longitud;
    private EditText txt_nfruta;
    private EditText txt_tfruta;
    private Button btn_cancelar;
    private Button btn_guardar;
    private String nombre_imagen;
    final static int cons = 0;
    String Lb_latitud;
    String Lb_longitud;
    LocationManager locationManager = null;
    final static int PERMISSION_ALL = 1;
    final static String[] PERMISSIONS = {android.Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        txt_latitud = (TextView) findViewById(R.id.latitud);
        txt_longitud = (TextView) findViewById(R.id.longitud);
        btn_guardar = (Button) findViewById(R.id.btn_guardar);
        btn_cancelar = (Button) findViewById(R.id.btn_cancelar);
        txt_nfruta = (EditText) findViewById(R.id.n_fruto);
        txt_tfruta = (EditText) findViewById(R.id.t_fruto);

        btn_guardar.setOnClickListener(this);
        btn_cancelar.setOnClickListener(this);


        Button boton = (Button) findViewById(R.id.localizar);
        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                txt_latitud.setText(Lb_latitud);
                txt_longitud.setText(Lb_longitud);

            }
        });

        if (Build.VERSION.SDK_INT >= 23 && !isPermissionGranted()) {
            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        } else requestLocation();
        if (!isLocationEnabled())
            showAlert(1);

        labelform = (TextView) findViewById(R.id.labelform);
        Bundle parametro = getIntent().getExtras();

        if (parametro != null){

            String string = parametro.getString("tarea");
            String[] parts = string.split("  F");
            String part1 = parts[0];
            //String part2 = parts[1];

            labelform.setText(part1);

        }
    }



    @Override
    public void onLocationChanged(Location location) {

        //LatLng myCoordinates = new LatLng(location.getLatitude(), location.getLongitude());

        Lb_latitud = String.valueOf(location.getLatitude());
        Lb_longitud = String.valueOf(location.getLongitude());

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }



    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }



    private void requestLocation() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        String provider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //controlar proveedor-tiempo de actualizacio-cantidad de metros entre actualizacion
        locationManager.requestLocationUpdates(provider, 2000, 1, this);
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isPermissionGranted() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v("mylog", "Permission is granted");
            return true;
        } else {
            Log.v("mylog", "Permission not granted");
            return false;
        }
    }



    private void showAlert(final int status) {
        String message, title, btnText;
        if (status == 1) {
            message = "El servicio de geolocalización se encuentra desactivado.\nPor favor, active la geolocalización " +
                    "para usar los mapas";
            title = "Activar Geolocalización del dispositivo";
            btnText = "Configuración de ubicaciones";
        } else {
            message = "Please allow this app to access location!";
            title = "Permission access";
            btnText = "Grant";
        }
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle(title)
                .setMessage(message)
                .setPositiveButton(btnText, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        if (status == 1) {
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                        } else
                            requestPermissions(PERMISSIONS, PERMISSION_ALL);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        finish();
                    }
                });
        dialog.show();
    }

    @Override
    public void onClick(View v) {

        int id;
        id = v.getId();

        String idf = (String) labelform.getText();

        String[] parts = idf.split(": ");
        String part1 = parts[0];
        nombre_imagen = parts[1];

        switch (id){

            case R.id.btn_cancelar:
                Intent lis = new Intent(getApplicationContext(), Lista.class);
                startActivity(lis);
                break;

            case R.id.btn_guardar:

                validar_formulario();

                break;

        }

    }

    private void validar_formulario() {

        String txt_lat;
        String txt_long;
        String txt_nFru;
        String txt_tFru;
        int img_muestra;

        txt_lat = (String) txt_latitud.getText();
        txt_long = (String) txt_longitud.getText();
        txt_nFru = txt_nfruta.getText().toString();
        txt_tFru = txt_tfruta.getText().toString();
        img_muestra = Integer.parseInt(nombre_imagen);

        if(txt_lat.trim().equals("")) {

            Toast.makeText(getApplicationContext(), "Ubicación no Encontrada", Toast.LENGTH_SHORT).show();

        }else if(txt_nFru.trim().equals("")){

            Toast.makeText(getApplicationContext(), "Ingrese Numero de Frutas", Toast.LENGTH_SHORT).show();


        }else if(txt_tFru.trim().equals("")){

            Toast.makeText(getApplicationContext(), "Ingrese Tamaño de Fruta", Toast.LENGTH_SHORT).show();


        }else{

            guardar_datos(txt_lat, txt_long, txt_tFru, txt_nFru, img_muestra);
        }
    }

    private void guardar_datos(String txt_lat, String txt_long, String txt_tFru, String txt_nFru, int img_muestra) {

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        ContentValues form = new ContentValues();
        form.put("Id", img_muestra);
        form.put("id_tarea", img_muestra);
        form.put("latitud", txt_lat);
        form.put("longitud", txt_long);
        form.put("n_fruta", txt_nFru);
        form.put("t_fruta",txt_tFru);

        long insertar = bd.insert("L_datoM", null, form);

        Toast.makeText(getApplicationContext(), "Listo", Toast.LENGTH_SHORT).show();
        bd.close();

        actualizar_tarea(img_muestra);
    }

    private void actualizar_tarea(int img_muestra) {


        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("realizada","1");

        int update = bd.update("L_tareas",valores,"Id_tarea=" + String.valueOf(img_muestra),null);

        if (update == 1)
            Toast.makeText(this, "se ingresaron Correctamente los Datos", Toast.LENGTH_SHORT)
                    .show();

    Intent lis = new Intent(getApplicationContext(), Lista.class);
        startActivity(lis);

    }


}
