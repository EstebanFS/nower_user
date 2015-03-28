package castofo_nower.com.co.nower.controllers;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Calendar;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.support.DateManager;

public class RegisterActivity extends FragmentActivity {

    private TextView nameView;
    private TextView emailView;
    private TextView passwordView;
    private TextView passwordConfirmationView;
    private Calendar birthday; //Calendario donde se almacena la fecha escogida
    private TextView birthdayView;

    private RadioGroup genderRadio;
    private final int maleOptionId = R.id.male;
    private final int femaleOptionId = R.id.female;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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

    public void onAlreadyHaveAccountClicked(View v) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("email", emailView.getText().toString());
        startActivity(intent);
        finish();
    }

    public void onRegisterClicked(View v) {
        if (!validateGender()) return;
        Intent intent = new Intent(this, TabsHandler.class);
        startActivity(intent);
        finish();
    }

    public boolean validateGender() {
        int selected = genderRadio.getCheckedRadioButtonId();
        switch (selected) {
            case maleOptionId:
                //Se undió male
                break;
            case femaleOptionId:
                //Se undió female
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
            Calendar birthday = ((RegisterActivity) getActivity()).getBirthday();
            if (birthday == null) birthday = Calendar.getInstance();
            birthday.set(Calendar.YEAR, year);
            birthday.set(Calendar.MONTH, month);
            birthday.set(Calendar.DAY_OF_MONTH, day);
            ((RegisterActivity) getActivity()).setBirthday(birthday);
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
}
