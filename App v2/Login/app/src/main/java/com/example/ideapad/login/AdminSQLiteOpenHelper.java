package com.example.ideapad.login;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by IdeaPad on 20-09-2017.
 */

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {
    public AdminSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table L_sesion (L_rut text)");
        db.execSQL("create table L_tareas (Id_tarea int primary key, id_tipo text,id_predio text, id_cuartel text, fecha_inicio text, fecha_termino text, realizada text, id_trabajador text)");

        db.execSQL("create table L_ubicacion_tarea (Id_ubt int primary key, id_tarea text, latitud text, longitud text, n_fruta text, t_fruta text, realizada text, observacion text)");
        db.execSQL("create table Task_tarea (Id_ubt int primary key,id_tarea text, n_fruta text, t_fruta text)");
        db.execSQL("create table L_predio (Id_predio int primary key, nombre_predio text)");
        //db.execSQL("create table L_datoM (Id int primary key, id_tarea text, latitud text, longitud text, n_fruta text, t_fruta text)");
        //db.execSQL("create table L_datoS (Id int primary key, id_tarea text, sensor text, valor_humedad text, latitud text, longitud text, fecha text)");
        db.execSQL("create table L_cuartel (Id_cuartel int primary key, id_predio text, id_tipo_cultivo text)");
        db.execSQL("create table L_punto_cuartel (Id_punto int primary key, id_cuartel text, latitud text, longitud text)");

        db.execSQL("create table L_ubicacion_sensor (Id_us int primary key, id_tarea text, id_placa text, latitud text, longitud text, observacion text, realizada text)");
        db.execSQL("create table L_microcontrolador (Id_placa int primary key, nombre_ap text, contrasena text)");
        db.execSQL("create table L_lectura_humedad (Id_lectura int primary key, id_us text, valor text, fecha text, hora text)");
        //db.execSQL("create table L_lectura_humedad2 (Id_lectura text, id_us text, valor text, fecha text, hora text)");

        //db.execSQL("create table L_sensor (Id_sensor int primary key, id_tipo_sensor text)");
        //db.execSQL("create table L_tipo_sensor (Id_tipo_sensor int primary key, tipo text)");


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


    }
}
