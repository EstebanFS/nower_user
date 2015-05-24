package castofo_nower.com.co.nower.controllers;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.models.Branch;
import castofo_nower.com.co.nower.models.MapData;
import castofo_nower.com.co.nower.support.ListItemsCreator;
import castofo_nower.com.co.nower.support.SearchHandler;

public class BranchesList extends ActionBarActivity {

  private ListView branchesList;
  private ListItemsCreator branchesListToShow;

  private SearchView searchView;
  private MenuItem searchMenuItem;

  public static final String LIST_BRANCHES = "LIST_BRANCHES";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_branches_list);
    branchesList = (ListView) findViewById(R.id.branches_list);

    branchesListToShow = new ListItemsCreator(this, R.layout.promo_item,
                                              generateData(), LIST_BRANCHES);

    branchesList.setAdapter(branchesListToShow);

    setEmptyListMessage();

    setOnListItemClickListener();
  }

  public static ArrayList<Object> generateData() {
    ArrayList<Object> branches = new ArrayList<Object>();
    Map<String, Branch> lexicOrderedBranches = new TreeMap<>
                                               (String.CASE_INSENSITIVE_ORDER);
    // Con este ciclo se ponen los establecimientos en orden lexicográfico
    // según su nombre completo.
    for (Map.Entry<Integer, Branch> branchIdBranch
         : MapData.getBranchesMap().entrySet()) {
      Branch branch = branchIdBranch.getValue();
      String completeName = branch.getStoreName() + " - " + branch.getName();
      lexicOrderedBranches.put(completeName, branch);
    }

    // Con las tiendas en orden alfabético, ahora es posible generar la lista.
    for (Map.Entry<String, Branch> branchStoreNameBranch
         : lexicOrderedBranches.entrySet()) {
      branches.add(branchStoreNameBranch.getValue());
    }

    return branches;
  }

  public void setEmptyListMessage() {
    // Se muestra un mensaje en caso de que la lista de tiendas esté vacía.
    View empty = findViewById(R.id.empty_list);
    branchesList.setEmptyView(empty);
  }

  public void setOnListItemClickListener() {
    branchesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position,
                              long id) {
        int branchId = view.getId();
        openSelectedBranch(branchId, position);
      }
    });
  }

  public void openSelectedBranch(int branchId, int position) {
    // Se cierra la barra de búsqueda y se limpia el texto.
    if (branchId != SearchHandler.NO_RESULTS_FOUND) {
      if (searchView != null && searchView.isShown()) {
        searchMenuItem.collapseActionView();
        searchView.setQuery("", false);
      }
      Intent showPromos = new Intent(BranchesList.this,
                                     PromoCardsAnimator.class);
      showPromos.putExtra("action", NowerMap.SHOW_BRANCH_PROMOS);
      showPromos.putExtra("branch_id", branchId);
      showPromos.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
      startActivity(showPromos);
    }
  }

  public void prepareSearch() {
    SearchHandler.setParamsForSearch(BranchesList.this, null, searchView,
                                     branchesListToShow, LIST_BRANCHES);
    SearchHandler.setQueryListener();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
      String query = intent.getStringExtra(SearchManager.QUERY);
      searchView.setQuery(query, false);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_branches_list, menu);
    SearchManager searchManager = (SearchManager)
                                  getSystemService(Context.SEARCH_SERVICE);
    searchMenuItem = menu.findItem(R.id.action_search);
    searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
    searchView.setSearchableInfo(searchManager
                                 .getSearchableInfo(getComponentName()));
    prepareSearch();

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
