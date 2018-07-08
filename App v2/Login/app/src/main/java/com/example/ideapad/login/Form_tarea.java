package com.example.ideapad.login;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Form_tarea extends AppCompatActivity implements View.OnClickListener {

    private TextView tarea_lb;
    private TextView observacion_lb;
    private EditText txt_nfruta;
    private EditText txt_tfruta;
    private Button btn_cancelar;
    private Button btn_guardar;
    private int marker_eliminado;
    private int ID_tarea;
    private int ID_predio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_tarea);

        btn_guardar = (Button) findViewById(R.id.btn_guardar);
        btn_cancelar = (Button) findViewById(R.id.btn_cancelar);
        txt_nfruta = (EditText) findViewById(R.id.n_fruto);
        txt_tfruta = (EditText) findViewById(R.id.t_fruto);
        tarea_lb = (TextView) findViewById(R.id.tarea_lb);
        observacion_lb = (TextView) findViewById(R.id.observacion_lb);

        btn_guardar.setOnClickListener(this);
        btn_cancelar.setOnClickListener(this);

        Bundle parametro = getIntent().getExtras();

        marker_eliminado = parametro.getInt("marker");
        tarea_lb.setText(parametro.getString("titulo"));
        observacion_lb.setText(parametro.getString("observacion"));
        ID_tarea = parametro.getInt("id_tarea");
        ID_predio = parametro.getInt("id_predio");


    }

    private void guardar_datos(String txt_tFru, String txt_nFru) {

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        ContentValues form = new ContentValues();
        form.put("Id_ubt", marker_eliminado);
        form.put("id_tarea", ID_tarea);
        form.put("n_fruta", txt_nFru);
        form.put("t_fruta",txt_tFru);

        long insertar = bd.insert("Task_tarea", null, form);

        Toast.makeText(getApplicationContext(), "Listo", Toast.LENGTH_SHORT).show();
        bd.close();


        actualizar_tarea();
    }

    private void actualizar_tarea() {


        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("realizada","1");

        int update = bd.update("L_ubicacion_tarea",valores,"Id_ubt=" + marker_eliminado,null);

        if (update == 1)
            Toast.makeText(this, "Se Ingresaron Correctamente los Datos", Toast.LENGTH_SHORT).show();

        Intent lista_T = new Intent(this, Mapa_ubicacionesT.class);
        lista_T.putExtra("id_tarea", ID_tarea);
        lista_T.putExtra("id_predio",ID_predio);
        startActivity(lista_T);

    }

    @Override
    public void onClick(View v) {

        int id;
        id = v.getId();

        String txt_nFru = txt_nfruta.getText().toString();
        String txt_tFru = txt_tfruta.getText().toString();

        switch (id){

            case R.id.btn_cancelar:

                onBackPressed();
                break;

            case R.id.btn_guardar:

                guardar_datos(txt_nFru,txt_tFru);
                break;

        }

    }
}

