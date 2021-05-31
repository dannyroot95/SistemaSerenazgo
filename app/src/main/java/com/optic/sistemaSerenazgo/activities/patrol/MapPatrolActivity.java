package com.optic.sistemaSerenazgo.activities.patrol;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ValueEventListener;
import com.optic.sistemaSerenazgo.R;
import com.optic.sistemaSerenazgo.activities.MainActivity;
import com.optic.sistemaSerenazgo.activities.serene.IncidentList;
import com.optic.sistemaSerenazgo.activities.serene.RegisterIncident;
import com.optic.sistemaSerenazgo.includes.MyToolbar;
import com.optic.sistemaSerenazgo.providers.AuthProvider;
import com.optic.sistemaSerenazgo.providers.GeofireProvider;
import com.optic.sistemaSerenazgo.providers.TokenProvider;

import es.dmoral.toasty.Toasty;

// implementamos el método ONMAPREADYCALLBACK PARA EL USO DE GOOGLE MAPS

public class MapPatrolActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private AuthProvider mAuthProvider;
    private GeofireProvider mGeofireProvider;
    private TokenProvider mTokenProvider;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;
    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;
    private Marker mMarker;
    private Button mButtonConnect;
    private boolean mIsConnect = false;
    private LatLng mCurrentLatLng;
    private ValueEventListener mListener;
    private FloatingActionButton floatingActionButton;
    LocationCallback mLocationCallback = new LocationCallback() {


        // OBTENEMOS NUESTRA POSCICION EN TIEMPO REAL

        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (mMarker != null) {
                        mMarker.remove();
                    }

                    mMarker = mMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude())
                            )
                            .title("Tu posicion")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car))
                    );

                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(16f)
                                    .build()
                    ));

                    updateLocation();
                    Log.d("ENTRO", "ACTUALIZANDO POSCICIÓN");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppThemeDark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_driver);
        MyToolbar.show(this, "Patrullero", false);

        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("active_drivers");
        mTokenProvider = new TokenProvider();
        floatingActionButton = findViewById(R.id.btnRegisterIncidentOnPatrol);

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        // CONECTAMOS Ó DESCONECTAMOS EL GPS
        mButtonConnect = findViewById(R.id.btnConnect);
        mButtonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsConnect) {
                    disconnect();
                } else {
                    startLocation();
                }
            }
        });


        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String xtraOriginLat = String.valueOf(mCurrentLatLng.latitude);
                if (!xtraOriginLat.equals("0.0")){
                    Intent intent = new Intent(MapPatrolActivity.this , RegisterIncident.class);
                    intent.putExtra("origin_lat", mCurrentLatLng.latitude);
                    intent.putExtra("origin_lng", mCurrentLatLng.longitude);
                    intent.putExtra("origin", mCurrentLatLng);
                    startActivity(intent);
                    finish();
                }
                else {
                    Toasty.info(MapPatrolActivity.this, "Espere un momento...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        gpsActived();
        generateToken();
        //isDriverWorking();
    }

    //ELIMINAR EL LISTENER AL DESTRUIR EL ACTIVITY

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListener != null) {
            mGeofireProvider.isDriverWorking(mAuthProvider.getId()).removeEventListener(mListener);
        }
    }

    // ACTUALIZANDO UBICACIÓN
    private void updateLocation() {
        if (mAuthProvider.existSession() && mCurrentLatLng != null) {
            mGeofireProvider.saveLocation(mAuthProvider.getId(), mCurrentLatLng);
        }
    }

    // INICIALIZAMOS EL MÉTODO DE ONMAPREADY DE GOOGLE MAP

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //mMap.getUiSettings().setZoomControlsEnabled(true);
        //DECLARAMOS LOS PERMISOS (SOLO PARA ANDROID 8 EN ADELANTE)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(false);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);
        startLocation();
    }

    //VERIFICAMOS LOS PERMISOS DENTRO DEL ACTIVITY , PREVIAMENTE TAMBIEN DECLARA EN EL MANIFEST

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (gpsActived()) {
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    } else {
                        showAlertDialogNOGPS();
                    }
                } else {
                    checkLocationPermissions();
                }
            } else {
                checkLocationPermissions();
            }
        }
    }
    //VERIFICAMOS LOS PERMISOS DENTRO DEL ACTIVITY , PREVIAMENTE TAMBIEN DECLARA EN EL MANIFEST
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
        else {
            showAlertDialogNOGPS();
        }
    }

    //ALERT DIALOG DE VERIFICACION DE ACTIVACION DE GPS
    private void showAlertDialogNOGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor activa tu ubicacion para continuar")
                .setCancelable(false)
                .setNegativeButton("Refrescar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(MapPatrolActivity.this,MapPatrolActivity.class));
                        finish();
                    }
                })
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                }).create().show();
    }


    //VERIFICAR SI EL GPS ESTA ACTIVO

    private boolean gpsActived() {
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }
        return isActive;
    }

    //MÉTODO PRIVADO PARA DESCONECTAR O CONECTAR EL GPS

    private void disconnect() {
        if (mFusedLocation != null) {
            mButtonConnect.setText("Conectar GPS");
            mIsConnect = false;
            mFusedLocation.removeLocationUpdates(mLocationCallback);
            if (mAuthProvider.existSession()) {
                mGeofireProvider.removeLocation(mAuthProvider.getId());
            }
        }
        else {
            Toasty.error(this, "No te puedes desconectar", Toast.LENGTH_SHORT).show();
        }
    }

    //INICIALIZAR UBICACION

    private void startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {
                    mButtonConnect.setText("Desconectar GPS");
                    mIsConnect = true;
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                }
                else {
                    showAlertDialogNOGPS();
                }
            }
            else {
                checkLocationPermissions();
            }
        } else {
            if (gpsActived()) {
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }
            else {
                showAlertDialogNOGPS();
            }
        }
    }

    //INICILIZAR LAS CONFIGURACIONES

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                            .setTitle("Proporciona los permisos para continuar")
                            .setMessage("Esta aplicacion requiere de los permisos de ubicacion para poder utilizarse")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ActivityCompat.requestPermissions(MapPatrolActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                                }
                            })
                            .create()
                            .show();
            }
            else {
                ActivityCompat.requestPermissions(MapPatrolActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    //MOSTRAR MENU SUPERIOR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // SELECCIONAR OPCIONES DEL MENU SUPERIOR

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_incident) {
            Intent intent = new Intent(MapPatrolActivity.this, IncidentList.class);
            startActivity(intent);
            finish();
        }

        if (item.getItemId() == R.id.action_logout) {
            logout();
        }
        if (item.getItemId() == R.id.action_update) {
            Intent intent = new Intent(MapPatrolActivity.this, UpdateProfilePatrolActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    //CERRAR SESIÓN
    void logout() {
        disconnect();
        mAuthProvider.logout();
        Intent intent = new Intent(MapPatrolActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    //GENERAR TOKEN AL INICIAR SESIÓN
    void generateToken() {
        mTokenProvider.create(mAuthProvider.getId());
    }

    //INICIALIZAR UBICACION AL CERRAR Y ABRIR APP
    @Override
    protected void onStart() {
        super.onStart();
        startLocation();
    }
}
