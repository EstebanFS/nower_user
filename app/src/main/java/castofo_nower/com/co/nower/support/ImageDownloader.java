package castofo_nower.com.co.nower.support;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;

import java.io.InputStream;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.connection.HttpHandler;

public class ImageDownloader extends AsyncTask<Void, Void, Bitmap> {

  // Acción a realizar una vez se recupere la imagen.
  private int action;
  // En caso de tener que actualizar un ImageView.
  private ImageView imageView;
  private View viewToHide;
  private String imageURL;
  // En caso de tener que actualizar un marcador.
  private View bubbleMarker;
  private Marker marker;
  private Context context;
  private static LruCache<String, Bitmap> imagesCache;

  private static final int UPDATE_VIEW_HIDE_VIEW = 1;
  private static final int UPDATE_VIEW_NO_HIDE_VIEW = 2;
  private static final int UPDATE_MARKER = 3;


  public ImageDownloader(ImageView imageView, View viewToHide,
                         String imageURL) {
    this.action = UPDATE_VIEW_HIDE_VIEW;
    this.imageView = imageView;
    this.viewToHide = viewToHide;
    this.imageURL = imageURL;
    // Configurar la memoria caché para guardar los logos.
    if (ImageDownloader.imagesCache == null) setupLruCache();
  }

  public ImageDownloader(ImageView imageView, String imageURL) {
    this.action = UPDATE_VIEW_NO_HIDE_VIEW;
    this.imageView = imageView;
    this.imageURL = imageURL;
    // Configurar la memoria caché para guardar los logos.
    if (ImageDownloader.imagesCache == null) setupLruCache();
  }

  public ImageDownloader(View bubbleMarker, Marker marker, Context context,
                         String imageURL) {
    this.action = UPDATE_MARKER;
    this.bubbleMarker = bubbleMarker;
    this.imageURL = imageURL;
    this.marker = marker;
    this.context = context;
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
      // Esta operación aborta el AsyncTask para evitar volver a descargar la
      // imagen, ya que se encontraba en disponible en caché.
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
    switch (action) {
      case UPDATE_VIEW_HIDE_VIEW:
        viewToHide.setVisibility(View.GONE);
        imageView.setImageBitmap(result);
        imageView.setVisibility(View.VISIBLE);
        break;
      case UPDATE_VIEW_NO_HIDE_VIEW:
        imageView.setImageBitmap(result);
        imageView.setVisibility(View.VISIBLE);
        break;
      case UPDATE_MARKER:
        ImageView logoView =
                (ImageView) bubbleMarker.findViewById(R.id.marker_logo);
        logoView.setImageBitmap(result);
        Bitmap markerIcon = createBitmapFromView(context, bubbleMarker);
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(markerIcon));
    }
  }

  public static Bitmap createBitmapFromView(Context context, View view) {
    DisplayMetrics displayMetrics = new DisplayMetrics();
    ((Activity) context).getWindowManager().getDefaultDisplay()
            .getMetrics(displayMetrics);
    view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
    view.layout(0, 0, displayMetrics.widthPixels,
            displayMetrics.heightPixels);
    view.buildDrawingCache();
    Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
            view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    view.draw(canvas);
    return bitmap;
  }
}
