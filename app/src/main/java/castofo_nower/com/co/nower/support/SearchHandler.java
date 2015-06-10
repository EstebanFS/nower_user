package castofo_nower.com.co.nower.support;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;

import castofo_nower.com.co.nower.controllers.BranchesListFragment;
import castofo_nower.com.co.nower.controllers.UserPromosListFragment;
import castofo_nower.com.co.nower.models.Branch;
import castofo_nower.com.co.nower.models.Redemption;

public class SearchHandler {

  public static Context context;
  public static Fragment fragment;
  public static SearchView searchView;
  public static ListItemsCreator listToFilter;
  public static String action;

  public static final int NO_RESULTS_FOUND = -1;

  public static void setParamsForSearch(Context contextToSet,
                                        Fragment fragmentToSet,
                                        SearchView searchViewToSet,
                                        ListItemsCreator listAdapter,
                                        String actionToSet) {
    context = contextToSet;
    fragment = fragmentToSet;
    searchView = searchViewToSet;
    listToFilter = listAdapter;
    action = actionToSet;
  }

  public static void setQueryListener(){
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        switch (action) {
          case UserPromosListFragment.LIST_USER_PROMOS:
            int promoId;
            // Solamente existe un resultado cuando no se encontraron
            // coincidencias.
            if (listToFilter.getCount() == 1) {
              promoId = ((Redemption) listToFilter.getItem(0)).getPromoId();
            }
            // Se toma el item en la posición 1 ya que en la posición 0 habría
            // un encabezado.
            else promoId = ((Redemption) listToFilter.getItem(1)).getPromoId();
            if (promoId != NO_RESULTS_FOUND) {
              if (fragment != null) {
                ((UserPromosListFragment) fragment)
                        .openSelectedRedemption(promoId, 1);
              }
            }
            break;
          case BranchesListFragment.LIST_BRANCHES:
            int branchId = ((Branch) listToFilter.getItem(0)).getId();
            if (branchId != NO_RESULTS_FOUND) {
              if (fragment != null) {
                ((BranchesListFragment) fragment)
                        .openSelectedBranch(branchId, 0);
              }
            }
            break;
        }

        return true;
      }

      @Override
      public boolean onQueryTextChange(String query) {
        if (listToFilter.getCount() > 0) {
          String trimmedQuery = query.trim();
          listToFilter.getFilter().filter(trimmedQuery);

          return true;
        }

        return false;
      }
    });
  }
}
