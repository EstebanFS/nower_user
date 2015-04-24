package castofo_nower.com.co.nower.support;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.helpers.AlertDialogsResponses;
import castofo_nower.com.co.nower.helpers.GeolocationInterface;


public class Geolocation extends Service implements LocationListener,
AlertDialogsResponses {

  private final Context context;
  private GeolocationInterface listeningActivity;

  // LocationManager es la clase de Android encargada de gestionar la
  // geolocalización del usuario.
  protected LocationManager locationManager;

  private UserFeedback userFeedback = new UserFeedback();

  private boolean isGPSEnabled = false;
  private boolean isNetworkEnabled = false;
  private boolean canGetLocation = false;

  private Location location;

  private double latitude;
  private double longitude;

  private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
  private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 3;

  public static final String ENABLE_GPS = "ENABLE_GPS";
  public static final int ENABLE_GPS_CODE = 0;

  public Geolocation(Context context) {
    this.context = context;
    verifyLocationPossibilities();
  }

  // En este punto se determina a qué Activity será enviado el aviso de cambio
  // en la localización.
  public void addListeningActivity(GeolocationInterface activity) {
    this.listeningActivity = activity;
  }

  public void verifyLocationPossibilities() {
    locationManager = (LocationManager) context
                      .getSystemService(LOCATION_SERVICE);

    isGPSEnabled = locationManager.isProviderEnabled
                   (LocationManager.GPS_PROVIDER);

    isNetworkEnabled = locationManager.isProviderEnabled
                       (LocationManager.NETWORK_PROVIDER);

    if (!isGPSEnabled && !isNetworkEnabled) {

    }
    else {
      //Sí va a ser posible obtener la localización del usuario.
      this.canGetLocation = true;
    }
  }

  public void getUserLocation() {
    try {

      //Primero se intenta la localización con GPS.
      if (isGPSEnabled) {

        getLocationUsingGPS();

      }

      // Luego se intenta con NETWORK_PROVIDER en caso de no haber podido usar
      // el GPS.
      if (isNetworkEnabled) {

        if (location == null) {

          locationManager.requestLocationUpdates
          (LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
           MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

          if (locationManager != null) {
            location = locationManager.getLastKnownLocation
                       (LocationManager.NETWORK_PROVIDER);

            if (location != null) {
              latitude = location.getLatitude();
              longitude = location.getLongitude();
            }
          }
        }
      }


    }
    catch (Exception e) {

    }
  }

  public void getLocationUsingGPS() {
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                           MIN_TIME_BW_UPDATES,
                                           MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                           this);

    if (locationManager != null) {
      location = locationManager.getLastKnownLocation
                 (LocationManager.GPS_PROVIDER);

      if (location != null) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
      }
    }
  }

  public void stopUsingGPS() {
    if (locationManager != null) {
      locationManager.removeUpdates(Geolocation.this);
    }
  }

  public double getLatitude() {
    if (location != null) {
      latitude = location.getLatitude();
    }

    return latitude;
  }

  public double getLongitude() {
    if (location != null) {
      longitude = location.getLongitude();
    }

    return longitude;
  }

  public boolean canGetLocation() {
    return this.canGetLocation;
  }

  public void askToEnableGPS() {
    userFeedback.addListeningActivity(this);
    // Se muestra un diálogo al usuario para que active el GPS.
    UserFeedback
    .showAlertDialog(context, R.string.gps, context.getResources()
                     .getString(R.string.enable_gps_suggestion),
                     R.string.activate, UserFeedback.NO_BUTTON_TO_SHOW,
                     ENABLE_GPS);
  }

  @Override
  public void notifyUserResponse(String action, int buttonPressedId) {
    switch (action) {
      case ENABLE_GPS:
        if (buttonPressedId == R.string.activate) {
          // El usuario decidió activar su GPS y se le redirige a la
          // configuración del dispositivo.
          Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
          ((Activity) context).startActivityForResult
          (intent, Geolocation.ENABLE_GPS_CODE);
        }
        break;
    }
  }

  @Override
  public void onLocationChanged(Location arg0) {
    Log.i("GPS", "Actualización GPS");
    latitude = arg0.getLatitude();
    longitude = arg0.getLongitude();
    // Se utiliza como callback para la actividad en donde está el mapa.
    // De esta manera se le da aviso para que tenga en cuenta la última
    // modificiación de la localización.
    listeningActivity.notifyLocationChange(latitude, longitude);
  }

  @Override
  public void onProviderDisabled(String arg0) {
  }

  @Override
  public void onProviderEnabled(String arg0) {
  }

  @Override
  public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

}
