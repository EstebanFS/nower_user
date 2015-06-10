package castofo_nower.com.co.nower.support;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.controllers.BranchesListFragment;
import castofo_nower.com.co.nower.controllers.LoginFragment;
import castofo_nower.com.co.nower.controllers.RegisterFragment;
import castofo_nower.com.co.nower.controllers.UserPromosListFragment;

public class TabsAdapter extends FragmentPagerAdapter {

  public static final int AUTHENTICATION = 1;
  public static final int PROMOS_NAVIGATION = 2;

  private int PAGE_COUNT;
  private String[] tabs;
  private Fragment[] fragments;

  public TabsAdapter(FragmentManager fm, Context context, int type) {
    super(fm);
    switch (type) {
      case AUTHENTICATION:
        tabs = context.getResources().getStringArray(R.array.auth_titles);
        PAGE_COUNT = 2;
        fragments = new Fragment[PAGE_COUNT];
        fragments[0] = new RegisterFragment();
        fragments[1] = new LoginFragment();
        break;
      case PROMOS_NAVIGATION:
        tabs = context.getResources().getStringArray(R.array.tabs_titles);
        PAGE_COUNT = 2;
        fragments = new Fragment[PAGE_COUNT];
        fragments[0] = new BranchesListFragment();
        fragments[1] = new UserPromosListFragment();
        break;
    }
  }

  @Override
  public Fragment getItem(int index) {
    if (index < fragments.length && fragments[index] != null) {
      return fragments[index];
    }
    else return new Fragment();
  }

  @Override
  public int getCount() {
    return PAGE_COUNT;
  }

  @Override
  public CharSequence getPageTitle(int position) {
    return tabs[position];
  }
}
