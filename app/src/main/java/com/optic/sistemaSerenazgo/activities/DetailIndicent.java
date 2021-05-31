package com.optic.sistemaSerenazgo.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.optic.sistemaSerenazgo.R;
import com.optic.sistemaSerenazgo.activities.patrol.IncidentListPatrol;
import com.optic.sistemaSerenazgo.activities.serene.IncidentList;
import com.optic.sistemaSerenazgo.includes.MyToolbar;
import com.optic.sistemaSerenazgo.models.Incidents;

import java.io.IOException;
import java.util.List;

public class DetailIndicent extends AppCompatActivity implements OnMapReadyCallback {

    SharedPreferences mPref;
    TextView detailSereno , detailHora , detailFecha, detailLugar,
             detailModalidad, detailAfectado, detailPlaca,detailResumen;

    private MapView mapView;
    private GoogleMap mMap;
    private Geocoder geocoder;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    LinearLayout mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppThemeDark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_indicent);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        final Incidents incidents = (Incidents) bundle.getSerializable("detail");

        MyToolbar.show(this, "Detalle de Incidente #"+incidents.getNumFicha(), false);
        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);

        mLayout         = findViewById(R.id.linearPlaca);
        detailSereno    = findViewById(R.id.detail_sereno);
        detailHora      = findViewById(R.id.detail_hora);
        detailFecha     = findViewById(R.id.detail_fecha);
        detailLugar     = findViewById(R.id.detail_ocurrencia);
        detailModalidad = findViewById(R.id.detail_modalidad);
        detailAfectado  = findViewById(R.id.detail_afectado);
        detailPlaca     = findViewById(R.id.detail_placa);
        detailResumen   = findViewById(R.id.detail_resumen);

        setupUI(incidents);

        geocoder = new Geocoder(this);
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

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        final Incidents incidents = (Incidents) bundle.getSerializable("detail");
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        try {
            List<Address> addresses = geocoder.getFromLocation(incidents.getLat(), incidents.getLng(), 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                LatLng position = new LatLng(address.getLatitude(), address.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(position)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red))
                        .title("Lugar del incidente");
                mMap.addMarker(markerOptions).showInfoWindow();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 18));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupUI(Incidents incidents)
    {
        detailSereno.setText(incidents.getSereno());
        detailHora.setText(incidents.getHora());
        detailFecha.setText(incidents.getFecha());
        detailLugar.setText(incidents.getLugarDeOcurrencia());
        detailModalidad.setText(incidents.getModalidad());
        detailAfectado.setText(incidents.getAfectado());
        if (!incidents.getPlaca().isEmpty()){
            mLayout.setVisibility(View.VISIBLE);
            detailPlaca.setText(incidents.getPlaca());
        }
        //
        detailResumen.setText(incidents.getResumen());

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(DetailIndicent.this,IncidentList.class));
        finish();
    }
}