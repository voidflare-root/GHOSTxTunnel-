package https.socks.android.activities;

import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.view.View.OnClickListener;
import android.os.Bundle;
import com.nphdevs.bluespace.R;
import android.view.View;
import android.widget.TextView;
import android.content.pm.PackageInfo;
import https.socks.android.util.Utils;
import https.socks.android.ExceptionHandler;

public class AboutActivity extends BaseActivity implements OnClickListener {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO: Implement this method
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
		Toolbar tb = findViewById(R.id.toolbar_main);
		setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        PackageInfo pinfo = Utils.getAppInfo(this);
        if (pinfo != null) {
            String version_nome = pinfo.versionName;
            int version_code = pinfo.versionCode;
            String header_text = String.format("%s (%d)", version_nome, version_code);
			TextView app_info_text = (TextView) findViewById(R.id.appVersion);
			app_info_text.setText(header_text);
		}


		//No quitar estos creditos, dejalo como apoyo al canal
		(findViewById(R.id.tgDev)).setOnClickListener(this);
		(findViewById(R.id.tgChannel)).setOnClickListener(this);

	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.tgDev) {
			open("https://t.me/HexGHOSTxTunnel");
		} else if (id == R.id.tgChannel) {
			open("https://t.me/HexGHOSTxTunnel");
		}
	}

	private void open(String u) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(u));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

}


