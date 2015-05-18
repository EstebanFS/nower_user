package castofo_nower.com.co.nower.support;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import castofo_nower.com.co.nower.connection.HttpHandler;
import castofo_nower.com.co.nower.helpers.AlertDialogsResponse;

public class UserFeedback {

  private static AlertDialogsResponse listeningActivity;

  public static final int NO_BUTTON_TO_SHOW = -1;

  // En este punto se determina a qué Activity será enviado el aviso de la
  // elección del usuario.
  public void addListeningActivity(AlertDialogsResponse activity) {
    this.listeningActivity = activity;
  }

  public static void showAlertDialog
  (final Context context, int titleId, String message,
   final int positiveButtonId, final int negativeButtonId, final String action)
  {
    android.app.AlertDialog.Builder builder = new android.app.AlertDialog
                                              .Builder(context);

    builder.setTitle(context.getResources().getString(titleId));
    builder.setMessage(message);

    builder.setPositiveButton(context.getResources()
                              .getString(positiveButtonId),
                              new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                  if (!action.equals(HttpHandler.NO_INTERNET)) {
                                    listeningActivity.notifyUserResponse
                                    (action, positiveButtonId);
                                  }
                                }
                              });

    // Se pregunta para saber si es necesario poner un botón de negación o no.
    if (negativeButtonId != NO_BUTTON_TO_SHOW) {
      builder.setNegativeButton(context.getResources()
                                .getString(negativeButtonId),
                                new DialogInterface.OnClickListener() {
                                  @Override
                                  public void onClick(DialogInterface dialog,
                                                      int which) {
                                    listeningActivity.notifyUserResponse
                                    (action, negativeButtonId);
                                  }
                                });
    }

    final android.app.AlertDialog alertDialog = builder.create();

    alertDialog.show();
  }

  public static void showToastMessage
  (Context context, String message, int duration) {
    Toast.makeText(context, message, duration).show();
  }
}
