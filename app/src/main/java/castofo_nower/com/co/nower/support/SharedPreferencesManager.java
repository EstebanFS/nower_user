package castofo_nower.com.co.nower.support;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPreferencesManager {


  private static Context context;
  private static final String PREFS_NAME = "castofo_nower.com.co.nower";
  private static SharedPreferences sharedPreferences;
  private static SharedPreferences.Editor editor;

  // Claves para guardar información.
  public static final String USER_ID = "USER_ID";
  public static final String USER_EMAIL = "USER_EMAIL";
  public static final String USER_NAME = "USER_NAME";
  public static final String USER_GENDER = "USER_GENDER";
  public static final String USER_BIRTHDAY = "USER_BIRTHDAY";

  private static int noValueSaved = -1;

  public static void setup(Context context) {
    SharedPreferencesManager.context = context;
    sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    editor = sharedPreferences.edit();
  }

  public static void saveStringValue(String key, String value) {
    editor.putString(key, value).commit();
  }

  public static void saveIntegerValue(String key, int value) {
    editor.putInt(key, value).commit();
  }

  public static String getStringValue(String key) {
    return sharedPreferences.getString(key, null);
  }

  public static int getIntegerValue(String key) {
    return sharedPreferences.getInt(key, noValueSaved);
  }

  public static void clearSharedPreferences() {
    editor.clear().commit();
  }

}
