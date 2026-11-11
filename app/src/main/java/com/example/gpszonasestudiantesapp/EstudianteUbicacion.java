package com.example.gpszonasestudiantesapp;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "estudiantes_ubicacion")
public class EstudianteUbicacion {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String nombreEstudiante;
    public String curso;
    public double latitud;
    public double longitud;
    public String direccion;
    public String zonaBarrio;
    public String ciudad;
    public long timestamp;

    public EstudianteUbicacion() {
    }

    @Ignore
    public EstudianteUbicacion(String nombreEstudiante, String curso, double latitud, double longitud,
                               String direccion, String zonaBarrio, String ciudad, long timestamp) {
        this.nombreEstudiante = nombreEstudiante;
        this.curso = curso;
        this.latitud = latitud;
        this.longitud = longitud;
        this.direccion = direccion;
        this.zonaBarrio = zonaBarrio;
        this.ciudad = ciudad;
        this.timestamp = timestamp;
    }
}
