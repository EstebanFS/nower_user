package castofo_nower.com.co.nower.controllers;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import castofo_nower.com.co.nower.helpers.SubscribedActivities;
import castofo_nower.com.co.nower.models.MapData;
import castofo_nower.com.co.nower.models.Promo;
import castofo_nower.com.co.nower.models.Redemption;
import castofo_nower.com.co.nower.models.User;
import castofo_nower.com.co.nower.support.ListItemsCreator;


public class UserPromoList extends ListActivity implements SubscribedActivities
{

  private ListItemsCreator userPromosListToShow;

  private HttpHandler httpHandler = new HttpHandler();
  public static final String ACTION_USER_REDEMPTIONS = "/user/redemptions";
  private Map<String, String> params = new HashMap<String, String>();

  public static final String LIST_USER_PROMOS = "LIST_USER_PROMOS";
  public static final String SHOW_PROMO_TO_REDEEM = "SHOW_PROMO_TO_REDEEM";

  public static final int HEADER_ID = -1;

  private Map<Integer, Promo> promosMap = new TreeMap<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_promo_list);

    // Se indica al HttpHandler la actividad que estará esperando la respuesta
    // a la petición.
    httpHandler.addListeningActivity(this);

    // Se hace para actualizar el estado de las promociones que ha obtenido el
    // usuario.
    sendRequest(ACTION_USER_REDEMPTIONS);
  }

  public void sendRequest(String request) {
    if (httpHandler.isInternetConnectionAvailable(this)) {
      if (request.equals(ACTION_USER_REDEMPTIONS)) {
        httpHandler.sendRequest(HttpHandler.API_V1, ACTION_USER_REDEMPTIONS,
                                "/" + User.id, params, new HttpGet(),
                                UserPromoList.this);
      }
    }
    else {
      Toast.makeText(getApplicationContext(),
                     getResources()
                     .getString(R.string.internet_connection_required),
                     Toast.LENGTH_SHORT).show();
    }
  }

  public static ArrayList<Object> generateData() {
    ArrayList<Object> userPromos = new ArrayList<Object>();
    ArrayList<Object> userPromosNotRedeemed = new ArrayList<Object>();
    ArrayList<Object> userPromosRedeemed = new ArrayList<Object>();
    Stack<Object> notRedeemedPromos = new Stack<>();
    Stack<Object> redeemedPromos = new Stack<>();

    // Se agrega una Redemption inexistente como indicador de inicio de lista
    // para la promociones no redimidas.
    userPromosNotRedeemed.add(new Redemption("0", 0, false, null));
    // Se agrega una Redemption inexistente como indicador de inicio de lista
    // para la promociones redimidas.
    userPromosRedeemed.add(new Redemption("1", 0, true, null));

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

  public void setEmptyListMessage() {
    // Se muestra un mensaje en caso de que la lista de promociones del usuario
    // esté vacía.
    View empty = findViewById(R.id.empty_list);
    ListView list = (ListView) findViewById(android.R.id.list);
    list.setEmptyView(empty);
  }

  protected void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);
    int promoId = v.getId();
    if (promoId != HEADER_ID) {
      Intent showPromoToRedeem = new Intent(UserPromoList.this,
                                            PromoCardAnimator.class);
      showPromoToRedeem.putExtra("action", SHOW_PROMO_TO_REDEEM);
      showPromoToRedeem.putExtra("promo_id", promoId);
      showPromoToRedeem.putExtra("store_name", User.getTakenPromos()
                                 .get(promoId).getStoreName());
      showPromoToRedeem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
      startActivity(showPromoToRedeem);
    }
  }

  @Override
  protected void onRestart() {
    super.onRestart();
    // Se hace actualización del estado de las promociones del usuario al
    // regresar a la lista.
    sendRequest(ACTION_USER_REDEMPTIONS);
  }

  @Override
  public void notify(String action, JSONObject responseJson) {
    try {
      if (action.equals(ACTION_USER_REDEMPTIONS)) {
        Log.i("responseJson", responseJson.toString());
        if (responseJson.getInt(HttpHandler.HTTP_STATUS) == HttpHandler.SUCCESS)
        {
          promosMap.clear();

          JSONArray userRedemptions = responseJson.getJSONArray("redemptions");
          for (int i = 0; i < userRedemptions.length(); ++i) {
            JSONObject internRedemption = userRedemptions.getJSONObject(i);
            int id = internRedemption.getInt("id");
            int user_id = internRedemption.getInt("user_id");
            String code = internRedemption.getString("code");
            boolean redeemed = internRedemption.getBoolean("redeemed");
            String storeName = internRedemption.getString("store_name");

            // Se captura la información de la promoción asociada.
            JSONObject redemptionPromo = internRedemption
                                         .getJSONObject("promo");
            int promoId = redemptionPromo.getInt("id");
            String title = redemptionPromo.getString("title");
            String expirationDate = redemptionPromo
                                    .getString("expiration_date");
            int availableRedemptions = redemptionPromo
                                       .getInt("available_redemptions");


            Redemption redemption = new Redemption(code, promoId, redeemed,
                                                   storeName);

            // Se adiciona la promoción a la lista de promociones del usuario.
            User.addPromoToTakenPromos(redemption.getPromoId(), redemption);

            Promo promo = new Promo(promoId, title, expirationDate,
                                    availableRedemptions, null, null);
            // Se adiciona la promoción del usuario a la lista de promociones
            // general.
            promosMap.put(promo.getId(), promo);
          }
          MapData.setPromosMap(promosMap);

          // Ya con las promociones del usuario actualizadas, es posible
          // mostrar la lista de redimidas y no redimidas.
          userPromosListToShow = new ListItemsCreator(this, R.layout.promo_item,
                                                      generateData(),
                                                      LIST_USER_PROMOS);

          setListAdapter(userPromosListToShow);

          setEmptyListMessage();
        }
      }
    }
    catch (JSONException e) {

    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_user_promo_list, menu);
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
