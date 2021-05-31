package com.optic.sistemaSerenazgo.providers;

import com.optic.sistemaSerenazgo.models.FCMBody;
import com.optic.sistemaSerenazgo.models.FCMResponse;
import com.optic.sistemaSerenazgo.retrofit.IFCMApi;
import com.optic.sistemaSerenazgo.retrofit.RetrofitClient;

import retrofit2.Call;

//CREAMOS UN PROVIDER PARA UTILIZAR MULTIPLES METODOS Y OPERACIONES

public class NotificationProvider {

    //URL DEL API REQUEST
    private String url = "https://fcm.googleapis.com";

    public NotificationProvider() {
    }

    //PARA HACER UNA PETICION AL API REQUEST DE TIPO POST UTILIZANDO LA INTERFACE CREADA
    public Call<FCMResponse> sendNotification(FCMBody body) {
        return RetrofitClient.getClientObject(url).create(IFCMApi.class).send(body);
    }
}
