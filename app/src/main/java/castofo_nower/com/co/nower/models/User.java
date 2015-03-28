package castofo_nower.com.co.nower.models;

import java.util.HashMap;
import java.util.Map;


public class User {

    public static int id = 22;
    public static String email;
    public static String name;
    public static boolean gender;
    public static String birthday;
    public static String password;

    public static Map<String, Redemption> obtainedPromos = new HashMap<>();
    public static Map<Integer, String> promosToRedeemCodes = new HashMap<>();


    public void setUserData(int idToSet, String emailToSet, String nameToSet, boolean genderToSet,
                            String birthdayToSet, String passwordToSet) {
        id = idToSet;
        email = emailToSet;
        name = nameToSet;
        gender = genderToSet;
        birthday = birthdayToSet;
        password = passwordToSet;
    }

    public static void addPromoToRedeem(String code, Redemption r) {
        obtainedPromos.put(code, r);
    }

    public static void addPromoToRedeemCode(Integer promoId, String c) {
        promosToRedeemCodes.put(promoId, c);
    }

    public static Map<Integer, Promo> getUserPromosToRedeem() {
        Map<Integer, Promo> userPromosToRedeem = new HashMap<>();
        for(Map.Entry<String, Redemption> promoToRedeem : User.obtainedPromos.entrySet()){
            Redemption r = promoToRedeem.getValue();
            Promo p = MapData.getPromo(r.getPromoId());
            userPromosToRedeem.put(p.getId(), p);
        }

        return userPromosToRedeem;
    }
}
