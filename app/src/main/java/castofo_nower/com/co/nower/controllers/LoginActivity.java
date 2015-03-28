package castofo_nower.com.co.nower.controllers;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import castofo_nower.com.co.nower.R;


public class LoginActivity extends Activity {

    private TextView emailView;
    private TextView passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView title = (TextView) findViewById(R.id.login_header);
        Typeface headerFont = Typeface.createFromAsset(getAssets(),
                "fonts/exo2_extra_bold.otf");
        title.setTypeface(headerFont);

        emailView = (TextView) findViewById(R.id.email);
        passwordView = (TextView) findViewById(R.id.password);

        if (getIntent().hasExtra("email")) {
            String emailFromRegister = getIntent()
                    .getExtras().getString("email");
            emailView.setText(emailFromRegister);
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

    public void onDontHaveAccountClicked(View v) {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("email", emailView.getText().toString());
        startActivity(intent);
        finish();
    }

    public void onLoginClicked(View v) {
        Intent intent = new Intent(this, TabsHandler.class);
        startActivity(intent);
        finish();
    }
}
