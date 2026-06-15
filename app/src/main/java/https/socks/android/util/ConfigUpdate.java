package https.socks.android.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by: KervzCodes
 * Date Crated: 08/10/2020
 * Project: SocksHttp-master (ENGLISH)
 **/
public class ConfigUpdate extends AsyncTask<String, String, String> {

    private Context context;
    private OnUpdateListener listener;
    private ProgressDialog progressDialog;
    private boolean isOnCreate;
    private static final String CONFIG_GLYPHS = AESCrypt.JaTest("4paZ4paa4pab4pac4pad4pae4paf4paD4paE4paF4paG4paH4paI4paJ4paK4paQ");

    public ConfigUpdate(Context context, OnUpdateListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void start(boolean isOnCreate) {
        this.isOnCreate = isOnCreate;
        execute();
    }

    public interface OnUpdateListener {
        void onUpdateListener(String result);
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            StringBuilder sb = new StringBuilder();
            URL url = new URL(ConfigSecrets.updateUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(20000);
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode < HttpURLConnection.HTTP_OK || responseCode >= HttpURLConnection.HTTP_MULT_CHOICE) {
                return "Error on getting data: HTTP " + responseCode;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String response;

            while ((response = br.readLine()) != null) {
                sb.append(response);
            }
            return extractConfig(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "Error on getting data: " + e.getMessage();
        }
    }

    private String extractConfig(String response) throws JSONException {
        String data = response == null ? "" : response.trim();
        if (data.startsWith("{")) {
            JSONObject object = new JSONObject(data);
            if (object.has("config")) {
                return object.getString("config").trim();
            }
            if (object.has("encryptedBase64")) {
                return AESCrypt.Jacodes(object.getString("encryptedBase64").trim());
            }
        }
        validateGlyphConfig(data);
        return data;
    }

    private void validateGlyphConfig(String data) throws JSONException {
        int glyphCount = 0;
        for (int i = 0; i < data.length(); i++) {
            String ch = String.valueOf(data.charAt(i));
            if (!CONFIG_GLYPHS.contains(ch)) {
                throw new JSONException("Remote config has invalid encrypted text. Upload the generated config.json again.");
            }
            glyphCount++;
        }
        if (glyphCount == 0 || glyphCount % 2 != 0) {
            throw new JSONException("Remote config is incomplete. Upload the generated config.json again.");
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (!isOnCreate) {
			progressDialog = new ProgressDialog(context, 5);
	//		progressDialog.setIcon(R.drawable.made_with_love);
			progressDialog.setTitle("Checking Server Update");
			progressDialog.setMessage("Loading please wait...");
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.show();
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (!isOnCreate && progressDialog != null) {
            progressDialog.dismiss();
        }
        if (listener != null) {
            listener.onUpdateListener(s);
        }
    }
}
