package castofo_nower.com.co.nower.models;

import java.util.HashMap;
import java.util.Map;


public class User {

    public static int id = 2;
    public static String email;
    public static String name;
    public static boolean gender;
    public static String birthday;
    public static String password;

    public static Map<String, Redemption> obtainedPromos = new HashMap<>();

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
}
