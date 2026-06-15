package https.socks.android.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Debug;
import android.widget.Toast;

import com.nphdevs.bluespace.BuildConfig;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Locale;

public final class SecurityGuard {
    private static final int KEY = 91;
    private static final int[] PACKAGE_NAME = new int[]{56, 52, 54, 117, 60, 51, 52, 40, 47, 35, 41, 52, 52, 47, 117, 45, 43, 53};
    private static final int[] SIGNATURE_SHA256 = new int[]{105, 98, 97, 30, 109, 97, 25, 31, 97, 108, 30, 97, 26, 105, 97, 29, 30, 97, 25, 105, 97, 109, 99, 97, 98, 108, 97, 26, 108, 97, 30, 108, 97, 24, 98, 97, 105, 99, 97, 107, 105, 97, 26, 105, 97, 99, 106, 97, 98, 99, 97, 98, 99, 97, 26, 110, 97, 30, 98, 97, 29, 107, 97, 111, 106, 97, 31, 30, 97, 26, 104, 97, 105, 29, 97, 109, 108, 97, 109, 104, 97, 110, 29, 97, 104, 25, 97, 98, 109, 97, 109, 99, 97, 110, 26};
    private static final String[] RISK_PACKAGES = new String[]{
            "de.robv.android.xposed.installer",
            "org.lsposed.manager",
            "io.github.lsposed.manager",
            "com.saurik.substrate",
            "com.topjohnwu.magisk",
            "io.github.vvb2060.magisk",
            "com.frida.server",
            "com.frida",
            "com.guoshi.httpcanary",
            "app.greyshirts.sslcapture",
            "com.minhui.networkcapture",
            "com.emanuelef.remote_capture",
            "com.lody.virtual",
            "com.lody.virtual.client",
            "com.parallel.space",
            "com.excelliance.multiaccount",
            "com.cloneapp.parallelspace",
            "com.dualspace.multispace",
            "com.applisto.appcloner",
            "com.bly.dkplat",
            "bin.mt.plus",
            "bin.mt.plus.canary"
    };
    private static final String[] RISK_CLASSES = new String[]{
            "de.robv.android.xposed.XposedBridge",
            "de.robv.android.xposed.XC_MethodHook",
            "com.saurik.substrate.MS$MethodPointer",
            "com.lody.virtual.client.core.VirtualCore",
            "com.swift.sandhook.SandHook",
            "me.weishu.epic.art.Epic",
            "org.lsposed.lspd.nativebridge.HookBridge",
            "rikka.sui.Sui"
    };
    private static final String[] RISK_MAPS = new String[]{
            "frida",
            "gum-js-loop",
            "xposed",
            "lsposed",
            "substrate",
            "sandhook",
            "riru",
            "zygisk",
            "edxp",
            "epic"
    };

    private SecurityGuard() {
    }

    public static boolean verifyOrClose(Activity activity) {
        if (BuildConfig.DEBUG) {
            return true;
        }

        if (isTrusted(activity) && isRuntimeEnvironmentSafe(activity)) {
            return true;
        }

        Toast.makeText(activity, "Secure mode blocked", Toast.LENGTH_LONG).show();
        activity.finishAffinity();
        return false;
    }

    public static void enforceConfigAccess(Context context) throws SecurityException {
        if (BuildConfig.DEBUG) {
            return;
        }
        if (!isTrusted(context)) {
            throw new SecurityException("App verification failed");
        }
        if (!isRuntimeEnvironmentSafe(context)) {
            throw new SecurityException("Secure mode blocked");
        }
    }

    public static boolean isTrusted(Context context) {
        try {
            if (!decode(PACKAGE_NAME).equals(context.getPackageName())) {
                return false;
            }

            String expected = decode(SIGNATURE_SHA256);
            for (Signature signature : getSignatures(context)) {
                if (expected.equals(sha256(signature.toByteArray()))) {
                    return true;
                }
            }
        } catch (Exception ignored) {
            return false;
        }
        return false;
    }

    private static boolean isRuntimeEnvironmentSafe(Context context) {
        return !isDebuggerActive()
                && !hasRiskPackage(context)
                && !hasRiskClass()
                && !hasRiskMaps();
    }

    private static boolean isDebuggerActive() {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger();
    }

    private static boolean hasRiskPackage(Context context) {
        PackageManager manager = context.getPackageManager();
        for (String packageName : RISK_PACKAGES) {
            try {
                manager.getPackageInfo(packageName, 0);
                return true;
            } catch (PackageManager.NameNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    private static boolean hasRiskClass() {
        ClassLoader loader = SecurityGuard.class.getClassLoader();
        for (String className : RISK_CLASSES) {
            try {
                Class.forName(className, false, loader);
                return true;
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    private static boolean hasRiskMaps() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/self/maps"));
            String line;
            while ((line = reader.readLine()) != null) {
                String lower = line.toLowerCase(Locale.US);
                for (String marker : RISK_MAPS) {
                    if (lower.contains(marker)) {
                        return true;
                    }
                }
            }
        } catch (IOException ignored) {
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
        return false;
    }

    private static Signature[] getSignatures(Context context) throws PackageManager.NameNotFoundException {
        PackageManager manager = context.getPackageManager();
        String packageName = context.getPackageName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageInfo info = manager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES);
            if (info.signingInfo.hasMultipleSigners()) {
                return info.signingInfo.getApkContentsSigners();
            }
            return info.signingInfo.getSigningCertificateHistory();
        }

        PackageInfo info = manager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
        return info.signatures;
    }

    private static String sha256(byte[] input) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(input);
        StringBuilder builder = new StringBuilder(digest.length * 3);
        for (int i = 0; i < digest.length; i++) {
            if (i > 0) {
                builder.append(':');
            }
            builder.append(String.format(Locale.US, "%02X", digest[i]));
        }
        return builder.toString();
    }

    private static String decode(int[] values) {
        StringBuilder builder = new StringBuilder(values.length);
        for (int value : values) {
            builder.append((char) (value ^ KEY));
        }
        return builder.toString();
    }
}
