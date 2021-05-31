package com.optic.sistemaSerenazgo.channel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.optic.sistemaSerenazgo.R;

public class NotificationHelper extends ContextWrapper {

    //CREAMOS LOS CANALES PARA LANZAR LA NOTIFICACIONES
    private static final String CHANNEL_ID = "com.optic.sistemaSerenazgo";
    private static final String CHANNEL_NAME = "sistemaSerenazgo";

    private NotificationManager manager;

    // VERIFICAMOS LAS VERSIONES DE ANDROID QUE SE ESTA UTILIZANDO EN EL MOVIL Y SEGUN A ESO SE EJECUTARA
    // UNA NOTIFIACION Y ASPECTO SEGUN LA VERSION Q SE TENGA
    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels() {
        NotificationChannel notificationChannel = new
                NotificationChannel(
                        CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_NONE
                );
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLightColor(Color.GRAY);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(notificationChannel);
    }

    public NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    //ESTRUTURAMOS LO QUE CONTENDRÁ LA NOTIFICACIÓN SEGUN LA VERSION UTILIZAMOS UN (NOTIFICACTION Ó NOTIFIACIONCOMPAT)
    //PARA VERSIONES MAS ANTIGUAS
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotification(String title, String body, PendingIntent intent, Uri soundUri) {
        return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setSound(soundUri)
                    .setContentIntent(intent)
                    .setSmallIcon(R.drawable.ic_car)
                    .setStyle(new Notification.BigTextStyle()
                                .bigText(body).setBigContentTitle(title));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotificationActions(String title,
                                                       String body,
                                                       Uri soundUri,
                                                       Notification.Action acceptAction ,
                                                       Notification.Action cancelAction) {
        return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setSound(soundUri)
                    .setSmallIcon(R.drawable.ic_car)
                    .addAction(acceptAction)
                    .addAction(cancelAction)
                    .setStyle(new Notification.BigTextStyle()
                                .bigText(body).setBigContentTitle(title));
    }

    public NotificationCompat.Builder getNotificationOldAPI(String title, String body, PendingIntent intent, Uri soundUri) {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_car)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title));
    }

    public NotificationCompat.Builder getNotificationOldAPIActions(
            String title,
            String body,
            Uri soundUri,
            NotificationCompat.Action acceptAction,
            NotificationCompat.Action cancelAction) {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setSmallIcon(R.drawable.ic_car)
                .addAction(acceptAction)
                .addAction(cancelAction)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title));
    }

}
