package castofo_nower.com.co.nower.controllers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import castofo_nower.com.co.nower.helpers.AlertDialogsResponse;
import castofo_nower.com.co.nower.helpers.ParsedErrors;
import castofo_nower.com.co.nower.helpers.SubscribedActivities;
import castofo_nower.com.co.nower.models.Branch;
import castofo_nower.com.co.nower.models.MapData;
import castofo_nower.com.co.nower.models.Promo;
import castofo_nower.com.co.nower.models.Redemption;
import castofo_nower.com.co.nower.models.User;
import castofo_nower.com.co.nower.support.ImageDownloader;
import castofo_nower.com.co.nower.support.PagerBuilder;
import castofo_nower.com.co.nower.support.RequestErrorsHandler;
import castofo_nower.com.co.nower.support.UserFeedback;
import castofo_nower.com.co.nower.support.DateManager;
import castofo_nower.com.co.nower.support.WideImageView;

public class PromoCardsAnimator extends Activity implements
SubscribedActivities, AlertDialogsResponse, ParsedErrors {

  private HttpHandler httpHandler = new HttpHandler();
  public static final String ACTION_PROMOS_DETAILS = "/promos/details";
  public static final String ACTION_NOW = "/promo/now";
  private Map<String, String> params = new HashMap<String, String>();

  private ViewPager promosFlipper;
  private ArrayList<View> promoCards = new ArrayList<>();
  private View requestingView;

  private UserFeedback userFeedback = new UserFeedback();

  private RequestErrorsHandler requestErrorsHandler = new
                                                      RequestErrorsHandler();

  private WideImageView promoPicture;
  private LinearLayout promoPictureProgress;
  private ProgressBar storeLogoProgress;
  private ImageView storeLogo;
  private TextView promoTitle;
  private TextView promoStoreName;
  private ImageView promoAvailableRedemptionsIcon;
  private TextView promoAvailableRedemptions;
  private TextView promoDescription;
  private TextView promoTerms;
  private LinearLayout emptyPromosMessage;

  private TextView redemptionCode;

  // Indicador para saber qué acción se está tratando de ejecutar.
  public static String action;

  private int branchId;
  private String code;
  private String storeName;
  private String storeLogoURL;
  private boolean isUserPromoRedeemed;
  private ArrayList<Promo> promos = new ArrayList<>();
  private Map<Integer, Redemption> userPromos = new LinkedHashMap<>();
  private Map<Integer, Promo> promosMap = new TreeMap<>();

  private boolean isUserAbleToTakePromos;

  public static final String TAKE_PROMO = "TAKE_PROMO";
  public static final String OBTAINED_PROMO = "OBTAINED_PROMO";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initView();

    // Se indica al HttpHandler la actividad que estará esperando la respuesta
    // a la petición.
    httpHandler.addListeningActivity(this);

    // Se indica al UserFeedback la actividad que estará esperando la
    // respuesta de la elección del usuario..
    userFeedback.addListeningActivity(this);

    requestErrorsHandler.addListeningActivity(this);

    isUserAbleToTakePromos = SplashActivity.isThereLoginInstance();

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
    setContentView(R.layout.activity_promo_cards_animator);
    getActionBar().setDisplayHomeAsUpEnabled(true);
    promoCards.clear();
    promosFlipper = (ViewPager) findViewById(R.id.promos_flipper);
    promosFlipper.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      public void onPageScrollStateChanged(int state) { }
      public void onPageScrolled(int position, float positionOffset,
                                 int positionOffsetPixels) {
        int padding = 40;
        // Si no hay ninguna vista.
        if (promoCards.isEmpty()) {
          promosFlipper.setPadding(0, 0, 0, 0);
        }
        // Si la vista es la última.
        else if (position == promoCards.size() - 1) {
          // Si existe algún elemento anterior se utiliza padding hacia la
          // izquierda.
          if (position - 1 >= 0) {
            promosFlipper.setPadding(padding, 0, 0, 0);
          }
          // Si no existe elemento anterior, no se pone padding en ninguna
          // dirección (acá se incluye cuando es un único elemento).
          else {
            promosFlipper.setPadding(0, 0, 0, 0);
          }
        }
        // Si no es la última vista se pone padding a la derecha
        else promosFlipper.setPadding(0, 0, padding, 0);
      }
      public void onPageSelected(int position) { }
    });
  }

  public void capturePromos() {
    promos.clear();
    userPromos.clear();
    action = getIntent().getExtras().getString("action");
    if (action.equals(NowerMap.SHOW_BRANCH_PROMOS)) {
      branchId = getIntent().getExtras().getInt("branch_id");
      Branch currentBranch = MapData.getBranchesMap().get(branchId);
      storeName = currentBranch.getStoreName();
      storeLogoURL = currentBranch.getStoreLogoURL();
      // En este punto se capturan las promociones correspondientes al
      // establecimiento seleccionado por el usuario.
      setBranchPromos();

      // Estas promociones se capturan para saber cuáles deben mostrarse con
      // botón y cuáles con código.
      userPromos = User.getTakenPromos();
    }
    else if (action.equals(UserPromosList.SHOW_PROMO_TO_REDEEM)) {
      int promoId = getIntent().getExtras().getInt("promo_id");
      code = User.getTakenPromos().get(promoId).getCode();
      isUserPromoRedeemed = User.getTakenPromos().get(promoId).isRedeemed();
      storeName = User.getTakenPromos().get(promoId).getStoreName();
      storeLogoURL = User.getTakenPromos().get(promoId).getStoreLogoURL();
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
                              params, new HttpPost(), PromoCardsAnimator.this);
    }
    else if (request.equals(ACTION_NOW)) {
      httpHandler.sendRequest(HttpHandler.NAME_SPACE, ACTION_NOW, "", params,
                              new HttpPost(), PromoCardsAnimator.this);
    }
  }

  public void showEmptyBranchMessage() {
    emptyPromosMessage = (LinearLayout) findViewById(R.id.empty_promos_message);
    promosFlipper.setVisibility(View.GONE);
    emptyPromosMessage.setVisibility(View.VISIBLE);
  }

  public void addPromosToFlipper() {
    if (!promos.isEmpty()) {
      promoCards.clear();
      for (int i = 0; i < promos.size(); ++i) {
        Promo promo = promos.get(i);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService
                                  (Context.LAYOUT_INFLATER_SERVICE);
        View promoCard = inflater.inflate(R.layout.promo_card, null);
        // Con este ID se identificará la promoción actual que visualiza el
        // usuario.
        promoCard.setId(promo.getId());
        // Se agrega una nueva tarjeta de promoción.
        promoCards.add(promoCard);

        // Se capturan los campos que se van a modificar.
        promoPicture = (WideImageView) promoCard
                       .findViewById(R.id.promo_picture);
        promoPicture.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            int currentPromoId = getPagerCurrentView().getId();
            String imageHDURL = promosMap.get(currentPromoId).getPictureHDURL();
            if (imageHDURL != null) {
              Intent intent = new Intent(PromoCardsAnimator.this,
                      PromoPictureViewer.class);
              Bitmap image = ((BitmapDrawable) ((WideImageView) v).getDrawable())
                      .getBitmap();

              Bundle extras = new Bundle();
              extras.putParcelable("promo_picture_bitmap", image);
              extras.putString("image_url", imageHDURL);
              intent.putExtras(extras);
              startActivity(intent);
            }
            else UserFeedback.showToastMessage(PromoCardsAnimator.this,
                    getResources().getString(R.string.no_promo_picture),
                    Toast.LENGTH_SHORT);
          }
        });
        promoPictureProgress = (LinearLayout) promoCard
                .findViewById(R.id.promo_picture_progress);
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
        promoAvailableRedemptionsIcon = (ImageView) promoCard.findViewById
                                        (R.id.available_redemptions_icon);
        promoDescription = (TextView) promoCard.findViewById
                           (R.id.promo_description);
        promoTerms = (TextView) promoCard.findViewById(R.id.promo_terms);

        final Button nowButton = (Button) promoCard
                                 .findViewById(R.id.now_button);
        if (!isUserAbleToTakePromos) changeNowToRegister(nowButton);

        redemptionCode = (TextView) promoCard.findViewById
                         (R.id.redemption_code);

        // Se recupera la imagen de la promoción
        if (promo.getPictureURL() != null) {
          ImageDownloader imageDownloader
                  = new ImageDownloader(promoPicture, promoPictureProgress,
                                        promo.getPictureURL());
          imageDownloader.execute();
        }
        else {
          promoPictureProgress.setVisibility(View.GONE);
          promoPicture.setVisibility(View.VISIBLE);
        }
        // Se recupera el logo de la tienda.
        if (storeLogoURL != null) {
          ImageDownloader imageDownloader
          = new ImageDownloader(storeLogo, storeLogoProgress, storeLogoURL);
          imageDownloader.execute();
        }
        else {
          storeLogoProgress.setVisibility(View.GONE);
          storeLogo.setVisibility(View.VISIBLE);
        }

        // Se modifica la información de la promoción a mostrar.
        promoTitle.setText(promo.getTitle());
        promoStoreName.setText(storeName);
        promoExpirationDate.setText(promo.getExpirationDate());
        // Si se trata de una promoción que ya tomó el usuario, es necesario
        // de todas formas cerrar el candado de las promociones disponibles en
        // caso de que ya se hayan agotado.
        if (promo.getAvailableRedemptions() == 0) {
          closeAvailablePromos(promoAvailableRedemptionsIcon);
        }
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
        else if (action.equals(UserPromosList.SHOW_PROMO_TO_REDEEM)) {
          changeButtonToCode(nowButton);
        }

        CountDownTimer countDownTimer = createCountDownTimer
                                        (promoExpirationDate,
                                         promo.getExpirationDate(), nowButton);
        countDownTimer.start();
      }
      putPromoCards();
    }
    else {
      showEmptyBranchMessage();
    }
  }

  // Es necesario que el usuario ingrese antes de poder acceder a las
  // promociones.
  public void changeNowToRegister(Button nowButton) {
    nowButton.setText(getResources().getString(R.string.register_or_log_in));
    nowButton.setBackground(getResources()
                            .getDrawable(R.drawable.blue_button_background));
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
        if (isUserAbleToTakePromos) {
          // Se desactiva el botón cuando el tiempo límite para tomar la
          // promoción se haya cumplido.
          nowButton.setEnabled(false);
        }
      }};

    return countDownTimer;
  }

  public void putPromoCards() {
    PagerAdapter adapter = new PagerBuilder(promoCards);
    // Se hacen visibles las tarjetas de promoción.
    promosFlipper.setAdapter(adapter);
    promosFlipper.setClipToPadding(false);
  }

  public void now(View v) {
    if (v == getPagerCurrentView().findViewById(R.id.now_button)) {
      if (isUserAbleToTakePromos) {
        requestingView = getPagerCurrentView();
        params.put("promo_id", String.valueOf(requestingView.getId()));
        params.put("user_id", String.valueOf(User.id));
        askToTakePromo();
      }
      else {
        SplashActivity.handleRequest(PromoCardsAnimator.this,
                                     UserPromosList.USER_NEEDS_TO_REGISTER);
      }
    }
  }

  public View getPagerCurrentView() {
    int promoTag = promosFlipper.getCurrentItem();
    View promoView = promosFlipper.findViewWithTag(promoTag);

    return promoView;
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
    promoAvailableRedemptionsIcon = (ImageView) requestingView.findViewById
                                    (R.id.available_redemptions_icon);
    promoAvailableRedemptions = (TextView) requestingView.findViewById
                                (R.id.promo_available_redemptions);
    closeAvailablePromos(promoAvailableRedemptionsIcon);
    promoAvailableRedemptions.setText(getResources().getString(R.string.zero));
    Button nowButton = (Button) requestingView.findViewById(R.id.now_button);
    nowButton.setEnabled(false);
  }

  public void closeAvailablePromos(ImageView availableRedemptionsIcon) {
    availableRedemptionsIcon.setImageDrawable
            (getResources().getDrawable(R.drawable.ic_people_limit_reached));
  }

  @Override
  public void notifyUserResponse(String action, int buttonPressedId) {
    switch (action) {
      case TAKE_PROMO:
        if (buttonPressedId == R.string._continue) sendRequest(ACTION_NOW);
        break;
      case OBTAINED_PROMO:
        if (buttonPressedId == R.string.go_to_my_promos) {
          Intent openUserPromosList = new Intent(this, UserPromosList.class);
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
        if (errorsMessages.containsKey("user_id")) {
          UserFeedback.showToastMessage(getApplicationContext(),
                  errorsMessages.get("user_id"),
                  Toast.LENGTH_LONG);
          //TODO cerrar sesión porque se intentó utilizar un usuario inválido.
        }
        else if (errorsMessages.containsKey("user")) {
          UserFeedback.showAlertDialog(this, R.string.promo_limit_exceeded,
                  errorsMessages.get("user"), R.string.ok,
                  UserFeedback.NO_BUTTON_TO_SHOW, action);
        }
        else if (errorsMessages.containsKey("promo_stock")) {
          disableNowButtonDueToNoMoreStock();
          UserFeedback.showAlertDialog(this, R.string.never,
                  errorsMessages.get("promo_stock"), R.string.ok,
                  UserFeedback.NO_BUTTON_TO_SHOW, action);
        }
        else if (errorsMessages.containsKey("promo")) {
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
             String pictureURL;
             if (internPromo.getJSONObject("picture").getJSONObject("large")
                     .isNull("url")) {
               pictureURL = null;
             }
             else pictureURL = internPromo.getJSONObject("picture")
                     .getJSONObject("large").getString("url");

             String pictureHDURL;
             if (internPromo.getJSONObject("picture")
                     .getJSONObject("extra_large").isNull("url")) {
               pictureHDURL = null;
             }
             else pictureHDURL = internPromo.getJSONObject("picture")
                     .getJSONObject("extra_large").getString("url");

             // Se genera la lista de promociones para ser actualizada
             // localmente, ya incluyendo descripción y términos.
             Promo promo = new Promo(promoId, title, expirationDate,
                                     availableRedemptions, description, terms,
                                     pictureURL, pictureHDURL);
             promosMap.put(promo.getId(), promo);

             if (PromoCardsAnimator.action.equals(NowerMap.SHOW_BRANCH_PROMOS))
             {
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
             else if (PromoCardsAnimator.action
                      .equals(UserPromosList.SHOW_PROMO_TO_REDEEM)) {
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
              int userId = redemption.getInt("user_id");
              boolean redeemed = redemption.getBoolean("redeemed");

              JSONObject promo = responseJson.getJSONObject("promo");
              int availableRedemptions = promo.getInt("available_redemptions");

              Redemption r = new Redemption(code, promoId, redeemed, storeName,
                                            storeLogoURL);
              // Se adiciona la promoción a la lista de promociones del usuario.
              User.addPromoToTakenPromos(promoId, r);

              // Se actualiza la cantidad de promociones disponibles.
              MapData.getPromosMap().get(promoId)
              .setAvailableRedemptions(availableRedemptions);

              // Se ponen estos valores en las variables como los actuales para
              // poder simular la obtención de la promoción para el usuario.
              this.code = code;
              isUserPromoRedeemed = redeemed;

              // Se capturan botón y código de la vista actual para realizar el
              // intercambio.
              Button nowButton = (Button) requestingView.findViewById
                                 (R.id.now_button);
              redemptionCode = (TextView) requestingView.findViewById
                               (R.id.redemption_code);
              changeButtonToCode(nowButton);

              promoAvailableRedemptions = (TextView) requestingView.findViewById
                                          (R.id.promo_available_redemptions);
              promoAvailableRedemptions.setText(String.valueOf
                                                (availableRedemptions));

              if (availableRedemptions == 0) {
                // Es necesario cerrar el candado de las promociones
                // disponibles.
                promoAvailableRedemptionsIcon
                = (ImageView) requestingView.findViewById
                  (R.id.available_redemptions_icon);
                closeAvailablePromos(promoAvailableRedemptionsIcon);
              }

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
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_promo_cards_animator, menu);
    if (SplashActivity.isThereLoginInstance()) {
      menu.findItem(R.id.action_log_in).setVisible(false);
    }
    else {
      menu.findItem(R.id.action_log_out).setVisible(false);
    }

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    switch (id) {
      case android.R.id.home:
        finish();
        return true;
      case R.id.action_log_in:
        SplashActivity.handleRequest(PromoCardsAnimator.this,
                                     UserPromosList.USER_NEEDS_TO_REGISTER);
        return true;
      case R.id.action_log_out:
        SplashActivity.handleRequest(PromoCardsAnimator.this,
                                     UserPromosList.LOG_OUT);
        return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
