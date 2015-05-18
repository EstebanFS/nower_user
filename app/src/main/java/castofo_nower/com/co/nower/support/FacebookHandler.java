package castofo_nower.com.co.nower.support;

import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONObject;

import castofo_nower.com.co.nower.helpers.FacebookLoginResponse;

public class FacebookHandler {

  private static FacebookHandler instance = null;
  private CallbackManager callbackManager = null;
  private FacebookCallback loginCallback = null;
  private FacebookLoginResponse listeningActivity;
  private AccessToken accessToken;

  private FacebookHandler() {

  }

  public static FacebookHandler getInstance() {
    if (instance == null) instance = new FacebookHandler();
    return instance;
  }

  // En este punto se determina a qué Activity será enviada la información
  // luego de que el login de Facebook se procesa.
  public void addListeningActivity(FacebookLoginResponse activity) {
    this.listeningActivity = activity;
  }

  public CallbackManager getCallbackManagerInstance() {
    if (callbackManager == null) {
      callbackManager = CallbackManager.Factory.create();
    }
    return callbackManager;
  }

  // Se llama este callback luego de haber obtenido la información pública y
  // el email del usuario.
  private GraphRequest.GraphJSONObjectCallback graphCallback
  = new GraphRequest.GraphJSONObjectCallback() {
    @Override
    public void onCompleted(JSONObject object, GraphResponse response) {
      listeningActivity.notifyFacebookResponse(object, response);
    }
  };

  public FacebookCallback getLoginCallback() {
    if (loginCallback == null) {
      loginCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
          accessToken = loginResult.getAccessToken();
          GraphRequest request = GraphRequest.newMeRequest(accessToken,
                                                           graphCallback);
          Bundle parameters = new Bundle();
          parameters.putString("fields", "id,name,email,age_range,gender");
          request.setParameters(parameters);
          request.executeAsync();
        }

        @Override
        public void onCancel() { }

        @Override
        public void onError(FacebookException exception) { }
      };
    }
    return loginCallback;
  }

  public void logout() {
    LoginManager.getInstance().logOut();
  }

  public AccessToken getAccessToken() {
    return accessToken;
  }
}
