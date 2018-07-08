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
        db.execSQL("create table L_tareas (Id_tarea int primary key, id_tipo text, latitud text, longitud text, fecha text, realizada text, observacion text, id_trabajador text)");
        db.execSQL("create table L_datoM (Id int primary key, id_tarea text, latitud text, longitud text, n_fruta text, t_fruta text)");
        db.execSQL("create table L_datoS (Id int primary key, id_tarea text, sensor text, valor_humedad text, latitud text, longitud text, fecha text)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
