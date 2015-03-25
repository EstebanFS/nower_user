package castofo_nower.com.co.nower.models;


public class User {

    public static int id = 2;
    public static String email;
    public static String name;
    public static boolean gender;
    public static String birthday;
    public static String password;

    public void setUsetData(int idToSet, String emailToSet, String nameToSet, boolean genderToSet,
                            String birthdayToSet, String passwordToSet) {
        id = idToSet;
        email = emailToSet;
        name = nameToSet;
        gender = genderToSet;
        birthday = birthdayToSet;
        password = passwordToSet;
    }
}
