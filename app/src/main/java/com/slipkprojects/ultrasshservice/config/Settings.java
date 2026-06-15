package com.slipkprojects.ultrasshservice.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.netfreemexico.generador.constants.AppConstants;
import com.slipkprojects.ultrasshservice.util.securepreferences.SecurePreferences;
import com.slipkprojects.ultrasshservice.util.securepreferences.model.SecurityConfig;

/**
* Configurações
*/

public class Settings implements SettingsConstants
{
	private Context mContext;
	private SharedPreferences mPrefs;
	private SecurePreferences mPrefsPrivate;
	
	private static SecurityConfig minimumConfig = new SecurityConfig.Builder("fubgf777gf6")
		.build();
	
	public Settings(Context context) {
		mContext = context;
		
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mPrefsPrivate = SecurePreferences.getInstance(mContext, "SecureData", minimumConfig);
	}
	
	
	public String getPrivString(String key) {
		String defaultStr = "";
	
		switch (key) {
			case PORTA_LOCAL_KEY:
				defaultStr = "1080";
			break;
		}
		
		return mPrefsPrivate.getString(key, defaultStr);
	}
	
	public SecurePreferences getPrefsPrivate() {
		return mPrefsPrivate;
	}
	
	
	/**
	* Config File
	*/
	
	public String getMensagemConfigExportar() {
		return mPrefs.getString(CONFIG_MENSAGEM_EXPORTAR_KEY, "");
	}

	public void setMensagemConfigExportar(String str) {
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putString(CONFIG_MENSAGEM_EXPORTAR_KEY, str);
		editor.commit();
	}
	
	
	/**
	* Geral
	*/
	
	public String getIdioma() {
		return mPrefs.getString(IDIOMA_KEY, "default");
	}
	
	public void setIdioma(String str) {
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putString(IDIOMA_KEY, str);
		editor.commit();
	}
	
	/*public String getModoNoturno() {
		return mPrefs.getString(MODO_NOTURNO_KEY, "off");
	}*/
	
	/*public void setModoNoturno(String str) {
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putString(MODO_NOTURNO_KEY, str);
		editor.commit();
	}*/
	
	public boolean getModoDebug() {
		return mPrefs.getBoolean(MODO_DEBUG_KEY, false);
	}
	
	public void setModoDebug(boolean is) {
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putBoolean(MODO_DEBUG_KEY, is);
		editor.commit();
	}
	
	public int getMaximoThreadsSocks() {
		String n = mPrefs.getString(MAXIMO_THREADS_KEY, "8th");
		if (n == null || n.isEmpty()) {
			n = "8th";
		}
		return Integer.parseInt(n.replace("th", ""));
	}
	
	public boolean getHideLog() {
		return mPrefs.getBoolean(HIDE_LOG_KEY, false);
	}
	
	public boolean getAutoClearLog() {
		return mPrefs.getBoolean(AUTO_CLEAR_LOGS_KEY, true);
	}

	public boolean getHttpPing() {
		return mPrefs.getBoolean(HTTP_PING_KEY, false);
	}
	
	public boolean getIsFilterApps() {
		return mPrefs.getBoolean(FILTER_APPS, false);
	}
	
	public boolean getIsFilterBypassMode() {
		return mPrefs.getBoolean(FILTER_BYPASS_MODE, false);
	}
	
	public String[] getFilterApps() {
		String txt = mPrefs.getString(FILTER_APPS_LIST, "");
		if (txt.isEmpty()) {
			return new String[]{};
		}
		else {
			return txt.split("\n");
		}
	}
	
	public boolean getIsTetheringSubnet() {
		return mPrefs.getBoolean(TETHERING_SUBNET, false);
	}
	
	public boolean getIsDisabledDelaySSH() {
		return mPrefs.getBoolean(DISABLE_DELAY_KEY, false);
	}

	public boolean getBypass(){
		return mPrefs.getBoolean(BYPASS_KEY, false);
	}

	public void setBypass(boolean use){
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putBoolean(BYPASS_KEY, use);
		editor.commit();
	}
	
	/**
	* Vpn Settings
	*/
	
	public boolean getVpnDnsForward(){
		return mPrefs.getBoolean(DNSFORWARD_KEY, false);
	}
	
	public void setVpnDnsForward(boolean use){
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putBoolean(DNSFORWARD_KEY, use);
		if (use) {
			editor.putString(DNSTYPE_KEY, DNS_CUSTOM_KEY);
			editor.putString(DNSRESOLVER_KEY1, "1.1.1.1");
			editor.putString(DNSRESOLVER_KEY2, "1.0.0.1");
		} else {
			editor.putString(DNSTYPE_KEY, DNS_GOOGLE_KEY);
			editor.putString(DNSRESOLVER_KEY, "8.8.8.8");
			editor.putString(DNSRESOLVER_KEY1, "8.8.8.8");
			editor.putString(DNSRESOLVER_KEY2, "8.8.4.4");
		}
		editor.commit();
	}
	
	public String getVpnDnsResolver1(){
		return mPrefs.getString(DNSRESOLVER_KEY1, "1.1.1.1");
	}
	public String getVpnDnsResolver2(){
		return mPrefs.getString(DNSRESOLVER_KEY2, "1.0.0.1");
	}
	
	public void setVpnDnsResolver(String str) {
		if (str == null || str.isEmpty()) {
			str = "1.1.1.1";
		}
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putString(DNSTYPE_KEY, DNS_CUSTOM_KEY);
		editor.putString(DNSRESOLVER_KEY, str);
		editor.putString(DNSRESOLVER_KEY1, str);
		editor.putString(DNSRESOLVER_KEY2, "1.0.0.1");
		editor.commit();
	}

	public void setVpnDnsResolvers(String primary, String secondary) {
		if (primary == null || primary.isEmpty()) {
			primary = "1.1.1.1";
		}
		if (secondary == null || secondary.isEmpty()) {
			secondary = "1.0.0.1";
		}
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putString(DNSTYPE_KEY, DNS_CUSTOM_KEY);
		editor.putString(DNSRESOLVER_KEY1, primary);
		editor.putString(DNSRESOLVER_KEY2, secondary);
		editor.commit();
	}

	public boolean getVpnUdpForward(){
		return mPrefs.getBoolean(UDPFORWARD_KEY, false);
	}
	
	public void setVpnUdpForward(boolean use){
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putBoolean(UDPFORWARD_KEY, use);
		editor.commit();
	}

	public String getVpnUdpResolver(){
		return mPrefs.getString(UDPRESOLVER_KEY, "127.0.0.1:7300");
	}
	
	public boolean get_compression() {
        return mPrefs.getBoolean(data_compression_key, true);
	}
	
	public void setVpnUdpResolver(String str) {
		if (str == null || str.isEmpty()) {
			str = "127.0.0.1:7300";
		}
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putString(UDPRESOLVER_KEY, str);
		editor.commit();
	}
	
	/**
	* SSH Settings
	*/
	
	
	public String getSSHKeypath() {
		return mPrefs.getString(KEYPATH_KEY, "");
	}

	public int getSSHPinger() {
		String ping = mPrefs.getString(PINGER_KEY, "3");
		if (ping == null || ping.isEmpty()) {
			ping = "3";
		}
		return Integer.parseInt(ping);
	}
	

	/**
	* Utils
	*/
	
	public static void setDefaultConfig(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();

		editor.putBoolean(DNSFORWARD_KEY, false);
		editor.putString(DNSTYPE_KEY, DNS_GOOGLE_KEY);
		editor.putString(DNSRESOLVER_KEY, "8.8.8.8");
		editor.putString(DNSRESOLVER_KEY1, "8.8.8.8");
		editor.putString(DNSRESOLVER_KEY2, "8.8.4.4");
		editor.putBoolean(UDPFORWARD_KEY, true);
		editor.putString(UDPRESOLVER_KEY, "127.0.0.1:7300");
		//editor.putString(MODO_NOTURNO_KEY, "off");
		editor.putString(PINGER_KEY, "3");
		editor.putString(MAXIMO_THREADS_KEY, "8th");
		editor.remove(MODO_DEBUG_KEY);
		editor.remove(HIDE_LOG_KEY);
		editor.remove(AUTO_CLEAR_LOGS_KEY);
		editor.remove(HTTP_PING_KEY);
		editor.remove(FILTER_APPS);
		editor.remove(FILTER_BYPASS_MODE);
		editor.remove(FILTER_APPS_LIST);
		editor.remove(TETHERING_SUBNET);
		editor.remove(data_compression_key);
		editor.remove(DISABLE_DELAY_KEY);
		
		editor.commit();
	}
	
	public static void clearSettings(Context context) {
		SharedPreferences priv = SecurePreferences.getInstance(context, "SecureData", minimumConfig);
		SharedPreferences.Editor edit = priv.edit();
		edit.clear();
		edit.commit();
	}
	
}
