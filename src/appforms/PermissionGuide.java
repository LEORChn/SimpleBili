package appforms;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import java.lang.reflect.*;
import leorchn.lib.*;
import static simplebili.App.getContext;
import android.net.*;
public class PermissionGuide extends Activity1 {

	@Override protected void oncreate(){} 
	@Override public void onClick(View v) {
		// TODO: Implement this method
	}
	int hasinit=0;
	@Override protected boolean onIdle() {
		switch(hasinit){
			case 0:
				
		}
		return hasinit<9;
	}
	public static boolean isCanNotify(){
		AppOpsManager appops = (AppOpsManager) getContext().getSystemService(Context.APP_OPS_SERVICE);
		int uid = getContext().getApplicationInfo().uid;
		String pkg=getContext().getPackageName();
		Class appopsclass = null;
		try{
			appopsclass = Class.forName(AppOpsManager.class.getName());
			Method checkopnothrowmethod = appopsclass.getMethod("checkOpNoThrow", Integer.TYPE, Integer.TYPE, String.class);
			Field oppostnotificationvalue = appopsclass.getDeclaredField("OP_POST_NOTIFICATION");
			int value = (int)oppostnotificationvalue.get(Integer.class);
			return ((int)checkopnothrowmethod.invoke(appops,value, uid, pkg) == AppOpsManager.MODE_ALLOWED);
		}catch(Throwable e){
			return false;
		}
	}
	public static void tosetting(Activity a) {
		Intent localintent = new Intent();
		localintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		int bver=Build.VERSION.SDK_INT;
		if (bver >= 9) {
			localintent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
			localintent.setData(Uri.fromParts("package", a.getPackageName(), null));
		} else if (bver <= 8) {
			localintent.setAction(Intent.ACTION_VIEW);
			localintent.setClassName("com.android.settings", "com.android.setting.InstalledAppDetails");
			localintent.putExtra("com.android.settings.ApplicationPkgName", a.getPackageName());
		}
		a.startActivity(localintent);
	}
}
