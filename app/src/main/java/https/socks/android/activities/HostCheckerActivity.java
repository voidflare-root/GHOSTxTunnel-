package https.socks.android.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.nphdevs.bluespace.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;

public class HostCheckerActivity extends AppCompatActivity {

    private EditText inputHost;
    private Button btnCheck;
    private TextView txtResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_checker);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Host Checker");
        }

        inputHost = findViewById(R.id.inputHost);
        btnCheck = findViewById(R.id.btnCheck);
        txtResult = findViewById(R.id.txtResult);

        btnCheck.setOnClickListener(v -> runCheck());
    }

    private void runCheck() {
        String host = cleanHost(inputHost.getText().toString());
        if (host.isEmpty()) {
            txtResult.setText("Enter a host first.");
            return;
        }

        hideKeyboard();
        btnCheck.setEnabled(false);
        txtResult.setText("$ host-check " + host + "\n\nRunning checks...");

        new Thread(() -> {
            String report = buildReport(host);
            runOnUiThread(() -> {
                txtResult.setText(report);
                btnCheck.setEnabled(true);
            });
        }).start();
    }

    private String buildReport(String host) {
        StringBuilder out = new StringBuilder();
        out.append("$ host-check ").append(host).append("\n\n");

        out.append("[DNS]\n");
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress address : addresses) {
                out.append("  ").append(address.getHostAddress()).append("\n");
            }
        } catch (Exception e) {
            out.append("  failed: ").append(e.getMessage()).append("\n");
        }

        out.append("\n[TCP]\n");
        int[] ports = {80, 443, 22, 8080, 8443};
        for (int port : ports) {
            out.append("  ").append(host).append(":").append(port).append("  ");
            long start = System.currentTimeMillis();
            try (Socket socket = new Socket()) {
                socket.connect(new java.net.InetSocketAddress(host, port), 5000);
                out.append("open (").append(System.currentTimeMillis() - start).append(" ms)\n");
            } catch (Exception e) {
                out.append("closed/filtered\n");
            }
        }

        out.append("\n[HTTP :80]\n");
        out.append(readHttpFirstLine(host, 80, false));

        out.append("\n[HTTPS :443]\n");
        out.append(readHttpsStatus(host));

        return out.toString();
    }

    private String readHttpFirstLine(String host, int port, boolean tls) {
        StringBuilder out = new StringBuilder();
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), 6000);
            socket.setSoTimeout(6000);
            OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
            writer.write("HEAD / HTTP/1.1\r\nHost: " + host + "\r\nUser-Agent: DARKxGHOSTTunnel/1.0\r\nConnection: close\r\n\r\n");
            writer.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = reader.readLine();
            if (line == null || line.trim().isEmpty()) {
                out.append("  no HTTP response\n");
            } else {
                out.append("  ").append(line).append("\n");
            }
        } catch (Exception e) {
            out.append("  failed: ").append(e.getMessage()).append("\n");
        }
        return out.toString();
    }

    private String readHttpsStatus(String host) {
        HttpURLConnection connection = null;
        StringBuilder out = new StringBuilder();
        try {
            URL url = new URL("https://" + host + "/");
            long start = System.currentTimeMillis();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(7000);
            connection.setReadTimeout(7000);
            connection.setInstanceFollowRedirects(false);
            int code = connection.getResponseCode();
            out.append("  HTTP ").append(code).append(" (")
                    .append(System.currentTimeMillis() - start).append(" ms)\n");
            String server = connection.getHeaderField("Server");
            String location = connection.getHeaderField("Location");
            if (server != null) {
                out.append("  Server: ").append(server).append("\n");
            }
            if (location != null) {
                out.append("  Redirect: ").append(location).append("\n");
            }
        } catch (Exception e) {
            out.append("  failed: ").append(e.getMessage()).append("\n");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return out.toString();
    }

    private String cleanHost(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .replace("https://", "")
                .replace("http://", "")
                .split("/")[0]
                .split(":")[0]
                .trim();
    }

    private void hideKeyboard() {
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(inputHost.getWindowToken(), 0);
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
