package com.example.ideapad.login;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class Mapa_ubicacionS2 extends FragmentActivity implements OnMapReadyCallback, LocationListener {


        /*

    No esta en funcionamiento

     */

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_ubicacion_s2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle parametro = getIntent().getExtras();
        ID_tarea = parametro.getInt("id_tarea");
        ID_predio = parametro.getInt("id_predio");

        autoZoom_tb= (ToggleButton)findViewById(R.id.AutoZoom_tbS);
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

        Button boton_TM = (Button) findViewById(R.id.okS);
        boton_TM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent lis = new Intent(getApplicationContext(), Lista_tareaS.class);
                lis.putExtra("id_predio",ID_predio);
                startActivity(lis);

            /*    int validar = validar_tareas();

                if(validar == 0){

                    actualizar_tareas();

                } else {

                    Intent lis = new Intent(getApplicationContext(), Lista_tareas_m.class);
                    lis.putExtra("id_predio",ID_predio);
                    startActivity(lis);

                }*/
            }
        });
        buscar_cuartel();
        obtener_centro();
    }    //----------------------------carga de poligono del cuartel-------------------------------------
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
    //----------------------------------------------------------------------------------------------





    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onLocationChanged(Location location) {

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
}
