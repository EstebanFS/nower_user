package castofo_nower.com.co.nower.controllers;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.connection.HttpHandler;
import castofo_nower.com.co.nower.helpers.GeolocationInterface;
import castofo_nower.com.co.nower.helpers.SubscribedActivities;
import castofo_nower.com.co.nower.models.Branch;
import castofo_nower.com.co.nower.models.Redemption;
import castofo_nower.com.co.nower.models.User;
import castofo_nower.com.co.nower.support.Geolocation;
import castofo_nower.com.co.nower.models.MapData;
import castofo_nower.com.co.nower.models.Promo;;


public class NowerMap extends FragmentActivity implements SubscribedActivities,
                                                          GeolocationInterface,
                                                          GoogleMap.OnMarkerClickListener,
                                                          GoogleMap.OnInfoWindowClickListener {

    private GoogleMap map;
    private int ZOOM_LEVEL = 14;
    private Geolocation geolocation;
    public Marker userMarker = null;
    public Marker currentMarker = null;

    private HttpHandler httpHandler = new HttpHandler();
    public static final String ACTION_PROMOS = "/promos/locations";
    private Map<String, String> params = new HashMap<String, String>();

    public static final int OP_SUCCEEDED = 0;
    public static final String SHOW_BRANCH_PROMOS = "SHOW_BRANCH_PROMOS";


    private Map<Marker, Branch> branchesMap = new HashMap<>();
    private Map<Integer, ArrayList<Promo>> promosMap = new HashMap<>();
    private Map<Integer, Branch> detailedBranchesMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nower_map);

        geolocation = new Geolocation(NowerMap.this);
        // Se indica a Geolocation la actividad que estará esperando el aviso
        // de cambio de localización.
        geolocation.addListeningActivity(this);
        // Se indica al HttpHandler la actividad que estará esperando la respuesta a la petición.
        httpHandler.addListeningActivity(this);

        // Se captura el mapa dentro de la variable map para poderlo gestionar.
        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMap();

        if (map != null) {
            setUpMap();
            setMapListeners();
            verifyLocationProviders();
            getCurrentPromos();
        } else {

        }
    }

    public void setUpMap() {
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        // Se remueven los controles de zoom para que solamente funcione con pinch-zoom.
        map.getUiSettings().setZoomControlsEnabled(false);
        // Se muestra la brújula dentro del mapa para indicar el Norte.
        map.getUiSettings().setCompassEnabled(true);
        // Se activa la geolocalización del usuario.
        map.setMyLocationEnabled(true);
    }

    public void setMapListeners() {
        map.setOnMarkerClickListener(this);
        map.setOnInfoWindowClickListener(this);
    }

    public void verifyLocationProviders() {
        geolocation.verifyLocationPossibilities();
        if (geolocation.canGetLocation()) {
            centerMapInUserPosition();
        } else {
            // Si no es posible obtener la localización,  se muestra un diálogo para activar el GPS.
            geolocation.askToEnableGPS();
        }
    }

    public void centerMapInUserPosition() {
        geolocation.getUserLocation();
        double latitude = geolocation.getLatitude();
        double longitude = geolocation.getLongitude();
        // Se centra el mapa en la localización actual del usuario.
        animateCameraToPosition(latitude, longitude);
        // Se pone un marcador para indicarle al usuario su posición actual.
        putUserMarker(latitude, longitude);
    }

    public void animateCameraToPosition(double lat, double lon) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), ZOOM_LEVEL));
    }

    public void putUserMarker(double latitude, double longitude) {
        userMarker = map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                        .title(getResources().getString(R.string.you_are_here))
                        .icon(BitmapDescriptorFactory.fromResource
                                (R.drawable.user_marker))
        );
        userMarker.showInfoWindow();
    }

    public void getCurrentPromos() {
        if (httpHandler.isInternetConnectionAvailable(this)) {
            httpHandler.sendRequest(HttpHandler.API_V1, ACTION_PROMOS, "", params, new HttpGet(),
                    NowerMap.this);
        } else {
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.internet_connection_required),
                    Toast.LENGTH_SHORT).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Geolocation.ENABLE_GPS_CODE) {
            if (resultCode == OP_SUCCEEDED) {
                //El usuario activó el GPS.
                geolocation.verifyLocationPossibilities();
                if (geolocation.canGetLocation()) {
                    // Este método se invoca con el fin de que el listener de la localización
                    // sea encendido, ya que se activó el GPS.
                    geolocation.getUserLocation();
                }
                // Ahora se espera el cambio de localización para centrar el mapa.
                // Este cambio se recibe a través de la interface GeolocationInterface.
            }
        }
    }

    @Override
    public void locationChanged(double latitude, double longitude) {
        animateCameraToPosition(latitude, longitude);
        // Es la primera vez que se va a poner el marcador del usuario.
        if (userMarker == null) {
            putUserMarker(latitude, longitude);
        }
        // El marcador ya existía pero se debe mover, ya que la localización del usuario cambió.
        else {
            userMarker.setPosition(new LatLng(latitude, longitude));
            userMarker.showInfoWindow();
        }
    }

    public void getUserRedemptions() {
        if (httpHandler.isInternetConnectionAvailable(this)){
            httpHandler.sendRequest(HttpHandler.API_V1, UserPromoList.ACTION_USER_REDEMPTIONS, "/"
                                    + User.id, params, new HttpGet(), NowerMap.this);
        }
        else {
            Toast.makeText(getApplicationContext(),
                           getResources().getString(R.string.internet_connection_required),
                           Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void notify(String action, JSONObject responseJson) {
        try {
            if (action.equals(ACTION_PROMOS)) {
                Log.i("responseJson", responseJson.toString());
                if (responseJson.getInt(HttpHandler.HTTP_STATUS) == HttpHandler.SUCCESS) {
                    ArrayList<Promo> promoList = new ArrayList<>();
                    Map<Integer, Promo> detailedPromosMap = new HashMap<>();
                    branchesMap.clear();
                    promosMap.clear();
                    detailedBranchesMap.clear();

                    JSONArray locations = responseJson.getJSONArray("locations");
                    // Se recorren todas las promociones obtenidas para dibujarlas en el mapa.
                    for (int i = 0; i < locations.length(); ++i) {
                        promoList.clear();

                        JSONObject internLocation = locations.getJSONObject(i);
                        int id = internLocation.getInt("id");
                        String name = internLocation.getString("name");
                        double latitude = internLocation.getDouble("latitude");
                        double longitude = internLocation.getDouble("longitude");
                        int storeId = internLocation.getInt("store_id");
                        String storeName = internLocation.getString("store_name");

                        JSONArray promos = internLocation.getJSONArray("promos");
                        for (int j = 0; j < promos.length(); ++j) {
                            JSONObject internPromo = promos.getJSONObject(j);
                            int promoId = internPromo.getInt("id");
                            String title = internPromo.getString("title");
                            String expirationDate = internPromo.getString("expiration_date");
                            int availableRedemptions = internPromo.getInt("available_redemptions");
                            // Se genera la lista de promociones para esa localización.
                            Promo p = new Promo(promoId, title, expirationDate,
                                    availableRedemptions);
                            promoList.add(p);

                            // Se crea un mapa de promociones para luego buscar su descripción
                            // con el id.
                            detailedPromosMap.put(promoId, p);
                        }
                        putMarkerAndSavePromoList(id, name, latitude, longitude, storeId, storeName,
                                promoList);
                    }
                    // Se envían al modelo MapData para que puedan ser accedidos
                    // desde otras actividades.
                    // Se debe borrar explícitamente este branchesMap debido a que cada marcador
                    // agregado tiene un ID diferente, razón por la cual, de no hacer esto,
                    // los marcadores no se soobreescribirían sino que se agregarían infinitamente.
                    MapData.clearBranchesMap();
                    MapData.setBranchesMap(branchesMap);

                    MapData.clearPromosMap();
                    MapData.setPromosMap(promosMap);
                    MapData.clearDetailedBranchesMap();
                    MapData.setDetailedBranchesMap(detailedBranchesMap);
                    MapData.clearDetailedPromosMap();
                    MapData.setDetailedPromosMap(detailedPromosMap);

                    // Se hace para actualizar las promociones que el usuario ha obtenido.
                    getUserRedemptions();
                }
            }
            else if (action.equals(UserPromoList.ACTION_USER_REDEMPTIONS)) {
                Log.i("responseJson", responseJson.toString());
                if (responseJson.getInt(HttpHandler.HTTP_STATUS) == HttpHandler.SUCCESS) {
                    JSONArray userRedemptions = responseJson.getJSONArray("redemptions");
                    for(int i = 0; i < userRedemptions.length(); ++i) {
                        JSONObject internRedemption = userRedemptions.getJSONObject(i);
                        int id = internRedemption.getInt("id");
                        int user_id = internRedemption.getInt("user_id");
                        String code = internRedemption.getString("code");
                        int promoId = internRedemption.getInt("promo_id");
                        boolean redeemed = internRedemption.getBoolean("redeemed");

                        Redemption r = new Redemption(code, promoId, redeemed);
                        // Se adiciona la promoción a la lista de promociones del usuario.
                        User.addPromoToRedeem(code, r);
                        // Se asocia el código para redimir con la promoción correspondiente.
                        User.addPromoToRedeemCode(promoId, code);
                    }
                }
            }
        } catch (JSONException e) {

        }
    }

    // Este método se encarga de poner todas las promociones en el mapa y guardarlas localmente.
    public void putMarkerAndSavePromoList(int id, String name, double latitude, double longitude,
                                          int storeId, String storeName,
                                          ArrayList<Promo> promoList) {

        Branch branch = new Branch(id, name, latitude, longitude, storeId, storeName);

        Marker locationMarker = map.addMarker(new MarkerOptions()
                                              .position(new LatLng(latitude, longitude))
                                              .title(storeName + " - " + name)
                                              .icon(BitmapDescriptorFactory.fromResource
                                                    (R.drawable.nower_marker))
                                             );

        branchesMap.put(locationMarker, branch);
        // Se debe clonar el objeto promoList para que al limpiarlo no se pierda el contenido.
        // Esto se debe a que se genera una referencia al objeto y no se guarda como valor.
        promosMap.put(id, new ArrayList<Promo>(promoList));

        detailedBranchesMap.put(id, branch);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (!marker.equals(userMarker)) {
            if (currentMarker == null) {
                marker.showInfoWindow();
                currentMarker = marker;
            } else {
                if (currentMarker.equals(marker)) {
                    marker.hideInfoWindow();
                    currentMarker = null;
                } else {
                    currentMarker.hideInfoWindow();
                    marker.showInfoWindow();
                    currentMarker = marker;
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        // Se activa el detector de gestos para animar las tarjetas de promociones.
        if (!marker.equals(userMarker)) {
            int branchId = MapData.getBranch(marker).getId();
            Intent showPromos = new Intent(NowerMap.this, PromoCardAnimator.class);
            showPromos.putExtra("action", SHOW_BRANCH_PROMOS);
            showPromos.putExtra("branch_id", branchId);
            showPromos.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(showPromos);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nower_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
