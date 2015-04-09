package castofo_nower.com.co.nower.models;

import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


public class MapData {

  public static Map<Marker, Integer> branchesIdsMap = new HashMap<>();
  public static Map<Integer, Branch> branchesMap = new TreeMap<>();
  public static Map<Integer, Promo> promosMap = new TreeMap<>();

  public static void setBranchesIdsMap(Map<Marker, Integer> branchesIdsMapToSet) {
    for (Map.Entry<Marker, Integer> markerBranchId : branchesIdsMapToSet.entrySet()) {
      branchesIdsMap.put(markerBranchId.getKey(), markerBranchId.getValue());
    }
  }

  public static void setBranchesMap(Map<Integer, Branch> branchesMapToSet) {
    for (Map.Entry<Integer, Branch> branchIdBranch : branchesMapToSet.entrySet()) {
      branchesMap.put(branchIdBranch.getKey(), branchIdBranch.getValue());
    }
  }

  public static void setPromosMap(Map<Integer, Promo> promosMapToSet) {
    for (Map.Entry<Integer, Promo> promoIdPromo : promosMapToSet.entrySet()) {
      promosMap.put(promoIdPromo.getKey(), promoIdPromo.getValue());
    }
  }

  public static void clearBranchesIdsMap() {
    branchesIdsMap.clear();
  }

  public static void clearBranchesMap() {
    branchesMap.clear();
  }

  public static void clearPromosMap() {
    promosMap.clear();
  }

}
