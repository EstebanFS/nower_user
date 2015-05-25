package castofo_nower.com.co.nower.support;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.controllers.BranchesListFragment;
import castofo_nower.com.co.nower.controllers.UserPromosListFragment;

public class TabsAdapter extends FragmentPagerAdapter {

  private final int PAGE_COUNT = 2;
  private String[] tabs;


  public TabsAdapter(FragmentManager fm, Context context) {
    super(fm);
    tabs = context.getResources().getStringArray(R.array.tabs_titles);
  }

  @Override
  public Fragment getItem(int index) {
    switch (index) {
      case 0:
        return new BranchesListFragment();
      case 1:
        return new UserPromosListFragment();
    }
    return new BranchesListFragment();
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
