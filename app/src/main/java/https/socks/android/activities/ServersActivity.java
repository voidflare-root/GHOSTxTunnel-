package https.socks.android.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.nphdevs.bluespace.R;
import com.slipkprojects.ultrasshservice.config.Settings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import https.socks.android.SocksHttpMainActivity;
import https.socks.android.adapter.ServersAdapter;
import https.socks.android.model.ServerModel;
import https.socks.android.util.ConfigUtil;

public class ServersActivity extends AppCompatActivity {
    private List<ServerModel> servers;
    private ServersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servers);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        servers = new ArrayList<>();
        adapter = new ServersAdapter(servers);
        adapter.setOnClick(p -> {
            ((SharedPreferences) new Settings(this).getPrefsPrivate()).edit().putInt("LastSelectedServer", p).apply();
            SocksHttpMainActivity.updateMainViews(this);
            finish();
        });

        RecyclerView recyclerView = findViewById(R.id.servers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        loadServers();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadServers() {
        ConfigUtil configUtil = new ConfigUtil(this);
        new Thread(() -> {
            try {
                JSONArray array = configUtil.getServersArray();

                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    ServerModel model = new ServerModel();
                    model.setServerFlag(object.getString("Flag"));
                    model.setServerName(object.getString("Name"));
                    model.setServerInfo(object.getString("Info"));

                    servers.add(model);
                }

                runOnUiThread(() -> adapter.notifyDataSetChanged());
            } catch (Exception ignore) {

            }
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) onBackPressed();
        return true;
    }
}