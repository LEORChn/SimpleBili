package leorchn;

import android.app.*;
import android.content.*;
import leorchn.lib.*;

import static leorchn.lib.Activity1.*;

public class App extends Application implements Consts{
	public App(){
		super();
		c=getContext=this;
	}
	public void onCreate(){
		super.onCreate();
		//setMainCookie();
		Activity1.pl("==== application loaded");
		if(DEBUG){
			tip(string("当前正在调试模式运行\n",getApplicationInfo().loadLabel(getPackageManager())));
			startService(new Intent(this,appforms.MemMonitor.class));
		}
	}
	static Context c, getContext;
	public static Context getContext(){return c;}
}
