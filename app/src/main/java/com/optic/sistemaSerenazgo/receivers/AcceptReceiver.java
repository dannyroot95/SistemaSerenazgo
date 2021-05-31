package com.optic.sistemaSerenazgo.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.optic.sistemaSerenazgo.activities.patrol.MapPatrolBookingActivity;
import com.optic.sistemaSerenazgo.providers.AuthProvider;
import com.optic.sistemaSerenazgo.providers.SereneBookingProvider;
import com.optic.sistemaSerenazgo.providers.GeofireProvider;

public class AcceptReceiver extends BroadcastReceiver {

    // CREAMOS UN BROADCAST RECEIVER PARA ACTUALIZAR ESTADO DE PETICION DE UNA SOLICITUD

    private SereneBookingProvider mClientBookingProvider;
    private GeofireProvider mGeofireProvider;
    private AuthProvider mAuthProvider;

    @Override
    public void onReceive(Context context, Intent intent) {
        // RECIBIMOS EL ID DEL LA PATRULLA
        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("active_drivers");
        mGeofireProvider.removeLocation(mAuthProvider.getId());
        // RECIBIMOS EL ID DEL SERENO
        String idClient = intent.getExtras().getString("idClient");
        mClientBookingProvider = new SereneBookingProvider();
        mClientBookingProvider.updateStatus(idClient, "accept");

        // CREAMOS UN NOTIFICADOR
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

        // LANZAMOS EL ACTIVITY DONDE SE VISUALIZA LA RUTA
        Intent intent1 = new Intent(context, MapPatrolBookingActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.setAction(Intent.ACTION_RUN);
        intent1.putExtra("idClient", idClient);
        context.startActivity(intent1);

    }

}
