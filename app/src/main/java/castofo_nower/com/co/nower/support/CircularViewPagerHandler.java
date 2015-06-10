package castofo_nower.com.co.nower.support;

import android.content.Context;
import android.support.v4.view.ViewPager;

import castofo_nower.com.co.nower.controllers.NowerMap;

public class CircularViewPagerHandler implements ViewPager.OnPageChangeListener
{

  private ViewPager mViewPager;
  private int mCurrentPosition;
  private int mScrollState;
  private Context context;

  public CircularViewPagerHandler(final ViewPager viewPager, Context context) {
    mViewPager = viewPager;
    this.context = context;
  }

  @Override
  public void onPageSelected(final int position) {
    mCurrentPosition = position;
    ((NowerMap) context).showMarkerAndSlider(mViewPager.getChildAt(position)
                                             .getId());
  }

  @Override
  public void onPageScrollStateChanged(final int state) {
    handleScrollState(state);
    mScrollState = state;
  }

  private void handleScrollState(final int state) {
    if (state == ViewPager.SCROLL_STATE_IDLE) {
      setNextItemIfNeeded();
    }
  }

  private void setNextItemIfNeeded() {
    if (!isScrollStateSettling()) {
      handleSetNextItem();
    }
  }

  private boolean isScrollStateSettling() {
    return mScrollState == ViewPager.SCROLL_STATE_SETTLING;
  }

  private void handleSetNextItem() {
    final int lastPosition = mViewPager.getAdapter().getCount() - 1;
    if(mCurrentPosition == 0) {
      mViewPager.setCurrentItem(lastPosition, false);
    }
    else if(mCurrentPosition == lastPosition) {
      mViewPager.setCurrentItem(0, false);
    }
  }

  @Override
  public void onPageScrolled(final int position, final float positionOffset,
                             final int positionOffsetPixels) { }
}