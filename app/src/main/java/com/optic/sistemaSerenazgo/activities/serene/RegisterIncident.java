package com.optic.sistemaSerenazgo.activities.serene;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
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
import com.optic.sistemaSerenazgo.activities.patrol.MapPatrolActivity;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class RegisterIncident extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerDragListener {

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    DatabaseReference mDatabase2;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat simpleDateFormatDate = new SimpleDateFormat("dd/MM/yyy");
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat simpleDateFormatHour= new SimpleDateFormat("HH:mm");
    String formatHour = simpleDateFormatHour.format(new Date());
    String formatDate = simpleDateFormatDate.format(new Date());
    TextView nameSereno,hour,date,btnMapping;
    TextView lat,lon,textFicha;
    String modalidad;
    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private String mExtraOrigin;
    Spinner mSpinner;
    private ProgressDialog mDialog;
    SharedPreferences mPref;
    private MapView mapView;
    private GoogleMap mMap;
    private Geocoder geocoder;
    private LatLng origen;
    private LatLng destino;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final String TAG = "RegisterIncident";
    String typeUser = "";
    private LinearLayout mLinearMap;
    MaterialButton btnRegister;
    FloatingActionButton btnCloseMapping;
    TextInputEditText txtOcurrencia , txtPlaca , txtResumen , txtAfectado;
    Long timestamp;

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                origen = new LatLng(location.getLatitude(), location.getLongitude());
                if (getApplicationContext() != null) {
                    Toast.makeText(RegisterIncident.this, "Tu poscicion : " + origen, Toast.LENGTH_LONG).show();
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
        setTheme(R.style.AppThemeDark2);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_incident_sereno);
        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);
        typeUser = mPref.getString("user", "");
        checkUltimateDate();
        modalidad = "";
        mDialog = new ProgressDialog(this);
        nameSereno = findViewById(R.id.txtSereno);
        hour = findViewById(R.id.txtHour);
        date = findViewById(R.id.txtDate);
        lat = findViewById(R.id.txtLat);
        lon = findViewById(R.id.txtLon);
        hour.setText(formatHour);
        date.setText(formatDate);

        try {
            @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = formatter.parse(formatDate);
            assert date != null;
            long output=date.getTime()/1000L;
            String str=Long.toString(output);
            timestamp = Long.parseLong(str) * 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }


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
        txtOcurrencia = findViewById(R.id.textOcurrencia);
        textFicha = findViewById(R.id.txtNumFicha);
        btnRegister = findViewById(R.id.btnRegisterMapIncident);

        txtPlaca = findViewById(R.id.edt_placa);
        txtResumen = findViewById(R.id.edt_reseña);
        txtAfectado = findViewById(R.id.edt_afectado);

        String mExtraStringLat = String.valueOf(mExtraOriginLat);
        String mExtraStringLon = String.valueOf(mExtraOriginLng);
        lat.setText(mExtraStringLat);
        lon.setText(mExtraStringLon);

        ArrayAdapter<CharSequence> adapterSpinnerModalidad = ArrayAdapter.createFromResource(this,R.
                array.modalidad,R.layout.support_simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapterSpinnerModalidad);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                modalidad = adapterView.getItemAtPosition(i).toString();
                if (modalidad.equals("Acc.Tránsito")){
                    txtPlaca.setVisibility(View.VISIBLE);
                }
                else{
                    txtPlaca.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        btnMapping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard();
                mapView.setVisibility(View.VISIBLE);
                btnCloseMapping.setVisibility(View.VISIBLE);
            }
        });

        btnCloseMapping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapView.setVisibility(View.GONE);
                btnCloseMapping.setVisibility(View.INVISIBLE);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerIncident();
            }
        });

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

    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

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
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_my_location))
                        .title("Tu ubicación");
                mMap.addMarker(markerOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(aukde, 16));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterIncident.this,R.style.ThemeOverlay);
                builder.setTitle("Alerta!");
                builder.setCancelable(false);
                builder.setMessage("Desea borrar esta poscición?");
                builder.setPositiveButton(Html.fromHtml("<font color='#000'>SI</font>"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        marker.remove();
                    }
                });

                builder.setNegativeButton(Html.fromHtml("<font color='#000'>NO</font>"), new DialogInterface.OnClickListener() {
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

    @Override
    public void onMarkerDragStart(Marker marker) {
        Log.d(TAG, "onMarkerDragStart: ");
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        Log.d(TAG, "onMarkerDrag: ");
    }

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
                        getName();
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

    private void registerIncident(){

         final String sereno = nameSereno.getText().toString();
         final String numFicha = textFicha.getText().toString();
         final String hora = hour.getText().toString();
         final String fecha = date.getText().toString();
         final String ocurrencia = Objects.requireNonNull(txtOcurrencia.getText()).toString();
         final String latitud = lat.getText().toString();
         final String longitud = lon.getText().toString();
         final String nombreAfectado = txtAfectado.getText().toString();
         final String placa = txtPlaca.getText().toString();
         final String resumen = txtResumen.getText().toString();
         final Double la = Double.parseDouble(latitud);
         final Double lo = Double.parseDouble(longitud);

        if(!sereno.isEmpty() &&!ocurrencia.isEmpty() && !latitud.equals("") && !longitud.equals("")
                && !modalidad.equals("-") && !nombreAfectado.isEmpty() && !resumen.isEmpty()) {

            if (txtPlaca.getVisibility() == View.VISIBLE && !placa.isEmpty() ||
                    txtPlaca.getVisibility() == View.GONE && placa.isEmpty()  ){

                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterIncident.this,R.style.ThemeOverlay);
                builder.setTitle("Hey!");
                builder.setIcon(R.drawable.ic_check);
                builder.setCancelable(false);
                builder.setMessage("Esta seguro de guardar este registro? ");
                builder.setPositiveButton(Html.fromHtml("<font color='#000'>SI</font>"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mappingData(sereno,numFicha,hora,fecha,ocurrencia,modalidad,latitud,
                                longitud,la,lo,nombreAfectado,placa,resumen);
                    }
                });
                builder.setNegativeButton(Html.fromHtml("<font color='#000'>NO</font>"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create();
                builder.show();

            }
            else{
                Toasty.error(this, "Verifique los campos!", Toast.LENGTH_SHORT,true).show();
            }
        }
        else {
            Toasty.error(this, "Verifique los campos!", Toast.LENGTH_SHORT,true).show();
            mDialog.dismiss();
        }
    }

    private void mappingData(final String sereno,final String numFicha ,final String hora ,final String fecha ,final String ocurrencia ,
                             final String modalidad ,final String latitud ,final String longitud ,
                             final Double la ,final Double lo ,final String afectado ,final String placa ,
                             final String resumen){

        mDialog.show();
        mDialog.setCancelable(false);
        mDialog.setMessage("Registrando incidente...");

        Map<String, Object> map = new HashMap<>();
        map.put("numFicha",numFicha);
        map.put("hora", hora);
        map.put("fecha",fecha);
        map.put("lugarDeOcurrencia",ocurrencia);
        map.put("modalidad",modalidad);
        map.put("afectado",afectado);
        map.put("placa",placa);
        map.put("resumen",resumen);
        map.put("lat",latitud);
        map.put("lng",longitud);

        mDatabase.child("Users").child("Clients").child(mAuth.getUid()).child("incidents").push().setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Map<String, Object> map2 = new HashMap<>();
                map2.put("sereno",sereno);
                map2.put("numFicha",numFicha);
                map2.put("hora", hora);
                map2.put("fecha",fecha);
                map2.put("lugarDeOcurrencia",ocurrencia);
                map2.put("modalidad",modalidad);
                map2.put("afectado",afectado);
                map2.put("placa",placa);
                map2.put("resumen",resumen);
                map2.put("timestamp",timestamp);
                map2.put("lat",la);
                map2.put("lng",lo);

                mDatabase.child("incidents").push().setValue(map2).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String, Object> map3 = new HashMap<>();
                        map3.put("lat",la);
                        map3.put("lng",lo);
                        map3.put("timestamp",timestamp);
                        map3.put("fecha",fecha);
                        mDatabase.child("heatMap").push().setValue(map3).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mDialog.dismiss();
                                if (typeUser.equals("client")){
                                    startActivity(new Intent(RegisterIncident.this,MapSereneActivity.class));
                                    finish();
                                } else {
                                    startActivity(new Intent(RegisterIncident.this,MapPatrolActivity.class));
                                    finish();
                                }
                                Toasty.success(RegisterIncident.this, "Registro exitoso!", Toast.LENGTH_SHORT,true).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mDialog.dismiss();
                                Toasty.error(RegisterIncident.this, "Ocurrió un error!", Toast.LENGTH_SHORT,true).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mDialog.dismiss();
                        Toasty.error(RegisterIncident.this, "Ocurrió un error!", Toast.LENGTH_SHORT,true).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mDialog.dismiss();
                Toasty.error(RegisterIncident.this, "Ocurrió un error!", Toast.LENGTH_SHORT,true).show();
            }
        });

    }

    private void getName(){
        String type = "";
        if (typeUser.equals("client")){
            type = "Clients";
        } else {
           type = "Drivers";
        }
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
        mReference.child("Users").child(type).child(Objects.requireNonNull(mAuth.getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String name = dataSnapshot.child("name").getValue().toString();
                    nameSereno.setText(name);
                }
                else{
                    Toasty.error(RegisterIncident.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBackPressed() {

        if (mapView.getVisibility() == View.VISIBLE){
            mapView.setVisibility(View.GONE);
            btnCloseMapping.setVisibility(View.INVISIBLE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterIncident.this,R.style.ThemeOverlay);
        builder.setTitle("Alerta!");
        builder.setIcon(R.drawable.ic_warning);
        builder.setCancelable(false);
        builder.setMessage("Descartar cambios? ");
        builder.setPositiveButton(Html.fromHtml("<font color='#000'>SI</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (typeUser.equals("client")){
                    startActivity(new Intent(RegisterIncident.this,MapSereneActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(RegisterIncident.this,MapPatrolActivity.class));
                    finish();
                }
            }
        });
        builder.setNegativeButton(Html.fromHtml("<font color='#000'>NO</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create();
        builder.show();
    }
}