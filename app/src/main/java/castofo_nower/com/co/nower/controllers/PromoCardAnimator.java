package castofo_nower.com.co.nower.controllers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.CountDownTimer;
import android.support.v4.view.GestureDetectorCompat;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.connection.HttpHandler;
import castofo_nower.com.co.nower.helpers.AlertDialogsResponses;
import castofo_nower.com.co.nower.helpers.SubscribedActivities;
import castofo_nower.com.co.nower.models.MapData;
import castofo_nower.com.co.nower.models.Promo;
import castofo_nower.com.co.nower.models.Redemption;
import castofo_nower.com.co.nower.models.User;
import castofo_nower.com.co.nower.support.AlertDialogCreator;
import castofo_nower.com.co.nower.support.DateManager;

import com.manuelpeinado.fadingactionbar.FadingActionBarHelper;


public class PromoCardAnimator extends Activity implements SubscribedActivities,
                                                           AlertDialogsResponses,
                                                           GestureDetector.OnGestureListener {

  private GestureDetectorCompat gestureDetector;
  private ViewFlipper promosFlipper;

  Animation slide_in_left, slide_out_right, slide_in_right, slide_out_left;

  private HttpHandler httpHandler = new HttpHandler();
  public static final String ACTION_PROMOS_DETAILS = "/promos/details";
  public static final String ACTION_NOW = "/promo/now";
  private Map<String, String> params = new HashMap<String, String>();

  private TextView promoTitle;
  private TextView promoStoreName;
  private TextView promoAvailableRedemptions;
  private TextView promoDescription;
  private TextView promoTerms;

  private Button nowButton;
  private TextView redemptionCode;

  private AlertDialogCreator alertDialogCreator = new AlertDialogCreator();

  // Indicador para saber qué acción se está tratando de ejecutar.
  public static String action;

  private int branchId;
  private String code;
  private String storeName;
  private boolean isUserPromoRedeemed;
  private ArrayList<Promo> promos = new ArrayList<>();
  private Map<Integer, Redemption> userPromos = new TreeMap<>();
  private Map<Integer, Promo> promosMap = new TreeMap<>();

  public static final String TAKE_PROMO = "TAKE_PROMO";
  public static final String OBTAINED_PROMO = "OBTAINED_PROMO";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initView();

    gestureDetector = new GestureDetectorCompat(this, this);


    // Se indica al HttpHandler la actividad que estará esperando la respuesta a la petición.
    httpHandler.addListeningActivity(this);

    // Se indica al AlertDialogCreator la actividad que estará esperando la respuesta de la
    // elección del usuario..
    alertDialogCreator.addListeningActivity(this);

    setFlipperAnimation();
    capturePromos();
    setPromosIdsList();
    sendRequest(ACTION_PROMOS_DETAILS);
  }

  public void initView() {
    FadingActionBarHelper helper = new FadingActionBarHelper()
                                   .actionBarBackground(R.drawable.ab_background)
                                   .headerLayout(R.layout.header)
                                   .contentLayout(R.layout.activity_promo_card_animator);
    setContentView(helper.createView(this));
    ImageView headerImage = ((ImageView) findViewById(R.id.header_image));
    headerImage.setImageResource(R.drawable.promo);
    helper.initActionBar(this);
    getActionBar().setDisplayHomeAsUpEnabled(true);

    promosFlipper = (ViewFlipper) findViewById(R.id.promos_flipper);
  }

  public void setFlipperAnimation() {
    slide_in_left = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
    slide_out_right = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
    slide_in_right = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
    slide_out_left = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
  }

  public void capturePromos() {
    promos.clear();
    userPromos.clear();
    action = getIntent().getExtras().getString("action");
    if (action.equals(NowerMap.SHOW_BRANCH_PROMOS)) {
      branchId = getIntent().getExtras().getInt("branch_id");
      storeName = MapData.branchesMap.get(branchId).getStoreName();
      // En este punto se capturan las promociones correspondientes al establecimiento seleccionado
      // por el usuario.
      setBranchPromos();

      // Estas promociones se capturan para saber cuáles deben mostrarse con botón y cuáles con
      // código.
      userPromos = User.takenPromos;
    }
    else if (action.equals(UserPromoList.SHOW_PROMO_TO_REDEEM)) {
      int promoId = getIntent().getExtras().getInt("promo_id");
      code = User.takenPromos.get(promoId).getCode();
      isUserPromoRedeemed = User.takenPromos.get(promoId).isRedeemed();
      branchId = getIntent().getExtras().getInt("branch_id");
      storeName = MapData.branchesMap.get(branchId).getStoreName();
      Promo promo = MapData.promosMap.get(promoId);
      // En este punto se captura la promoción que desea redimir el usuario.
      promos.add(promo);
    }
  }

  public void setBranchPromos() {
    ArrayList<Integer> promosIds = MapData.branchesMap.get(branchId).getPromosIds();
    for (Integer promoId : promosIds) {
      promos.add(MapData.promosMap.get(promoId));
    }
  }

  // Se construye la lista de promociones que debe ser actualizada localmente.
  public void setPromosIdsList() {
    String pIdsList = "[";
    for (int i = 0; i < promos.size(); ++i) {
      pIdsList += ("{\"id\": " + promos.get(i).getId() + "},");
    }
    pIdsList = pIdsList.substring(0, pIdsList.length() - 1) + "]";
    // El String que se envía podrá ser traducido fácilmente a JSONArray.
    params.put("promos_ids_list", pIdsList);
  }

  public void sendRequest(String request) {
    if (httpHandler.isInternetConnectionAvailable(this)) {
      if (request.equals(ACTION_PROMOS_DETAILS)) {
        httpHandler.sendRequest(HttpHandler.API_V1, ACTION_PROMOS_DETAILS, "", params,
                                new HttpPost(), PromoCardAnimator.this);
      }
      else if (request.equals(ACTION_NOW)) {
        httpHandler.sendRequest(HttpHandler.API_V1, ACTION_NOW, "", params, new HttpPost(),
                                PromoCardAnimator.this);
      }
    }
    else {
      Toast.makeText(getApplicationContext(),
                     getResources().getString(R.string.internet_connection_required),
                     Toast.LENGTH_SHORT).show();
    }
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
      promoStoreName = (TextView) promoCard.findViewById(R.id.promo_store_name);
      final TextView promoExpirationDate = (TextView)
                                           promoCard.findViewById(R.id.promo_expiration_date);
      promoAvailableRedemptions = (TextView) promoCard
                                  .findViewById(R.id.promo_available_redemptions);
      promoDescription = (TextView) promoCard.findViewById(R.id.promo_description);
      promoTerms = (TextView) promoCard.findViewById(R.id.promo_terms);

      nowButton = (Button) promoCard.findViewById(R.id.now_button);
      redemptionCode = (TextView) promoCard.findViewById(R.id.redemption_code);

      // Se modifica la información de la promoción a mostrar.
      promoTitle.setText(promo.getTitle());
      promoStoreName.setText(storeName);
      promoExpirationDate.setText(promo.getExpirationDate());
      promoAvailableRedemptions.setText(String.valueOf(promo.getAvailableRedemptions()));
      promoDescription.setText(promo.getDescription());
      promoTerms.setText(promo.getTerms());

      if (action.equals(NowerMap.SHOW_BRANCH_PROMOS)) {
        if (userPromos.containsKey(promo.getId())) {
          // El usuario no debería poder tomar esta promoción porque ya la tiene.
          code = User.takenPromos.get(promo.getId()).getCode();
          isUserPromoRedeemed = User.takenPromos.get(promo.getId()).isRedeemed();
          changeButtonToCode();
        }
      }
      else if (action.equals(UserPromoList.SHOW_PROMO_TO_REDEEM)) {
        changeButtonToCode();
      }

      CountDownTimer countDownTimer = createCountDownTimer(promoExpirationDate,
                                                           promo.getExpirationDate());
      countDownTimer.start();
    }
  }

  public CountDownTimer createCountDownTimer(final TextView countDownView,
                                             String expirationDate) {
    long millisUntilFinished, promoDeadLine;
    try {
      promoDeadLine = DateManager.getTimeStamp(expirationDate);
    } catch (ParseException e) {
      return null;
    }

    Date currentTime = new Date();
    millisUntilFinished = promoDeadLine - currentTime.getTime();

    CountDownTimer countDownTimer = new CountDownTimer(millisUntilFinished, 1000) {
      @Override
      public void onTick(long millisUntilFinished) {
        long seconds = millisUntilFinished / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        countDownView.setText(String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60));
      }

      @Override
      public void onFinish() {
        countDownView.setText(getResources()
                .getString(R.string.promo_expired));
      }
    };
    return countDownTimer;
  }

  public void changeButtonToCode() {
    // El usuario está tratando de visualizar una promoción que ya obtuvo.
    // Por eso, desaparece el botón de Now y aparece el código para redimirla.
    nowButton.setVisibility(View.GONE);
    redemptionCode.setText(code);
    // En caso de tratarse de una promoción ya redimida por el usuario, el código aparece en color
    // gris.
    if (isUserPromoRedeemed) redemptionCode.setTextColor(getResources().getColor(R.color.gray));
    redemptionCode.setVisibility(View.VISIBLE);
  }

  public void now(View v) {
    params.put("promo_id", String.valueOf(promosFlipper.getCurrentView().getId()));
    params.put("user_id", String.valueOf(User.id));
    askToTakePromo();
  }

  public void askToTakePromo() {
    // Se muestra un diálogo al usuario para que decida si desea tomar la promoción actual o no.
    AlertDialog promoNowAD = AlertDialogCreator
                             .createAlertDialog(this, R.string.promo, R.string.confirm_taking_promo,
                                     R.string._continue, R.string.cancel, TAKE_PROMO);
    promoNowAD.show();
  }

  public void showObtainedPromo() {
    // Se muestra un diálogo al usuario para indicarle que ya ha sido acreedor de la promoción.
    AlertDialog promoObtainedAD = AlertDialogCreator
                                  .createAlertDialog(this, R.string.promo_obtained,
                                                     R.string.promo_now_in_list, R.string.ok,
                                                     AlertDialogCreator.NO_BUTTON_TO_SHOW,
                                                     OBTAINED_PROMO);
    promoObtainedAD.show();
  }

  @Override
  public void notifyToRespond(String action) {
    if (action.equals(TAKE_PROMO)) {
      sendRequest(ACTION_NOW);
    }
  }

  @Override
  public void notify(String action, JSONObject responseJson) {
    try {
      if (action.equals(ACTION_PROMOS_DETAILS)) {
        Log.i("responseJson", responseJson.toString());
        if (responseJson.getInt(HttpHandler.HTTP_STATUS) == HttpHandler.SUCCESS) {
          promos.clear();
          promosMap.clear();

          JSONArray promosDescriptionAndTerms = responseJson.getJSONArray("promos");
          for (int i = 0; i < promosDescriptionAndTerms.length(); ++i) {
            JSONObject internPromo = promosDescriptionAndTerms.getJSONObject(i);
            int promoId = internPromo.getInt("id");
            String title = internPromo.getString("title");
            String expirationDate = internPromo.getString("expiration_date");
            int availableRedemptions = internPromo.getInt("available_redemptions");
            String description = internPromo.getString("description");
            String terms = internPromo.getString("terms");

            // Se genera la lista de promociones para ser actualizada localmente, ya incluyendo
            // descripción y términos.
            Promo promo = new Promo(promoId, title, expirationDate, availableRedemptions,
                                    description, terms);
            promos.add(promo);
            promosMap.put(promo.getId(), promo);
          }

          // Se actualiza la lista de promociones para el establecimiento en cuestión.
          MapData.setPromosMap(promosMap);

          addPromosToFlipper();
        }
      } else if (action.equals(ACTION_NOW)) {
        Log.i("responseJson", responseJson.toString());
        if (responseJson.getInt(HttpHandler.HTTP_STATUS) == HttpHandler.SUCCESS) {
          if (responseJson.getBoolean("success")) {
            JSONObject redemption = responseJson.getJSONObject("redemption");
            String code = redemption.getString("code");
            int promoId = redemption.getInt("promo_id");
            int user_id = redemption.getInt("user_id");
            boolean redeemed = redemption.getBoolean("redeemed");

            Redemption r = new Redemption(code, promoId, redeemed);
            // Se adiciona la promoción a la lista de promociones del usuario.
            User.addPromoToTakenPromos(promoId, r);

            // Se ponen estos valores en las variables como los actuales para poder simular la
            // obtención de la promoción para el usuario.
            this.code = code;
            isUserPromoRedeemed = redeemed;

            // Se capturan botón y código de la vista actual para realizar el intercambio.
            nowButton = (Button) promosFlipper.getCurrentView().findViewById(R.id.now_button);
            redemptionCode = (TextView) promosFlipper.getCurrentView()
                             .findViewById(R.id.redemption_code);
            changeButtonToCode();

            showObtainedPromo();
          } else {
            Toast.makeText(getApplicationContext(),
                           getResources().getString(R.string.take_promo_error),
                           Toast.LENGTH_SHORT).show();
            //TODO acciones cuando no se pudo tomar la promoción.
          }
        }
      }

      params.clear();

    } catch (JSONException e) {

    }
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent event) {
    super.dispatchTouchEvent(event);
    return gestureDetector.onTouchEvent(event);
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
    float sensitivity = 90;

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
    if (id == android.R.id.home) {
      finish();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

}
