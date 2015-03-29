package castofo_nower.com.co.nower.controllers;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.connection.HttpHandler;
import castofo_nower.com.co.nower.helpers.SubscribedActivities;
import castofo_nower.com.co.nower.models.User;
import castofo_nower.com.co.nower.support.DateManager;
import castofo_nower.com.co.nower.support.SharedPreferencesManager;


public class Register extends FragmentActivity implements SubscribedActivities{

    private TextView nameView;
    private TextView emailView;
    private TextView passwordView;
    private TextView passwordConfirmationView;
    // Calendario donde se almacena la fecha escogida.
    private Calendar birthday;
    private TextView birthdayView;

    private String name;
    private String email;
    private String password;
    private String passwordConfirmation;
    private String gender;


    private RadioGroup genderRadio;
    private final int maleOptionId = R.id.male;
    private final int femaleOptionId = R.id.female;

    private HttpHandler httpHandler = new HttpHandler();
    public static final String ACTION_REGISTER = "/users";
    private Map<String, String> params = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        httpHandler.addListeningActivity(this);

        SharedPreferencesManager.setup(this);

        TextView title = (TextView) findViewById(R.id.register_header);
        Typeface headerFont = Typeface.createFromAsset(getAssets(),
                "fonts/exo2_extra_bold.otf");
        title.setTypeface(headerFont);

        nameView = (TextView) findViewById(R.id.name);
        emailView = (TextView) findViewById(R.id.email);
        passwordView = (TextView) findViewById(R.id.password);
        passwordConfirmationView = (TextView)
                findViewById(R.id.password_confirmation);
        birthdayView = (TextView) findViewById(R.id.birthday);
        genderRadio = (RadioGroup) findViewById(R.id.gender);

        if (getIntent().hasExtra("email")) {
            String emailFromLogin = getIntent().getExtras().getString("email");
            emailView.setText(emailFromLogin);
        }
    }

    public void onAlreadyHaveAccountClicked(View v) {
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("email", emailView.getText().toString());
        startActivity(intent);
        finish();
    }

    public void onRegisterClicked(View v) {
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
            passwordView.setError(getResources().getString(R.string.write_your_password));
            everythingOkToRegister = false;
        }
        if (passwordConfirmation.isEmpty()) {
            passwordConfirmationView.setError(getResources()
                                              .getString(R.string.write_your_password_again));
            everythingOkToRegister = false;
        }
        if (birthday == null) {
            birthdayView.setError(getResources().getString(R.string.select_your_birthday));
            everythingOkToRegister = false;
        }
        if (!isGenderSelected) {
            Toast.makeText(getApplicationContext(),
                           getResources().getString(R.string.select_your_gender),
                           Toast.LENGTH_SHORT).show();
            everythingOkToRegister = false;
        }

        if (everythingOkToRegister) {
            if (password.equals(passwordConfirmation)) {
                setParamsForRegister();
            }
            else {
                Toast.makeText(getApplicationContext(),
                               getResources().getString(R.string.passwords_not_matching),
                               Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean validateGender() {
        int selected = genderRadio.getCheckedRadioButtonId();
        switch (selected) {
            case maleOptionId:
                gender = "1";
                break;
            case femaleOptionId:
                gender = "0";
                break;
            case -1:
                //No se seleccionó ninguno
                return false;
        }
        return true;
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar birthday = ((Register) getActivity()).getBirthday();
            if (birthday == null) birthday = Calendar.getInstance();
            birthday.set(Calendar.YEAR, year);
            birthday.set(Calendar.MONTH, month);
            birthday.set(Calendar.DAY_OF_MONTH, day);
            ((Register) getActivity()).setBirthday(birthday);
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
        register();
    }

    public void register() {
        if (httpHandler.isInternetConnectionAvailable(this)) {
            httpHandler.sendRequest(HttpHandler.API_V1, ACTION_REGISTER, "", params, new HttpPost(),
                                    Register.this);
        } else {
            Toast.makeText(getApplicationContext(),
                           getResources().getString(R.string.internet_connection_required),
                           Toast.LENGTH_SHORT).show();
        }
    }

    public void saveUserData(int id, String email, String name, String gender, String birthday) {
        // Se almacenan los datos del usuario que acaba de registrarse.
        SharedPreferencesManager
        .saveIntegerValue(SharedPreferencesManager.USER_ID, id);
        SharedPreferencesManager
        .saveStringValue(SharedPreferencesManager.USER_EMAIL, email);
        SharedPreferencesManager
        .saveStringValue(SharedPreferencesManager.USER_NAME, name);
        SharedPreferencesManager
        .saveStringValue(SharedPreferencesManager.USER_GENDER, gender);
        SharedPreferencesManager
        .saveStringValue(SharedPreferencesManager.USER_BIRTHDAY, birthday);

        User.setUserData(id, email, name, gender, birthday);
    }

    public void openNowerMap() {
        Intent openMap = new Intent(Register.this, TabsHandler.class);
        startActivity(openMap);
        finish();
    }

    @Override
    public void notify(String action, JSONObject responseJson) {
        try {
            if (action.equals(ACTION_REGISTER)) {
                Log.i("responseJson", responseJson.toString());
                if (responseJson.getInt(HttpHandler.HTTP_STATUS) == HttpHandler.SUCCESS) {
                    if (responseJson.getBoolean("success")) {
                        JSONObject user = responseJson.getJSONObject("user");
                        int id = user.getInt("id");
                        String email = user.getString("email");
                        String name = user.getString("name");
                        boolean gender = user.getBoolean("gender");
                        String birthday = user.getString("birthday");

                        // Es necesario convertir el género a String para poderlo almacenar.
                        String genderString = "0";
                        if (gender) genderString = "1";

                        saveUserData(id, email, name, genderString, birthday);

                        openNowerMap();
                    }
                    else {
                        JSONObject errors = responseJson.getJSONObject("errors");
                        JSONArray email = errors.getJSONArray("email");
                        String emailError = email.get(0).toString();
                        if (emailError.equalsIgnoreCase("has already been taken")) {
                            emailView.setError(getResources()
                                               .getString(R.string.email_already_taken));
                        }
                        else {
                            Toast.makeText(getApplicationContext(),
                                           getResources().getString(R.string.register_error),
                                           Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            params.clear();

        } catch (JSONException e) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
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
