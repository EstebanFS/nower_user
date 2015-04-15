package castofo_nower.com.co.nower.models;

import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


public class MapData {

  private static Map<Marker, Integer> branchesIdsMap;
  private static Map<Integer, Branch> branchesMap;
  private static Map<Integer, Promo> promosMap;

  public static double userLat = -1.0;
  public static double userLong = -1.0;


  public static void setBranchesIdsMap(Map<Marker, Integer> branchesIdsMapToSet)
  {
    if (branchesIdsMap == null) branchesIdsMap = new HashMap<>();
    for (Map.Entry<Marker, Integer> markerBranchId
         : branchesIdsMapToSet.entrySet()) {
      branchesIdsMap.put(markerBranchId.getKey(), markerBranchId.getValue());
    }
  }

  public static void setBranchesMap(Map<Integer, Branch> branchesMapToSet) {
    if (branchesMap == null) branchesMap = new TreeMap<>();
    for (Map.Entry<Integer, Branch> branchIdBranch
         : branchesMapToSet.entrySet()) {
      branchesMap.put(branchIdBranch.getKey(), branchIdBranch.getValue());
    }
  }

  public static void setPromosMap(Map<Integer, Promo> promosMapToSet) {
    if (promosMap == null) promosMap = new TreeMap<>();
    for (Map.Entry<Integer, Promo> promoIdPromo : promosMapToSet.entrySet()) {
      promosMap.put(promoIdPromo.getKey(), promoIdPromo.getValue());
    }
  }

  public static void clearBranchesIdsMap() {
    if (branchesIdsMap != null) branchesIdsMap.clear();
  }

  public static void clearBranchesMap() {
    if (branchesMap != null) branchesMap.clear();
  }

  public static void clearPromosMap() {
    if (promosMap != null) promosMap.clear();
  }

  public static Map<Marker, Integer> getBranchesIdsMap() {
    if (branchesIdsMap == null) branchesIdsMap = new HashMap<>();
    return branchesIdsMap;
  }

  public static Map<Integer, Branch> getBranchesMap() {
    if (branchesMap == null) branchesMap = new TreeMap<>();
    return branchesMap;
  }

  public static Map<Integer, Promo> getPromosMap() {
    if (promosMap == null) promosMap = new TreeMap<>();
    return promosMap;
  }

}
