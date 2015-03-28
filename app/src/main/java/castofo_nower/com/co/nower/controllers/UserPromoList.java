package castofo_nower.com.co.nower.controllers;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;


import java.util.ArrayList;
import java.util.Map;

import castofo_nower.com.co.nower.R;

import castofo_nower.com.co.nower.models.Redemption;
import castofo_nower.com.co.nower.models.User;
import castofo_nower.com.co.nower.support.ListItemsCreator;


public class UserPromoList extends ListActivity {

    private ListItemsCreator userPromosListToShow;

    public static final String LIST_USER_PROMOS = "LIST_USER_PROMOS";
    public static final String SHOW_PROMO_TO_REDEEM = "SHOW_PROMO_TO_REDEEM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_promo_list);

        // Se muestra un mensaje en caso de que la lista de promociones del usuario esté vacía.
        View empty = findViewById(R.id.empty_list);
        ListView list=(ListView)findViewById(android.R.id.list);
        list.setEmptyView(empty);

        userPromosListToShow = new ListItemsCreator(this, R.layout.promo_item, generateData(),
                                                    LIST_USER_PROMOS);

        setListAdapter(userPromosListToShow);
    }

    public static ArrayList<Object> generateData(){
        ArrayList<Object> userPromosToRedeem = new ArrayList<Object>();
        for(Map.Entry<String, Redemption> promoToRedeem : User.obtainedPromos.entrySet()){
            userPromosToRedeem.add(promoToRedeem.getValue());
        }

        return userPromosToRedeem;
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String code = (Integer.toHexString(v.getId())).toUpperCase();
        Intent showPromoToRedeem = new Intent(UserPromoList.this, PromoCardAnimator.class);
        showPromoToRedeem.putExtra("action", SHOW_PROMO_TO_REDEEM);
        showPromoToRedeem.putExtra("code", code);
        showPromoToRedeem.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(showPromoToRedeem);
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
