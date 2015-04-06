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

import castofo_nower.com.co.nower.R;

import castofo_nower.com.co.nower.connection.HttpHandler;
import castofo_nower.com.co.nower.helpers.SubscribedActivities;
import castofo_nower.com.co.nower.models.Redemption;
import castofo_nower.com.co.nower.models.User;
import castofo_nower.com.co.nower.support.ListItemsCreator;


public class UserPromoList extends ListActivity implements SubscribedActivities{

    private ListItemsCreator userPromosListToShow;

    private HttpHandler httpHandler = new HttpHandler();
    public static final String ACTION_USER_REDEMPTIONS = "/user/redemptions";
    private Map<String, String> params = new HashMap<String, String>();

    public static final String LIST_USER_PROMOS = "LIST_USER_PROMOS";
    public static final String SHOW_PROMO_TO_REDEEM = "SHOW_PROMO_TO_REDEEM";

    // Se usa localmente solamente para asociar de manera temporal cada id de promoción
    // a redimir, con una tienda en particular.
    private Map<Integer, String> redemptionsCodesStores = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_promo_list);

        // Se indica al HttpHandler la actividad que estará esperando la respuesta a la petición.
        httpHandler.addListeningActivity(this);

        // Se hace para actualizar el estado de las promociones que ha obtenido el usuario.
        getUserRedemptions();
    }

    public void getUserRedemptions() {
        if (httpHandler.isInternetConnectionAvailable(this)){
            httpHandler.sendRequest(HttpHandler.API_V1, ACTION_USER_REDEMPTIONS, "/" + User.id,
                                    params, new HttpGet(), UserPromoList.this);
        }
        else {
            Toast.makeText(getApplicationContext(),
                           getResources().getString(R.string.internet_connection_required),
                           Toast.LENGTH_SHORT).show();
        }
    }

    public static ArrayList<Object> generateData(){
        ArrayList<Object> userPromos = new ArrayList<Object>();
        ArrayList<Object> userPromosNotRedeemed = new ArrayList<Object>();
        ArrayList<Object> userPromosRedeemed = new ArrayList<Object>();

        // Se agrega una Redemption inexistente como indicador de inicio de lista
        // para la promociones no redimidas.
        userPromosNotRedeemed.add(new Redemption("0", 0, false));
        // Se agrega una Redemption inexistente como indicador de inicio de lista
        // para la promociones no redimidas.
        userPromosRedeemed.add(new Redemption("1", 0, true));

        for(Map.Entry<String, Redemption> promoToRedeem : User.obtainedPromos.entrySet()){
            if (!promoToRedeem.getValue().isRedeemed()) {
                // Se adiciona la promoción dentro de la lista de no redimidas.
                userPromosNotRedeemed.add(promoToRedeem.getValue());
            }
            else {
                // Se adiciona la promoción dentro de la lista de redimidas.
                userPromosRedeemed.add(promoToRedeem.getValue());
            }
        }

        // Si existe al menos una promoción redimida o no redimida, se mostrará
        // la lista al usuario. De lo contrario, no contendrá nada la lista para que se
        // muestre el mensaje cuando está vacía.
        if (userPromosNotRedeemed.size() > 1 || userPromosRedeemed.size() > 1) {
            // Se adicionan ambas sublistas a la lista general que se va a mostrar.
            userPromos.addAll(userPromosNotRedeemed);
            userPromos.addAll(userPromosRedeemed);
        }

        return userPromos;
    }

    public void setEmptyListMessage() {
        // Se muestra un mensaje en caso de que la lista de promociones del usuario esté vacía.
        View empty = findViewById(R.id.empty_list);
        ListView list = (ListView) findViewById(android.R.id.list);
        list.setEmptyView(empty);
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        int promoId = v.getId();
        Intent showPromoToRedeem = new Intent(UserPromoList.this, PromoCardAnimator.class);
        showPromoToRedeem.putExtra("action", SHOW_PROMO_TO_REDEEM);
        showPromoToRedeem.putExtra("promo_id", promoId);
        showPromoToRedeem.putExtra("store_name", redemptionsCodesStores.get(promoId));
        showPromoToRedeem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(showPromoToRedeem);
    }

    @Override
    public void notify(String action, JSONObject responseJson) {
        try {
            if (action.equals(ACTION_USER_REDEMPTIONS)) {
                Log.i("responseJson", responseJson.toString());
                if (responseJson.getInt(HttpHandler.HTTP_STATUS) == HttpHandler.SUCCESS) {
                    JSONArray userRedemptions = responseJson.getJSONArray("redemptions");
                    for(int i = 0; i < userRedemptions.length(); ++i) {
                        JSONObject internRedemption = userRedemptions.getJSONObject(i);
                        int id = internRedemption.getInt("id");
                        int user_id = internRedemption.getInt("user_id");
                        String code = internRedemption.getString("code");
                        int promoId = internRedemption.getInt("promo_id");
                        boolean redeemed = internRedemption.getBoolean("redeemed");

                        Redemption r = new Redemption(code, promoId, redeemed);
                        // Se adiciona la promoción a la lista de promociones del usuario.
                        User.addPromoToRedeem(code, r);
                        // Se asocia el código para redimir con la promoción correspondiente.
                        User.addPromoToRedeemCode(promoId, code);

                        String  storeName = internRedemption.getString("store_name");
                        redemptionsCodesStores.put(promoId, storeName);
                    }

                    // Ya con las promociones del usuario actualizadas,
                    // es posible mostrar la lista de redimidas y no redimidas.
                    userPromosListToShow = new ListItemsCreator(this, R.layout.promo_item,
                                                                generateData(), LIST_USER_PROMOS);

                    setListAdapter(userPromosListToShow);

                    setEmptyListMessage();
                }
            }
        } catch (JSONException e) {

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
