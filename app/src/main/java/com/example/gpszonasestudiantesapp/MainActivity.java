package com.example.gpszonasestudiantesapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;

    // FusedLocationProviderClient y Location Services
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location ultimaUbicacion = null;

    // Elementos de la interfaz (UI)
    private TextView tvCoordenadasGPS, tvDireccionGeocodificada, tvRankingZonas;
    private TextInputEditText etNombreEstudiante, etCurso;
    private Button btnObtenerUbicacion, btnReportarEstudiante, btnBorrarTodo;
    private Spinner spinnerCursos;
    private RecyclerView recyclerEstudiantes;
    private EstudianteAdapter estudianteAdapter;

    // Persistencia Room
    private AppDatabase db;
    private String cursoSeleccionado = "Todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);

        // Inicializar FusedLocationProviderClient usando LocationServices
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initViews();
        setupRecyclerView();
        setupListeners();
        setupLocationCallback();

        solicitarPermisosYObtenerUbicacion();
        cargarDatosUI();
    }

    private void initViews() {
        tvCoordenadasGPS = findViewById(R.id.tvCoordenadasGPS);
        tvDireccionGeocodificada = findViewById(R.id.tvDireccionGeocodificada);
        tvRankingZonas = findViewById(R.id.tvRankingZonas);

        etNombreEstudiante = findViewById(R.id.etNombreEstudiante);
        etCurso = findViewById(R.id.etCurso);

        btnObtenerUbicacion = findViewById(R.id.btnObtenerUbicacion);
        btnReportarEstudiante = findViewById(R.id.btnReportarEstudiante);
        btnBorrarTodo = findViewById(R.id.btnBorrarTodo);

        spinnerCursos = findViewById(R.id.spinnerCursos);
        recyclerEstudiantes = findViewById(R.id.recyclerEstudiantes);
    }

    private void setupRecyclerView() {
        estudianteAdapter = new EstudianteAdapter();
        recyclerEstudiantes.setLayoutManager(new LinearLayoutManager(this));
        recyclerEstudiantes.setAdapter(estudianteAdapter);
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        actualizarUbicacionEnPantalla(location);
                        break;
                    }
                }
            }
        };
    }

    private void setupListeners() {
        btnObtenerUbicacion.setOnClickListener(v -> solicitarPermisosYObtenerUbicacion());

        btnReportarEstudiante.setOnClickListener(v -> registrarUbicacionEstudiante());

        btnBorrarTodo.setOnClickListener(v -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                db.estudianteUbicacionDao().borrarTodos();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Base de datos de estudiantes limpiada", Toast.LENGTH_SHORT).show();
                    cargarDatosUI();
                });
            });
        });

        spinnerCursos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cursoSeleccionado = parent.getItemAtPosition(position).toString();
                cargarDatosUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void solicitarPermisosYObtenerUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }

        // Obtener última ubicación conocida con FusedLocationProviderClient
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                actualizarUbicacionEnPantalla(location);
            } else {
                tvCoordenadasGPS.setText("Solicitando señal de GPS activa...");
                solicitarActualizacionesActivas();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error obteniendo ubicación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void solicitarActualizacionesActivas() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void actualizarUbicacionEnPantalla(Location location) {
        this.ultimaUbicacion = location;
        tvCoordenadasGPS.setText(String.format(Locale.getDefault(), "Lat: %.6f | Lon: %.6f", location.getLatitude(), location.getLongitude()));

        // Uso de Geocoder para convertir coordenadas a dirección y zona/barrio
        geocodificarUbicacion(location.getLatitude(), location.getLongitude());
    }

    private void geocodificarUbicacion(double lat, double lon) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            String direccionTexto = "Dirección no disponible por Geocoder";
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    StringBuilder sb = new StringBuilder();

                    if (address.getThoroughfare() != null) {
                        sb.append(address.getThoroughfare());
                        if (address.getSubThoroughfare() != null) {
                            sb.append(" #").append(address.getSubThoroughfare());
                        }
                    } else if (address.getAddressLine(0) != null) {
                        sb.append(address.getAddressLine(0));
                    }

                    String subLocality = address.getSubLocality() != null ? address.getSubLocality() :
                            (address.getLocality() != null ? address.getLocality() : "Zona Centro");

                    direccionTexto = sb.toString() + " (" + subLocality + ", " + address.getCountryName() + ")";
                }
            } catch (IOException e) {
                direccionTexto = "Geocoder offline (Lat: " + lat + ", Lon: " + lon + ")";
            }

            String finalDireccion = direccionTexto;
            runOnUiThread(() -> tvDireccionGeocodificada.setText("📍 " + finalDireccion));
        });
    }

    private void registrarUbicacionEstudiante() {
        String nombre = etNombreEstudiante.getText() != null ? etNombreEstudiante.getText().toString().trim() : "";
        String curso = etCurso.getText() != null ? etCurso.getText().toString().trim() : "";

        if (nombre.isEmpty() || curso.isEmpty()) {
            Toast.makeText(this, "Ingrese el nombre del estudiante y el curso", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si no se tiene ubicación de hardware activa (ej. emulador sin fix), se asume una referencia de Bogotá
        double lat = ultimaUbicacion != null ? ultimaUbicacion.getLatitude() : 4.6583;
        double lon = ultimaUbicacion != null ? ultimaUbicacion.getLongitude() : -74.0935;

        Executors.newSingleThreadExecutor().execute(() -> {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            String direccion = "Calle Principal";
            String zonaBarrio = "Chapinero";
            String ciudad = "Bogotá";

            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address addr = addresses.get(0);
                    if (addr.getAddressLine(0) != null) {
                        direccion = addr.getAddressLine(0);
                    }
                    if (addr.getSubLocality() != null) {
                        zonaBarrio = addr.getSubLocality();
                    } else if (addr.getLocality() != null) {
                        zonaBarrio = addr.getLocality();
                    }
                    if (addr.getLocality() != null) {
                        ciudad = addr.getLocality();
                    }
                }
            } catch (Exception ignored) {
            }

            EstudianteUbicacion nuevo = new EstudianteUbicacion(
                    nombre,
                    curso,
                    lat,
                    lon,
                    direccion,
                    zonaBarrio,
                    ciudad,
                    System.currentTimeMillis()
            );

            db.estudianteUbicacionDao().insertar(nuevo);

            runOnUiThread(() -> {
                Toast.makeText(this, "¡Ubicación del estudiante reportada para el curso!", Toast.LENGTH_SHORT).show();
                etNombreEstudiante.setText("");
                cargarDatosUI();
            });
        });
    }

    private void cargarDatosUI() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<String> cursosBd = db.estudianteUbicacionDao().obtenerCursosDisponibles();
            List<String> listaCursosSpinner = new ArrayList<>();
            listaCursosSpinner.add("Todos");
            listaCursosSpinner.addAll(cursosBd);

            List<EstudianteUbicacion> estudiantes;
            if (cursoSeleccionado.equals("Todos")) {
                estudiantes = db.estudianteUbicacionDao().obtenerTodos();
            } else {
                estudiantes = db.estudianteUbicacionDao().obtenerPorCurso(cursoSeleccionado);
            }

            List<ZonaEstadistica> zonasTop = db.estudianteUbicacionDao().obtenerZonasConMasEstudiantes(cursoSeleccionado);

            runOnUiThread(() -> {
                // Actualizar spinner sin reiniciar si ya tiene elementos
                int posActual = spinnerCursos.getSelectedItemPosition();
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listaCursosSpinner);
                spinnerCursos.setAdapter(spinnerAdapter);
                int index = listaCursosSpinner.indexOf(cursoSeleccionado);
                if (index >= 0) {
                    spinnerCursos.setSelection(index, false);
                }

                // Actualizar RecyclerView
                estudianteAdapter.setEstudiantes(estudiantes);

                // Construir recomendación de optimización de tutorías y laboratorios
                if (zonasTop.isEmpty()) {
                    tvRankingZonas.setText("No hay estudiantes registrados para analizar zonas.");
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("📍 ZONAS CON MAYOR PARTICIPACIÓN:\n");
                    int i = 1;
                    for (ZonaEstadistica z : zonasTop) {
                        sb.append(i).append(". ").append(z.zona).append(": ")
                                .append(z.totalEstudiantes).append(" estudiante(s)\n");
                        i++;
                    }
                    ZonaEstadistica principal = zonasTop.get(0);
                    sb.append("\n💡 RECOMENDACIÓN DE ASIGNACIÓN:\n");
                    sb.append("Establecer servicios de tutoría presencial y franjas de laboratorios prioritarias en la zona: '")
                            .append(principal.zona)
                            .append("' donde habita la mayor concentración de estudiantes (")
                            .append(principal.totalEstudiantes)
                            .append(" participantes).");

                    tvRankingZonas.setText(sb.toString());
                }
            });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                solicitarPermisosYObtenerUbicacion();
            } else {
                Toast.makeText(this, "Permiso de localización denegado. Algunas funciones estarán limitadas.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}
