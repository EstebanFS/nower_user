package castofo_nower.com.co.nower.models;


import java.util.ArrayList;

public class User {

    public static int id = 2;
    public static String email;
    public static String name;
    public static boolean gender;
    public static String birthday;
    public static String password;

    public static ArrayList<Redemption> obtainedPromos = new ArrayList<>();

    public void setUserData(int idToSet, String emailToSet, String nameToSet, boolean genderToSet,
                            String birthdayToSet, String passwordToSet) {
        id = idToSet;
        email = emailToSet;
        name = nameToSet;
        gender = genderToSet;
        birthday = birthdayToSet;
        password = passwordToSet;
    }

    public static void addPromoToRedeem(Redemption r) {
        obtainedPromos.add(r);
    }
}
