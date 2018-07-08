package com.example.ideapad.login;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.util.AsyncListUtil;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Menu extends AppCompatActivity {

    //private Button Lista_tareas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        Button boton_TM = (Button) findViewById(R.id.lista_tareasM);
        boton_TM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(), "Cargando.......", Toast.LENGTH_LONG).show();

                Intent lis = new Intent(getApplicationContext(), Lista.class);
                startActivity(lis);
            }
        });


        Button boton_TS = (Button) findViewById(R.id.lista_tareasS);
        boton_TS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(), "Cargando.......", Toast.LENGTH_LONG).show();

                Intent lis = new Intent(getApplicationContext(), ListaT_sensor.class);
                startActivity(lis);

            }
        });

        Button boton_DM = (Button) findViewById(R.id.lista_M);
        boton_DM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent lis = new Intent(getApplicationContext(), Lista_datos_manuales.class);
                startActivity(lis);
            }
        });


        Button boton_DS = (Button) findViewById(R.id.lista_S);
        boton_DS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent lis = new Intent(getApplicationContext(), Lista_datos_sensor.class);
                startActivity(lis);

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





}
