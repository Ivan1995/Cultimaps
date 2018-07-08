package com.example.ideapad.login;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class Lista_datos_manuales extends AppCompatActivity implements AdapterView.OnItemClickListener {

        /*

    No esta en funcionamiento

     */

    ListView listaview_DM;

    ArrayList<String> lista_informacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_datos_manuales);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        listaview_DM = (ListView) findViewById(R.id.lista_DM);

        consulta_tareas();

    }

    private void consulta_tareas() {

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);

        SQLiteDatabase bd = admin.getWritableDatabase();

        //TareaC tarea = null;

        //lista_tareas = new ArrayList<String>();
        lista_informacion = new ArrayList<String>();

        Cursor fila = bd.rawQuery("select * from L_datoM", null);

        if(fila.moveToFirst()){

            for (fila.moveToFirst(); !fila.isAfterLast(); fila.moveToNext()) {
                // do what you need with the cursor here

                //Toast.makeText(getApplicationContext(), fila.getString(0) + " - " + fila.getString(4), Toast.LENGTH_LONG).show();

                lista_informacion.add("Datos de la Tarea N°: " + fila.getString(0));

            }

        }

        bd.close();

        ArrayAdapter adaptador = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,lista_informacion);
        listaview_DM.setOnItemClickListener(this);
        listaview_DM.setAdapter(adaptador);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

       /* String valor = (String) parent.getItemAtPosition(position);

        Intent nuevo_formulario = new Intent(Lista_datos_manuales.this, Formulario.class);
        nuevo_formulario.putExtra("tarea", valor);
        startActivity(nuevo_formulario);*/


    }

    @Override
    public void onBackPressed() {

        Intent lis = new Intent(getApplicationContext(), Menu.class);
        startActivity(lis);
    }
}
