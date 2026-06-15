package com.slipkprojects.ultrasshservice;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.nphdevs.bluespace.R;
import com.slipkprojects.ultrasshservice.config.Settings;
import com.slipkprojects.ultrasshservice.logger.ConnectionStatus;
import com.slipkprojects.ultrasshservice.logger.SkStatus;
import com.slipkprojects.ultrasshservice.tunnel.TunnelManagerHelper;

public class LaunchVpn extends AppCompatActivity
	implements DialogInterface.OnCancelListener
{
	public static final String EXTRA_HIDELOG = "com.slipkprojects.sockshttp.showNoLogWindow";

	private static final int START_VPN_PROFILE = 70;
	
	private Settings mConfig;
	private boolean mhideLog = false;
	
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.launchvpn);
		
		mConfig = new Settings(this);

        startVpnFromIntent();
		//throw new RuntimeException();
    }
	
	protected void startVpnFromIntent() {

        final Intent intent = getIntent();
        final String action = intent.getAction();


        if (Intent.ACTION_MAIN.equals(action)) {
            // Check if we need to clear the log
            if (mConfig.getAutoClearLog())
				SkStatus.clearLog();

            mhideLog = intent.getBooleanExtra(EXTRA_HIDELOG, false);
			
            launchVPN();
        }
    }


	@Override
	public void onCancel(DialogInterface p1)
	{
		SkStatus.updateStateString("USER_VPN_PASSWORD_CANCELLED", "", R.string.state_user_vpn_password_cancelled,
			ConnectionStatus.LEVEL_NOTCONNECTED);
		finish();
	}
	
	private void showLogWindow() {
        Intent updateView = new Intent("com.slipkprojects.sockshttp:openLogs");
		LocalBroadcastManager.getInstance(this)
			.sendBroadcast(updateView);
    }
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == START_VPN_PROFILE) {
            if (resultCode == Activity.RESULT_OK) {
				if (!mhideLog) {
					showLogWindow();
				}
				TunnelManagerHelper.startSocksHttp(this);

				finish();
				
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User does not want us to start, so we just vanish
                SkStatus.updateStateString("USER_VPN_PERMISSION_CANCELLED", "", R.string.state_user_vpn_permission_cancelled,
					ConnectionStatus.LEVEL_NOTCONNECTED);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    SkStatus.logError(R.string.nought_alwayson_warning);

                finish();
            }
        }
    }
	
	private void launchVPN() {
		Intent intent = VpnService.prepare(this);
        	
        if (intent != null) {
            SkStatus.updateStateString("USER_VPN_PERMISSION", "", R.string.state_user_vpn_permission,
				ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT);
            // Start the query
            try {
                startActivityForResult(intent, START_VPN_PROFILE);
            } catch (ActivityNotFoundException ane) {
                // Shame on you Sony! At least one user reported that
                // an official Sony Xperia Arc S image triggers this exception
                SkStatus.logError(R.string.no_vpn_support_image);
                showLogWindow();
            }
        } else {
            onActivityResult(START_VPN_PROFILE, Activity.RESULT_OK, null);
        }
    }
	
}
