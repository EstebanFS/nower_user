package castofo_nower.com.co.nower.controllers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import java.util.List;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.support.TabsAdapter;

public class AuthenticationHandler extends ActionBarActivity {

  public static final int REGISTER_INDEX = 0;
  public static final int LOGIN_INDEX = 1;

  private ViewPager viewPager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_authentication_handler);
    initFragments();
  }

  public void initFragments() {
    viewPager = (ViewPager) findViewById(R.id.view_pager);
    viewPager.setAdapter(new TabsAdapter(getSupportFragmentManager(), this,
            TabsAdapter.AUTHENTICATION));
  }

  public void changeFragment(int position) {
    if (viewPager != null && position < viewPager.getChildCount()) {
      viewPager.setCurrentItem(position);
    }
  }

  public Fragment getFragmentAt(int position) {
    List<Fragment> fragmentsList =  getSupportFragmentManager().getFragments();
    if (fragmentsList != null && position < fragmentsList.size()) {
      return fragmentsList.get(position);
    }
    else return null;
  }

  public void onRegisterClicked(View v) {
    RegisterFragment registerFragment = (RegisterFragment)
            getFragmentAt(REGISTER_INDEX);
    if (registerFragment != null) {
      registerFragment.onRegisterClicked();
    }
  }

  public void onLoginClicked(View v) {
    LoginFragment loginFragment = (LoginFragment) getFragmentAt(LOGIN_INDEX);
    if (loginFragment != null) {
      loginFragment.onLoginClicked();
    }
  }

  public void showDatePickerDialog(View v) {
    RegisterFragment registerFragment = (RegisterFragment)
            getFragmentAt(REGISTER_INDEX);
    if (registerFragment != null) {
      registerFragment.showDatePickerDialog();
    }
  }

  public void onDontHaveAccountClicked(View v) {
    changeFragment(REGISTER_INDEX);
  }

  public void onAlreadyHaveAccountClicked(View v) {
    changeFragment(LOGIN_INDEX);
  }
}
