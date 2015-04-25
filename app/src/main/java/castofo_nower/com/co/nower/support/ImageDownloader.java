package castofo_nower.com.co.nower.support;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.InputStream;

import castofo_nower.com.co.nower.connection.HttpHandler;

public class ImageDownloader extends AsyncTask<Void, Void, Bitmap> {
  private ImageView imageView;
  private ProgressBar progress;
  private String imageURL;
  private static LruCache<String, Bitmap> imagesCache;

  public ImageDownloader(ImageView imageView, ProgressBar progress,
                         String imageURL) {
    this.imageView = imageView;
    this.progress = progress;
    this.imageURL = imageURL;
    // Configurar la memoria caché para guardar los logos.
    if (ImageDownloader.imagesCache == null) setupLruCache();
  }

  private void setupLruCache() {
    // Obtenemos la memoria máxima que puede usar la aplicación.
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

    // Usamos 1 / 8 de la memoria disponible para el caché.
    final int cacheSize = maxMemory / 8;

    imagesCache = new LruCache<String, Bitmap> (cacheSize) {
      @Override
      protected int sizeOf(String key, Bitmap bitmap) {
        // Se mide en kilobytes.
        return bitmap.getByteCount() / 1024;
      }
    };
  }

  protected void onPreExecute() {
    Bitmap cachedImage = ImageDownloader.imagesCache.get(imageURL);
    if (cachedImage != null) {
      showResultImage(cachedImage);
      this.cancel(true);
    }
  }

  protected Bitmap doInBackground(Void... params) {
    String fullImageURL = HttpHandler.DOMAIN + imageURL;
    Bitmap imageBitMap = null;
    try {
      InputStream in = new java.net.URL(fullImageURL).openStream();
      imageBitMap = BitmapFactory.decodeStream(in);
    }
    catch (Exception e) {

    }
    ImageDownloader.imagesCache.put(imageURL, imageBitMap);
    return imageBitMap;
  }

  protected void onPostExecute(Bitmap result) {
    showResultImage(result);
  }

  private void showResultImage(Bitmap result) {
    if (progress != null) progress.setVisibility(View.GONE);
    imageView.setImageBitmap(result);
    imageView.setVisibility(View.VISIBLE);
  }
}
