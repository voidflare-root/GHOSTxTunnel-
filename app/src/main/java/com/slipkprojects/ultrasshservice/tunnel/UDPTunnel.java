package com.slipkprojects.ultrasshservice.tunnel;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import com.slipkprojects.ultrasshservice.config.Settings;
import com.slipkprojects.ultrasshservice.logger.SkStatus;

import java.io.File;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;

public class UDPTunnel {
    private static String udp_server;
    private Context context;
    private UDPListener udpListener;
    private Thread udpService;
    private Process udpProcess;
    private File fileca;
    private File fileConf;
    private String dir;

    //Udp Variables
    //private String udp_server;
    private String udp_auth;
    private String udp_obfs;
    private String udp_port;
    private String udp_down;
    private String udp_up;
    private String udp_address;
    private String udp_listen = "127.0.0.1:1080";
    private String ca_path;
    private String retry = "3";
    private UDPStreamGobbler stream;
    private UDPStreamGobbler error;
    private String estado = "";
    private Settings mConfig;
    private boolean udpActivo = false;
    private boolean reiniciarUdp = false;
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;
    private boolean isConnected;
    private Handler handler;
    private Runnable runnable;

    public UDPTunnel(Context context) {
        this.context = context;
        mConfig = new Settings(context);

        udp_server = mConfig.getPrivString(Settings.UDP_SERVER);
        udp_port = mConfig.getPrivString(Settings.UDP_PORT);
        udp_auth = mConfig.getPrivString(Settings.UDP_AUTH);
        udp_down = mConfig.getPrivString(Settings.UDP_DOWN);
        udp_obfs = mConfig.getPrivString(Settings.UDP_OBFS);
        udp_up = mConfig.getPrivString(Settings.UDP_UP);
        dir = context.getFilesDir().getPath();
        udpListener = TunnelManagerThread.getUDPListener();
        setUdpProcess();
    }


    UDPStreamGobbler.OnResultListener resultListener = new UDPStreamGobbler.OnResultListener() {
        @Override
        public void onResult(String result) {
            if (result.contains("no recent network activity")) {
                if (!estado.equals("conexionperdida")) {
                    SkStatus.logInfo("<font color='red'><strong>UDP connection Lost</strong></font>");
                }
                estado = "conexionperdida";
                udpListener.onConnectionLost();
                return;
            } else if (result.contains("auth error")) {
                if (!estado.equals("autenticacionfallida")) {
                    SkStatus.logInfo(result);
                    SkStatus.logInfo("<font color='red'><strong>Authentication failed, invalid password/expired/may logged-in on another device</strong></font>");
                }
                estado = "autenticacionfallida";
                udpListener.onAuthFailed();
                return;
            } else if (result.toLowerCase().contains("connected")) {
                if (!estado.equals("conectado")) {
                    //SkStatus.logInfo(result);
                    udpListener.onConnected();
                }
                estado = "conectado";

            }



        }
    };

    private void setUdpProcess() {
        udpService = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    fileca = new File(dir, "zi.ca.crt");
                    ca_path = fileca.getAbsolutePath();
                    InetAddress inetAddress = InetAddress.getByName(udp_server);
                    udp_address = inetAddress.getHostAddress() + ":" + udp_port;
                    String format = String.format("{\n  \"server\": \"%s\",\n  \"obfs\": \"%s\",\n  \"auth_str\": \"%s\",\n  \"up_mbps\": %s,\n  \"down_mbps\": %s,\n  \"retry\": %s,\n  \"retry_interval\": 1,\n  \"socks5\": {\n    \"listen\": \"%s\"\n  },\n  \"insecure\": true,\n  \"ca\": \"%s\",\n  \"recv_window_conn\": %s,\n  \"recv_window\": %s\n}", new Object[]{udp_address, udp_obfs, udp_auth, udp_up, udp_down, retry, udp_listen, ca_path, "196608", null});
                    fileConf = new File(dir, "udp.json");
                    if (!printToFile(fileca, "-----BEGIN CERTIFICATE-----\nMIIDizCCAnOgAwIBAgIUGxLl5Ou4dR1h3c9lUcaM5bp4ZBswDQYJKoZIhvcNAQEL\nBQAwVTELMAkGA1UEBhMCQ04xCzAJBgNVBAgMAkdEMQswCQYDVQQHDAJTWjEUMBIG\nA1UECgwLWklWUE4sIEluYy4xFjAUBgNVBAMMDVpJVlBOIFJvb3QgQ0EwHhcNMjMw\nMjExMDkwMjM1WhcNMzMwMjA4MDkwMjM1WjBVMQswCQYDVQQGEwJDTjELMAkGA1UE\nCAwCR0QxCzAJBgNVBAcMAlNaMRQwEgYDVQQKDAtaSVZQTiwgSW5jLjEWMBQGA1UE\nAwwNWklWUE4gUm9vdCBDQTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEB\nAMQsHTq2UD4WDOvNUFGQuKd0PEitgQzSh12qH9aJ5jnCtbWjqVNDRQSW0ietg4Po\nqOfKLOBvGOJcGkrYlAAynnwsufdkZd2Jj2+FAXloAbMBK5cjqRANfPJ7ns3S5zL2\nt2+Xv/O6H58NL5QksyIHb2Vcosfelwuvj5Lq+MvyqGZikce5IaykgjjV0OsrBnsC\neK4yAeoxsqVixGwmcJDLGOIJDGYcDdaElqJqFCyOjOhLLDymx9JbeOb3DpiRNFNN\nlwXi2rfvpnmpGNwNt9sclWAQTL3cfV4GsCovT02r1qxcAqqRE4U1nqMRqk0KfyQn\nUebOat/0jNJI9YxJByuVBK0CAwEAAaNTMFEwHQYDVR0OBBYEFGk91bjhFZfcKkpm\n5SxVkqnSGhXBMB8GA1UdIwQYMBaAFGk91bjhFZfcKkpm5SxVkqnSGhXBMA8GA1Ud\nEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAEr4aeE0ib5/7neEcRWCE1pg\nw0j/958bdaSdQJJvYEpc7brCHhp5lmNJA+MjVcCXCL4/8KfuEcyGNPPSPo7wbuYJ\nO9jsJmQOklfyvlKGJschvc8AZ0E0AGdrgGam1KApjrb6Xly5bqgV4KPBQ7KttBVw\nwFfTm0yjD3nAjaSXi3I/MG+gMGnUXoTMZa3iS2pomBMHLdTksiujbbH7RP9mzPT3\n7UvyVmtw7eQFEjEYceVWHlhXCjL9gpcJiX/wu9XzREDpNCqY2R3zb+ZGYuQD0L5h\nzv0u1CF+Cfkkg8luxol+aWc+1ac/8TGLV1WOGj4FuEMfxQPXWFqhc8VEyxZ/r/w=\n-----END CERTIFICATE-----") || !printToFile(UDPTunnel.this.fileConf, format)) {
                        // detenerUdp();
                        udpListener.onError();
                        return;
                    }
                    File file = new File(context.getApplicationInfo().nativeLibraryDir, "libfarikudp.so");
                    String[] strArr = {file.getAbsolutePath(), "client", "--config", fileConf.getAbsolutePath()};
                    udpProcess = new ProcessBuilder(new String[0]).command(strArr).redirectErrorStream(true).start();
                    stream = new UDPStreamGobbler(udpProcess.getInputStream(), resultListener);
                    error = new UDPStreamGobbler(udpProcess.getErrorStream(), resultListener);
                    stream.start();
                    error.start();
                    udpProcess.waitFor();
                } catch (Exception e) {
                    SkStatus.logInfo("UDP Tunnel Error: " + e.toString());
                    // udpListener.onError();
                }

            }
        });
    }

    public void iniciarUdp() {
        try {
            if (udpService != null) {
                SkStatus.logInfo("Iniciando Servicio UDP");
                udpListener.onConnecting();
                udpService.start();
            }
        } catch (Exception e) {
            SkStatus.logInfo("Error: al iniciar udp");
        }

    }

    public void detenerUdp() {

        if (udpService != null) {
            SkStatus.logInfo("Deteniendo Servicio UDP");
            udpService.interrupt();
        }

        if (stream != null) {
            stream.setInterrupted(true);
            stream.interrupt();
        }

        if (error != null) {
            error.setInterrupted(true);
            error.interrupt();
        }

        if (udpProcess != null) {
            udpProcess.destroy();
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            SkStatus.updateStateString(SkStatus.SSH_DESCONECTADO, "Desconectado");
            estado = "desconectado";
        }, 1000);

    }

    private boolean printToFile(File file, String str) {
        try {
            PrintWriter printWriter = new PrintWriter(file);
            printWriter.println(str);
            printWriter.flush();
            printWriter.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Inet4Address getIPv4Addresses(InetAddress[] inetAddressArr) {
        for (InetAddress inetAddress : inetAddressArr) {
            if (inetAddress instanceof Inet4Address) {
                return (Inet4Address)inetAddress;
            }
        }
        return null;
    }

}
