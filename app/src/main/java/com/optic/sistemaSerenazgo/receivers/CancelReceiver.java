package com.optic.sistemaSerenazgo.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.optic.sistemaSerenazgo.providers.SereneBookingProvider;

public class CancelReceiver extends BroadcastReceiver {
    private SereneBookingProvider mClientBookingProvider;

    //CANCELAMOS LA SOLICITUD Y ENVIAMOS UNA NOTIFICACION DE CANCELACION DE PETICION
    @Override
    public void onReceive(Context context, Intent intent) {
        String idClient = intent.getExtras().getString("idClient");
        mClientBookingProvider = new SereneBookingProvider();
        mClientBookingProvider.updateStatus(idClient, "cancel");

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);
    }
}
