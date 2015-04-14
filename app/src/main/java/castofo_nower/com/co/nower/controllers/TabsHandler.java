package castofo_nower.com.co.nower.controllers;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

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

    actionBar = getActionBar();
    actionBar.setHomeButtonEnabled(false);
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

    for (String tab_name : tabs) {
      ActionBar.Tab tab = actionBar.newTab();
      tab.setText(tab_name);
      tab.setTabListener(this);
      actionBar.addTab(tab);
    }

  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_tab_handler, menu);
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

  @Override
  public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
    viewPager.setCurrentItem(tab.getPosition());
    Intent showTabContent = null;
    switch (tab.getPosition()) {
      case 0:
        showTabContent = new Intent(TabsHandler.this, NowerMap.class);
        break;
      case 1:
        showTabContent = new Intent(TabsHandler.this, BranchesList.class);
        break;
      case 2:
        showTabContent = new Intent(TabsHandler.this, UserPromoList.class);
        break;
    }
    if (showTabContent != null) startActivity(showTabContent);
  }

  @Override
  public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

  }

  @Override
  public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

  }
}
