package castofo_nower.com.co.nower.support;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import castofo_nower.com.co.nower.controllers.BranchesListFragment;
import castofo_nower.com.co.nower.controllers.UserPromosListFragment;


public class TabsAdapter extends FragmentPagerAdapter {

  public TabsAdapter(FragmentManager fm) {
    super(fm);
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
    return 2;
  }
}
