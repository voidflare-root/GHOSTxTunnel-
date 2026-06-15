package https.socks.android;

import android.graphics.*;
import android.os.*;
import android.widget.*;
import com.nphdevs.bluespace.R;
import androidx.appcompat.widget.Toolbar;
import https.socks.android.activities.BaseActivity;

public class CrashWindow extends BaseActivity {

    private TextView error;
    @Override
    protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.crash_window);
		this.error = (TextView) findViewById(R.id.crashwindow);
        this.error.setText("The session was closed safely. Please open GHOSTxTunnel again.");
		Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(R.string.app_name);
		}
		toolbar.setTitleTextColor(Color.WHITE);
    }
}
