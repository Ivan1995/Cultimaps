package com.example.ideapad.login;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class Lista_predioTS extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ArrayList<String> lista_informacion;
    ArrayList<Integer> Id_predio;
    ListView listaview_tareas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_predio_ts);
        listaview_tareas = (ListView) findViewById(R.id.lista_pts);

        consulta_predio();
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

                Intent lista_T = new Intent(this, Lista_tareaS.class);
                lista_T.putExtra("id_predio", id_predio);
                startActivity(lista_T);

            }

        }
    }
    @Override
    public void onBackPressed() {

        Intent lis = new Intent(getApplicationContext(), Menu.class);
        startActivity(lis);
    }
}
