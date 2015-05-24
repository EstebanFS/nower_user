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
import android.widget.ListView;

import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.connection.HttpHandler;
import castofo_nower.com.co.nower.helpers.SubscribedActivities;
import castofo_nower.com.co.nower.models.Branch;
import castofo_nower.com.co.nower.models.MapData;
import castofo_nower.com.co.nower.models.Promo;
import castofo_nower.com.co.nower.support.ListItemsCreator;
import castofo_nower.com.co.nower.support.SearchHandler;

public class BranchesListFragment extends ListFragment implements
        SubscribedActivities {

  private ListItemsCreator branchesListToShow;
  private SwipeRefreshLayout swipeRefreshLayout;

  private HttpHandler httpHandler = new HttpHandler();

  public static final String LIST_BRANCHES = "LIST_BRANCHES";

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.fragment_branches_list,
            container, false);

    // Se indica al HttpHandler la actividad que estará esperando la respuesta
    // a la petición.
    httpHandler.addListeningActivity(this);

    branchesListToShow = new ListItemsCreator(getActivity(),
            R.layout.promo_item, generateData(), LIST_BRANCHES);

    setListAdapter(branchesListToShow);

    setEmptyListMessage(layout);

    // Se inicializa el swipe to refresh
    setupSwipeRefreshLayout(layout);

    return layout;
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

  public void setEmptyListMessage(View view) {
    // Se muestra un mensaje en caso de que la lista de tiendas esté vacía.
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
              // Este cálculo sirve para determinar si se está en la parte
              // más alta del scroll, es decir, cuando el offset de desplaza-
              // miento es 0.
              offset = -firstItem.getTop() + firstVisibleItem
                      * firstItem.getHeight();
            }
            // Solo si estoy en la parte más alta, puedo habilitar el swipeRe-
            // freshLayout, de lo contrario no lo habilito para que no salga
            // cuando no he terminado de hacer scroll hasta arriba.
            if (offset == 0) swipeRefreshLayout.setEnabled(true);
            else swipeRefreshLayout.setEnabled(false);
          }
          else swipeRefreshLayout.setEnabled(false);
        }
      }
    });
  }

  private void setupSwipeRefreshLayout(View view) {
    swipeRefreshLayout = (SwipeRefreshLayout) view
            .findViewById(R.id.swipe_refresh_layout);
    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout
            .OnRefreshListener() {
      @Override
      public void onRefresh() {
        sendRequest(NowerMap.ACTION_PROMOS);
      }
    });
  }

  private void hideSwipeToRefreshLayout() {
    if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
      swipeRefreshLayout.setRefreshing(false);
    }
  }

  public void sendRequest(String request) {
    if (request.equals(NowerMap.ACTION_PROMOS)) {
      Map <String, String> params = new HashMap<>();
      if (MapData.userLat != -1.0 && MapData.userLong != -1.0) {
        params.put("latitude", String.valueOf(MapData.userLat));
        params.put("longitude", String.valueOf(MapData.userLong));
        httpHandler.sendRequest(HttpHandler.NAME_SPACE, NowerMap.ACTION_PROMOS,
                "", params, new HttpPost(), getActivity());
      }
      else {
        hideSwipeToRefreshLayout();
      }
    }
  }


  public ListItemsCreator getBranchesListToShow() {
    return branchesListToShow;
  }

  @Override
  public void notify(String action, JSONObject responseJson) {
    try {
      Log.i("responseJson", responseJson.toString());
      int responseStatusCode = responseJson.getInt(HttpHandler.HTTP_STATUS);
      if (action.equals(NowerMap.ACTION_PROMOS)) {
        switch (responseStatusCode) {
          case HttpHandler.OK:
            //Map<Marker, Integer> branchesIdsMap = new HashMap<>();
            Map<Integer, Branch> branchesMap = new TreeMap<>();
            Map<Integer, Promo> promosMap = new TreeMap<>();

            JSONArray locations = responseJson.getJSONArray("locations");
            // Se recorren todas las promociones obtenidas para dibujarlas en
            // el mapa.
            for (int i = 0; i < locations.length(); ++i) {
              ArrayList<Integer> promoList = new ArrayList<>();

              JSONObject internLocation = locations.getJSONObject(i);
              int id = internLocation.getInt("id");
              String name = internLocation.getString("name");
              double latitude = internLocation.getDouble("latitude");
              double longitude = internLocation.getDouble("longitude");
              int storeId = internLocation.getInt("store_id");
              String storeName = internLocation.getString("store_name");
              String storeLogoURL;
              if (internLocation.isNull("store_logo")) storeLogoURL = null;
              else storeLogoURL = internLocation.getString("store_logo");

              JSONArray promos = internLocation.getJSONArray("promos");
              for (int j = 0; j < promos.length(); ++j) {
                JSONObject internPromo = promos.getJSONObject(j);
                int promoId = internPromo.getInt("id");
                String title = internPromo.getString("title");
                String expirationDate = internPromo
                        .getString("expiration_date");
                int availableRedemptions = internPromo
                        .getInt("available_redemptions");
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
                // Se genera la lista de promociones para esa localización,
                // aún sin descripción ni términos.
                Promo promo = new Promo(promoId, title, expirationDate,
                        availableRedemptions, null, null,
                        pictureURL, pictureHDURL);
                promoList.add(promo.getId());

                // Se agrega la promoción a un mapa de promociones.
                promosMap.put(promoId, promo);
              }

              Branch branch = new Branch(id, name, latitude, longitude, storeId,
                      storeName, storeLogoURL, promoList);
              branchesMap.put(branch.getId(), branch);

              //putMarkerAndSaveBranch(branch);
            }

            MapData.clearBranchesMap();
            MapData.setBranchesMap(branchesMap);

            MapData.clearPromosMap();
            MapData.setPromosMap(promosMap);

            branchesListToShow.updateListData(generateData(), true);
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
    int branchId = v.getId();
    openSelectedBranch(branchId, position);
  }

  public void openSelectedBranch(int branchId, int position) {
    // Se cierra la barra de búsqueda y se limpia el texto.
    if (branchId != SearchHandler.NO_RESULTS_FOUND) {
      ((TabsHandler) getActivity()).closeSearchView();
      Intent showPromos = new Intent(getActivity(), PromoCardsAnimator.class);
      showPromos.putExtra("action", NowerMap.SHOW_BRANCH_PROMOS);
      showPromos.putExtra("branch_id", branchId);
      showPromos.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
      startActivity(showPromos);
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    // Inflate the menu; this adds items to the action bar if it is present.
    inflater.inflate(R.menu.menu_branches_list, menu);
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
    if (branchesListToShow != null) {
      branchesListToShow.updateListData(generateData(), true);
    }
    super.onResume();
  }
}
