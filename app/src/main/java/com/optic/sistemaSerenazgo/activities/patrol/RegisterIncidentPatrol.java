package com.optic.sistemaSerenazgo.activities.patrol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.optic.sistemaSerenazgo.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class RegisterIncidentPatrol extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerDragListener {

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    DatabaseReference mDatabase2;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;
    SimpleDateFormat simpleDateFormatHour= new SimpleDateFormat("HH:mm");
    SimpleDateFormat simpleDateFormatDate = new SimpleDateFormat("dd/MM/yyy");
    String formatHour = simpleDateFormatHour.format(new Date());
    String formatDate = simpleDateFormatDate.format(new Date());
    TextView hour,date;
    TextView lat,lon,mod,spinnerTextMod,textFicha;
    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private String mExtraOrigin;
    private ProgressDialog mDialogActualizeData;
    Spinner mSpinner;
    private ProgressDialog mDialog;

    private MapView mapView;
    private GoogleMap mMap;
    private Geocoder geocoder;
    private LatLng origen;
    private LatLng destino;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final String TAG = "RegisterIncident";

    private LinearLayout mLinearMap;
    Button btnMapping , btnRegister;
    FloatingActionButton btnCloseMapping;
    TextInputEditText txtMod;

    //UBICACION EN TIEMPO REAL

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                origen = new LatLng(location.getLatitude(), location.getLongitude());
                if (getApplicationContext() != null) {
                    Toast.makeText(RegisterIncidentPatrol.this, "Tu poscicion : " + origen, Toast.LENGTH_LONG).show();
                    //obtener locatizacion en tiempo real
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(15f)
                                    .build()
                    ));
                }
            }
        }
    };


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppThemeDark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_incident_patrol);
        checkUltimateDate();
        mDialog = new ProgressDialog(this);
        hour = findViewById(R.id.txtHour);
        date = findViewById(R.id.txtDate);
        lat = findViewById(R.id.txtLat);
        lon = findViewById(R.id.txtLon);
        mod = findViewById(R.id.spMod);
        hour.setText(formatHour);
        date.setText(formatDate);
        mSpinner = findViewById(R.id.spinnerMod);
        mExtraOriginLat = getIntent().getDoubleExtra("origin_lat", 0);
        mExtraOriginLng = getIntent().getDoubleExtra("origin_lng", 0);
        mExtraOrigin = getIntent().getStringExtra("origin");
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase2 = FirebaseDatabase.getInstance().getReference();
        btnMapping = findViewById(R.id.btnMapear);
        btnCloseMapping = findViewById(R.id.btnCloseMap);
        btnCloseMapping.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.places_text_black_alpha_87)));
        btnCloseMapping.setVisibility(View.INVISIBLE);
        txtMod = findViewById(R.id.textOcurrencia);
        spinnerTextMod = findViewById(R.id.spMod);
        textFicha = findViewById(R.id.txtNumFicha);
        btnRegister = findViewById(R.id.btnRegisterMapIncident);

        String mExtraStringLat = String.valueOf(mExtraOriginLat);
        String mExtraStringLon = String.valueOf(mExtraOriginLng);
        lat.setText(mExtraStringLat);
        lon.setText(mExtraStringLon);

        //OCULTAR LINEAR LAYOUT
        int alto = 0;
        mLinearMap = findViewById(R.id.map_container);
        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, alto);
        mLinearMap.setLayoutParams(params);

        //CONSUMIMOS EL ARRAY STRING CREADA Y MOSTRARLA EN EL SPINNER (strings)

        ArrayAdapter<CharSequence> adapterSpinnerModalidad = ArrayAdapter.createFromResource(this,R.
                array.modalidad,R.layout.support_simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapterSpinnerModalidad);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(!adapterView.getItemAtPosition(i).toString().equals("-")){
                    mod.setText(adapterView.getItemAtPosition(i).toString());
                    mod.setTextColor(Color.BLACK);
                }
                else {
                    mod.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //ESTADOS DE OCULTAR Y MOSTRAR UN MAPVIEW DENTRO DE UN ACTIVITY
        btnMapping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                mLinearMap.setLayoutParams(params);
                btnCloseMapping.setVisibility(View.VISIBLE);
            }
        });

        btnCloseMapping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                mLinearMap.setLayoutParams(params);
                btnCloseMapping.setVisibility(View.INVISIBLE);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerIncident();
            }
        });

        // METODO PARA UTILIZAR EL MAPVIEW
        geocoder = new Geocoder(this);
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //INICIALIZAMOS ON BOTTOM DENTRO DEL ACTIVITY
        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    // METODO DEL GOOGLE MAP
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerDragListener(this);
        try {

            List<Address> addresses = geocoder.getFromLocation(mExtraOriginLat, mExtraOriginLng, 1);

            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                LatLng aukde = new LatLng(address.getLatitude(), address.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(aukde)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car))
                        .title("Tu ubicación");
                mMap.addMarker(markerOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(aukde, 16));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //METODO QUE TE PERMITE UTILIZAR AL PRESIONAR EL EN MAPA SE EJECUTA UNA OPERACIÓN (COLOCAR UN MARCADOR EN EL MAPA)

    @Override
    public void onMapLongClick(LatLng latLng) {
        Log.d(TAG, "onMapLongClick: " + latLng.toString());
        int height = 100;
        int width = 100;
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.icon_pin_red);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap punto = Bitmap.createScaledBitmap(b, width, height, false);
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses.size()  > 0) {
                Address address = addresses.get(0);
                String streetAddress = address.getAddressLine(0);
                mMap.clear();
                onMapReady(mMap);
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(streetAddress)
                        .draggable(true)
                        .icon(BitmapDescriptorFactory.fromBitmap(punto))
                ).showInfoWindow();

                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition.Builder()
                                .target(new LatLng(address.getLatitude(), address.getLongitude()))
                                .zoom(16f)
                                .build()
                ));

                destino = new LatLng(address.getLatitude(), address.getLongitude());
                Toasty.success(this, "Ubicación Agregada!", Toast.LENGTH_SHORT,true).show();
                lat.setText("" + destino.latitude);
                lon.setText("" + destino.longitude);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        //MOSTRAR UN ALERT DIALOG AL PRESIONAR EN EL TITULO DE UN MARKER

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterIncidentPatrol.this);
                builder.setTitle("Alerta!");
                builder.setCancelable(false);
                builder.setMessage("Desea borrar esta poscición?");
                builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        marker.remove();
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.create();
                builder.show();
            }
        });
    }

    //CICLO DE VIDA AL ARRARSTRAR UN ELEMENTO DENTRO DEL MAPA

    @Override
    public void onMarkerDragStart(Marker marker) {
        Log.d(TAG, "onMarkerDragStart: ");
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        Log.d(TAG, "onMarkerDrag: ");
    }

    //METDOD PARA ARRASTRAR EL MARCADOR

    @Override
    public void onMarkerDragEnd(Marker marker) {
        Log.d(TAG, "onMarkerDragEnd: ");
        LatLng latLng = marker.getPosition();
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                String streetAddress = address.getAddressLine(0);
                destino = new LatLng(address.getLatitude(), address.getLongitude());
                Toasty.success(this, "Ubicación Actualizada!", Toast.LENGTH_SHORT,true).show();
                lat.setText("" + destino.latitude);
                lon.setText("" + destino.longitude);
                marker.setTitle(streetAddress);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }


    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }


    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    // BUSCAR EL ULTIMO REGISTRO EN TIEMPO REAL Y MOSTRAR EN LA FICHA DE REGISTRO DE INCIDENTE

    private void checkUltimateDate(){
        Query ultimateDate = FirebaseDatabase.getInstance().getReference().child("incidents").orderByKey().limitToLast(1);
        ultimateDate.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        if (childSnapshot.hasChild("numFicha")) {
                            String num = childSnapshot.child("numFicha").getValue().toString();
                            int numToString = Integer.parseInt(num);
                            int newNumPedido = numToString + 1;
                            String stNewNumPedido = String.valueOf(newNumPedido);
                            textFicha.setText(stNewNumPedido);
                        }
                        else{
                            textFicha.setText("0");
                        }
                    }
                }
                else{
                    textFicha.setText("0");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    //REGITRAR INCIDENTE EN LA BASE DE DATOS

    private void registerIncident(){

        String mod = txtMod.getText().toString();
        String latitud = lat.getText().toString();
        String longitud = lon.getText().toString();
        String modality = spinnerTextMod.getText().toString();
        Double la = Double.parseDouble(latitud);
        Double lo = Double.parseDouble(longitud);

        if(!mod.isEmpty() && !latitud.equals("") && !longitud.equals("") && !modality.equals("")) {

            mDialog.show();
            mDialog.setCancelable(false);
            mDialog.setMessage("Registrando incidente...");


            Map<String, Object> map = new HashMap<>();
            map.put("numFicha",textFicha.getText().toString());
            map.put("hora", hour.getText().toString());
            map.put("fecha",date.getText().toString());
            map.put("lugarDeOcurrencia",mod);
            map.put("modalidad",modality);
            map.put("lat",latitud);
            map.put("lng",longitud);

            mDatabase.child("Users").child("Drivers").child(mAuth.getUid()).child("incidents").push().setValue(map);


            Map<String, Object> map2 = new HashMap<>();
            map2.put("numFicha",textFicha.getText().toString());
            map2.put("hora", hour.getText().toString());
            map2.put("fecha",date.getText().toString());
            map2.put("lugarDeOcurrencia",mod);
            map2.put("modalidad",modality);
            map2.put("lat",la);
            map2.put("lng",lo);

            mDatabase.child("incidents").push().setValue(map2);

            Map<String, Object> map3 = new HashMap<>();
            map3.put("lat",la);
            map3.put("lng",lo);
            map3.put("fecha",date.getText().toString());
            mDatabase.child("heatMap").push().setValue(map3);
            mDialog.dismiss();
            startActivity(new Intent(RegisterIncidentPatrol.this, MapPatrolActivity.class));
            finish();
            Toasty.success(this, "Registro exitoso!", Toast.LENGTH_SHORT,true).show();
        }
        else {
            Toasty.info(this, "Verifique los campos!", Toast.LENGTH_SHORT).show();
            mDialog.dismiss();
        }
    }

    //LNZAR ALERT DIALOG ON BACK BUTTON

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterIncidentPatrol.this);
        builder.setTitle("Alerta!");
        builder.setIcon(R.drawable.ic_warning);
        builder.setCancelable(false);
        builder.setMessage("Descartar cambios? ");
        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(RegisterIncidentPatrol.this, MapPatrolActivity.class));
                finish();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create();
        builder.show();
    }
}