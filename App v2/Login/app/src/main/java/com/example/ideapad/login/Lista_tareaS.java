package com.example.ideapad.login;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class Lista_tareaS extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ListView listaview_tareas;
    ArrayList<String> lista_informacion;
    ArrayList<Integer> realizada;
    ArrayList<Integer> id_tarea;
    int ID_predio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_tarea_s);

        Bundle parametro = getIntent().getExtras();
        ID_predio = parametro.getInt("id_predio");

        listaview_tareas = (ListView) findViewById(R.id.lista_ts);

        consulta_tareas();
    }

    private void consulta_tareas() {

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);

        SQLiteDatabase bd = admin.getWritableDatabase();

        lista_informacion = new ArrayList<String>();
        realizada = new ArrayList<Integer>();
        id_tarea = new ArrayList<Integer>();

        Cursor fila = bd.rawQuery("select * from L_tareas where id_tipo = 2 and id_predio = " + ID_predio, null);

        if(fila.moveToFirst()){

            for (fila.moveToFirst(); !fila.isAfterLast(); fila.moveToNext()) {

                lista_informacion.add("Tarea N°: " + fila.getString(0));
                id_tarea.add(fila.getInt(0));
                realizada.add(fila.getInt(6));

            }

        }

        bd.close();

        ArrayAdapter adaptador = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,lista_informacion){

            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                for(int i=0;i<realizada.size();i++)
                {
                    if(position == i)
                    {
                        if (realizada.get(i) == 1) {
                            // Set a background color for ListView regular row/item
                            view.setBackgroundColor(Color.parseColor("#004D40"));

                        } else {
                            // Set the background color for alternate row/item
                            view.setBackgroundColor(Color.parseColor("#B71C1C"));
                        }
                    }

                }
                return view;
            }
        };
        listaview_tareas.setOnItemClickListener(this);
        listaview_tareas.setAdapter(adaptador);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        for(int i=0;i<id_tarea.size();i++)
        {
            if(position == i)
            {
                int id_tarea_T = id_tarea.get(i);

                Intent lista_T = new Intent(this, Mapa_ubicacionesS.class);
                lista_T.putExtra("id_tarea", id_tarea_T);
                lista_T.putExtra("id_predio",ID_predio);
                startActivity(lista_T);

            }

        }
    }
    @Override
    public void onBackPressed() {

        Intent lis = new Intent(getApplicationContext(), Lista_predioTS.class);
        startActivity(lis);
    }
}
