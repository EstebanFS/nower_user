package castofo_nower.com.co.nower.controllers;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.astuetz.PagerSlidingTabStrip;

import java.util.List;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.support.ListItemsCreator;
import castofo_nower.com.co.nower.support.SearchHandler;
import castofo_nower.com.co.nower.support.TabsAdapter;

public class TabsHandler extends ActionBarActivity {

  private ViewPager viewPager;
  private PagerSlidingTabStrip tabs;

  private SearchView searchView;
  private MenuItem searchMenuItem;

  private MenuItem logInItem;
  private MenuItem logOutItem;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_tabs_handler);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    initTabs();
    handleWithExtras();
  }

  public void initTabs() {
    viewPager = (ViewPager) findViewById(R.id.view_pager);
    viewPager.setAdapter(new TabsAdapter(getSupportFragmentManager(), this));

    tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
    // Se conectan las tabs al ViewPager.
    tabs.setViewPager(viewPager);

    tabs.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
      @Override
      public void onPageSelected(int position) { changeTab(position); }
    });
  }

  public void changeTab(int position) {
    if (viewPager != null && position < viewPager.getChildCount()) {
      viewPager.setCurrentItem(position);
    }

    // Cuando se cambie de pestaña, se debe configurar la búsqueda para la
    // respectiva lista.
    closeSearchView();
    prepareSearchForTabAt(position);
  }

  private void handleWithExtras() {
    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      String sourceActivity = extras.getString("source");
      if (sourceActivity != null) {
        if (sourceActivity.equals(SplashActivity.class.getSimpleName())) {
          openMap();
        }
      }
      String action = extras.getString("action");
      if (action != null) {
        switch (action) {
          case BranchesListFragment.LIST_BRANCHES:
            changeTab(0);
            break;
          case UserPromosListFragment.LIST_USER_PROMOS:
            changeTab(1);
            break;
        }
      }
    }
  }

  public void closeSearchView() {
    if (searchView != null && searchView.isShown()) {
      searchMenuItem.collapseActionView();
      searchView.setQuery("", false);
    }
  }

  private Fragment getFragmentAt(int position) {
    List<Fragment> fragmentsList =  getSupportFragmentManager().getFragments();
    if (fragmentsList != null && position < fragmentsList.size()) {
      return fragmentsList.get(position);
    }
    else return null;
  }

  private void openMap() {
    Intent openMap = new Intent(TabsHandler.this, NowerMap.class);
    startActivity(openMap);
    finish();
  }

  public static void handleRequest(Context context, String action, int... flags)
  {
    Intent openTabs = new Intent(context, TabsHandler.class);
    if (flags != null) {
      for (int i : flags) {
        openTabs.addFlags(i);
      }
    }
    openTabs.putExtra("action", action);
    context.startActivity(openTabs);
  }

  public void goToRegister(View v) {
    SplashActivity.handleRequest(this, UserPromosListFragment
                                       .USER_NEEDS_TO_REGISTER);
  }

  private void prepareSearchForTabAt(int position) {
    Fragment currentFragment = getFragmentAt(position);
    ListItemsCreator listToBeFiltered;
    if (currentFragment != null) {
      switch (position) {
        case 0:
          BranchesListFragment branchesListFragment =
                  (BranchesListFragment) currentFragment;
          listToBeFiltered = branchesListFragment
                  .getBranchesListToShow();
          if (listToBeFiltered != null) {
            prepareSearch(branchesListFragment, listToBeFiltered,
                          BranchesListFragment.LIST_BRANCHES);
          }
          break;
        case 1:
          UserPromosListFragment userPromosListFragment =
                  (UserPromosListFragment) currentFragment;
          listToBeFiltered = userPromosListFragment
                  .getUserPromosListToShow();
          if (listToBeFiltered != null) {
            prepareSearch(userPromosListFragment, listToBeFiltered,
                          UserPromosListFragment.LIST_USER_PROMOS);
          }
          break;
      }
    }
  }

  public void prepareSearch(Fragment fragmentUsingSearch,
                            ListItemsCreator listToBeFiltered,
                            String action) {
    SearchHandler.setParamsForSearch(TabsHandler.this, fragmentUsingSearch,
                                     searchView, listToBeFiltered, action);
    SearchHandler.setQueryListener();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
      String query = intent.getStringExtra(SearchManager.QUERY);
      searchView.setQuery(query, false);
    }
    setIntent(intent);
    handleWithExtras();
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (viewPager != null) prepareSearchForTabAt(viewPager.getCurrentItem());
  }

  @Override
  protected void onRestart() {
    super.onRestart();
    // Se hace actualización del estado de las promociones del usuario al
    // regresar a la lista.
    UserPromosListFragment userPromosListFragment =
            (UserPromosListFragment) getFragmentAt(1);
    if (userPromosListFragment != null) {
      userPromosListFragment.sendRequest(UserPromosListFragment
              .ACTION_USER_REDEMPTIONS);
    }
  }

  private void setupLoginAndLogoutMenuItems() {
    if (SplashActivity.isThereLoginInstance()) {
      logInItem.setVisible(false);
      logOutItem.setVisible(true);
    }
    else {
      logOutItem.setVisible(false);
      logInItem.setVisible(true);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_tabs_handler, menu);

    logInItem = menu.findItem(R.id.action_log_in);
    logOutItem = menu.findItem(R.id.action_log_out);

    setupLoginAndLogoutMenuItems();

    SearchManager searchManager = (SearchManager)
            getSystemService(Context.SEARCH_SERVICE);
    searchMenuItem = menu.findItem(R.id.action_search);
    MenuItemCompat.setOnActionExpandListener(searchMenuItem,
            new MenuItemCompat.OnActionExpandListener() {
      @Override
      public boolean onMenuItemActionExpand(MenuItem item) {
        if (logInItem != null) logInItem.setVisible(false);
        if (logOutItem != null) logOutItem.setVisible(false);
        return true;
      }

      @Override
      public boolean onMenuItemActionCollapse(MenuItem item) {
        setupLoginAndLogoutMenuItems();
        return true;
      }
    });
    searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
    searchView.setSearchableInfo(searchManager
            .getSearchableInfo(getComponentName()));
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
        SplashActivity.handleRequest(TabsHandler.this,
                UserPromosListFragment.USER_NEEDS_TO_REGISTER);
        return true;
      case R.id.action_log_out:
        SplashActivity.handleRequest(TabsHandler.this,
                UserPromosListFragment.LOG_OUT);
        return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
