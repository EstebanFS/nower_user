package castofo_nower.com.co.nower.controllers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.connection.HttpHandler;
import castofo_nower.com.co.nower.helpers.ParsedErrors;
import castofo_nower.com.co.nower.helpers.SubscribedActivities;
import castofo_nower.com.co.nower.models.MapData;
import castofo_nower.com.co.nower.models.Promo;
import castofo_nower.com.co.nower.models.Redemption;
import castofo_nower.com.co.nower.models.User;
import castofo_nower.com.co.nower.support.ListItemsCreator;
import castofo_nower.com.co.nower.support.RequestErrorsHandler;
import castofo_nower.com.co.nower.support.UserFeedback;

public class UserPromosListFragment extends ListFragment implements
        SubscribedActivities, ParsedErrors {

  private ListItemsCreator userPromosListToShow;
  private SwipeRefreshLayout swipeRefreshLayout;

  private HttpHandler httpHandler = new HttpHandler();
  public static final String ACTION_USER_REDEMPTIONS = "/user/redemptions";
  private Map<String, String> params = new HashMap<String, String>();

  private RequestErrorsHandler requestErrorsHandler = new
          RequestErrorsHandler();

  private static Map<Integer, Promo> promosMap = new TreeMap<>();

  private boolean isUserAbleToTakePromos;

  public static final String LIST_USER_PROMOS = "LIST_USER_PROMOS";
  public static final String SHOW_PROMO_TO_REDEEM = "SHOW_PROMO_TO_REDEEM";

  public static final int HEADER_ID = -1;

  public static final String USER_NEEDS_TO_REGISTER = "USER_NEEDS_TO_REGISTER";

  public static final String LOG_OUT = "LOG_OUT";


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.fragment_user_promo_list,
                                   container, false);
    //setContentView(R.layout.activity_user_promo_list);

    // Se indica al HttpHandler la actividad que estará esperando la respuesta
    // a la petición.
    httpHandler.addListeningActivity(this);

    requestErrorsHandler.addListeningActivity(this);

    isUserAbleToTakePromos = SplashActivity.isThereLoginInstance();

    setEmptyListMessage(layout);

    // Se inicializa el swipe to refresh
    setupSwipeRefreshLayout(layout);

    // Se hace para actualizar el estado de las promociones que ha obtenido el
    // usuario.
    sendRequest(ACTION_USER_REDEMPTIONS);
    return layout;
  }

  public void setEmptyListMessage(View view) {
    // Se muestra un mensaje en caso de que la lista de promociones del usuario
    // esté vacía.
    View empty = view.findViewById(R.id.empty_list);
    final ListView list = (ListView) view.findViewById(android.R.id.list);
    list.setEmptyView(empty);
    list.setOnScrollListener(new AbsListView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(AbsListView view, int scrollState) {

      }

      @Override
      public void onScroll(AbsListView view, int firstVisibleItem,
                           int visibleItemCount, int totalItemCount) {
        if (swipeRefreshLayout != null) {
          if (firstVisibleItem == 0) {
            int offset = 0;
            if (totalItemCount > 0) {
              View firstItem = list.getChildAt(0);
              offset = -firstItem.getTop() + firstVisibleItem
                      * firstItem.getHeight();
            }
            if (offset == 0) swipeRefreshLayout.setEnabled(true);
            else swipeRefreshLayout.setEnabled(false);
          }
          else swipeRefreshLayout.setEnabled(false);
        }
      }
    });
    if (!isUserAbleToTakePromos) {
      Button registerOrLogIn = (Button)
              view.findViewById(R.id.register_or_login_button);
      registerOrLogIn.setVisibility(View.VISIBLE);
    }
  }

  private void setupSwipeRefreshLayout(View view) {
    swipeRefreshLayout = (SwipeRefreshLayout) view
            .findViewById(R.id.swipe_refresh_layout);
    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout
                                                .OnRefreshListener() {
      @Override
      public void onRefresh() {
        sendRequest(ACTION_USER_REDEMPTIONS);
      }
    });
  }

  private void hideSwipeToRefreshLayout() {
    if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
      swipeRefreshLayout.setRefreshing(false);
    }
  }

  public void sendRequest(String request) {
    if (isUserAbleToTakePromos) {
      if (request.equals(ACTION_USER_REDEMPTIONS)) {
        httpHandler.sendRequest(HttpHandler.NAME_SPACE, ACTION_USER_REDEMPTIONS,
                "/" + User.id, params, new HttpGet(), getActivity());
      }
    }
    else hideSwipeToRefreshLayout();
  }

  public static ArrayList<Object> generateData() {
    ArrayList<Object> userPromos = new ArrayList<Object>();
    ArrayList<Object> userPromosNotRedeemed = new ArrayList<Object>();
    ArrayList<Object> userPromosRedeemed = new ArrayList<Object>();
    Stack<Object> notRedeemedPromos = new Stack<>();
    Stack<Object> redeemedPromos = new Stack<>();

    // Se agrega una Redemption inexistente como indicador de inicio de lista
    // para la promociones no redimidas.
    userPromosNotRedeemed.add(new Redemption("0", 0, false, null, null));
    // Se agrega una Redemption inexistente como indicador de inicio de lista
    // para la promociones redimidas.
    userPromosRedeemed.add(new Redemption("1", 0, true, null, null));

    // Se utiliza este ciclo para almacenar en dos pilas distintas las
    //promociones redimidas y no redimidas del usuario.
    for (Map.Entry<Integer, Redemption> promoIdRedemption
            : User.getTakenPromos().entrySet()) {
      if (!promoIdRedemption.getValue().isRedeemed()) {
        // Se adiciona la promoción dentro de la pila de no redimidas.
        notRedeemedPromos.push(promoIdRedemption.getValue());
      }
      else {
        // Se adiciona la promoción dentro de la pila de redimidas.
        redeemedPromos.push(promoIdRedemption.getValue());
      }
    }

    // Ambos ciclos sirven para pasar las promociones de las pilas a las
    // listas, de forma que queden organizadas cronológicamente de manera
    // descendente.
    while (!notRedeemedPromos.empty()) {
      userPromosNotRedeemed.add(notRedeemedPromos.pop());
    }
    while (!redeemedPromos.empty()) {
      userPromosRedeemed.add(redeemedPromos.pop());
    }

    // Si existe al menos una promoción redimida o no redimida, se mostrará
    // la lista al usuario.
    // De lo contrario, no contendrá nada la lista para que se muestre el
    // mensaje cuando está vacía.
    if (userPromosNotRedeemed.size() > 1 || userPromosRedeemed.size() > 1) {
      // Se adicionan ambas sublistas a la lista general que se va a mostrar.
      userPromos.addAll(userPromosNotRedeemed);
      userPromos.addAll(userPromosRedeemed);
    }

    return userPromos;
  }

  public static void updateUserRedemptions(JSONArray userRedemptions) {
    try {
      promosMap.clear();
      User.clearTakenPromos();
      for (int i = 0; i < userRedemptions.length(); ++i) {
        JSONObject internRedemption = userRedemptions.getJSONObject(i);
        int id = internRedemption.getInt("id");
        int userId = internRedemption.getInt("user_id");
        String code = internRedemption.getString("code");
        boolean redeemed = internRedemption.getBoolean("redeemed");
        String storeName = internRedemption.getString("store_name");
        String storeLogoURL;
        if (internRedemption.isNull("store_logo")) storeLogoURL = null;
        else storeLogoURL = internRedemption.getString("store_logo");


        // Se captura la información de la promoción asociada.
        JSONObject redemptionPromo = internRedemption.getJSONObject("promo");
        int promoId = redemptionPromo.getInt("id");
        String title = redemptionPromo.getString("title");
        String expirationDate = redemptionPromo.getString("expiration_date");
        int availableRedemptions = redemptionPromo
                .getInt("available_redemptions");
        String pictureURL;
        if (redemptionPromo.getJSONObject("picture").getJSONObject("large")
                .isNull("url")) {
          pictureURL = null;
        }
        else pictureURL = redemptionPromo.getJSONObject("picture")
                .getJSONObject("large").getString("url");

        String pictureHDURL;
        if (redemptionPromo.getJSONObject("picture")
                .getJSONObject("extra_large").isNull("url")) {
          pictureHDURL = null;
        }
        else pictureHDURL = redemptionPromo.getJSONObject("picture")
                .getJSONObject("extra_large").getString("url");

        Redemption redemption = new Redemption(code, promoId, redeemed,
                storeName, storeLogoURL);

        // Se adiciona la promoción a la lista de promociones del usuario.
        User.addPromoToTakenPromos(redemption.getPromoId(), redemption);

        Promo promo = new Promo(promoId, title, expirationDate,
                availableRedemptions, null, null, pictureURL,
                pictureHDURL);
        // Se adiciona la promoción del usuario a la lista de promociones
        // general.
        promosMap.put(promo.getId(), promo);
      }
      MapData.setPromosMap(promosMap);
    }
    catch (JSONException e) {

    }
  }

  @Override
  public void notifyParsedErrors(String action,
                                 Map<String, String> errorsMessages) {
    switch (action) {
      case ACTION_USER_REDEMPTIONS:
        if (errorsMessages.containsKey("user")) {
          UserFeedback.showToastMessage(getActivity(),
                  errorsMessages.get("user"),
                  Toast.LENGTH_LONG);
        }
        //TODO cerrar sesión porque se intentó utilizar un usuario inválido.
        break;
    }
  }

  @Override
  public void notify(String action, JSONObject responseJson) {
    try {
      Log.i("responseJson", responseJson.toString());
      int responseStatusCode = responseJson.getInt(HttpHandler.HTTP_STATUS);
      if (action.equals(ACTION_USER_REDEMPTIONS)) {
        switch (responseStatusCode) {
          case HttpHandler.OK:
            updateUserRedemptions(responseJson.getJSONArray("redemptions"));
            // Ya con las promociones del usuario actualizadas, es posible
            // mostrar la lista de redimidas y no redimidas.
            userPromosListToShow = new ListItemsCreator
                    (getActivity(), R.layout.promo_item, generateData(),
                            LIST_USER_PROMOS);

            setListAdapter(userPromosListToShow);
            break;
          case HttpHandler.UNAUTHORIZED:
            RequestErrorsHandler
                    .parseErrors(action, responseJson.getJSONObject("errors"));
            break;
        }
        hideSwipeToRefreshLayout();
      }
    }
    catch (JSONException e) {

    }
  }

  public void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);
    int promoId = v.getId();
    if (promoId != HEADER_ID) {
      Intent showPromoToRedeem = new Intent(getActivity(),
              PromoCardsAnimator.class);
      showPromoToRedeem.putExtra("action", SHOW_PROMO_TO_REDEEM);
      showPromoToRedeem.putExtra("promo_id", promoId);
      showPromoToRedeem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
      startActivity(showPromoToRedeem);
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    // Inflate the menu; this adds items to the action bar if it is present.
    inflater.inflate(R.menu.menu_user_promo_list, menu);
    super.onCreateOptionsMenu(menu,inflater);
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

  @Override
  public void onResume() {
    if (userPromosListToShow != null) {
      userPromosListToShow.updateListData(generateData());
    }
    super.onResume();
  }
}