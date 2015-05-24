package castofo_nower.com.co.nower.controllers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.appevents.AppEventsLogger;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.models.User;
import castofo_nower.com.co.nower.support.FacebookHandler;
import castofo_nower.com.co.nower.support.SharedPreferencesManager;

public class SplashActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);
    SharedPreferencesManager.setup(this);

    if (getIntent().getExtras() != null) {
      Intent requestedAction = null;
      switch (getIntent().getExtras().getString("action")) {
        case NowerMap.NO_MAP:
          // La aplicación se debe cerrar porque el usuario decidió salir.
          finish();
          break;
        case UserPromosListFragment.USER_NEEDS_TO_REGISTER:
          // El usuario necesita registrarse o iniciar sesión para poder
          // acceder a las promociones.
          requestedAction = new Intent(SplashActivity.this, Register.class);
          break;
        case Login.OPEN_MAP:
          // El usuario acaba de registrarse o de iniciar sesión.
          requestedAction = new Intent(SplashActivity.this, TabsHandler.class);
          requestedAction.putExtra("source",
                                   SplashActivity.class.getSimpleName());
          break;
        case UserPromosListFragment.LOG_OUT:
          User.clearData();
          FacebookHandler.getInstance().logout();
          SharedPreferencesManager.clearSharedPreferences();
          requestedAction = new Intent(SplashActivity.this, TabsHandler.class);
          requestedAction.putExtra("source",
                                   SplashActivity.class.getSimpleName());
          break;
      }
      if (requestedAction != null) {
        requestedAction.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(requestedAction);
        finish();
      }
    }
    else {
      Thread splashTimer = new Thread() {
        public void run() {
          try {
            // Se muestra el splash durante un segundo.
            sleep(1000);
          } catch (InterruptedException e) {

          } finally {
            // Luego se ingresa propiamente a la aplicación.
            Intent openApp = new Intent(SplashActivity.this, TabsHandler.class);
            openApp.putExtra("source", SplashActivity.class.getSimpleName());
            openApp.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            if (isThereLoginInstance()) updateUserData();
            startActivity(openApp);
            finish();
          }
        }
      };

      splashTimer.start();
    }
  }

  public static boolean isThereLoginInstance() {
    int userId = SharedPreferencesManager
                 .getIntegerValue(SharedPreferencesManager.USER_ID);
    // Un valor diferente de -1 indicaría que el usuario aún tiene sesión
    // activa.
    if (userId == SharedPreferencesManager.NO_VALUE_SAVED) return false;
    else return true;
  }

  public void updateUserData() {
    // Se actualizan los datos del usuario dado que ya tenía sesión activa.
    int id = SharedPreferencesManager.getIntegerValue(SharedPreferencesManager
                                                      .USER_ID);
    String email = SharedPreferencesManager
                   .getStringValue(SharedPreferencesManager.USER_EMAIL);
    String name = SharedPreferencesManager
                  .getStringValue(SharedPreferencesManager.USER_NAME);
    String gender = SharedPreferencesManager
                    .getStringValue(SharedPreferencesManager.USER_GENDER);
    String birthday = SharedPreferencesManager
                      .getStringValue(SharedPreferencesManager.USER_BIRTHDAY);
    String facebookId = SharedPreferencesManager.getStringValue
                        (SharedPreferencesManager.USER_FACEBOOK_ID);

    User.setUserData(id, email, name, gender, birthday, facebookId);
  }

  public static void handleRequest(Context context, String action) {
    boolean isNecessaryToClearStack = false;
    if (action.equals(NowerMap.NO_MAP) || action.equals(Login.OPEN_MAP)
        || action.equals(UserPromosListFragment.LOG_OUT)) {
      isNecessaryToClearStack = true;
    }

    Intent request = new Intent(context, SplashActivity.class);
    request.putExtra("action", action);

    if (isNecessaryToClearStack) {
      request.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                       | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    context.startActivity(request);

    if (isNecessaryToClearStack) ((Activity) context).finish();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_splash, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onResume() {
    super.onResume();
    // Facebook: Logs 'install' and 'app activate' App Events.
    AppEventsLogger.activateApp(this);
  }

  @Override
  protected void onPause() {
    super.onPause();
    // Facebook: Logs 'app deactivate' App Event.
    AppEventsLogger.deactivateApp(this);
  }
}
