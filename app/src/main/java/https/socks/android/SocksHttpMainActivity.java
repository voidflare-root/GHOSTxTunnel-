package https.socks.android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.OpenableColumns;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.netfreemexico.generador.NetFreeMXGen;
import com.nphdevs.bluespace.R;
import com.slipkprojects.ultrasshservice.LaunchVpn;
import com.slipkprojects.ultrasshservice.config.ConfigParser;
import com.slipkprojects.ultrasshservice.config.Settings;
import com.slipkprojects.ultrasshservice.logger.ConnectionStatus;
import com.slipkprojects.ultrasshservice.logger.SkStatus;
import com.slipkprojects.ultrasshservice.tunnel.TunnelManagerHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import https.socks.android.activities.AboutActivity;
import https.socks.android.activities.BaseActivity;
import https.socks.android.activities.ConfigGeralActivity;
import https.socks.android.activities.HostCheckerActivity;
import https.socks.android.activities.ServersActivity;
import https.socks.android.activities.SpeedTestActivity;
import https.socks.android.activities.SubdomainFinderActivity;
import https.socks.android.activities.TetheringActivity;
import https.socks.android.adapter.LogsAdapter;
import https.socks.android.util.AESCrypt;
import https.socks.android.util.ConfigUpdate;
import https.socks.android.util.ConfigUtil;
import https.socks.android.util.RetrieveData;
import https.socks.android.util.SecurityGuard;
import https.socks.android.util.StoredData;
import https.socks.android.util.Utils;

/**
 * Activity Principal
 *
 * @author SlipkHunter
 */

@SuppressLint({"SetTextI18n", "NonConstantResourceId"})
@SuppressWarnings("deprecation")
public class SocksHttpMainActivity extends BaseActivity
        implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, SkStatus.StateListener {
    private SharedPreferences prefs;
    private DrawerLayout drawer;
    private static final int PICK_FILE = 85;

    private ViewPager vp;

    public static final String TODAY_DATA = "todaydata";
    private static final String TELEGRAM_CHANNEL_URL = "https://t.me/HexGHOSTxTunnel";
    private static final String CONFIG_UPDATE_CHANNEL_ID = "config_updates";
    private static final int CONFIG_UPDATE_NOTIFICATION_ID = 2101;
    private static final int NOTIFICATION_PERMISSION_REQUEST = 2102;
    private static final String PREF_DARK_MODE = "dark_mode_enabled";
    private static final String PREF_CONFIG_SOURCE_NAME = "config_source_name";
    private SharedPreferences myData;
    private static final int S_ONSTART_CALLED = 2;
    private static final int S_BIND_CALLED = 1;
    public TextView bytes_in_view;
    public TextView bytes_out_view;

    private static final String UPDATE_VIEWS = "MainUpdate";
    public static final String OPEN_LOGS = "com.slipkprojects.sockshttp:openLogs";
    private Settings mConfig;
    private Handler mHandler;
    private LogsAdapter mLogAdapter;
    private MenuItem clearLogsMenuItem;
    private Button starterButton;
    private ConfigUtil config;
    private TextView serverName, serverInfo, textStatus, configVersion, networkStatus;
    private TextView customDnsStatus, serverLocationNotes;
    private SwitchCompat customDnsSwitch;
    private ImageView serverImage;
    private LinearLayout serversL;
    private static final String[] tabTitle = {"HOME", "LOGS"};
    private static final String CLOUDFLARE_DNS_PRIMARY = "1.1.1.1";
    private static final String CLOUDFLARE_DNS_SECONDARY = "1.0.0.1";
    private static final String GOOGLE_DNS_PRIMARY = "8.8.8.8";
    private static final String GOOGLE_DNS_SECONDARY = "8.8.4.4";
    private static final long HTTP_PING_INTERVAL_MS = 5000L;
    private boolean httpPingRunning = false;
    private long lastHttpPingAt = 0L;
    private boolean serverLocationLogged = false;
    private boolean tunnelConnected = false;
    private String connectedLiveLocationInfo = null;

    //poner en false si no quieres que la aplicacion sea modo token/user-pass, al poner en false se leera el usuario y contraseña del generador
    private final boolean MODO_TOKEN_USER_PASS = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        applySavedTheme();
        super.onCreate(savedInstanceState);
        if (!SecurityGuard.verifyOrClose(this)) {
            return;
        }
        BaseActivity.antiremod(this);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        mHandler = new Handler();
        mConfig = new Settings(this);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        doLayout();
        prefs = mConfig.getPrefsPrivate();
        myData = getSharedPreferences(TODAY_DATA, Context.MODE_PRIVATE);
        boolean showFirstTime = prefs.getBoolean("connect_first_time", true);
        int lastVersion = prefs.getInt("last_version", 0);
        // http_sign(this);
        if (showFirstTime) {
            SharedPreferences.Editor pEdit = prefs.edit();
            pEdit.putBoolean("connect_first_time", false);
            pEdit.apply();

            Settings.setDefaultConfig(this);
        }

        try {
            int idAtual = ConfigParser.getBuildId(this);

            if (lastVersion < idAtual) {
                SharedPreferences.Editor pEdit = prefs.edit();
                pEdit.putInt("last_version", idAtual);
                pEdit.apply();

                // se estiver atualizando
                if (!showFirstTime) {
                    if (lastVersion <= 12) {
                        Settings.setDefaultConfig(this);
                        Settings.clearSettings(this);

                    }
                }
            }
        } catch (IOException ignored) {
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_VIEWS);
        filter.addAction(OPEN_LOGS);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mActivityReceiver, filter);
        if (!StoredData.isSetData) {
            StoredData.setZero();
        }
        liveData();


        NetFreeMXGen.getInstance().init(this, prefs, MODO_TOKEN_USER_PASS);

        //Activa esta linea de abajo si tu contraseña del token es personalizada es decir que la contraseña no sea el token, cambialo por tu contraseña
        //NetFreeMXGen.getInstance().setTokenPassword("Cristian");


        Utils.notificationP(this);
        
        showWelcomeDialog();
    }

    private void showWelcomeDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_join_telegram, null);
        AlertDialog alert = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(true)
                .create();
        Button closeButton = view.findViewById(R.id.btnCloseTelegram);
        Button joinButton = view.findViewById(R.id.btnJoinTelegram);
        closeButton.setOnClickListener(v -> alert.dismiss());
        joinButton.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_CHANNEL_URL)));
            alert.dismiss();
        });
        alert.show();
    }

    public void s(View v) {
        startActivity(new Intent(SocksHttpMainActivity.this, ServersActivity.class));
    }

    private void doLayout() {
        setContentView(R.layout.activity_main_drawer);
        Toolbar toolbar_main = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
            getSupportActionBar().setSubtitle("Network: checking | IP: checking");
        }
        prefs = mConfig.getPrefsPrivate();
        bytes_in_view = findViewById(R.id.bytes_in);
        bytes_out_view = findViewById(R.id.bytes_out);
        drawer = findViewById(R.id.drawer);
        NavigationView navi = findViewById(R.id.navigation);
        starterButton = findViewById(R.id.activity_starterButtonMain);
        config = new ConfigUtil(this);
        navi.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar_main, R.string.app_name, R.string.app_name);
        toggle.syncState();
        drawer.addDrawerListener(toggle);
        requestUpdateNotificationPermission();
        updateConfig(true);
        starterButton.setOnClickListener(this);
        doTabs();

        serverName = findViewById(R.id.sName);
        serverInfo = findViewById(R.id.sInfo);
        serverImage = findViewById(R.id.sIcon);
        textStatus = findViewById(R.id.textStatus);
        networkStatus = null;
        customDnsStatus = findViewById(R.id.customDnsStatus);
        customDnsSwitch = findViewById(R.id.customDnsSwitch);
        serverLocationNotes = findViewById(R.id.serverLocationNotes);
        ((TextView) findViewById(R.id.appVersion)).setText(getAppInfoString(this));
        configVersion = findViewById(R.id.configVersion);
        configVersion.setText(config.getVersion());
        serversL = findViewById(R.id.serversL);
        setupCustomDnsSwitch();
        startHttpPingLoop();
        updateView();

        // Home Screen Buttons Logic
        try {
            // Speed Test Button
            Button speedTestBtn = findViewById(R.id.btn_speed_test);
            if (speedTestBtn != null) {
                speedTestBtn.setOnClickListener(v -> startActivity(new Intent(this, SpeedTestActivity.class)));
            }

            LinearLayout taskUpdate = findViewById(R.id.btn_task_update);
            if (taskUpdate != null) {
                taskUpdate.setOnClickListener(v -> updateConfig(false));
            }

            LinearLayout taskTelegram = findViewById(R.id.btn_task_telegram);
            if (taskTelegram != null) {
                taskTelegram.setOnClickListener(v -> openTelegramChannel());
            }

            LinearLayout taskExit = findViewById(R.id.btn_task_exit);
            if (taskExit != null) {
                taskExit.setOnClickListener(v -> Utils.exitAll(SocksHttpMainActivity.this));
            }
        } catch (Exception ignored) {}
    }

    private void setupCustomDnsSwitch() {
        if (customDnsSwitch == null) {
            return;
        }
        customDnsSwitch.setOnCheckedChangeListener(null);
        boolean dnsForward = mConfig.getVpnDnsForward();
        if (dnsForward) {
            mConfig.setVpnDnsResolvers(CLOUDFLARE_DNS_PRIMARY, CLOUDFLARE_DNS_SECONDARY);
        } else {
            mConfig.setVpnDnsForward(false);
        }
        customDnsSwitch.setChecked(dnsForward);
        updateCustomDnsStatus(dnsForward);
        customDnsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mConfig.setVpnDnsForward(isChecked);
            if (isChecked) {
                mConfig.setVpnDnsResolvers(CLOUDFLARE_DNS_PRIMARY, CLOUDFLARE_DNS_SECONDARY);
                SkStatus.logInfo("Custom DNS enabled: Cloudflare " + CLOUDFLARE_DNS_PRIMARY + " / " + CLOUDFLARE_DNS_SECONDARY);
            } else {
                SkStatus.logInfo("Custom DNS disabled: Google " + GOOGLE_DNS_PRIMARY + " / " + GOOGLE_DNS_SECONDARY);
            }
            updateCustomDnsStatus(isChecked);
        });
    }

    private void updateCustomDnsStatus(boolean enabled) {
        if (customDnsStatus == null) {
            return;
        }
        customDnsStatus.setText(enabled
                ? "Cloudflare DNS: " + CLOUDFLARE_DNS_PRIMARY + " / " + CLOUDFLARE_DNS_SECONDARY
                : "Google DNS: " + GOOGLE_DNS_PRIMARY + " / " + GOOGLE_DNS_SECONDARY);
    }

    private void openTelegramChannel() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_CHANNEL_URL)));
        } catch (Exception e) {
            SocksHttpApp.toast(getApplicationContext(), R.color.red, "Telegram not found");
        }
    }


    private void liveData() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(() -> getData());
            }
        }, 0, 1000);
    }

    private void startHttpPingLoop() {
        if (httpPingRunning) {
            return;
        }
        httpPingRunning = true;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!httpPingRunning) {
                    return;
                }
                maybeRunHttpPing();
                mHandler.postDelayed(this, HTTP_PING_INTERVAL_MS);
            }
        }, HTTP_PING_INTERVAL_MS);
    }

    private void maybeRunHttpPing() {
        if (!tunnelConnected || !SkStatus.isTunnelActive() || !mConfig.getHttpPing()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastHttpPingAt < HTTP_PING_INTERVAL_MS - 500) {
            return;
        }
        lastHttpPingAt = now;
        new Thread(() -> {
            PingTarget target = getSelectedServerPingTarget();
            if (target == null) {
                SkStatus.logWarning("HTTP Ping: timeout");
                return;
            }
            long start = System.currentTimeMillis();
            Socket socket = null;
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(target.host, target.port), 4000);
                long ping = System.currentTimeMillis() - start;
                SkStatus.logInfo("HTTP Ping: " + ping + " ms");
            } catch (Exception e) {
                SkStatus.logWarning("HTTP Ping: timeout");
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException ignored) {}
                }
            }
        }).start();
    }

    private PingTarget getSelectedServerPingTarget() {
        try {
            String activeHost = firstNonEmpty(mConfig.getPrivString(Settings.SERVIDOR_KEY));
            String activePort = firstNonEmpty(mConfig.getPrivString(Settings.SERVIDOR_PORTA_KEY));
            if (activeHost != null) {
                int port = getPortFromString(activePort, getEmbeddedPort(activeHost));
                activeHost = cleanHost(activeHost);
                if (!activeHost.trim().isEmpty()) {
                    return new PingTarget(activeHost.trim(), port);
                }
            }

            JSONObject object = getSelectedServerObject();
            if (object != null) {
                String host = firstNonEmpty(
                        object.optString("sshServer"),
                        object.optString("ServerHost"),
                        object.optString("ServerIP"),
                        object.optString("IP"),
                        object.optString("Server"),
                        object.optString("Host"),
                        object.optString("ProxyIP"),
                        object.optString("ProxyHost"),
                        object.optString("SNI"),
                        object.optString("Sni"),
                        getV2rayHost(object));
                if (host != null) {
                    int port = getSelectedServerPort(object, host);
                    host = cleanHost(host);
                    if (!host.trim().isEmpty()) {
                        return new PingTarget(host.trim(), port);
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public void getData() {
        boolean isRunning = SkStatus.isTunnelActive();
        long mUpload, mDownload, saved_Send, saved_Down/*,up, down*/;
        String saved_date, tDate;
        List<Long> allData;
        allData = RetrieveData.findData();
        mDownload = allData.get(0);
        mUpload = allData.get(1);
        StoredData.storedData(mDownload, mUpload);
        Calendar ca = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        tDate = sdf.format(ca.getTime());
        saved_date = myData.getString("today_date", "empty");
        SharedPreferences.Editor editor = myData.edit();
        if (saved_date.equals(tDate)) {
            saved_Send = myData.getLong("UP_DATA", 0);
            saved_Down = myData.getLong("DOWN_DATA", 0);
            editor.putLong("UP_DATA", mUpload + saved_Send);
            editor.putLong("DOWN_DATA", mDownload + saved_Down);
            editor.apply();
        } else {
            editor.clear();
            editor.putString("today_date", tDate);
            editor.apply();
        }
        if (isRunning) {
            bytes_out_view.setText(render_bandwidth(myData.getLong("UP_DATA", 0)));
            bytes_in_view.setText(render_bandwidth(myData.getLong("DOWN_DATA", 0)));
        } else {
            myData.edit().putLong("UP_DATA", 0).apply();
            myData.edit().putLong("DOWN_DATA", 0).apply();
            bytes_out_view.setText("0 B");
            bytes_in_view.setText("0 B");
        }
        updateNetworkStatus();

    }

    private void updateNetworkStatus() {
        try {
            ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = manager != null ? manager.getActiveNetworkInfo() : null;
            if (info != null && info.isConnected()) {
                String name = getNetworkDisplayName(info);
                String ip = getDeviceIpAddress();
                updateTopBarNetwork(name, ip);
                if (networkStatus != null) {
                    networkStatus.setText(name);
                    networkStatus.setTextColor(Color.GREEN);
                }
            } else {
                updateTopBarNetwork("Offline", "0.0.0.0");
                if (networkStatus != null) {
                    networkStatus.setText("Offline");
                    networkStatus.setTextColor(Color.RED);
                }
            }
        } catch (Exception e) {
            updateTopBarNetwork("Unknown", "0.0.0.0");
            if (networkStatus != null) {
                networkStatus.setText("Unknown");
                networkStatus.setTextColor(Color.YELLOW);
            }
        }
    }

    private void updateTopBarNetwork(String networkName, String ip) {
        try {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle("Network: " + networkName + " | IP: " + ip);
            }
        } catch (Exception ignored) {}
    }

    private String getNetworkDisplayName(NetworkInfo info) {
        if (info == null) {
            return "Unknown";
        }
        if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            return getWifiName();
        }
        if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            return getCarrierName();
        }
        String typeName = info.getTypeName();
        return typeName != null && !typeName.trim().isEmpty() ? typeName : "Online";
    }

    private String getWifiName() {
        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager != null ? wifiManager.getConnectionInfo() : null;
            if (wifiInfo != null) {
                String ssid = wifiInfo.getSSID();
                if (ssid != null) {
                    ssid = ssid.replace("\"", "").trim();
                    if (!ssid.isEmpty() && !ssid.equalsIgnoreCase("<unknown ssid>")) {
                        return ssid;
                    }
                }
            }
        } catch (Exception ignored) {}
        return "Wi-Fi";
    }

    private String getCarrierName() {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            CharSequence carrier = telephonyManager != null ? telephonyManager.getNetworkOperatorName() : null;
            if (carrier != null) {
                String name = carrier.toString().trim();
                if (!name.isEmpty()) {
                    return name;
                }
            }
        } catch (Exception ignored) {}
        return "Mobile Data";
    }

    private String getDeviceIpAddress() {
        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager != null ? wifiManager.getConnectionInfo() : null;
            if (wifiInfo != null && wifiInfo.getIpAddress() != 0) {
                int ip = wifiInfo.getIpAddress();
                return String.format(Locale.US, "%d.%d.%d.%d",
                        ip & 0xff,
                        (ip >> 8) & 0xff,
                        (ip >> 16) & 0xff,
                        (ip >> 24) & 0xff);
            }
        } catch (Exception ignored) {}

        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }
                for (InetAddress address : Collections.list(networkInterface.getInetAddresses())) {
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {}
        return "0.0.0.0";
    }

    public void doTabs() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mLogAdapter = new LogsAdapter(layoutManager, this);
        RecyclerView logList = findViewById(R.id.recyclerLog);
        logList.setAdapter(mLogAdapter);
        logList.setLayoutManager(layoutManager);
        mLogAdapter.scrollToLastPosition();
        vp = findViewById(R.id.viewpager);
        TabLayout tabs = findViewById(R.id.tablayout);
        vp.setAdapter(new MyAdapter(Arrays.asList(tabTitle)));
        vp.setOffscreenPageLimit(2);
        tabs.setTabMode(TabLayout.MODE_FIXED);
        tabs.setTabGravity(TabLayout.GRAVITY_FILL);
        tabs.setupWithViewPager(vp);
        vp.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                updateClearLogsMenuVisibility();
            }
        });
        updateClearLogsMenuVisibility();

    }

    private void updateClearLogsMenuVisibility() {
        if (clearLogsMenuItem != null && vp != null) {
            clearLogsMenuItem.setVisible(vp.getCurrentItem() == 1);
        }
    }

    @SuppressLint("StringFormatMatches")
    private String getAppInfoString(Context c) {
        c.getPackageManager();
        String version = "";
        try {
            @SuppressLint("PackageManagerGetSignatures")

            PackageInfo packageinfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            version = String.format(packageinfo.versionName);

        } catch (PackageManager.NameNotFoundException ignore) {
        }
        return version;

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.miReleaseNotes:
                showReleaseNotesDialog();
                break;
            case R.id.miUpdateResources:
                updateConfig(false);
                break;
            case R.id.miHostChecker:
                startActivity(new Intent(this, HostCheckerActivity.class));
                break;
            case R.id.miSubdomainFinder:
                startActivity(new Intent(this, SubdomainFinderActivity.class));
                break;
            case R.id.radio:
                try {
                    try {
                        Intent in = new Intent(Intent.ACTION_MAIN);
                        in.setClassName("com.android.phone", "com.android.phone.settings.RadioInfo");
                        this.startActivity(in);
                    } catch (Exception e) {
                        Intent in1 = new Intent(Intent.ACTION_MAIN);
                        in1.setClassName("com.android.phone", "com.android.phone.settings.RadioInfo");
                        this.startActivity(in1);

                    }
                } catch (Exception f) {
                    SocksHttpApp.toast(getApplicationContext(), R.color.red, "Function not supported by your device");

                }
                break;
            case R.id.miAbout:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                break;
            case R.id.feedback:
                startActivity(new Intent("android.intent.action.VIEW",
                        Uri.parse(TELEGRAM_CHANNEL_URL)));
                break;
            case R.id.fb:
                startActivity(new Intent("android.intent.action.VIEW",
                        Uri.parse(TELEGRAM_CHANNEL_URL)));
                break;
            case R.id.settings:
                Intent hntent = new Intent(this, ConfigGeralActivity.class);
                startActivity(hntent);
                break;
            case R.id.miTethering:
                try {
                    startActivity(new Intent(this, TetheringActivity.class));
                } catch (Exception e) {
                    SocksHttpApp.toast(getApplicationContext(), R.color.red, "Tethering Activity Not Found!");
                }
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showReleaseNotesDialog() {
        String notes = config.geNote();
        if (notes == null || notes.trim().isEmpty()) {
            notes = "No release notes found in the current config.";
        }
        TextView message = new TextView(this);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        message.setPadding(padding, padding, padding, 0);
        message.setText(notes);
        message.setTextIsSelectable(true);
        message.setAutoLinkMask(android.text.util.Linkify.WEB_URLS);
        android.text.util.Linkify.addLinks(message, android.text.util.Linkify.WEB_URLS);
        message.setLinksClickable(true);
        message.setMovementMethod(LinkMovementMethod.getInstance());

        new AlertDialog.Builder(this)
                .setTitle("Release Notes")
                .setView(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showHostCheckerDialog() {
        EditText input = new EditText(this);
        input.setHint("example.com or https://example.com");
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);

        new AlertDialog.Builder(this)
                .setTitle("Host Checker")
                .setView(input)
                .setPositiveButton("CHECK", (dialog, which) -> checkHostResponse(input.getText().toString()))
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void checkHostResponse(String rawHost) {
        String host = rawHost == null ? "" : rawHost.trim();
        if (host.isEmpty()) {
            SocksHttpApp.toast(getApplicationContext(), R.color.red, "Enter a host first");
            return;
        }

        new Thread(() -> {
            String result;
            HttpURLConnection connection = null;
            try {
                String urlText = host.startsWith("http://") || host.startsWith("https://")
                        ? host : "https://" + host;
                URL url = new URL(urlText);
                long start = System.currentTimeMillis();
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                connection.setConnectTimeout(7000);
                connection.setReadTimeout(7000);
                connection.setInstanceFollowRedirects(false);
                int code = connection.getResponseCode();
                long time = System.currentTimeMillis() - start;
                String server = connection.getHeaderField("Server");
                String location = connection.getHeaderField("Location");

                StringBuilder builder = new StringBuilder();
                builder.append("Host: ").append(url.getHost()).append("\n");
                builder.append("Response: HTTP ").append(code).append("\n");
                builder.append("Time: ").append(time).append(" ms");
                if (server != null) {
                    builder.append("\nServer: ").append(server);
                }
                if (location != null) {
                    builder.append("\nRedirect: ").append(location);
                }
                result = builder.toString();
            } catch (Exception e) {
                result = "Host check failed:\n" + e.getMessage();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            String finalResult = result;
            runOnUiThread(() -> showSimpleResult("Host Checker", finalResult));
        }).start();
    }

    private void showSubdomainFinderDialog() {
        EditText input = new EditText(this);
        input.setHint("example.com");
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);

        new AlertDialog.Builder(this)
                .setTitle("Subdomain Finder")
                .setView(input)
                .setPositiveButton("FIND", (dialog, which) -> findSubdomains(input.getText().toString()))
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void findSubdomains(String rawDomain) {
        String domain = rawDomain == null ? "" : rawDomain.trim()
                .replace("https://", "")
                .replace("http://", "")
                .split("/")[0];
        if (domain.isEmpty()) {
            SocksHttpApp.toast(getApplicationContext(), R.color.red, "Enter a domain first");
            return;
        }

        new Thread(() -> {
            String[] prefixes = {"www", "m", "api", "app", "cdn", "mail", "portal", "vpn", "ssh", "server", "dev", "test"};
            Set<String> found = new LinkedHashSet<>();
            for (String prefix : prefixes) {
                String candidate = prefix + "." + domain;
                try {
                    InetAddress[] addresses = InetAddress.getAllByName(candidate);
                    if (addresses.length > 0) {
                        found.add(candidate + " -> " + addresses[0].getHostAddress());
                    }
                } catch (Exception ignored) {
                }
            }

            StringBuilder builder = new StringBuilder();
            if (found.isEmpty()) {
                builder.append("No common subdomains resolved for ").append(domain);
            } else {
                builder.append("Found subdomains:\n");
                for (String item : found) {
                    builder.append("\n").append(item);
                }
            }
            runOnUiThread(() -> showSimpleResult("Subdomain Finder", builder.toString()));
        }).start();
    }

    private void showSimpleResult(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }


    public class MyAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            // TODO: Implement this method
            return 2;
        }

        @Override
        public boolean isViewFromObject(@NonNull View p1, @NonNull Object p2) {
            // TODO: Implement this method
            return p1 == p2;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            int[] ids = new int[]{R.id.tab1, R.id.tab2};
            int id;
            id = ids[position];
            // TODO: Implement this method
            return findViewById(id);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // TODO: Implement this method
            return titles.get(position);
        }

        private final List<String> titles;

        public MyAdapter(List<String> str) {
            titles = str;
        }
    }


    private void updateConfig(final boolean isOnCreate) {
        new ConfigUpdate(this, result -> {
            try {
                if (!result.contains("Error on getting data")) {
                    SecurityGuard.enforceConfigAccess(this);
                    String json_data = AESCrypt.decrypt(ConfigUtil.getPassword(), result);
                    if (isNewVersion(json_data)) {
                        showConfigUpdateOnHome(json_data);
                        showConfigUpdateNotification(json_data);
                        newUpdateDialog(result);
                    } else {
                        hideConfigUpdateOnHome();
                        if (!isOnCreate) {
                            noUpdateDialog();
                        }
                    }
                } else if (result.contains("Error on getting data") && !isOnCreate) {
                    errorUpdateDialog(result);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (!isOnCreate) {
                    errorUpdateDialog("GitHub config invalid: " + e.getMessage());
                }
            }
        }).start(isOnCreate);
    }


    private boolean isNewVersion(String result) {
        try {
            String current = config.getVersion();
            String update = new JSONObject(result).getString("Version");
            return config.versionCompare(update, current);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showConfigUpdateOnHome(String json) {
        try {
            JSONObject object = new JSONObject(json);
            String version = object.optString("Version", "new");
            String message = "Server update available: v" + version;
            TextView bannerText = findViewById(R.id.configUpdateBannerText);
            View banner = findViewById(R.id.configUpdateBanner);
            if (bannerText != null) {
                bannerText.setText(message);
            }
            if (banner != null) {
                banner.setVisibility(View.VISIBLE);
                banner.setOnClickListener(v -> updateConfig(false));
            }
        } catch (Exception ignored) {}
    }

    private void hideConfigUpdateOnHome() {
        View banner = findViewById(R.id.configUpdateBanner);
        if (banner != null) {
            banner.setVisibility(View.GONE);
        }
    }

    private void showConfigUpdateNotification(String json) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            JSONObject object = new JSONObject(json);
            String version = object.optString("Version", "new");
            Intent intent = new Intent(this, SocksHttpMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager != null) {
                NotificationChannel channel = new NotificationChannel(
                        CONFIG_UPDATE_CHANNEL_ID,
                        "Config Updates",
                        NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("Server configuration update alerts");
                manager.createNotificationChannel(channel);
            }
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CONFIG_UPDATE_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_cloud_download)
                    .setContentTitle("Server update available")
                    .setContentText("Tap Update to apply config v" + version)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            if (manager != null) {
                manager.notify(CONFIG_UPDATE_NOTIFICATION_ID, builder.build());
            }
        } catch (Exception ignored) {}
    }

    private void clearConfigUpdateNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(CONFIG_UPDATE_NOTIFICATION_ID);
        }
    }

    private void requestUpdateNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST);
        }
    }


    private void newUpdateDialog(final String result) throws JSONException, GeneralSecurityException {
        SecurityGuard.enforceConfigAccess(this);
        String json = AESCrypt.decrypt(ConfigUtil.getPassword(), result);
        String releasenotes = new JSONObject(json).getString("ReleaseNotes");
        View inflate = LayoutInflater.from(this).inflate(R.layout.notif, null);
        AlertDialog.Builder builer = new AlertDialog.Builder(this);
        builer.setView(inflate);
        TextView title = inflate.findViewById(R.id.notiftext1);
        TextView ms = inflate.findViewById(R.id.confimsg);
        TextView ok = inflate.findViewById(R.id.appButton1);
        TextView cancel = inflate.findViewById(R.id.appButton2);
        title.setText("Notification");
        ms.setText(releasenotes);
        ok.setText("Apply");
        cancel.setText("Dismiss");
        final AlertDialog alert = builer.create();
        alert.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(alert.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alert.getWindow().setGravity(Gravity.CENTER);
        alert.show();
        ok.setOnClickListener(p1 -> {
            try {
                alert.dismiss();
                File file = new File(getFilesDir(), "Config.json");
                OutputStream out = new FileOutputStream(file);
                out.write(result.getBytes());
                out.flush();
                out.close();
                saveConfigSourceName("Online Config");
                SocksHttpApp.toast(SocksHttpMainActivity.this, R.color.green, "Config Update์");
                configVersion.setText(config.getVersion());
                hideConfigUpdateOnHome();
                clearConfigUpdateNotification();
                updateView();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        cancel.setOnClickListener(p1 -> alert.dismiss());
        alert.show();

    }

    private void noUpdateDialog() {
        View inflate = LayoutInflater.from(this).inflate(R.layout.notif, null);
        AlertDialog.Builder builer = new AlertDialog.Builder(this);
        builer.setView(inflate);
        TextView title = inflate.findViewById(R.id.notiftext1);
        TextView ms = inflate.findViewById(R.id.confimsg);
        TextView ok = inflate.findViewById(R.id.appButton1);
        TextView cancel = inflate.findViewById(R.id.appButton2);
        title.setText("No Update Available");
        ms.setText("Please try again soon.");
        ok.setText("Ok,Close");
        cancel.setText(".");
        cancel.setVisibility(View.GONE);
        final AlertDialog alert = builer.create();
        alert.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(alert.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alert.getWindow().setGravity(Gravity.CENTER);
        ok.setOnClickListener(p1 -> alert.dismiss());

        cancel.setOnClickListener(p1 -> alert.dismiss());
        alert.show();
    }

    private void errorUpdateDialog(String error) {
        View inflate = LayoutInflater.from(this).inflate(R.layout.notif, null);
        AlertDialog.Builder builer = new AlertDialog.Builder(this);
        builer.setView(inflate);
        TextView title = inflate.findViewById(R.id.notiftext1);
        TextView ms = inflate.findViewById(R.id.confimsg);
        TextView ok = inflate.findViewById(R.id.appButton1);
        TextView cancel = inflate.findViewById(R.id.appButton2);
        title.setText("Error");
        ms.setText("Update Error: " + error);
        ok.setText("Ok,Close");
        cancel.setText(".");
        cancel.setVisibility(View.GONE);
        final AlertDialog alert = builer.create();
        alert.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(alert.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alert.getWindow().setGravity(Gravity.CENTER);
        ok.setOnClickListener(p1 -> alert.dismiss());

        cancel.setOnClickListener(p1 -> alert.dismiss());
        alert.show();
    }


    public void offlineUpdate() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE) {
            if (resultCode == RESULT_OK) {
                try {
                    Uri uri = data.getData();
                    String intentData = importer(uri);
                    File file = new File(getFilesDir(), "Config.json");
                    OutputStream out = new FileOutputStream(file);
                    out.write(intentData.getBytes());
                    out.flush();
                    out.close();
                    saveConfigSourceName(getDisplayName(uri));
                    SocksHttpApp.toast(SocksHttpMainActivity.this, R.color.green, "Config Update");
                    configVersion.setText(config.getVersion());
                    updateView();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String importer(Uri uri) {
        BufferedReader reader;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    private void saveConfigSourceName(String sourceName) {
        if (prefs == null) {
            return;
        }
        String name = firstNonEmpty(sourceName, "Config.json");
        prefs.edit().putString(PREF_CONFIG_SOURCE_NAME, name).apply();
    }

    private String getConfigSourceName() {
        String saved = prefs != null ? firstNonEmpty(prefs.getString(PREF_CONFIG_SOURCE_NAME, "")) : null;
        if (saved != null) {
            return saved;
        }
        File file = new File(getFilesDir(), "Config.json");
        return file.exists() ? "Config.json" : "Built-in Config";
    }

    private String getDisplayName(Uri uri) {
        if (uri == null) {
            return "Config.json";
        }
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    String name = firstNonEmpty(cursor.getString(index));
                    if (name != null) {
                        return name;
                    }
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        String path = uri.getLastPathSegment();
        if (path == null || path.trim().isEmpty()) {
            return "Config.json";
        }
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    /**
     * Tunnel SSH
     */


    public void startOrStopTunnel(Activity activity) {
        if (SkStatus.isTunnelActive()) {
            tunnelConnected = false;
            serverLocationLogged = false;
            TunnelManagerHelper.stopSocksHttp(activity);

        } else {
            NetFreeMXGen.getInstance().loadServer(config.getServersArray());
            vp.setCurrentItem(1);
            Settings config = new Settings(activity);

            Intent intent = new Intent(activity, LaunchVpn.class);
            intent.setAction(Intent.ACTION_MAIN);

            if (config.getHideLog()) {
                intent.putExtra(LaunchVpn.EXTRA_HIDELOG, true);
            }

            activity.startActivity(intent);
        }
    }


    public void setStarterButton(Button starterButton) {
        String state = SkStatus.getLastState();
        boolean isRunning = SkStatus.isTunnelActive();
        if (starterButton != null) {
            int resId;

            if (SkStatus.SSH_INICIANDO.equals(state)) {
                resId = R.string.stop;
                starterButton.setEnabled(false);
            } else if (SkStatus.SSH_PARANDO.equals(state)) {
                resId = R.string.state_stopping;
                starterButton.setEnabled(false);
            } else {
                resId = isRunning ? R.string.stop : R.string.start;
                starterButton.setEnabled(true);
            }

            starterButton.setText(resId);
        }
    }


    @Override
    public void onClick(View p1) {

        if (p1.getId() == R.id.activity_starterButtonMain) {
            startOrStopTunnel(this);
        }
    }


    @SuppressLint("DefaultLocale")
    private String render_bandwidth(double bw) {
        String postfix;
        float div;
        Object[] objArr;
        float bwf = (float) bw;
        if (bwf >= 1.0E12f) {
            postfix = "TB";
            div = 1.0995116E12f;
        } else if (bwf >= 1.0E9f) {
            postfix = "GB";
            div = 1.0737418E9f;
        } else if (bwf >= 1000000.0f) {
            postfix = "MB";
            div = 1048576.0f;
        } else if (bwf >= 1000.0f) {
            postfix = "KB";
            div = 1024.0f;
        } else {
            objArr = new Object[S_BIND_CALLED];
            objArr[0] = bwf;
            return String.format("%.0f", objArr);
        }
        objArr = new Object[S_ONSTART_CALLED];
        objArr[0] = bwf / div;
        objArr[S_BIND_CALLED] = postfix;
        return String.format("%.2f %s", objArr);
    }

    @Override
    public void updateState(final String state, String msg, int localizedResId, final ConnectionStatus level, Intent intent) {
        mHandler.post(() -> {
            if (SkStatus.isTunnelActive()) {
                setStarterButton(starterButton);
                if (level.equals(ConnectionStatus.LEVEL_CONNECTED)) {
                    SocksHttpApp.toast(getApplicationContext(), R.color.green, "Connected!");
                    textStatus.setTextColor(Color.GREEN);
                    textStatus.setText("Connect");
                    tunnelConnected = true;
                    logConnectedServerLocation();
                }

                if (level.equals(ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET)) {
                    textStatus.setTextColor(Color.CYAN);
                    textStatus.setText("Connecting");
                    serversL.setEnabled(false);
                }
                if (level.equals(ConnectionStatus.LEVEL_AUTH_FAILED)) {
                    textStatus.setTextColor(Color.RED);
                    textStatus.setText("Auth Failed");
                }
                if (level.equals(ConnectionStatus.UNKNOWN_LEVEL)) {
                    textStatus.setTextColor(Color.RED);
                    textStatus.setText("Disconnect");
                    starterButton.setText(R.string.start);
                    starterButton.setEnabled(true);
                    serversL.setEnabled(true);
                    tunnelConnected = false;
                    serverLocationLogged = false;
                    connectedLiveLocationInfo = null;
                    updateView();
                }
            }
        });

    }

    public static void updateMainViews(Context context) {
        Intent updateView = new Intent(UPDATE_VIEWS);
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(updateView);
    }


    /**
     * Recebe locais Broadcast
     */

    private final BroadcastReceiver mActivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null)
                return;

            if (action.equals(UPDATE_VIEWS)) {
                updateView();
            }

        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        clearLogsMenuItem = menu.findItem(R.id.miClearLogs);
        updateClearLogsMenuVisibility();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        // Menu Itens
        switch (item.getItemId()) {
            case R.id.miClearLogs:
                if (mLogAdapter != null) {
                    mLogAdapter.clearLog();
                    SocksHttpApp.toast(getApplicationContext(), R.color.green, "Logs cleared");
                }
                return true;
            case R.id.miUpdate:
                updateConfig(false);
                break;
            case R.id.offline:
                offlineUpdate();
                break;
            case R.id.miOption:
                Intent Intent = new Intent(this, ConfigGeralActivity.class);
                startActivity(Intent);
                break;
            case R.id.miTheme:
                toggleDarkMode();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void applySavedTheme() {
        boolean darkMode = getSharedPreferences("theme", MODE_PRIVATE).getBoolean(PREF_DARK_MODE, false);
        AppCompatDelegate.setDefaultNightMode(darkMode
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void toggleDarkMode() {
        SharedPreferences themePrefs = getSharedPreferences("theme", MODE_PRIVATE);
        boolean darkMode = !themePrefs.getBoolean(PREF_DARK_MODE, false);
        themePrefs.edit().putBoolean(PREF_DARK_MODE, darkMode).apply();
        AppCompatDelegate.setDefaultNightMode(darkMode
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
        recreate();
    }

    @Override
    public void onResume() {
        super.onResume();

        //   addTime();
        SkStatus.addStateListener(this);
        setupCustomDnsSwitch();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //    doSaveData();
        SkStatus.removeStateListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        httpPingRunning = false;
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mActivityReceiver);
    }

    private void updateView() {
        try {
            JSONObject object = getSelectedServerObject();
            if (object == null) {
                return;
            }
            serverName.setText(getString(R.string.app_name) + " Server");
            serverInfo.setText(object.getString("Name") + " | " + object.getString("Info"));
            if (!tunnelConnected || connectedLiveLocationInfo == null) {
                updateServerLocationNotes(object);
            }
            String f = object.getString("Flag") + ".png";

            InputStream inputStream = getAssets().open("flags/" + f);
            serverImage.setImageDrawable(Drawable.createFromStream(inputStream, f));
            inputStream.close();
        } catch (Exception ignored) {
            serverImage.setImageResource(R.drawable.icon);
        }
    }

    private JSONObject getSelectedServerObject() {
        try {
            int p = prefs.getInt("LastSelectedServer", 0);
            if (config.getServersArray() == null || config.getServersArray().length() == 0) {
                return null;
            }
            if (p < 0 || p >= config.getServersArray().length()) {
                p = 0;
            }
            return config.getServersArray().getJSONObject(p);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void updateServerLocationNotes(JSONObject object) {
        if (serverLocationNotes == null || object == null) {
            return;
        }
        String name = object.optString("Name", "Selected Server");
        String country = firstNonEmpty(
                object.optString("Country"),
                object.optString("Pais"),
                object.optString("Flag"));
        String location = firstNonEmpty(
                object.optString("Location"),
                object.optString("City"),
                object.optString("Info"));
        StringBuilder builder = new StringBuilder();
        builder.append("Server: ").append(name);
        if (country != null) {
            builder.append("\nCountry: ").append(country.toUpperCase(Locale.US));
        }
        if (location != null) {
            builder.append("\nLocation: ").append(location);
        }
        serverLocationNotes.setText(builder.toString() + "\n");
    }

    private void logConnectedServerLocation() {
        if (serverLocationLogged) {
            return;
        }
        serverLocationLogged = true;
        new Thread(() -> {
            String liveInfo = getLiveVpnLocationInfo();
            if (liveInfo != null) {
                connectedLiveLocationInfo = liveInfo.replace(" | ", "\n") + "\n";
                runOnUiThread(() -> {
                    if (serverLocationNotes != null) {
                        serverLocationNotes.setText(connectedLiveLocationInfo);
                    }
                });
                return;
            }

            runOnUiThread(() -> {
                if (serverLocationNotes != null) {
                    if (connectedLiveLocationInfo != null) {
                        serverLocationNotes.setText(connectedLiveLocationInfo);
                    } else {
                        serverLocationNotes.setText("Real IP: checking...\nLocation: checking...\n");
                    }
                }
            });
        }).start();
    }

    private String getLiveVpnLocationInfo() {
        String publicVpnInfo = getGeoInfoFromUrl("https://ipapi.co/json/");
        if (publicVpnInfo == null) {
            publicVpnInfo = getGeoInfoFromUrl("http://ip-api.com/json/?fields=status,message,query,country,regionName,city,isp,org,as,timezone");
        }
        if (publicVpnInfo != null) {
            return publicVpnInfo;
        }

        try {
            PingTarget target = getSelectedServerPingTarget();
            if (target != null) {
                String ip = resolveIpv4(target.host);
                String selectedVpsInfo = getGeoInfoFromUrl("https://ipapi.co/" + ip + "/json/");
                if (selectedVpsInfo == null) {
                    selectedVpsInfo = getGeoInfoFromUrl("http://ip-api.com/json/" + ip + "?fields=status,message,query,country,regionName,city,isp,org,as,timezone");
                }
                if (selectedVpsInfo != null) {
                    return selectedVpsInfo;
                }
            }
        } catch (Exception ignored) {}

        return null;
    }

    private String getGeoInfoFromUrl(String value) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(value);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "DARKxGHOSTTunnel/1.0");
            if (connection.getResponseCode() < HttpURLConnection.HTTP_OK
                    || connection.getResponseCode() >= HttpURLConnection.HTTP_MULT_CHOICE) {
                return null;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            JSONObject object = new JSONObject(response.toString());
            if ("fail".equalsIgnoreCase(object.optString("status"))) {
                return null;
            }
            String ip = firstNonEmpty(
                    object.optString("ip"),
                    object.optString("query"));
            if (ip != null && !isIpv4(ip)) {
                ip = null;
            }
            String country = firstNonEmpty(
                    object.optString("country_name"),
                    object.optString("country"));
            String city = firstNonEmpty(
                    object.optString("city"),
                    object.optString("regionName"),
                    object.optString("region"));
            String org = firstNonEmpty(
                    object.optString("isp"),
                    object.optString("org"),
                    object.optString("asn"),
                    object.optString("as"));
            StringBuilder builder = new StringBuilder();
            if (ip != null) {
                builder.append("IP: ").append(ip);
            }
            if (country != null) {
                appendGeoPart(builder, "Country", country);
            }
            if (city != null) {
                appendGeoPart(builder, "Location", city);
            }
            if (org != null) {
                appendGeoPart(builder, "Network", org);
            }
            return builder.toString();
        } catch (Exception ignored) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void appendGeoPart(StringBuilder builder, String label, String value) {
        if (builder.length() > 0) {
            builder.append(" | ");
        }
        builder.append(label).append(": ").append(value);
    }

    private String resolveIpv4(String host) throws IOException {
        InetAddress[] addresses = InetAddress.getAllByName(host);
        for (InetAddress address : addresses) {
            if (address instanceof Inet4Address) {
                return address.getHostAddress();
            }
        }
        throw new IOException("IPv4 not found");
    }

    private boolean isIpv4(String value) {
        if (value == null) {
            return false;
        }
        String[] parts = value.trim().split("\\.");
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            try {
                int number = Integer.parseInt(part);
                if (number < 0 || number > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private String cleanHost(String host) {
        host = host.replace("http://", "").replace("https://", "");
        int slash = host.indexOf('/');
        if (slash > -1) {
            host = host.substring(0, slash);
        }
        int at = host.lastIndexOf('@');
        if (at > -1) {
            host = host.substring(at + 1);
        }
        int colon = host.indexOf(':');
        if (colon > -1) {
            host = host.substring(0, colon);
        }
        return host;
    }

    private String getV2rayHost(JSONObject object) {
        try {
            String v2ray = firstNonEmpty(object.optString("v2ray"), object.optString("V2RAY"));
            if (v2ray == null) {
                return null;
            }
            JSONObject root = new JSONObject(v2ray);
            JSONObject outbound = root.getJSONArray("outbounds").getJSONObject(0);
            JSONObject settings = outbound.optJSONObject("settings");
            if (settings == null) {
                return null;
            }
            if (settings.has("vnext")) {
                return settings.getJSONArray("vnext").getJSONObject(0).optString("address");
            }
            if (settings.has("servers")) {
                return settings.getJSONArray("servers").getJSONObject(0).optString("address");
            }
        } catch (Exception ignored) {}
        return null;
    }

    private int getSelectedServerPort(JSONObject object, String host) {
        int embeddedPort = getEmbeddedPort(host);
        if (embeddedPort > 0) return embeddedPort;
        String port = firstNonEmpty(
                object.optString("sshPort"),
                object.optString("ServerPort"),
                object.optString("Port"),
                object.optString("SSHPort"),
                object.optString("DropbearPort"),
                object.optString("ProxyPort"));
        return getPortFromString(port, 22);
    }

    private int getPortFromString(String port, int fallback) {
        if (port != null) {
            try {
                int parsed = Integer.parseInt(port.trim());
                if (parsed > 0 && parsed <= 65535) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {}
        }
        return fallback > 0 ? fallback : 22;
    }

    private int getEmbeddedPort(String host) {
        if (host == null) {
            return -1;
        }
        host = host.replace("http://", "").replace("https://", "");
        int slash = host.indexOf('/');
        if (slash > -1) {
            host = host.substring(0, slash);
        }
        int colon = host.lastIndexOf(':');
        if (colon > -1 && colon < host.length() - 1) {
            try {
                int parsed = Integer.parseInt(host.substring(colon + 1));
                if (parsed > 0 && parsed <= 65535) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {}
        }
        return -1;
    }

    private static class PingTarget {
        final String host;
        final int port;

        PingTarget(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }

    private String firstNonEmpty(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null) {
                value = value.trim();
                if (!value.isEmpty() && !"null".equalsIgnoreCase(value)) {
                    return value;
                }
            }
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AlertDialog dialog = new AlertDialog.Builder(this).
                create();
        dialog.setTitle(getString(R.string.attention));
        dialog.setMessage(getString(R.string.alert_exit));

        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.
                        string.exit),
                (dialog1, which) -> Utils.exitAll(SocksHttpMainActivity.this)
        );

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.
                        string.minimize),
                (dialog12, which) -> {
                    // minimiza app
                    Intent startMain = new Intent(Intent.ACTION_MAIN);
                    startMain.addCategory(Intent.CATEGORY_HOME);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(startMain);
                }
        );

        dialog.show();
    }
}
