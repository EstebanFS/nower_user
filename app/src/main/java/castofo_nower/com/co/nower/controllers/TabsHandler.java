package castofo_nower.com.co.nower.controllers;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.support.TabsAdapter;


public class TabsHandler extends FragmentActivity implements
ActionBar.TabListener {

  private ViewPager viewPager;
  private ActionBar actionBar;
  private String[] tabs;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_tabs_handler);
    tabs = getResources().getStringArray(R.array.tabs_names);

    viewPager = (ViewPager) findViewById(R.id.view_pager);
    viewPager.setAdapter(new TabsAdapter(getSupportFragmentManager()));
    viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
    {
      @Override
      public void onPageSelected(int position) {
        getActionBar().setSelectedNavigationItem(position);
      }
    });

    actionBar = getActionBar();
    actionBar.setHomeButtonEnabled(false);
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

    for (String tabName : tabs) {
      ActionBar.Tab tab = actionBar.newTab();
      tab.setText(tabName);
      tab.setTabListener(this);
      actionBar.addTab(tab);
    }
    handleWithExtras();
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
            if (viewPager != null && viewPager.getChildCount() >= 1) {
              getActionBar().setSelectedNavigationItem(0);
              viewPager.setCurrentItem(0);
            }
            break;
          case UserPromosListFragment.LIST_USER_PROMOS:
            if (viewPager != null && viewPager.getChildCount() >= 2) {
              getActionBar().setSelectedNavigationItem(1);
              viewPager.setCurrentItem(1);
            }
            break;
        }
      }
    }
  }

  private void openMap() {
    Intent openMap = new Intent(TabsHandler.this, NowerMap.class);
    startActivity(openMap);
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

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    handleWithExtras();
  }

  @Override
  protected void onRestart() {
    super.onRestart();
    // Se hace actualización del estado de las promociones del usuario al
    // regresar a la lista.
    List<Fragment> fragmentsList =  getSupportFragmentManager().getFragments();
    if (fragmentsList != null && fragmentsList.size() >= 2) {
      UserPromosListFragment userPromosListFragment
      = (UserPromosListFragment) fragmentsList.get(1);
      if (userPromosListFragment != null) {
        userPromosListFragment.sendRequest(UserPromosListFragment
                .ACTION_USER_REDEMPTIONS);
      }
    }
  }

  @Override
  public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
    viewPager.setCurrentItem(tab.getPosition());
  }

  @Override
  public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

  }

  @Override
  public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_tabs_handler, menu);
    if (SplashActivity.isThereLoginInstance()) {
      menu.findItem(R.id.action_log_in).setVisible(false);
    }
    else {
      menu.findItem(R.id.action_log_out).setVisible(false);
    }

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    switch (id) {
      case R.id.action_show_map:
        openMap();
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
