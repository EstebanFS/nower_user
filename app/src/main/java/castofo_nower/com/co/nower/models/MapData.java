package castofo_nower.com.co.nower.models;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MapData {

    public static Map<Marker, ArrayList<Promo>> promosMap = new HashMap<>();

    public static void addPromoListToMarker(Marker marker, ArrayList<Promo> promoList) {
        promosMap.put(marker, promoList);
    }
}
