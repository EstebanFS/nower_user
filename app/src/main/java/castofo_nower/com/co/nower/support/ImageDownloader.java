package castofo_nower.com.co.nower.support;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.InputStream;

import castofo_nower.com.co.nower.connection.HttpHandler;

public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
  private ImageView imageView;
  private ProgressBar progress;

  public ImageDownloader(ImageView imageView, ProgressBar progress) {
    this.imageView = imageView;
    this.progress = progress;
  }

  protected Bitmap doInBackground(String... urls) {
    String imageURL = HttpHandler.DOMAIN + urls[0];
    Bitmap imageBitMap = null;
    try {
      InputStream in = new java.net.URL(imageURL).openStream();
      imageBitMap = BitmapFactory.decodeStream(in);
    }
    catch (Exception e) {

    }
    return imageBitMap;
  }

  protected void onPostExecute(Bitmap result) {
    if (progress != null) progress.setVisibility(View.GONE);
    imageView.setImageBitmap(result);
    imageView.setVisibility(View.VISIBLE);
  }
}
