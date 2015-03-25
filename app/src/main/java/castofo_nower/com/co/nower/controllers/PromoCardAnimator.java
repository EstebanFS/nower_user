package castofo_nower.com.co.nower.controllers;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.connection.HttpHandler;
import castofo_nower.com.co.nower.helpers.SubscribedActivities;
import castofo_nower.com.co.nower.models.MapData;
import castofo_nower.com.co.nower.models.Promo;
import castofo_nower.com.co.nower.models.User;


public class PromoCardAnimator extends ActionBarActivity implements SubscribedActivities,
                                                                    GestureDetector
                                                                    .OnGestureListener {

    private GestureDetectorCompat gestureDetector;
    private ViewFlipper promosFlipper;

    Animation slide_in_left, slide_out_right, slide_in_right, slide_out_left;

    private HttpHandler httpHandler = new HttpHandler();
    public static final String ACTION_NOW = "/promo/now";
    private Map<String, String> params = new HashMap<String, String>();

    private TextView promoTitle;
    private TextView promoExpirationDate;
    private TextView promoAvailableRedemptions;

    private ArrayList<Promo> promos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promo_card_animator);
        gestureDetector = new GestureDetectorCompat(this, this);
        promosFlipper = (ViewFlipper) findViewById(R.id.promos_flipper);

        // Se indica al HttpHandler la actividad que estará esperando la respuesta a la petición.
        httpHandler.addListeningActivity(this);

        setFlipperAnimation();
        capturePromos();
        addPromosToFlipper();
    }

    public void setFlipperAnimation() {
        slide_in_left = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        slide_out_right = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
        slide_in_right = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        slide_out_left = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
    }

    public void capturePromos() {
        int branchId = getIntent().getExtras().getInt("branch_id");
        // En este punto se capturan las promociones correspondientes al establecimiento
        // seleccionado por el usuario.
        promos = MapData.getPromoList(branchId);
    }

    public void addPromosToFlipper() {
        for (int i = 0; i < promos.size(); ++i) {
            Promo promo = promos.get(i);
            LayoutInflater inflater = (LayoutInflater)
                                      this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View promoCard = inflater.inflate(R.layout.promo_card, null);
            // Con este ID se identificará la promoción actual que visualiza el usuario.
            promoCard.setId(promo.getId());

            // Se agrega una nueva tarjeta de promoción.
            promosFlipper.addView(promoCard);

            // Se capturan los campos que se van a modificar.
            promoTitle = (TextView) promoCard.findViewById(R.id.promo_title);
            promoExpirationDate = (TextView) promoCard.findViewById(R.id.promo_expiration_date);
            promoAvailableRedemptions = (TextView) promoCard
                                        .findViewById(R.id.promo_available_redemptions);

            // Se modifica la información de la promoción a mostrar.
            promoTitle.setText(promo.getTitle());
            promoExpirationDate.setText(promo.getExpirationDate());
            promoAvailableRedemptions.setText(String.valueOf(promo.getAvailableRedemptions()));
        }
    }

    public void now(View v) {
        params.put("promo_id", String.valueOf(promosFlipper.getCurrentView().getId()));
        params.put("user_id", String.valueOf(User.id));
        takePromo();
    }

    public void takePromo() {
        if (httpHandler.isInternetConnectionAvailable(this)){
            httpHandler.sendRequest(HttpHandler.API_V1, ACTION_NOW, "", params, new HttpPost(),
                                    PromoCardAnimator.this);
        }
        else {
            Toast.makeText(getApplicationContext(),
                           getResources().getString(R.string.internet_connection_required),
                           Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void notify(String action, JSONObject responseJson) {
        try {
            if (action.equals(ACTION_NOW)) {
                Log.i("responseJson", responseJson.toString());
                if (responseJson.getInt(HttpHandler.HTTP_STATUS) == HttpHandler.SUCCESS) {
                    if (responseJson.getBoolean("success")) {
                        JSONObject redemption = responseJson.getJSONObject("redemption");
                        Toast.makeText(getApplicationContext(), redemption.getString("code"),
                                       Toast.LENGTH_LONG).show();
                    }
                }
            }
        } catch (JSONException e) {

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float sensitivity = 50;

        // No es necesario pasar de promoción si solamente existe una.
        if (promosFlipper.getChildCount() > 1) {
            // Fling de izquierda a derecha.
            if ((e1.getX() - e2.getX()) > sensitivity) {
                promosFlipper.setInAnimation(slide_in_right);
                promosFlipper.setOutAnimation(slide_out_left);
                promosFlipper.showPrevious();
            }
            // Fling de derecha a izquierda.
            else if ((e2.getX() - e1.getX()) > sensitivity) {
                promosFlipper.setInAnimation(slide_in_left);
                promosFlipper.setOutAnimation(slide_out_right);
                promosFlipper.showNext();
            }
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_promo_card_animation, menu);
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
