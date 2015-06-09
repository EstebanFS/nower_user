package castofo_nower.com.co.nower.controllers;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.GraphResponse;
import com.facebook.login.widget.LoginButton;

import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.connection.HttpHandler;
import castofo_nower.com.co.nower.helpers.FacebookLoginResponse;
import castofo_nower.com.co.nower.helpers.ParsedErrors;
import castofo_nower.com.co.nower.helpers.SubscribedActivities;
import castofo_nower.com.co.nower.support.DateManager;
import castofo_nower.com.co.nower.support.FacebookHandler;
import castofo_nower.com.co.nower.support.RequestErrorsHandler;
import castofo_nower.com.co.nower.support.SharedPreferencesManager;
import castofo_nower.com.co.nower.support.UserFeedback;

public class RegisterFragment extends Fragment implements SubscribedActivities,
ParsedErrors, FacebookLoginResponse {

  private TextView nameView;
  private TextView emailView;
  private TextView passwordView;
  private TextView passwordConfirmationView;
  // Calendario donde se almacena la fecha escogida.
  private static Calendar birthday;
  private TextView birthdayView;

  private String name;
  private String email;
  private String password;
  private String passwordConfirmation;
  private String gender;

  private RadioGroup genderRadio;
  private final int maleOptionId = R.id.male;
  private final int femaleOptionId = R.id.female;

  private FacebookHandler facebookHandler = FacebookHandler.getInstance();
  private LoginButton loginButton;
  private CallbackManager callbackManager;

  private HttpHandler httpHandler = new HttpHandler();
  public static final String ACTION_REGISTER = "/users";
  private Map<String, String> params = new HashMap<String, String>();

  private RequestErrorsHandler requestErrorsHandler = new
          RequestErrorsHandler();

  public static final int NO_GENDER_SELECTED = -1;


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.fragment_register, container,
                                            false);

    httpHandler.addListeningActivity(this);

    requestErrorsHandler.addListeningActivity(this);

    facebookHandler.addListeningActivity(this);

    SharedPreferencesManager.setup(getActivity());

    TextView title = (TextView) layout.findViewById(R.id.register_header);
    Typeface headerFont = Typeface.createFromAsset(getActivity().getAssets(),
            "fonts/exo2_extra_bold.otf");
    title.setTypeface(headerFont);

    nameView = (TextView) layout.findViewById(R.id.name);
    emailView = (TextView) layout.findViewById(R.id.email);
    passwordView = (TextView) layout.findViewById(R.id.password);
    passwordConfirmationView = (TextView)
            layout.findViewById(R.id.password_confirmation);
    birthdayView = (TextView) layout.findViewById(R.id.birthday);
    genderRadio = (RadioGroup) layout.findViewById(R.id.gender);

    /*if (getIntent().hasExtra("email")) {
      String emailFromLogin = getIntent().getExtras().getString("email");
      emailView.setText(emailFromLogin);
    }*/
    initializeFacebookUI(layout);
    return layout;
  }

  private void initializeFacebookUI(View layout) {
    // Inicializar el SDK de Facebook.
    FacebookSdk.sdkInitialize(getActivity().getApplicationContext());

    callbackManager = facebookHandler.getCallbackManagerInstance();

    loginButton = (LoginButton) layout.findViewById(R.id.login_button);
    loginButton.setReadPermissions("public_profile, email");
    loginButton.setFragment(this);
    loginButton.registerCallback
            (callbackManager, facebookHandler.getLoginCallback());
  }

  public void onRegisterClicked() {
    boolean everythingOkToRegister = true;

    name = nameView.getText().toString().trim();
    email = emailView.getText().toString().trim();
    password = passwordView.getText().toString().trim();
    passwordConfirmation = passwordConfirmationView.getText().toString().trim();
    boolean isGenderSelected = validateGender();

    if (name.isEmpty()) {
      nameView.setError(getResources().getString(R.string.write_your_name));
      everythingOkToRegister = false;
    }
    if (email.isEmpty()) {
      emailView.setError(getResources().getString(R.string.write_your_email));
      everythingOkToRegister = false;
    }
    if (password.isEmpty()) {
      passwordView.setError(getResources()
              .getString(R.string.write_your_password));
      everythingOkToRegister = false;
    }
    if (passwordConfirmation.isEmpty()) {
      passwordConfirmationView.setError
              (getResources().getString(R.string.write_your_password_again));
      everythingOkToRegister = false;
    }
    if (birthday == null) {
      birthdayView.setError(getResources()
              .getString(R.string.select_your_birthday));
      everythingOkToRegister = false;
    }
    if (!isGenderSelected) {
      UserFeedback.showToastMessage(getActivity().getApplicationContext(),
              getResources().getString(R.string.select_your_gender),
              Toast.LENGTH_SHORT);
      everythingOkToRegister = false;
    }

    if (everythingOkToRegister) {
      if (password.equals(passwordConfirmation)) {
        setParamsForRegister();
      }
      else {
        UserFeedback.showToastMessage(getActivity().getApplicationContext(),
                getResources().getString(R.string.passwords_not_matching),
                Toast.LENGTH_SHORT);
      }
    }
  }

  public boolean validateGender() {
    int selected = genderRadio.getCheckedRadioButtonId();
    switch (selected) {
      case maleOptionId:
        gender = "m";
        break;
      case femaleOptionId:
        gender = "f";
        break;
      case NO_GENDER_SELECTED:
        //No se seleccionó ninguno.
        return false;
    }
    return true;
  }

  public void showDatePickerDialog() {
    DatePickerFragment newFragment = new DatePickerFragment();
    newFragment.setRegisterFragment(this);
    newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
  }

  public static class DatePickerFragment extends DialogFragment implements
          DatePickerDialog.OnDateSetListener {

    public static final int MAX_AGE = 100;
    public static final int MIN_AGE = 12;
    public static final int MAX_DAY = 31;
    public static final int MIN_DAY = 1;
    private RegisterFragment registerFragment;

    public void setRegisterFragment(RegisterFragment registerFragment) {
      this.registerFragment = registerFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      final Calendar c = Calendar.getInstance();
      int year = c.get(Calendar.YEAR);
      int month = c.get(Calendar.MONTH);
      int day = c.get(Calendar.DAY_OF_MONTH);

      // Se crea una nueva instancia de DatePickerDialog.
      DatePickerDialog datePickerDialog = new DatePickerDialog
              (getActivity(), this, year, month,
                      day);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        DatePicker datePicker = datePickerDialog.getDatePicker();
        Calendar calendarForLimits = Calendar.getInstance();
        // La máxima edad permitida son 100 años.
        calendarForLimits.add(Calendar.YEAR, -MAX_AGE);
        calendarForLimits.set(Calendar.MONTH, Calendar.JANUARY);
        calendarForLimits.set(Calendar.DAY_OF_MONTH, MIN_DAY);
        datePicker.setMinDate(calendarForLimits.getTimeInMillis());
        // La mínima edad permitida son 12 años.
        calendarForLimits.add(Calendar.YEAR, MAX_AGE - MIN_AGE);
        calendarForLimits.set(Calendar.MONTH, Calendar.DECEMBER);
        calendarForLimits.set(Calendar.DAY_OF_MONTH, MAX_DAY);
        datePicker.setMaxDate(calendarForLimits.getTimeInMillis());
      }

      if (birthday != null) {
        datePickerDialog.updateDate(birthday.get(Calendar.YEAR),
                birthday.get(Calendar.MONTH),
                birthday.get(Calendar.DAY_OF_MONTH));
      }
      return datePickerDialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
      Calendar birthday = registerFragment.getBirthday();
      if (birthday == null) birthday = Calendar.getInstance();
      birthday.set(Calendar.YEAR, year);
      birthday.set(Calendar.MONTH, month);
      birthday.set(Calendar.DAY_OF_MONTH, day);
      registerFragment.setBirthday(birthday);
    }
  }

  public Calendar getBirthday() {
    return birthday;
  }

  public void setBirthday(Calendar birthday) {
    this.birthday = birthday;
    updateBirthdayView();
  }

  public void updateBirthdayView() {
    String dateText = DateManager.getDateText(birthday);
    birthdayView.setText(dateText);
  }

  public void setParamsForRegister() {
    params.put("name", name);
    params.put("email", email);
    params.put("password", password);
    params.put("password_confirmation", passwordConfirmation);
    params.put("birthday", DateManager.getBirthdayText(birthday));
    params.put("gender", gender);
    sendRequest(ACTION_REGISTER);
  }

  public void sendRequest(String request) {
    if (request.equals(ACTION_REGISTER)) {
      httpHandler.sendRequest(HttpHandler.NAME_SPACE, ACTION_REGISTER, "",
              params, new HttpPost(), getActivity());
    }
    else if (request.equals(LoginFragment.ACTION_FACEBOOK_LOGIN)) {
      httpHandler.sendRequest(HttpHandler.NAME_SPACE,
              LoginFragment.ACTION_FACEBOOK_LOGIN, "", params,
              new HttpPost(), getActivity());
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    callbackManager.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  public void notifyParsedErrors(String action,
                                 Map<String, String> errorsMessages) {
    switch (action) {
      case ACTION_REGISTER:
        for (Map.Entry<String, String> errorMessage : errorsMessages.entrySet())
        {
          switch (errorMessage.getKey()) {
            case "name":
              nameView.setError(errorsMessages.get("name"));
              break;
            case "email":
              emailView.setError(errorsMessages.get("email"));
              break;
            case "password":
              passwordView.setError(errorsMessages.get("password"));
              break;
            case "password_confirmation":
              passwordConfirmationView.setError(errorsMessages
                      .get("password_confirmation"));
              break;
            case "birthday":
              birthdayView.setError(errorsMessages.get("birthday"));
              break;
            case "gender":
              UserFeedback.showToastMessage(getActivity()
                              .getApplicationContext(),
                      errorsMessages.get("gender"), Toast.LENGTH_SHORT);
              break;
          }
        }
        break;
      case LoginFragment.ACTION_FACEBOOK_LOGIN:
        // Cerrar la sesión de Facebook.
        facebookHandler.logout();
        // Mostrar el primer mensaje de error que llegue.
        for (Map.Entry<String, String> errorMessage : errorsMessages.entrySet())
        {
          UserFeedback
                  .showAlertDialog(getActivity(), R.string.sorry,
                          errorMessage.getValue(), R.string.got_it,
                          UserFeedback.NO_BUTTON_TO_SHOW,
                          LoginFragment.ACTION_FACEBOOK_LOGIN);
          break;
        }
        break;
    }
  }

  @Override
  public void notifyFacebookResponse(JSONObject object, GraphResponse response)
  {
    AccessToken accessToken = facebookHandler.getAccessToken();

    try {
      String name = object.getString("name");
      String email = object.getString("email");
      String gender = object.getString("gender");
      String authToken = accessToken.getToken();
      String facebookId = object.getString("id");
      Date expires = accessToken.getExpires();
      JSONObject ageRange = object.getJSONObject("age_range");
      params = LoginFragment.setParamsForFacebookLogin(params, name, email,
              gender, authToken, facebookId, expires, ageRange);
      sendRequest(LoginFragment.ACTION_FACEBOOK_LOGIN);
    }
    catch (JSONException e) {

    }
  }

  @Override
  public void notify(String action, JSONObject responseJson) {
    try {
      Log.i("responseJson", responseJson.toString());
      int responseStatusCode = responseJson.getInt(HttpHandler.HTTP_STATUS);
      if (action.equals(ACTION_REGISTER)) {
        switch (responseStatusCode) {
          case HttpHandler.CREATED:
            if (responseJson.getBoolean("success")) {
              LoginFragment.startSession(getActivity(), responseJson);
            }
            break;
          case HttpHandler.UNPROCESSABLE_ENTITY:
            RequestErrorsHandler
                    .parseErrors(action, responseJson.getJSONObject("errors"));
            break;
        }
      }
      else if (action.equals(LoginFragment.ACTION_FACEBOOK_LOGIN)) {
        switch (responseStatusCode) {
          case HttpHandler.OK:
            if (responseJson.getBoolean("success")) {
              LoginFragment.startSession(getActivity(), responseJson);
            }
            break;
          case HttpHandler.CREATED:
            if (responseJson.getBoolean("success")) {
              LoginFragment.startSession(getActivity(), responseJson);
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
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    // Inflate the menu; this adds items to the action bar if it is present.
    inflater.inflate(R.menu.menu_register, menu);
    super.onCreateOptionsMenu(menu, inflater);
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
