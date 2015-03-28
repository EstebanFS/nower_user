package castofo_nower.com.co.nower.support;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPreferencesManager {


    private static Context context;
    private static final String PREFS_NAME = "castofo_nower.com.co.nower";
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    // Claves para guardar información.
    public static final String USER_ID_KEY = "id";

    public static void setup(Context context) {
        SharedPreferencesManager.context = context;
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static void saveStringValue(String key, String value) {
        editor.putString(key, value).commit();
    }

    public static String getStringValue(String key) {
        return sharedPreferences.getString(key, null);
    }

    public static void clearSharedPreferences() {
        editor.clear().commit();
    }
}
