package castofo_nower.com.co.nower.models;

import java.util.LinkedHashMap;
import java.util.Map;


public class User {

  public static int id;
  public static String email;
  public static String name;
  public static String gender;
  public static String birthday;

  private static Map<Integer, Redemption> takenPromos;

  public static void setUserData(int idToSet, String emailToSet,
                                 String nameToSet, String genderToSet,
                                 String birthdayToSet) {
    id = idToSet;
    email = emailToSet;
    name = nameToSet;
    gender = genderToSet;
    birthday = birthdayToSet;
  }

  public static void addPromoToTakenPromos(Integer promoId,
                                           Redemption redemption) {
    if (takenPromos == null) takenPromos = new LinkedHashMap<>();
    takenPromos.put(promoId, redemption);
  }

  public static Map<Integer, Redemption> getTakenPromos() {
    if (takenPromos == null) takenPromos = new LinkedHashMap<>();
    return takenPromos;
  }

}
