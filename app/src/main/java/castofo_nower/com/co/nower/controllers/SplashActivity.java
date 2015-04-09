package castofo_nower.com.co.nower.controllers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.models.User;
import castofo_nower.com.co.nower.support.SharedPreferencesManager;


public class SplashActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);

    SharedPreferencesManager.setup(this);

    Thread splashTimer = new Thread() {
      public void run() {
        try {
          // Se muestra el splash durante un segundo.
          sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        } finally {
          // Luego se ingresa propiamente a la aplicación.
          Intent openApp = new Intent(SplashActivity.this, Login.class);
          if (isThereLoginInstance()) {
            updateUserData();
            openApp = new Intent(SplashActivity.this, TabsHandler.class);
          }
          startActivity(openApp);
          finish();
        }
      }
    };

    splashTimer.start();
  }

  public boolean isThereLoginInstance() {
    int userId = SharedPreferencesManager.getIntegerValue(SharedPreferencesManager.USER_ID);
    // Un valor diferente de -1 indicaría que el usuario aún tiene sesión activa.
    if (userId == -1) return false;
    else return true;
  }

  public void updateUserData() {
    // Se actualizan los datos del usuario dado que ya tenía sesión activa.
    int id = SharedPreferencesManager.getIntegerValue(SharedPreferencesManager.USER_ID);
    String email = SharedPreferencesManager.getStringValue(SharedPreferencesManager.USER_EMAIL);
    String name = SharedPreferencesManager.getStringValue(SharedPreferencesManager.USER_NAME);
    String gender = SharedPreferencesManager.getStringValue(SharedPreferencesManager.USER_GENDER);
    String birthday = SharedPreferencesManager
                      .getStringValue(SharedPreferencesManager.USER_BIRTHDAY);

    User.setUserData(id, email, name, gender, birthday, null);
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

}
