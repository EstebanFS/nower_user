package castofo_nower.com.co.nower.support;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import castofo_nower.com.co.nower.helpers.ParsedErrors;


public class RequestErrorsHandler {

  private static ParsedErrors listeningActivity;

  // En este punto se determina a qué Activity serán enviados los errores
  // organizados.
  public void addListeningActivity(ParsedErrors activity) {
    this.listeningActivity = activity;
  }

  public static void parseErrors(String action, JSONObject errors) {
    Map<String, String> errorsMessages = new HashMap<>();
    Iterator<?> keys = errors.keys();
    try {
      while (keys.hasNext()) {
        String key = (String) keys.next();
        JSONArray errorsStrings = errors.getJSONArray(key);
        String message = "";
        for (int i = 0; i < errorsStrings.length(); ++i) {
          if (message.isEmpty()) message += errorsStrings.getString(i);
          else message += ("\n" + errorsStrings.getString(i));
        }
        if (!message.isEmpty()) {
          errorsMessages.put(key, message);
        }
      }
      listeningActivity.notifyParsedErrors(action, errorsMessages);
    }
    catch (JSONException e) {

    }
  }

}
