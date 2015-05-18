package castofo_nower.com.co.nower.controllers;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.support.ImageDownloader;

public class PromoPictureViewer extends Activity {

  private ImageView promoPicture;
  private ProgressBar progressBar;
  private String imageURL;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_promo_picture_viewer);
    getActionBar().setDisplayHomeAsUpEnabled(true);
    getActionBar().setHomeButtonEnabled(true);

    promoPicture = (ImageView) findViewById(R.id.promo_picture);
    progressBar = (ProgressBar) findViewById(R.id.progress_bar);

    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      Bitmap pictureBitmap = extras.getParcelable("promo_picture_bitmap");
      promoPicture.setImageBitmap(pictureBitmap);
      imageURL = extras.getString("image_url");
    }
    loadImage();
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
