package https.socks.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Process;
import android.util.Log;
import java.lang.Thread.UncaughtExceptionHandler;

public class ExceptionHandler implements UncaughtExceptionHandler {
    private static final String TAG = "DARKxGHOST";
    private final Activity myContext;

    public ExceptionHandler(Activity activity) {
        this.myContext = activity;
    }

    public void uncaughtException(Thread thread, Throwable th) {
        Log.e(TAG, "Unexpected app exception", th);
        try {
            Intent intent = new Intent(this.myContext, CrashWindow.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            this.myContext.startActivity(intent);
            Process.killProcess(Process.myPid());
            System.exit(10);
        } catch (Throwable e) {
            throw new NoClassDefFoundError(e.getMessage());
        }
    }
}
