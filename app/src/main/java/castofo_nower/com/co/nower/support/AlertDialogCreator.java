package castofo_nower.com.co.nower.support;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;


public class AlertDialogCreator {

    public static AlertDialog createAlertDialog(final Context context, int titleId, int messageId,
                                                int positiveButtonId, int negativeButtonId,
                                                final String action) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);

        builder.setTitle(context.getResources().getString(titleId));
        builder.setMessage(context.getResources().getString(messageId));

        builder.setPositiveButton(context.getResources().getString(positiveButtonId),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (action) {
                            case Geolocation.ENABLE_GPS:
                                // El usuario aceptó activar su GPS y se le redirige a
                                // la configuración del dispositivo.
                                Intent intent = new Intent(Settings
                                                           .ACTION_LOCATION_SOURCE_SETTINGS);
                                ((Activity) context).startActivityForResult(intent,
                                                                            Geolocation
                                                                            .ENABLE_GPS_CODE);
                                break;

                        }
                    }
                });

        builder.setNegativeButton(context.getResources().getString(negativeButtonId),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (action) {
                            case Geolocation.ENABLE_GPS:
                                // No es necesario ejecutar ninguna acción si el usuario no decidió
                                // activar su GPS.
                                break;
                        }
                    }
                });

        final android.app.AlertDialog alertDialog = builder.create();

        return alertDialog;
    }
}
