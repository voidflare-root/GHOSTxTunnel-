/*
 * Copyright (c) 2016, Psiphon Inc.
 * All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.slipkprojects.ultrasshservice.tunnel.vpn;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.VpnService;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.nphdevs.bluespace.R;
import com.slipkprojects.ultrasshservice.MainReceiver;
import com.slipkprojects.ultrasshservice.SocksHttpService;
import com.slipkprojects.ultrasshservice.logger.SkStatus;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TunnelVpnService extends VpnService {

  private static final String LOG_TAG = "TunnelVpnService";
  public static final String TUNNEL_VPN_DISCONNECT_BROADCAST =
      "tunnelVpnDisconnectBroadcast";
  public static final String TUNNEL_VPN_START_BROADCAST =
      "tunnelVpnStartBroadcast";
  public static final String TUNNEL_VPN_START_SUCCESS_EXTRA =
      "tunnelVpnStartSuccessExtra";
  private static final int VPN_NOTIFICATION_ID = SocksHttpService.NOTIFICATION_CHANNEL_BG_ID.hashCode();
  private static final String VPN_CHANNEL_ID = SocksHttpService.NOTIFICATION_CHANNEL_BG_ID;

  private TunnelVpnManager m_tunnelManager = new TunnelVpnManager(this);

  public class LocalBinder extends Binder {
    public TunnelVpnService getService() {
      return TunnelVpnService.this;
    }
  }

  private final IBinder m_binder = new LocalBinder();

  @Override
  public IBinder onBind(Intent intent) {
    String action = intent.getAction();
    if (action != null && action.equals(SERVICE_INTERFACE)) {
      return super.onBind(intent);
    }
    return m_binder;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(LOG_TAG, "on start");
    ensureForeground();
    return m_tunnelManager.onStartCommand(intent, flags, startId);
  }

  @Override
  public void onCreate() {
    Log.d(LOG_TAG, "on create");
    ensureForeground();
    TunnelState.getTunnelState().setTunnelManager(m_tunnelManager);
  }

  @Override
  public void onDestroy() {
    Log.d(LOG_TAG, "on destroy");
    TunnelState.getTunnelState().setTunnelManager(null);
    m_tunnelManager.onDestroy();
  }

  @Override
  public void onRevoke() {
    SkStatus.logInfo("<strong>VPN service revoked</strong>");
    broadcastVpnDisconnect();
    // stopSelf will trigger onDestroy in the main thread.
    stopSelf();
  }

  public VpnService.Builder newBuilder() {
    return new VpnService.Builder();
  }

  private void ensureForeground() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel channel = new NotificationChannel(
          VPN_CHANNEL_ID,
          "Background service",
          NotificationManager.IMPORTANCE_LOW);
      NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
      if (manager != null) {
        manager.createNotificationChannel(channel);
      }
    }

    Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        ? new Notification.Builder(this, VPN_CHANNEL_ID)
        : new Notification.Builder(this);
    builder.setContentTitle(getString(R.string.app_name))
        .setContentText("Connected")
        .setSmallIcon(R.drawable.ic_cloud_black_24dp)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setContentIntent(SocksHttpService.getGraphPendingIntent(this));
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      Intent reconnectVPN = new Intent(this, MainReceiver.class);
      reconnectVPN.setAction(MainReceiver.ACTION_SERVICE_RESTART);
      builder.addAction(R.drawable.ic_autorenew_black_24dp,
          getString(R.string.reconnect),
          android.app.PendingIntent.getBroadcast(this, 10, reconnectVPN, SocksHttpService.getFlags()));

      Intent disconnectVPN = new Intent(this, MainReceiver.class);
      disconnectVPN.setAction(MainReceiver.ACTION_SERVICE_STOP);
      builder.addAction(R.drawable.ic_power_settings_new_black_24dp,
          getString(R.string.stop),
          android.app.PendingIntent.getBroadcast(this, 11, disconnectVPN, SocksHttpService.getFlags()));
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      builder.setCategory(Notification.CATEGORY_SERVICE);
    }
    startForeground(VPN_NOTIFICATION_ID, builder.build());
  }

  // Broadcast non-user-initiated VPN disconnect.
  public void broadcastVpnDisconnect() {
    dispatchBroadcast(new Intent(TUNNEL_VPN_DISCONNECT_BROADCAST));
  }

  // Broadcast VPN start. |success| is true if the VPN and tunnel were started
  // successfully, and false otherwise.
  public void broadcastVpnStart(boolean success) {
    Intent vpnStart = new Intent(TUNNEL_VPN_START_BROADCAST);
    vpnStart.putExtra(TUNNEL_VPN_START_SUCCESS_EXTRA, success);
    dispatchBroadcast(vpnStart);
  }

  private void dispatchBroadcast(final Intent broadcast) {
    LocalBroadcastManager.getInstance(TunnelVpnService.this)
        .sendBroadcast(broadcast);
  }
}
