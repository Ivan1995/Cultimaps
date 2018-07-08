package com.example.ideapad.login;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.*;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.support.v7.widget.Toolbar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.List;


public class Mapa_ubicacionesT extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    final static int PERMISSION_ALL = 1;
    final static String[] PERMISSIONS = {android.Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};
    private GoogleMap mMap;
    MarkerOptions mo;
    Marker marker;
    LocationManager locationManager;
    int ID_tarea;
    String env_lat;
    String env_long;
    int ID_predio;
    ToggleButton autoZoom_tb;
    Button ver_cuartel;
    LatLng myCoordinates;
    int activar_zoom = 1;
    List<LatLng> lista_puntos = new ArrayList<>();
    LatLng centro;
    //ArrayList<String> lista_ubicaciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_ubicaciones_t);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Bundle parametro = getIntent().getExtras();
        ID_tarea = parametro.getInt("id_tarea");
        ID_predio = parametro.getInt("id_predio");

        autoZoom_tb= (ToggleButton)findViewById(R.id.AutoZoom_tb);
        autoZoom_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    activar_zoom = 1;
                } else {

                    activar_zoom = 0;
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(centro));
                    float zoomLevel = 18.0f; //This goes up to 21
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centro, zoomLevel));
                }
            }
        });

        buscar_cuartel();
        obtener_centro();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mo = new MarkerOptions().position(new LatLng(0, 0))
                .title("Mi Posición Actual")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.m))
                .zIndex(666);
        if (Build.VERSION.SDK_INT >= 23 && !isPermissionGranted()) {
            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        } else requestLocation();
        if (!isLocationEnabled())
            showAlert(1);

        Button boton_TM = (Button) findViewById(R.id.ok);
        boton_TM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int validar = validar_tareas();

                if(validar == 0){

                    actualizar_tareas();

                } else {

                    Intent lis = new Intent(getApplicationContext(), Lista_tareas_m.class);
                    lis.putExtra("id_predio",ID_predio);
                    startActivity(lis);

                }
            }
        });


    }

    private void obtener_centro(){
        centro = null;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(int i = 0 ; i < lista_puntos.size() ; i++)
        {
            builder.include(lista_puntos.get(i));
        }
        LatLngBounds bounds = builder.build();
        centro =  bounds.getCenter();
    }

    public void buscar_cuartel(){

        String cuartel = "";
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);

        SQLiteDatabase bd = admin.getWritableDatabase();

        Cursor fila = bd.rawQuery("select * from L_tareas where Id_tarea =" + ID_tarea, null);

        if(fila.moveToFirst()){

            for (fila.moveToFirst(); !fila.isAfterLast(); fila.moveToNext()) {

                cuartel = fila.getString(3);
                buscar_puntos(cuartel);

            }

        }

        bd.close();

    }

    public void buscar_puntos(String cuartel){


        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);

        SQLiteDatabase bd = admin.getWritableDatabase();

        Cursor fila = bd.rawQuery("select * from L_punto_cuartel where id_cuartel =" + cuartel, null);

        if(fila.moveToFirst()){

            for (fila.moveToFirst(); !fila.isAfterLast(); fila.moveToNext()) {

                lista_puntos.add(new LatLng(Double.valueOf(fila.getString(2)), Double.valueOf(fila.getString(3))));

            }

        }

        bd.close();

    }


    public int validar_tareas(){

        int validar = 0;
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        Cursor fila = bd.rawQuery("select * from L_ubicacion_tarea where realizada = 0 and id_tarea = " + ID_tarea, null);

        if (fila.moveToFirst()) {

            if (fila.getString(0) != null) {

                validar = 1;

            }
        }

        bd.close();

        return validar;

    }

    public void actualizar_tareas(){

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("realizada","1");

        int update = bd.update("L_tareas",valores,"Id_tarea=" + ID_tarea,null);

        if (update == 1)
            Toast.makeText(this, "se ingresaron Correctamente los Datos", Toast.LENGTH_SHORT).show();

        Intent lis = new Intent(getApplicationContext(), Lista_tareas_m.class);
        lis.putExtra("id_predio",ID_predio);
        startActivity(lis);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        marker = mMap.addMarker(mo);
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        Polygon cuartel = mMap.addPolygon(new PolygonOptions()
                .addAll(lista_puntos)
                .strokeColor(Color.RED)
                .fillColor(Color.BLUE));

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);

        SQLiteDatabase bd = admin.getWritableDatabase();

        Cursor fila = bd.rawQuery("select * from L_ubicacion_tarea where id_tarea = " + ID_tarea, null);

        if(fila.moveToFirst()){

            for (fila.moveToFirst(); !fila.isAfterLast(); fila.moveToNext()) {

                double latitud = Double.parseDouble(fila.getString(2));
                double longitud = Double.parseDouble(fila.getString(3));

                LatLng Ubicacion = new LatLng(latitud, longitud);

                if(fila.getInt(6) == 0) {
                    mMap.addMarker(new MarkerOptions()
                            .position(Ubicacion)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.markerno))
                            .title("Tarea N°: " + fila.getString(0))
                            .snippet("Observación: " + fila.getString(7))
                            .zIndex(fila.getInt(0)));
                } else {

                    mMap.addMarker(new MarkerOptions()
                            .position(Ubicacion)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.markerok))
                            .zIndex(fila.getInt(0)));

                }
            }
        }

        bd.close();

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                float index = marker.getZIndex();
                String snippet = marker.getSnippet();
                int b = (int)Math.round(index);

                if (index != 666) {

                    Intent lis = new Intent(getApplicationContext(), Form_tarea.class);
                    lis.putExtra("titulo", marker.getTitle());
                    lis.putExtra("observacion", snippet);
                    lis.putExtra("marker", b);
                    lis.putExtra("id_tarea", ID_tarea);
                    lis.putExtra("id_predio",ID_predio);
                    startActivity(lis);
                    //Toast.makeText(getApplicationContext(),"hola   " +  b, Toast.LENGTH_LONG).show();
                } else {

                    Toast.makeText(getApplicationContext(), "Mi Posición", Toast.LENGTH_LONG).show();

                }
            }
        });
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

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
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
    public void onLocationChanged(Location location) {


        myCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
        marker.setPosition(myCoordinates);

        if (activar_zoom == 1){

            mMap.moveCamera(CameraUpdateFactory.newLatLng(myCoordinates));
            float zoomLevel = 18.0f; //This goes up to 21
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myCoordinates, zoomLevel));

        }

        env_lat = String.valueOf(location.getLatitude());
        env_long = String.valueOf(location.getLongitude());

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

    @Override
    public void onBackPressed() {

    }

}