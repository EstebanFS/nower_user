package castofo_nower.com.co.nower.helpers;

import com.facebook.GraphResponse;

import org.json.JSONObject;

public interface FacebookLoginResponse {

  public void notifyFacebookResponse(JSONObject object, GraphResponse response);
}
