package https.socks.android.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.nphdevs.bluespace.R;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class SubdomainFinderActivity extends AppCompatActivity {

    private static final String[] PREFIXES = {
            "www", "m", "api", "app", "cdn", "mail", "smtp", "pop", "imap", "webmail",
            "portal", "panel", "admin", "dashboard", "vpn", "ssh", "remote", "proxy",
            "server", "ns1", "ns2", "dev", "test", "stage", "staging", "beta", "blog",
            "shop", "store", "support", "help", "docs", "status", "cpanel", "ftp"
    };

    private EditText inputDomain;
    private Button btnFind, btnCopyDomains;
    private TextView txtResult;
    private final List<String> foundDomains = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subdomain_finder);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Subdomain Finder");
        }

        inputDomain = findViewById(R.id.inputDomain);
        btnFind = findViewById(R.id.btnFind);
        btnCopyDomains = findViewById(R.id.btnCopyDomains);
        txtResult = findViewById(R.id.txtResult);

        btnFind.setOnClickListener(v -> findSubdomains());
        btnCopyDomains.setOnClickListener(v -> copyDomains());
    }

    private void findSubdomains() {
        String domain = cleanDomain(inputDomain.getText().toString());
        if (domain.isEmpty()) {
            txtResult.setText("Enter a domain first.");
            return;
        }

        hideKeyboard();
        btnFind.setEnabled(false);
        foundDomains.clear();
        txtResult.setText("$ subfinder " + domain + "\n\nResolving common subdomains...");

        new Thread(() -> {
            StringBuilder out = new StringBuilder();
            out.append("$ subfinder ").append(domain).append("\n\n");

            for (String prefix : PREFIXES) {
                String candidate = prefix + "." + domain;
                try {
                    InetAddress[] addresses = InetAddress.getAllByName(candidate);
                    if (addresses.length > 0) {
                        foundDomains.add(candidate);
                        out.append(candidate).append(" -> ")
                                .append(addresses[0].getHostAddress()).append("\n");
                    }
                } catch (Exception ignored) {
                }
            }

            if (foundDomains.isEmpty()) {
                out.append("No common subdomains resolved.");
            } else {
                out.append("\nTotal: ").append(foundDomains.size()).append(" domain(s)");
            }

            runOnUiThread(() -> {
                txtResult.setText(out.toString());
                btnFind.setEnabled(true);
            });
        }).start();
    }

    private void copyDomains() {
        if (foundDomains.isEmpty()) {
            Toast.makeText(this, "No domains to copy", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder domains = new StringBuilder();
        for (String domain : foundDomains) {
            domains.append(domain).append("\n");
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("subdomains", domains.toString().trim()));
            Toast.makeText(this, "All domains copied", Toast.LENGTH_SHORT).show();
        }
    }

    private String cleanDomain(String value) {
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
            manager.hideSoftInputFromWindow(inputDomain.getWindowToken(), 0);
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
