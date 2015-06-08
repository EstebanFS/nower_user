package castofo_nower.com.co.nower.controllers;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.support.ImageDownloader;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class PromoPictureViewer extends ActionBarActivity {

  private Toolbar toolbar;
  private ImageViewTouch promoPicture;
  private ProgressBar progressBar;
  private String imageURL;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_promo_picture_viewer);
    initToolbar();

    promoPicture = (ImageViewTouch) findViewById(R.id.promo_picture);
    promoPicture.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
    progressBar = (ProgressBar) findViewById(R.id.progress_bar);

    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      Bitmap pictureBitmap = extras.getParcelable("promo_picture_bitmap");
      promoPicture.setImageBitmap(pictureBitmap);
      imageURL = extras.getString("image_url");
    }
    loadImage();
  }

  public void initToolbar() {
    toolbar = (Toolbar) findViewById(R.id.tool_bar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  private void loadImage() {
    if (imageURL != null) {
      ImageDownloader imageDownloader = new ImageDownloader(promoPicture,
              progressBar, imageURL);
      imageDownloader.execute();
    }
    else progressBar.setVisibility(View.GONE);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_promo_picture_viewer, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    switch (id) {
      case android.R.id.home:
        finish();
        return true;
      case R.id.action_settings:
        return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
