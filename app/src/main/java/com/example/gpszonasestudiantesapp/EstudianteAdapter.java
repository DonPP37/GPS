package com.example.gpszonasestudiantesapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EstudianteAdapter extends RecyclerView.Adapter<EstudianteAdapter.EstudianteViewHolder> {

    private List<EstudianteUbicacion> lista = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

    public void setEstudiantes(List<EstudianteUbicacion> nuevaLista) {
        this.lista = nuevaLista != null ? nuevaLista : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EstudianteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_estudiante_ubicacion, parent, false);
        return new EstudianteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EstudianteViewHolder holder, int position) {
        EstudianteUbicacion e = lista.get(position);
        holder.tvNombre.setText(e.nombreEstudiante);
        holder.tvCurso.setText(e.curso);
        holder.tvZona.setText("Zona / Barrio: " + e.zonaBarrio + " (" + e.ciudad + ")");
        holder.tvDireccion.setText("Dirección: " + e.direccion);
        holder.tvCoordenadas.setText(String.format(Locale.getDefault(), "Lat: %.6f, Lon: %.6f", e.latitud, e.longitud));
        holder.tvFecha.setText(dateFormat.format(new Date(e.timestamp)));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class EstudianteViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCurso, tvZona, tvDireccion, tvCoordenadas, tvFecha;

        public EstudianteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreEstudiante);
            tvCurso = itemView.findViewById(R.id.tvCursoEstudiante);
            tvZona = itemView.findViewById(R.id.tvZonaEstudiante);
            tvDireccion = itemView.findViewById(R.id.tvDireccionEstudiante);
            tvCoordenadas = itemView.findViewById(R.id.tvCoordenadasEstudiante);
            tvFecha = itemView.findViewById(R.id.tvFechaEstudiante);
        }
    }
}
