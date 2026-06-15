package https.socks.android.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.slipkprojects.ultrasshservice.config.Settings;
import com.nphdevs.bluespace.R;
import https.socks.android.preference.LocaleHelper;
import https.socks.android.util.AESCrypt;
import https.socks.android.util.ConfigSecrets;
import https.socks.android.util.SecurityGuard;
import https.socks.android.util.Utils;

import static android.content.pm.PackageManager.GET_META_DATA;
import org.json.*;
import java.io.*;
import android.widget.*;
import java.net.*;
import android.content.Intent;
import android.view.View;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.net.Uri;
import https.socks.android.SocksHttpApp;
import https.socks.android.ExceptionHandler;

/**
 * Created by Pankaj on 03-11-2017.
 * Fixed by Assistant for Package: com.ghostxroot.vpn
 */
public abstract class BaseActivity extends AppCompatActivity
{
	public static int mTheme = 0;
    public static String getConfigPassword() {
        return ConfigSecrets.legacyConfigPassword();
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
	}
	
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(LocaleHelper.setLocale(base));
	}
	
	public static void antiremod(final Context c){
        // Disabled: package-name guard caused false positives after legitimate app id changes.
    }
    
    public String getSign() {
        StringBuilder str = new StringBuilder();
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), getPackageManager().GET_SIGNATURES);
            for (Signature signs: pi.signatures) {
                str.append(signs.toCharsString());
            }
        } catch (Exception e) {}
        return str.toString();
    }
	
	protected void writeMyFile(JSONObject obj) {
		try {
            SecurityGuard.enforceConfigAccess(this);
			String encoded_name = 
				String.format("%s", URLEncoder.encode("Config.json", "UTF-8")
							  );

			File dir = new File(getFilesDir(), encoded_name);
			OutputStream out = new FileOutputStream(dir);
			String value = AESCrypt.encrypt(getConfigPassword(), obj.toString(2));
			out.write(value.getBytes());
			out.flush();

			if (out != null)
				out.close();

		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
    protected JSONObject getJSONConfig2(Context context) throws Exception {
        SecurityGuard.enforceConfigAccess(context);
		String json = null;
        File file = new File(context.getFilesDir(), "Config.json");
		if (file.exists()) {
			String json_file = Utils.readStream(new FileInputStream(file));
			json = AESCrypt.decrypt(getConfigPassword(), json_file);
			// return new JSONObject(json);
		} else {
			InputStream inputStream = context.getAssets().open("config/config.json");
			json = AESCrypt.decrypt(getConfigPassword(), Utils.readStream(inputStream));
			// return new JSONObject(json);
		}
        return new JSONObject(json);
    }
	
	protected void resetTitles() {
		try {
			ActivityInfo info = getPackageManager().getActivityInfo(getComponentName(), GET_META_DATA);
			if (info.labelRes != 0) {
				setTitle(info.labelRes);
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
	}
}
