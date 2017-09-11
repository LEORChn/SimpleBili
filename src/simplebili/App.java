package simplebili;

import android.app.*;
import android.content.*;

public class App extends Application {
	public App(){super();}
	public void onCreate(){ super.onCreate();
		c=getApplicationContext();
	}
	static Context c;
	public static Context getContext(){return c;}
}
