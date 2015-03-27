package castofo_nower.com.co.nower.connection;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.controllers.NowerMap;
import castofo_nower.com.co.nower.controllers.PromoCardAnimator;
import castofo_nower.com.co.nower.helpers.SubscribedActivities;


public class HttpHandler {

    // Códigos HTTP.
    public static final String HTTP_STATUS = "http_status";
    public static final int SUCCESS = 200;
    public static final int UNAUTHORIZED = 401;
    public static final int SERVER_INTERNAL_ERROR = 500;

    // Dominio del servidor.
    private static final String DOMAIN = "http://nowerserver.herokuapp.com/";
    public static final String API_V1 = "";

    private SubscribedActivities listeningActivity;

    // Parámetros necesarios para cumplir con las peticiones al Servidor.
    private String nameSpace;
    private String action;
    private String urlParams;
    private Map<String, String> params = new HashMap<String, String>();
    private HttpRequest httpRequest;
    private Context context;

    // En este punto se determina a qué Activity será enviado el resultado de la petición.
    public void addListeningActivity(SubscribedActivities activity) {
        this.listeningActivity = activity;
    }

    public JSONObject getResponse() {
        String url = DOMAIN + nameSpace + action + urlParams;

        HttpGet httpGet = null;
        HttpPost httpPost = null;
        HttpPut httpPut = null;

        HttpClient httpClient = new DefaultHttpClient();
        JSONObject jsonToSend = createJsonObject(action, params);

        HttpResponse httpResponse = null;
        // Debe inicializarse el código de la respuesta de la petición con algún valor.
        // Se selecciona el código de error del servidor por defecto.
        int responseStatusCode = SERVER_INTERNAL_ERROR;
        InputStream inputStream = null;
        // Contendrá el JSON de respuesta.
        String jsonString = "";
        JSONObject responseJson = new JSONObject();

        //Se prepara la petición según su tipo y se adicionan los parámetros
        // en caso de ser necesarios.
        if (httpRequest instanceof HttpGet) {
            httpGet = new HttpGet(url);
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-type", "application/json");
        }
        else if (httpRequest instanceof HttpPost) {
            httpPost = new HttpPost(url);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            try {
                httpPost.setEntity(new StringEntity(jsonToSend.toString()));
            } catch (UnsupportedEncodingException e) {

            }
        }
        else if (httpRequest instanceof HttpPut) {
            httpPut = new HttpPut(url);
            httpPut.setHeader("Accept", "application/json");
            httpPut.setHeader("Content-type", "application/json");
            try {
                httpPut.setEntity(new StringEntity(jsonToSend.toString()));
            } catch (UnsupportedEncodingException e) {

            }
        }

        //Se ejecuta la petición dependiendo de su tipo y se traduce la respuesta a String.
        try {
            if (httpRequest instanceof HttpGet) {
                httpResponse = httpClient.execute(httpGet);
            }
            else if (httpRequest instanceof HttpPost) {
                httpResponse = httpClient.execute(httpPost);
            }
            else if (httpRequest instanceof HttpPut) {
                httpResponse = httpClient.execute(httpPut);
            }
            // Se captura el código de respuesta a la petición.
            responseStatusCode = httpResponse.getStatusLine().getStatusCode();
            inputStream = httpResponse.getEntity().getContent();
            if (inputStream != null) jsonString = convertInputStreamToString(inputStream);

        } catch (IOException e) {

        }

        // Se convierte el String de la respuesta a un JSONObject y se le adiciona el código
        // del estado de la respuesta a la petición HTTP.
        try {
            responseJson = new JSONObject(jsonString);
            responseJson.put(HTTP_STATUS, responseStatusCode);
        } catch (JSONException e) {

        }

        return responseJson;
    }

    public void sendRequest(String nameSpace, String action, String urlParams,
                            Map<String, String> params, HttpRequest httpRequest, Context context) {
        this.nameSpace = nameSpace;
        this.action = action;
        this.urlParams = urlParams;
        this.params.clear();
        this.params.putAll(params);
        this.httpRequest = httpRequest;
        this.context = context;
        new ServerConnection().execute();
    }

    public JSONObject createJsonObject(String action, Map<String, String> params) {
        JSONObject json = new JSONObject();
        JSONObject internJson = new JSONObject();
        JSONArray internJsonArray;
        try {
            // Servicio a ejecutar según el parámetro action.
            switch (action) {
                case PromoCardAnimator.ACTION_PROMOS_DETAILS:
                    internJsonArray = new JSONArray(params.get("promos_ids_list"));
                    json.put("promos", internJsonArray);
                    break;
                case PromoCardAnimator.ACTION_NOW:
                    json.put("promo_id", params.get("promo_id"));
                    json.put("user_id", params.get("user_id"));
                    break;
            }
        } catch (JSONException e) {

        }

        Log.i("responseJson", "JSON enviado: " + json.toString());
        return json;
    }

    // Convierte la respuesta del servidor a String.
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }
        inputStream.close();

        return result;
    }

    public boolean isInternetConnectionAvailable(Context context) {
        boolean connectionFound = false;
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService
                                     (Context.CONNECTIVITY_SERVICE);
        if (conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable()
            && conMgr.getActiveNetworkInfo().isConnected()) {
            connectionFound = true;
        }

        return connectionFound;
    }

    // Codifica el String con un formato universalmente compatible.
    public static String encodeString(String s) {
        String out = "";
        try {
            out = new String(s.getBytes("UTF-8"), "ISO-8859-1");
        } catch (java.io.UnsupportedEncodingException e) {

        }

        return out;
    }

    /*
	 * Esta clase es necesaria para hacer la conexión asincrónica con el Servidor.
	 * Se reciben parámetros para la ejecución, tipo de parámetro de método que indica progreso,
	 * y tipo de parámetro que recibe el método post-ejecución.
	 */

    private class ServerConnection extends AsyncTask<Void, Void, JSONObject> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(context);

            switch (action) {
                case NowerMap.ACTION_PROMOS:
                    progressDialog.setMessage(context.getString(R.string.loading_promos));
                    break;
                case PromoCardAnimator.ACTION_PROMOS_DETAILS:
                    progressDialog.setMessage(context.getString(R.string.obtaining_promos));
                    break;
                case PromoCardAnimator.ACTION_NOW:
                    progressDialog.setMessage(context.getString(R.string.obtaining_promo_code));
                    break;
            }

            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        protected JSONObject doInBackground(Void... params) {
            JSONObject responseJson = getResponse();
            return responseJson;
        }

        protected void onPostExecute(JSONObject responseJson) {
            progressDialog.dismiss();
            listeningActivity.notify(action, responseJson);
        }
    }
}
