package castofo_nower.com.co.nower.controllers;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Map;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.models.Branch;
import castofo_nower.com.co.nower.models.MapData;
import castofo_nower.com.co.nower.support.ListItemsCreator;


public class BranchesList extends ListActivity {

    private ListItemsCreator branchesListToShow;

    public static final String LIST_BRANCHES = "LIST_BRANCHES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branches_list);
        branchesListToShow = new ListItemsCreator(this, R.layout.promo_item, generateData(),
                                                  LIST_BRANCHES);

        setListAdapter(branchesListToShow);

        setEmptyListMessage();
    }

    public static ArrayList<Object> generateData(){
        ArrayList<Object> branches = new ArrayList<Object>();
        for(Map.Entry<Marker, Branch> markerBranch : MapData.branchesMap.entrySet()){
            branches.add(markerBranch.getValue());
        }

        return branches;
    }

    public void setEmptyListMessage() {
        // Se muestra un mensaje en caso de que la lista de tiendas esté vacía.
        View empty = findViewById(R.id.empty_list);
        ListView list = (ListView) findViewById(android.R.id.list);
        list.setEmptyView(empty);
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        int branchId = v.getId();
        Intent showPromos = new Intent(BranchesList.this, PromoCardAnimator.class);
        showPromos.putExtra("action", NowerMap.SHOW_BRANCH_PROMOS);
        showPromos.putExtra("branch_id", branchId);
        showPromos.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(showPromos);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_branches_list, menu);
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
