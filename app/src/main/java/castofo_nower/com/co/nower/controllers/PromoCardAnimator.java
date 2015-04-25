package castofo_nower.com.co.nower.controllers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ProgressBar;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;


import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.connection.HttpHandler;
import castofo_nower.com.co.nower.helpers.AlertDialogsResponses;
import castofo_nower.com.co.nower.helpers.ParsedErrors;
import castofo_nower.com.co.nower.helpers.SubscribedActivities;
import castofo_nower.com.co.nower.models.MapData;
import castofo_nower.com.co.nower.models.Promo;
import castofo_nower.com.co.nower.models.Redemption;
import castofo_nower.com.co.nower.models.User;
import castofo_nower.com.co.nower.support.ImageDownloader;
import castofo_nower.com.co.nower.support.RequestErrorsHandler;
import castofo_nower.com.co.nower.support.UserFeedback;
import castofo_nower.com.co.nower.support.DateManager;

import com.manuelpeinado.fadingactionbar.FadingActionBarHelper;


public class PromoCardAnimator extends Activity implements SubscribedActivities,
GestureDetector.OnGestureListener, AlertDialogsResponses, ParsedErrors {

  private HttpHandler httpHandler = new HttpHandler();
  public static final String ACTION_PROMOS_DETAILS = "/promos/details";
  public static final String ACTION_NOW = "/promo/now";
  private Map<String, String> params = new HashMap<String, String>();

  private GestureDetectorCompat gestureDetector;
  private ViewFlipper promosFlipper;

  Animation slide_in_left, slide_out_right, slide_in_right, slide_out_left;

  private UserFeedback userFeedback = new UserFeedback();

  private RequestErrorsHandler requestErrorsHandler = new
                                                      RequestErrorsHandler();

  private ProgressBar storeLogoProgress;
  private ImageView storeLogo;
  private TextView promoTitle;
  private TextView promoStoreName;
  private TextView promoAvailableRedemptions;
  private TextView promoDescription;
  private TextView promoTerms;
  private TextView emptyPromosMessage;

  private TextView redemptionCode;

  // Indicador para saber qué acción se está tratando de ejecutar.
  public static String action;

  private int branchId;
  private String code;
  private String storeName;
  private boolean isUserPromoRedeemed;
  private ArrayList<Promo> promos = new ArrayList<>();
  private Map<Integer, Redemption> userPromos = new LinkedHashMap<>();
  private Map<Integer, Promo> promosMap = new TreeMap<>();

  public static final String TAKE_PROMO = "TAKE_PROMO";
  public static final String OBTAINED_PROMO = "OBTAINED_PROMO";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initView();

    gestureDetector = new GestureDetectorCompat(this, this);

    // Se indica al HttpHandler la actividad que estará esperando la respuesta
    // a la petición.
    httpHandler.addListeningActivity(this);

    // Se indica al UserFeedback la actividad que estará esperando la
    // respuesta de la elección del usuario..
    userFeedback.addListeningActivity(this);

    requestErrorsHandler.addListeningActivity(this);

    setFlipperAnimation();
    capturePromos();
    // Si el establecimiento no tiene promociones vigentes no tiene sentido
    // actualizar nada.
    if (!promos.isEmpty()) {
      setPromosIdsList();
      sendRequest(ACTION_PROMOS_DETAILS);
    }
    else {
      // El establecimiento no tiene promociones vigentes.
      showEmptyBranchMessage();
    }
  }

  public void initView() {
    FadingActionBarHelper helper = new FadingActionBarHelper()
                                   .actionBarBackground
                                   (R.drawable.ab_background)
                                   .headerLayout(R.layout.header)
                                   .contentLayout
                                   (R.layout.activity_promo_card_animator);
    setContentView(helper.createView(this));
    ImageView headerImage = ((ImageView) findViewById(R.id.header_image));
    headerImage.setImageResource(R.drawable.promo);
    helper.initActionBar(this);
    getActionBar().setDisplayHomeAsUpEnabled(true);

    promosFlipper = (ViewFlipper) findViewById(R.id.promos_flipper);
  }

  public void setFlipperAnimation() {
    slide_in_left = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
    slide_out_right = AnimationUtils.loadAnimation
                      (this, R.anim.slide_out_right);
    slide_in_right = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
    slide_out_left = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
  }

  public void capturePromos() {
    promos.clear();
    userPromos.clear();
    action = getIntent().getExtras().getString("action");
    if (action.equals(NowerMap.SHOW_BRANCH_PROMOS)) {
      branchId = getIntent().getExtras().getInt("branch_id");
      storeName = MapData.getBranchesMap().get(branchId).getStoreName();
      // En este punto se capturan las promociones correspondientes al
      // establecimiento seleccionado por el usuario.
      setBranchPromos();

      // Estas promociones se capturan para saber cuáles deben mostrarse con
      // botón y cuáles con código.
      userPromos = User.getTakenPromos();
    }
    else if (action.equals(UserPromoList.SHOW_PROMO_TO_REDEEM)) {
      int promoId = getIntent().getExtras().getInt("promo_id");
      code = User.getTakenPromos().get(promoId).getCode();
      isUserPromoRedeemed = User.getTakenPromos().get(promoId).isRedeemed();
      storeName = getIntent().getExtras().getString("store_name");
      Promo promo = MapData.getPromosMap().get(promoId);
      // En este punto se captura la promoción que desea redimir el usuario.
      promos.add(promo);
    }
  }

  public void setBranchPromos() {
    ArrayList<Integer> promosIds = MapData.getBranchesMap().get(branchId)
                                   .getPromosIds();
    for (Integer promoId : promosIds) {
      promos.add(MapData.getPromosMap().get(promoId));
    }
  }

  // Se construye la lista de promociones que debe ser actualizada localmente.
  public void setPromosIdsList() {
    JSONArray pIdsList = new JSONArray();
    for (int i = 0; i < promos.size(); ++i) {
      JSONObject promoId = new JSONObject();
      try {
        promoId.put("id", promos.get(i).getId());
        pIdsList.put(promoId);
      }
      catch (JSONException e) {

      }
    }
    // El String que se envía podrá ser traducido fácilmente a JSONArray.
    params.put("promos_ids_list", pIdsList.toString());
  }

  public void sendRequest(String request) {
    if (request.equals(ACTION_PROMOS_DETAILS)) {
      httpHandler.sendRequest(HttpHandler.NAME_SPACE, ACTION_PROMOS_DETAILS, "",
                              params, new HttpPost(), PromoCardAnimator.this);
    }
    else if (request.equals(ACTION_NOW)) {
      httpHandler.sendRequest(HttpHandler.NAME_SPACE, ACTION_NOW, "", params,
                              new HttpPost(), PromoCardAnimator.this);
    }
  }

  public void showEmptyBranchMessage() {
    emptyPromosMessage = (TextView) findViewById(R.id.empty_promos_message);
    promosFlipper.setVisibility(View.GONE);
    emptyPromosMessage.setVisibility(View.VISIBLE);
  }

  public void addPromosToFlipper() {
    if (!promos.isEmpty()) {
      for (int i = 0; i < promos.size(); ++i) {
        Promo promo = promos.get(i);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService
                                  (Context.LAYOUT_INFLATER_SERVICE);
        View promoCard = inflater.inflate(R.layout.promo_card, null);
        // Con este ID se identificará la promoción actual que visualiza el
        // usuario.
        promoCard.setId(promo.getId());

        // Se agrega una nueva tarjeta de promoción.
        promosFlipper.addView(promoCard);

        // Se capturan los campos que se van a modificar.
        storeLogoProgress = (ProgressBar) promoCard
                .findViewById(R.id.store_logo_progress);
        storeLogo = (ImageView) promoCard.findViewById(R.id.store_logo);
        promoTitle = (TextView) promoCard.findViewById(R.id.promo_title);
        promoStoreName = (TextView) promoCard.findViewById
                         (R.id.promo_store_name);
        final TextView promoExpirationDate = (TextView) promoCard.findViewById
                                             (R.id.promo_expiration_date);
        promoAvailableRedemptions = (TextView) promoCard.findViewById
                                    (R.id.promo_available_redemptions);
        promoDescription = (TextView) promoCard.findViewById
                           (R.id.promo_description);
        promoTerms = (TextView) promoCard.findViewById(R.id.promo_terms);

        final Button nowButton = (Button) promoCard
                                 .findViewById(R.id.now_button);
        redemptionCode = (TextView) promoCard.findViewById
                         (R.id.redemption_code);

        // Se modifica la información de la promoción a mostrar.
        if (/*coger el store logo url != null*/true) {
          String storeLogoUrl = "/uploads/store/logo/142/small_f83e705c24f60" +
                  "848897ed90bea805652228f8d7670fe7097290202051b4950d4.png";
          new ImageDownloader(storeLogo, storeLogoProgress)
                  .execute(storeLogoUrl);
        }
        else {
          // Poner el storeLogoProgress en GONE, y poner una foto genérica
          // en storeLogo.
        }
        promoTitle.setText(promo.getTitle());
        promoStoreName.setText(storeName);
        promoExpirationDate.setText(promo.getExpirationDate());
        promoAvailableRedemptions
        .setText(String.valueOf(promo.getAvailableRedemptions()));
        promoDescription.setText(promo.getDescription());
        promoTerms.setText(promo.getTerms());

        if (action.equals(NowerMap.SHOW_BRANCH_PROMOS)) {
          if (userPromos.containsKey(promo.getId())) {
            // El usuario no debería poder tomar esta promoción porque ya la
            // tiene.
            code = User.getTakenPromos().get(promo.getId()).getCode();
            isUserPromoRedeemed = User.getTakenPromos().get(promo.getId())
                                  .isRedeemed();
            changeButtonToCode(nowButton);
          }
        }
        else if (action.equals(UserPromoList.SHOW_PROMO_TO_REDEEM)) {
          changeButtonToCode(nowButton);
        }

        CountDownTimer countDownTimer = createCountDownTimer
                                        (promoExpirationDate,
                                         promo.getExpirationDate(), nowButton);
        countDownTimer.start();
      }
    }
    else {
      showEmptyBranchMessage();
    }
  }

  public CountDownTimer createCountDownTimer
  (final TextView countDownView, String expirationDate, final Button nowButton)
  {
    long millisUntilFinished, promoDeadLine;
    try {
      promoDeadLine = DateManager.getTimeStamp(expirationDate);
    }
    catch (ParseException e) {
      return null;
    }

    Date currentTime = new Date();
    millisUntilFinished = promoDeadLine - currentTime.getTime();

    CountDownTimer countDownTimer
    = new CountDownTimer(millisUntilFinished, 1000) {
      @Override
      public void onTick(long millisUntilFinished) {
        long seconds = millisUntilFinished / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        countDownView.setText(String.format("%02d:%02d:%02d", hours,
                                            minutes % 60, seconds % 60));
      }

      @Override
      public void onFinish() {
        countDownView.setText(getResources().getString(R.string.never));
        // Se desactiva el botón cuando el tiempo límite para tomar la
        // promoción se haya cumplido.
        nowButton.setEnabled(false);
      }};

    return countDownTimer;
  }



  public void changeButtonToCode(final Button nowButton) {
    // El usuario está tratando de visualizar una promoción que ya obtuvo.
    // Por eso, desaparece el botón de Now y aparece el código para redimirla.
    nowButton.setVisibility(View.GONE);
    redemptionCode.setText(code);
    // En caso de tratarse de una promoción ya redimida por el usuario, el
    // código aparece en color gris.
    if (isUserPromoRedeemed) {
      redemptionCode.setTextColor(getResources().getColor(R.color.gray));
    }
    redemptionCode.setVisibility(View.VISIBLE);
  }

  public void now(View v) {
    params.put("promo_id", String.valueOf(promosFlipper.getCurrentView()
                                          .getId()));
    params.put("user_id", String.valueOf(User.id));
    askToTakePromo();
  }

  public void askToTakePromo() {
    // Se muestra un diálogo al usuario para que decida si desea tomar la
    // promoción actual o no.
    UserFeedback
    .showAlertDialog(this, R.string.promo,
                     getResources().getString(R.string.confirm_taking_promo),
                     R.string._continue, R.string.cancel, TAKE_PROMO);
  }

  public void showObtainedPromo() {
    // Se muestra un diálogo al usuario para indicarle que ya ha sido acreedor
    // de la promoción.
    UserFeedback
    .showAlertDialog(this, R.string.promo_obtained,
                     getResources().getString(R.string.promo_now_in_list),
                     R.string.ok, R.string.go_to_my_promos, OBTAINED_PROMO);
  }

  public void disableNowButtonDueToNoMoreStock() {
    ImageView availableRedemptionsIcon = (ImageView) promosFlipper
                                         .getCurrentView().findViewById
                                         (R.id.available_redemptions_icon);
    availableRedemptionsIcon.setImageDrawable
    (getResources().getDrawable(R.drawable.ic_people_limit_reached));
    TextView availableRedemptions =  (TextView) promosFlipper.getCurrentView()
                                     .findViewById
                                     (R.id.promo_available_redemptions);
    availableRedemptions.setText(getResources().getString(R.string.zero));
    Button nowButton = (Button) promosFlipper.getCurrentView()
                       .findViewById(R.id.now_button);
    nowButton.setEnabled(false);
  }

  @Override
  public void notifyUserResponse(String action, int buttonPressedId) {
    switch (action) {
      case TAKE_PROMO:
        if (buttonPressedId == R.string._continue) sendRequest(ACTION_NOW);
        break;
      case OBTAINED_PROMO:
        if (buttonPressedId == R.string.go_to_my_promos) {
          Intent openUserPromosList = new Intent(this, UserPromoList.class);
          openUserPromosList.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
          startActivity(openUserPromosList);
        }
        break;
    }
  }

  @Override
  public void notifyParsedErrors(String action,
                                 Map<String, String> errorsMessages) {
    switch (action) {
      case ACTION_PROMOS_DETAILS:
        if (errorsMessages.containsKey("ids")) {
          UserFeedback.showToastMessage(getApplicationContext(),
                                        errorsMessages.get("ids"),
                                        Toast.LENGTH_SHORT);
        }
        break;
      case ACTION_NOW:
        if (errorsMessages.containsKey("user")) {
          UserFeedback.showToastMessage(getApplicationContext(),
                                        errorsMessages.get("user"),
                                        Toast.LENGTH_LONG);
          //TODO cerrar sesión porque se intentó utilizar un usuario inválido.
        }
        else if (errorsMessages.containsKey("promo")) {
          if (errorsMessages.get("promo")
              .contains(getResources().getString(R.string.no_more_stock))) {
            disableNowButtonDueToNoMoreStock();
          }
          UserFeedback
          .showAlertDialog(this, R.string.never, errorsMessages.get("promo"),
                           R.string.ok, UserFeedback.NO_BUTTON_TO_SHOW, action);
        }
        break;
    }
  }

  @Override
  public void notify(String action, JSONObject responseJson) {
    try {
      Log.i("responseJson", responseJson.toString());
      int responseStatusCode = responseJson.getInt(HttpHandler.HTTP_STATUS);
      if (action.equals(ACTION_PROMOS_DETAILS)) {
       switch (responseStatusCode) {
         case HttpHandler.OK:
           promos.clear();
           promosMap.clear();

           JSONArray promosDescriptionAndTerms = responseJson
                                                 .getJSONArray("promos");
           for (int i = 0; i < promosDescriptionAndTerms.length(); ++i) {
             JSONObject internPromo = promosDescriptionAndTerms
                                      .getJSONObject(i);
             int promoId = internPromo.getInt("id");
             String title = internPromo.getString("title");
             String expirationDate = internPromo.getString("expiration_date");
             int availableRedemptions = internPromo
                                        .getInt("available_redemptions");
             String description = internPromo.getString("description");
             String terms = internPromo.getString("terms");
             boolean hasExpired = internPromo.getBoolean("has_expired");

             // Se genera la lista de promociones para ser actualizada
             // localmente, ya incluyendo descripción y términos.
             Promo promo = new Promo(promoId, title, expirationDate,
                                     availableRedemptions, description, terms);
             promosMap.put(promo.getId(), promo);

             if (PromoCardAnimator.action.equals(NowerMap.SHOW_BRANCH_PROMOS)) {
               // Si la promoción no ha expirado ni la han tomado el número
               // máximo de personas, entonces se adiciona a las que serán
               // mostradas.
               if (!hasExpired && availableRedemptions > 0) {
                 promos.add(promo);
               }
               else {
                 // Se elimina el id de la promoción dentro del establecimiento,
                 // y no se muestra, ya que ha expirado.
                 MapData.removePromoIdInBranch(branchId, promoId);
               }
             }
             // Siempre se mostrará una promoción que el usuario ya haya tomado.
             else if (PromoCardAnimator.action
                      .equals(UserPromoList.SHOW_PROMO_TO_REDEEM)) {
               promos.add(promo);
             }
           }

           // Se actualiza la lista de promociones para el establecimiento en
           // cuestión.
           MapData.setPromosMap(promosMap);

           addPromosToFlipper();
           break;
         case HttpHandler.UNPROCESSABLE_ENTITY:
           RequestErrorsHandler
           .parseErrors(action, responseJson.getJSONObject("errors"));
           break;
       }
      }
      else if (action.equals(ACTION_NOW)) {
        switch (responseStatusCode) {
          case HttpHandler.OK:
            if (responseJson.getBoolean("success")) {
              JSONObject redemption = responseJson.getJSONObject("redemption");
              String code = redemption.getString("code");
              int promoId = redemption.getInt("promo_id");
              int user_id = redemption.getInt("user_id");
              boolean redeemed = redemption.getBoolean("redeemed");

              Redemption r = new Redemption(code, promoId, redeemed, storeName);
              // Se adiciona la promoción a la lista de promociones del usuario.
              User.addPromoToTakenPromos(promoId, r);

              // Se ponen estos valores en las variables como los actuales para
              // poder simular la obtención de la promoción para el usuario.
              this.code = code;
              isUserPromoRedeemed = redeemed;

              // Se capturan botón y código de la vista actual para realizar el
              // intercambio.
              Button nowButton = (Button) promosFlipper.getCurrentView()
                                 .findViewById(R.id.now_button);
              redemptionCode = (TextView) promosFlipper.getCurrentView()
                               .findViewById(R.id.redemption_code);
              changeButtonToCode(nowButton);

              showObtainedPromo();
            }
            break;
          case HttpHandler.UNPROCESSABLE_ENTITY:
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
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                          float distanceY) {
    return false;
  }

  @Override
  public void onLongPress(MotionEvent e) {

  }

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                         float velocityY) {
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
