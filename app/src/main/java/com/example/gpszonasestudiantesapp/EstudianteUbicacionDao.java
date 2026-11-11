package com.example.gpszonasestudiantesapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface EstudianteUbicacionDao {

    @Insert
    long insertar(EstudianteUbicacion estudiante);

    @Query("SELECT * FROM estudiantes_ubicacion ORDER BY timestamp DESC")
    List<EstudianteUbicacion> obtenerTodos();

    @Query("SELECT * FROM estudiantes_ubicacion WHERE curso = :cursoFiltro ORDER BY timestamp DESC")
    List<EstudianteUbicacion> obtenerPorCurso(String cursoFiltro);

    @Query("SELECT DISTINCT curso FROM estudiantes_ubicacion ORDER BY curso ASC")
    List<String> obtenerCursosDisponibles();

    @Query("SELECT zonaBarrio AS zona, COUNT(*) AS totalEstudiantes " +
           "FROM estudiantes_ubicacion " +
           "WHERE (:cursoFiltro IS NULL OR :cursoFiltro = 'Todos' OR curso = :cursoFiltro) " +
           "GROUP BY zonaBarrio " +
           "ORDER BY totalEstudiantes DESC")
    List<ZonaEstadistica> obtenerZonasConMasEstudiantes(String cursoFiltro);

    @Query("DELETE FROM estudiantes_ubicacion")
    void borrarTodos();
}
