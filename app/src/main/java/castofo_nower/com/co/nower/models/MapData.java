package castofo_nower.com.co.nower.models;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MapData {

    public static Map<Marker, Branch> branchesMap = new HashMap<>();
    public static Map<Integer, ArrayList<Promo>> promosMap = new HashMap<>();

    public static void setBranchesMap(Map<Marker, Branch> branchesMapToSet) {
        branchesMap = branchesMapToSet;
    }

    public static void setPromosMap(Map<Integer, ArrayList<Promo>> promosMapToSet) {
        promosMap = promosMapToSet;
    }

    public static Branch getBranch(Marker marker) {
        return branchesMap.get(marker);
    }

    public static ArrayList<Promo> getPromoList(int branchId) {
        return promosMap.get(branchId);
    }
}
