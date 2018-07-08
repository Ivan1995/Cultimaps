package com.example.ideapad.login;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Mapa_ubicacionesS extends FragmentActivity implements OnMapReadyCallback, LocationListener {

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
    List<LatLng> lista_markers = new ArrayList<>();
    LatLng centro;

    List<String> id_tar = new ArrayList<>();
    List<String> placa = new ArrayList<>();
    String idt;
    String idp;


    String networkSSID;
    String networkPass;
    WifiConfiguration conf = new WifiConfiguration();

    private static final String PROCESS_OK = "PROCESS_OK";
    private static final String PROCESS_ERROR = "PROCESS_ERROR";
    private ProgressDialog progressDialog;


     String id_ubT;
     String placaT;
     String SSIDT;
     String PASST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_ubicaciones_s);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
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

        Bundle parametro = getIntent().getExtras();
        ID_tarea = parametro.getInt("id_tarea");
        ID_predio = parametro.getInt("id_predio");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Procesando....");
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);

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

        buscar_cuartel();
        obtener_centro();

        Button boton_TM = (Button) findViewById(R.id.okS);
        boton_TM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent lis = new Intent(getApplicationContext(), Lista_tareaS.class);
                lis.putExtra("id_predio",ID_predio);
                startActivity(lis);

                int validar = validar_tareas();

                if(validar == 0){

                    actualizar_tareas();

                } else {

                    Intent list = new Intent(getApplicationContext(), Lista_tareaS.class);
                    list.putExtra("id_predio",ID_predio);
                    startActivity(list);

                }
            }
        });

    }

    public void actualizar_tareas(){

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("realizada","1");

        int update = bd.update("L_tareas",valores,"Id_tarea=" + ID_tarea,null);

        if (update == 1)
            Toast.makeText(this, "se ingresaron Correctamente los Datos", Toast.LENGTH_SHORT).show();

        Intent lis = new Intent(getApplicationContext(), Lista_tareaS.class);
        lis.putExtra("id_predio",ID_predio);
        startActivity(lis);

    }

    public int validar_tareas(){

        int validar = 0;
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        Cursor fila = bd.rawQuery("select * from L_ubicacion_sensor where realizada = 0 and id_tarea = " + ID_tarea, null);

        if (fila.moveToFirst()) {

            if (fila.getString(0) != null) {

                validar = 1;

            }
        }

        bd.close();

        return validar;

    }
    //----------------------------carga de poligono del cuartel-------------------------------------
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
        marker = mMap.addMarker(mo);
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        Polygon cuartel = mMap.addPolygon(new PolygonOptions()
                .addAll(lista_puntos)
                .strokeColor(Color.RED)
                .fillColor(Color.BLUE));

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);

        SQLiteDatabase bd = admin.getWritableDatabase();

        Cursor fila = bd.rawQuery("select * from L_ubicacion_sensor where id_tarea = " + ID_tarea, null);

        if(fila.moveToFirst()){

            for (fila.moveToFirst(); !fila.isAfterLast(); fila.moveToNext()) {

                double latitud = Double.parseDouble(fila.getString(3));
                double longitud = Double.parseDouble(fila.getString(4));
                String id_t = fila.getString(0);
                String id_placa = fila.getString(2);

                LatLng Ubicacion = new LatLng(latitud, longitud);
                id_tar.add(id_t);
                placa.add(id_placa);

                /*if(fila.getInt(6) == 0) {
                    lista_markers.add(new LatLng(latitud, longitud));
                }*/
                if(fila.getInt(6) == 0) {
                    mMap.addMarker(new MarkerOptions()
                            .position(Ubicacion)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.markerno))
                            .title("Tarea N°: " + fila.getString(0))
                            .snippet("RPi_" + fila.getString(2))
                            .zIndex(fila.getInt(0)));
                    lista_markers.add(new LatLng(latitud, longitud));
                } else {

                    mMap.addMarker(new MarkerOptions()
                            .position(Ubicacion)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.markerok))
                            .zIndex(fila.getInt(0)));

                }
            }
        }

        bd.close();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return true;
            }
        });

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

    //-------------------------- seguimeneto--------------------------------------------------------

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

        Location target = new Location("target");

        myCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
        marker.setPosition(myCoordinates);

        if (activar_zoom == 1){

            mMap.moveCamera(CameraUpdateFactory.newLatLng(myCoordinates));
            float zoomLevel = 18.0f; //This goes up to 21
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myCoordinates, zoomLevel));

        }

        env_lat = String.valueOf(location.getLatitude());
        env_long = String.valueOf(location.getLongitude());

        for(int i = 0 ; i < lista_markers.size() ; i++) {
            target.setLatitude(lista_markers.get(i).latitude);
            target.setLongitude(lista_markers.get(i).longitude);
            idt = id_tar.get(i);
            idp = placa.get(i);
            if(location.distanceTo(target) <= 100) {

                //Toast.makeText(getApplicationContext(), "llegue a un punto", Toast.LENGTH_LONG).show();

                AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
                dialogo1.setTitle("Confirmación para la conexión");
                dialogo1.setMessage("¿ Desea conectarse y guardar los datos de este dispositivo ?");
                dialogo1.setCancelable(false);
                dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo1, int id) {
                        conectar_sensor(idt, idp);
                    }
                });
                dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo1, int id) {
                        //cancelar();
                    }
                });
                dialogo1.show();
            }
        }

    }


    private class HardTask extends AsyncTask<String, Integer, String> {

        private Context context;

        public HardTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... params) {
            for (int i = 0; i <= 100; i++) {
                publishProgress(i);
                try {
                    // simulate hard work
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return PROCESS_ERROR;
                }
            }
            return PROCESS_OK;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            progressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if (PROCESS_OK.equals(result)) {
                Intent lis = new Intent(getApplicationContext(), Lista_fechas.class);
                lis.putExtra("id_tarea", ID_tarea);
                lis.putExtra("id_predio", ID_predio);
                lis.putExtra("id_ub", id_ubT);
                lis.putExtra("placa", placaT);
                lis.putExtra("SSID" , SSIDT);
                lis.putExtra("PASS", PASST);
                lis.putExtra("origen", 1);
                startActivity(lis);
                //Toast.makeText(context, "Process OK " + result, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Process ERROR " + result, Toast.LENGTH_LONG).show();
            }
        }
    }


    private void conectar_sensor(final String id_tr, final String id_pl) {

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        Cursor fila = bd.rawQuery("select * from L_microcontrolador where Id_placa =" + id_pl, null);

        if (fila.moveToFirst()) {

            if (fila.getString(0) != null) {

                networkSSID = fila.getString(1);
                networkPass = fila.getString(2);

                //Toast.makeText(getApplicationContext(), networkPass + "-" + networkSSID, Toast.LENGTH_LONG).show();

                conf.SSID = "\"" + networkSSID + "\"";
                conf.preSharedKey = "\""+ networkPass +"\"";

                WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                if ((wifiManager.isWifiEnabled() == false)) {
                    //Toast.makeText(this, "Conectando a WIFI...", Toast.LENGTH_LONG).show();
                    wifiManager.setWifiEnabled(true);
                }

                wifiManager.addNetwork(conf);

                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                for( WifiConfiguration i : list ) {
                    if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                        wifiManager.disconnect();
                        wifiManager.enableNetwork(i.networkId, true);
                        wifiManager.reconnect();
                        break;
                    }
                }

                id_ubT = id_tr;
                placaT = id_pl;
                SSIDT = networkSSID;
                PASST = networkPass;

                final HardTask downloadTask = new HardTask(this);
                downloadTask.execute("some_param");

                progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        downloadTask.cancel(true);
                    }
                });

            }
        }
        bd.close();

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
