package com.optic.sistemaSerenazgo.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

//IMPORTAMOS LA LIBRERIA RETROFIT PARA UTILIZAR UNA PETICION AL SERVIDOR Y ASI CON UN JSONARRAY
// CONSUMIR Y GENERAR DATOS EN UN ACTIVITY

public class RetrofitClient {

    public static Retrofit getClient(String url) {

        Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(url)
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .build();

        return  retrofit;
    }

    public static Retrofit getClientObject(String url) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return  retrofit;
    }
}
