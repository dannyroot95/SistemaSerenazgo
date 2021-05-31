package com.optic.sistemaSerenazgo.providers;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.optic.sistemaSerenazgo.R;
import com.optic.sistemaSerenazgo.retrofit.IGoogleApi;
import com.optic.sistemaSerenazgo.retrofit.RetrofitClient;

import java.util.Date;

import retrofit2.Call;

//CREAMOS UN PROVIDER PARA UTILIZAR MULTIPLES METODOS Y OPERACIONES

public class GoogleApiProvider {

    private Context context;


    public GoogleApiProvider(Context context) {
        this.context = context;
    }

    //PARA OBTENER MULTIPLES CARACTERISTICAS DE LA UBICACION Y RUTAS

    public Call<String> getDirections(LatLng originLatLng, LatLng destinationLatLng) {
        String baseUrl = "https://maps.googleapis.com";
        String query = "/maps/api/directions/json?mode=driving&transit_routing_preferences=less_driving&"
                     + "origin=" + originLatLng.latitude + "," + originLatLng.longitude + "&"
                     + "destination=" + destinationLatLng.latitude + "," + destinationLatLng.longitude + "&"
                     + "departure_time=" + (new Date().getTime() + (60*60*1000)) + "&"
                     + "traffic_model=best_guess&"
                     + "key=" + context.getResources().getString(R.string.google_maps_key);
        return RetrofitClient.getClient(baseUrl).create(IGoogleApi.class).getDirections(baseUrl + query);
    }
}
