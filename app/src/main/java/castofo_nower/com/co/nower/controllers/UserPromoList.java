package castofo_nower.com.co.nower.controllers;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.models.MapData;
import castofo_nower.com.co.nower.models.Promo;
import castofo_nower.com.co.nower.models.Redemption;
import castofo_nower.com.co.nower.models.User;
import castofo_nower.com.co.nower.support.ListItemsCreator;


public class UserPromoList extends ListActivity {

    private ListItemsCreator userPromosListToShow;

    public static final String LIST_USER_PROMOS = "LIST_USER_PROMOS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_promo_list);
        userPromosListToShow = new ListItemsCreator(this, R.layout.promo_item, generateData(),
                                                    LIST_USER_PROMOS);

        setListAdapter(userPromosListToShow);
    }

    public static ArrayList<Object> generateData(){
        ArrayList<Object> userPromos = new ArrayList<Object>();
        for (int i = 0; i < User.obtainedPromos.size(); ++i) {
            Redemption userRedemption = User.obtainedPromos.get(i);
            int promoId = userRedemption.getPromoId();
            Promo promo = MapData.detailedPromosMap.get(promoId);
            userPromos.add(promo);
        }

        return userPromos;
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        int promoId = v.getId();
        //TODO ir al detalle de la promoción
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
