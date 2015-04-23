package castofo_nower.com.co.nower.controllers;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.connection.HttpHandler;
import castofo_nower.com.co.nower.helpers.ParsedErrors;
import castofo_nower.com.co.nower.helpers.SubscribedActivities;
import castofo_nower.com.co.nower.models.User;
import castofo_nower.com.co.nower.support.RequestErrorsHandler;
import castofo_nower.com.co.nower.support.UserFeedback;
import castofo_nower.com.co.nower.support.SharedPreferencesManager;


public class Login extends Activity implements SubscribedActivities,
ParsedErrors {

  private TextView emailView;
  private TextView passwordView;

  private String email;
  private String password;

  private HttpHandler httpHandler = new HttpHandler();
  public static final String ACTION_LOGIN = "/users/login";
  private Map<String, String> params = new HashMap<String, String>();

  private RequestErrorsHandler requestErrorsHandler = new
                                                      RequestErrorsHandler();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    httpHandler.addListeningActivity(this);

    requestErrorsHandler.addListeningActivity(this);

    SharedPreferencesManager.setup(this);

    TextView title = (TextView) findViewById(R.id.login_header);
    Typeface headerFont = Typeface.createFromAsset(getAssets(),
                                                   "fonts/exo2_extra_bold.otf");
    title.setTypeface(headerFont);

    emailView = (TextView) findViewById(R.id.email);
    passwordView = (TextView) findViewById(R.id.password);

    if (getIntent().hasExtra("email")) {
      String emailFromRegister = getIntent().getExtras().getString("email");
      emailView.setText(emailFromRegister);
    }
  }

  public void onDontHaveAccountClicked(View v) {
    Intent intent = new Intent(this, Register.class);
    intent.putExtra("email", emailView.getText().toString());
    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    startActivity(intent);
    finish();
  }

  public void onLoginClicked(View v) {
    email = emailView.getText().toString().trim();
    password = passwordView.getText().toString().trim();

    if (email.isEmpty() && password.isEmpty()) {
      emailView.setError(getResources().getString(R.string.write_your_email));
      passwordView.setError(getResources()
                            .getString(R.string.write_your_password));
    }
    else if (email.isEmpty() && !password.isEmpty()) {
      emailView.setError(getResources().getString(R.string.write_your_email));
    }
    else if (!email.isEmpty() && password.isEmpty()) {
      passwordView.setError(getResources()
                            .getString(R.string.write_your_password));
    }
    else {
      setParamsForLogin();
    }
  }

  public void setParamsForLogin() {
    params.put("email", email);
    params.put("password", password);
    sendRequest(ACTION_LOGIN);
  }

  public void sendRequest(String request) {
    if (request.equals(ACTION_LOGIN)) {
      httpHandler.sendRequest(HttpHandler.NAME_SPACE, ACTION_LOGIN, "", params,
                              new HttpPost(), Login.this);
    }
  }

  public void saveUserData(int id, String email, String name, String gender,
                           String birthday) {
    // Se almacenan los datos del usuario que acaba de autenticarse.
    SharedPreferencesManager.saveIntegerValue(SharedPreferencesManager
                                              .USER_ID, id);
    SharedPreferencesManager.saveStringValue(SharedPreferencesManager
                                             .USER_EMAIL, email);
    SharedPreferencesManager.saveStringValue(SharedPreferencesManager
                                             .USER_NAME, name);
    SharedPreferencesManager.saveStringValue(SharedPreferencesManager
                                             .USER_GENDER, gender);
    SharedPreferencesManager.saveStringValue(SharedPreferencesManager
                                             .USER_BIRTHDAY, birthday);

    User.setUserData(id, email, name, gender, birthday);
  }

  public void openNowerMap() {
    Intent openMap = new Intent(Login.this, TabsHandler.class);
    openMap.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    startActivity(openMap);
    finish();
  }

  @Override
  public void notifyParsedErrors(String action,
                                 Map<String, String> errorsMessages) {
    switch (action) {
      case ACTION_LOGIN:
        if (errorsMessages.containsKey("login")) {
          UserFeedback.showToastMessage(getApplicationContext(),
                                        errorsMessages.get("login"),
                                        Toast.LENGTH_SHORT);
        }
        break;
    }
  }

  @Override
  public void notify(String action, JSONObject responseJson) {
    try {
      Log.i("responseJson", responseJson.toString());
      int responseStatusCode = responseJson.getInt(HttpHandler.HTTP_STATUS);
      if (action.equals(ACTION_LOGIN)) {
        switch (responseStatusCode) {
          case HttpHandler.OK:
            if (responseJson.getBoolean("success")) {
              String token = responseJson.getString("token");
              JSONObject user = responseJson.getJSONObject("user");
              int id = user.getInt("id");
              String email = user.getString("email");
              String name = user.getString("name");
              String gender = user.getString("gender");
              String birthday = user.getString("birthday");

              saveUserData(id, email, name, gender, birthday);

              openNowerMap();
            }
            break;
          case HttpHandler.BAD_REQUEST:
            RequestErrorsHandler
            .parseErrors(action, responseJson.getJSONObject("errors"));
            break;
        }
      }

      params.clear();

    }
    catch (JSONException e) {

    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_login, menu);
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
