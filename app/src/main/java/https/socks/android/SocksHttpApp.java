package https.socks.android;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nphdevs.bluespace.R;
import com.slipkprojects.ultrasshservice.SocksHttpCore;
/**
* App
*/

public class SocksHttpApp extends Application {
	private static final String TAG = SocksHttpApp.class.getSimpleName();
	public static final String PREFS_GERAL = "SocksHttpGERAL";
	public static final String APP_FLURRY_KEY = "RQQ8J9Q2N4RH827G32X9";
	private static SocksHttpApp mApp;
	private Activity currentActivity;
    private static SharedPreferences sharedPreferences;
	@Override
	public void onCreate() {
		super.onCreate();
		mApp = this;
		SocksHttpCore.init(this);
	}


    public static SocksHttpApp getApp() {
        return mApp;
    }
	public static SharedPreferences getSharedPreferences()  {
        return sharedPreferences;
    }
	public static SharedPreferences getDefSharedPreferences()  {
        return sharedPreferences;
    }
	public static void toast(Context contxt, int color, String string){
		LayoutInflater inflater = (LayoutInflater) contxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View inflate = inflater.inflate(R.layout.toast, (ViewGroup) null );
        LinearLayout ll1 = new LinearLayout(contxt);
		Toast llIl = Toast.makeText(contxt,Html.fromHtml(""),Toast.LENGTH_LONG);
		final TextView text1 = (TextView)inflate.findViewById(R.id.textqt);
		final ImageView img = (ImageView)inflate.findViewById(R.id.img);
        final RelativeLayout toastlayout = (RelativeLayout)inflate.findViewById(R.id.toastlayout);
		GradientDrawable var1 = new GradientDrawable();
		final Animation e = AnimationUtils.loadAnimation(contxt,R.anim.grow);
		var1.setColor(contxt.getResources().getColor(color));
		if (color == R.color.red){
			img.setBackgroundResource(R.drawable.err1);

		} else if (color == R.color.colorPrimary){
			img.setBackgroundResource(R.drawable.err);
			
		} else if (color == R.color.green){
			img.setBackgroundResource(R.drawable.cnt);
			
		} else {
			img.setBackgroundResource(R.drawable.err);
			
		}
        var1.setCornerRadius((float)50);
        var1.setOrientation(Orientation.RIGHT_LEFT);
        var1.setStroke(0, Color.parseColor("#ffffff"));
		text1.setText(Html.fromHtml(string));
        ll1.setBackgroundDrawable(var1);
        ll1.addView(inflate);
		toastlayout.setAnimation(e);
		llIl.setView(ll1);
		llIl.show();
	
	}
	
	
    
}
