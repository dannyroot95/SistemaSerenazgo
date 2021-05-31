package com.optic.sistemaSerenazgo.providers;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

//CREAMOS UN PROVIDER PARA UTILIZAR MULTIPLES METODOS Y OPERACIONES

public class GeofireProvider {

    private DatabaseReference mDatabase;
    private GeoFire mGeofire;

    //PARA OBTENER EL NODO DE UBICACIONES
    public GeofireProvider (String reference) {
        mDatabase = FirebaseDatabase.getInstance().getReference().child(reference);
        mGeofire = new GeoFire(mDatabase);
    }
    //PARA GUARDAR LA UBICACION
    public void saveLocation(String idDriver, LatLng latLng) {
        mGeofire.setLocation(idDriver, new GeoLocation(latLng.latitude, latLng.longitude));
    }

    //PARA ELIMINAR LA UBICACION
    public void removeLocation(String idDriver) {
        mGeofire.removeLocation(idDriver);
    }

    //PARA OBTENER EL ESTADO DE LA PATRULLA CERCANA
    public GeoQuery getActiveDrivers(LatLng latLng, double radius) {
        GeoQuery geoQuery = mGeofire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), radius);
        geoQuery.removeAllListeners();
        return geoQuery;
    }

    //PARA OBTENER ELA UBICACION DE LA PATRULLA
    public DatabaseReference getDriverLocation(String idDriver) {
        return mDatabase.child(idDriver).child("l");
    }

    //PARA OBTENER EL ESTADO DE LA PATRULLA SI ESTA EN RUTA
    public DatabaseReference isDriverWorking(String idDriver) {
        return FirebaseDatabase.getInstance().getReference().child("drivers_working").child(idDriver);
    }

}
