package com.example.gpszonasestudiantesapp;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.Executors;

@Database(entities = {EstudianteUbicacion.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCIA;

    public abstract EstudianteUbicacionDao estudianteUbicacionDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCIA == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCIA == null) {
                    INSTANCIA = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "gps_zonas_estudiantes.db"
                    ).addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            // Pre-poblar con datos de prueba de estudiantes para análisis de zonas
                            Executors.newSingleThreadExecutor().execute(() -> {
                                EstudianteUbicacionDao dao = getInstance(context).estudianteUbicacionDao();
                                long now = System.currentTimeMillis();

                                dao.insertar(new EstudianteUbicacion("Carlos Mendoza", "Dispositivos Móviles",
                                        4.6583, -74.0935, "Cl. 53 #60-00", "Teusaquillo", "Bogotá", now));
                                dao.insertar(new EstudianteUbicacion("Laura Gómez", "Dispositivos Móviles",
                                        4.6480, -74.0910, "Av. El Dorado #50-10", "Teusaquillo", "Bogotá", now - 10000));
                                dao.insertar(new EstudianteUbicacion("Santiago Reyes", "Dispositivos Móviles",
                                        4.6400, -74.0880, "Cra. 30 #45-03", "Teusaquillo", "Bogotá", now - 20000));
                                dao.insertar(new EstudianteUbicacion("Valentina Cruz", "Dispositivos Móviles",
                                        4.6097, -74.0817, "Cra. 7 #12-20", "La Candelaria", "Bogotá", now - 30000));
                                dao.insertar(new EstudianteUbicacion("Andrés Parra", "Dispositivos Móviles",
                                        4.6015, -74.0720, "Cl. 10 #3-15", "La Candelaria", "Bogotá", now - 40000));
                                dao.insertar(new EstudianteUbicacion("María Torres", "Dispositivos Móviles",
                                        4.6980, -74.0450, "Cl. 116 #15-30", "Usaquén", "Bogotá", now - 50000));
                                dao.insertar(new EstudianteUbicacion("Felipe Castro", "Dispositivos Móviles",
                                        4.6750, -74.0580, "Cl. 85 #11-20", "Chapinero", "Bogotá", now - 60000));
                            });
                        }
                    }).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCIA;
    }
}
