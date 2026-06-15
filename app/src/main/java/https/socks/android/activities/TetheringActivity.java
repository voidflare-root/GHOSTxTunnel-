package https.socks.android.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.nphdevs.bluespace.R;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class TetheringActivity extends AppCompatActivity {

    private TextView txtSSID, txtPass, txtIP, txtPort;
    private TextView step1Circle, step2Circle, step3Circle;
    private View line1, line2;
    private Button btnHotspot, btnRepeater, btnStart;
    private boolean isHotshareRunning = false;

    // Hotspot status broadcast receiver
    private final BroadcastReceiver hotspotReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
                int state = intent.getIntExtra("wifi_state", 11);
                // 13 = Enabled, 12 = Enabling
                boolean isOn = (state == 13 || state == 12);
                updateHotspotUI(isOn);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tethering);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize Views
        txtSSID = findViewById(R.id.txtSSID);
        txtPass = findViewById(R.id.txtPass);
        txtIP = findViewById(R.id.txtIP);
        txtPort = findViewById(R.id.txtPort);
        btnHotspot = findViewById(R.id.btnHotspot);
        btnRepeater = findViewById(R.id.btnRepeater);
        btnStart = findViewById(R.id.btnStartHotshare);

        // Step UI Views
        step1Circle = findViewById(R.id.step1_circle);
        step2Circle = findViewById(R.id.step2_circle);
        step3Circle = findViewById(R.id.step3_circle);
        line1 = findViewById(R.id.line1);
        line2 = findViewById(R.id.line2);

        btnHotspot.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", "com.android.settings.TetherSettings");
            try {
                startActivity(intent);
            } catch (Exception e) {
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            }
        });

        btnRepeater.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            } catch (Exception e) {
                Toast.makeText(this, "Wi-Fi settings not found", Toast.LENGTH_SHORT).show();
            }
        });

        btnStart.setOnClickListener(v -> {
            if (!isHotspotOnReliable() && !isHotshareRunning) {
                Toast.makeText(this, "Turn hotspot ON, then connect the other phone to this Wi-Fi.", Toast.LENGTH_LONG).show();
            }
            
            if (!isHotshareRunning) {
                startHotshare();
            } else {
                stopHotshare();
            }
        });
    }

    private void updateHotspotUI(boolean isOn) {
        if (isOn) {
            btnHotspot.setText("Open Hotspot Settings");
            if (!isHotshareRunning) {
                updateStepUI(2);
            }
        } else {
            btnHotspot.setText("Start Wi-Fi Hotspot");
            if (isHotshareRunning) {
                stopHotshare();
            }
            updateStepUI(1);
        }
    }

    private void updateStepUI(int step) {
        // Colors
        int activeColor = getResources().getColor(R.color.colorPrimary);
        int inactiveColor = 0xFF444444;
        int textActive = 0xFFFFFFFF;
        int textInactive = 0xFF888888;

        // Reset
        step2Circle.setBackgroundResource(R.drawable.circle_inactive);
        step3Circle.setBackgroundResource(R.drawable.circle_inactive);
        step2Circle.setTextColor(textInactive);
        step3Circle.setTextColor(textInactive);
        line1.setBackgroundColor(inactiveColor);
        line2.setBackgroundColor(inactiveColor);

        if (step >= 1) {
            step1Circle.setBackgroundResource(R.drawable.circle_active);
        }
        if (step >= 2) {
            step2Circle.setBackgroundResource(R.drawable.circle_active);
            step2Circle.setTextColor(textActive);
            line1.setBackgroundColor(activeColor);
        }
        if (step >= 3) {
            step3Circle.setBackgroundResource(R.drawable.circle_active);
            step3Circle.setTextColor(textActive);
            line2.setBackgroundColor(activeColor);
        }
    }

    private boolean isHotspotOnReliable() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("getWifiApState");
            method.setAccessible(true);
            int state = (Integer) method.invoke(wifiManager);
            return (state == 13 || state == 12);
        } catch (Exception e) {
            return false; 
        }
    }

    private void startHotshare() {
        String proxyIp = detectHotspotIp();
        String proxyPort = getLocalProxyPort();
        txtIP.setText(proxyIp);
        txtPort.setText(proxyPort);
        txtSSID.setText("Use Android Hotspot name");
        txtPass.setText("Set in Hotspot settings");
        btnStart.setText("STOP HOTSHARE");
        isHotshareRunning = true;
        updateStepUI(3);
        Toast.makeText(this, "Set proxy on client: " + proxyIp + ":" + proxyPort, Toast.LENGTH_LONG).show();
    }

    private void stopHotshare() {
        txtIP.setText("-");
        txtPort.setText("-");
        txtSSID.setText("-");
        txtPass.setText("-");
        btnStart.setText("START HOTSHARE");
        isHotshareRunning = false;
        updateStepUI(isHotspotOnReliable() ? 2 : 1);
    }

    private String getLocalProxyPort() {
        com.slipkprojects.ultrasshservice.config.Settings settings =
                new com.slipkprojects.ultrasshservice.config.Settings(this);
        return settings.getPrivString(com.slipkprojects.ultrasshservice.config.Settings.PORTA_LOCAL_KEY);
    }

    private String detectHotspotIp() {
        String fallback = "192.168.43.1";
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaces) {
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }
                String name = networkInterface.getName().toLowerCase();
                List<InetAddress> addresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress address : addresses) {
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                        String ip = address.getHostAddress();
                        if (name.contains("wlan") || name.contains("ap") || ip.startsWith("192.168.43.")
                                || ip.startsWith("192.168.137.") || ip.startsWith("172.20.")) {
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(hotspotReceiver, new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED"));
        updateHotspotUI(isHotspotOnReliable());
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(hotspotReceiver);
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
