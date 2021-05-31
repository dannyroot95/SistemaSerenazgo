package com.optic.sistemaSerenazgo.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface IGoogleApi {

    //OBTENEMOS LA URL DEL LA API PARA UTILIZAR LAS CARACTERISTICAS DE LAS DIRECCIONES EN UN MAPA
    @GET
    Call<String> getDirections(@Url String url);

}
