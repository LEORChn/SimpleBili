package simplebili;

import android.app.*;
import android.content.*;
import leorchn.lib.*;

public class App extends Application {
	public App(){super();}
	public void onCreate(){ super.onCreate();
		//setMainCookie();
		c=getApplicationContext();
		System.out.println("application loaded");
		if(com.LEORChn.SimpleBili.BuildConfig.DEBUG) startService(new Intent(this,appforms.MemMonitor.class));
	}
	static Context c;
	public static Context getContext(){return c;}
	//public static FSON
	/*public static void setMainCookie(){
		//c.get
		Activity1.mCookie="Cookie: ";
	}*/
}
