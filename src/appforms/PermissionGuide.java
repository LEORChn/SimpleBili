package appforms;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import java.lang.reflect.*;
import leorchn.lib.*;
import static simplebili.App.getContext;
public class PermissionGuide extends Activity1 {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		addIdleHandler();
		super.onCreate(savedInstanceState);
	} 
	@Override public void onClick(View v) {
		// TODO: Implement this method
	}

	@Override protected boolean onIdle() {
		
		return false;
	}
	public boolean isCanNotify(){
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
}
