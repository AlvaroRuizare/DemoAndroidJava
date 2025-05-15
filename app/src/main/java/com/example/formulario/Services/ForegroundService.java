package com.example.formulario.Services;

import static android.app.PendingIntent.getActivity;

import android.app.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.formulario.R;
import com.example.formulario.View.Listado;


public class ForegroundService extends Service {
    private static final String TAG = "ForegroundService";
    private static String CHANNEL_ID = "CanalNotifSegundoPlano";
    private static String CHANNEL_DESC = "El canal del servicio en segundo plano de notificaciones";


    @Override
    public void onCreate() {
        super.onCreate();
        crearCanalNotificacion();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        crearMostrarNotificacion();
        return START_STICKY; // Para que el servicio intente reiniciarse si Android lo detiene
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * NOTIFICACION - Crea el canal de la notificación
     */
    private void crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canalNotificacion = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_DESC,
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            canalNotificacion.enableLights(false); // Luz notificación
            canalNotificacion.enableVibration(true); // Habilitar vibración
            canalNotificacion.setShowBadge(false);
            canalNotificacion.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager gestorNotificaciones =
                    (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
            gestorNotificaciones.createNotificationChannel(canalNotificacion);
        }
    }


    /**
     * NOTIFICACION - Crea y muestra la notificación
     */
    private void crearMostrarNotificacion() {
        Intent notificationIntent = new Intent(this, Listado.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.login)
                .setContentTitle("Seguimiento de Ubicación")
                .setContentText("La aplicación está usando tu ubicación...")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setOngoing(true) // Para que no se pueda descartar fácilmente
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE);

        startForeground(1, builder.build());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Servicio destruido");
    }
}
