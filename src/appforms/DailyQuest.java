package appforms;

import android.app.*;
import android.content.*;
import java.util.*;

public class DailyQuest extends Service {
	/*服务固定*/public DailyQuest(){super();}public android.os.IBinder onBind(Intent i){return null;}
	Service This=this;
	ArrayList<String>cookieLst=new ArrayList<>();
	@Override public int onStartCommand(Intent i, int flags, int startId) {//适应多帐号，此方法只用于添加帐号信息
		String cok;
		if((cok=i.getStringExtra("cookie"))!=null){
			cookieLst.add(cok.toLowerCase().startsWith("cookie:")?cok:"Cookie: "+cok);
			// send pre-start request here
		}
		return super.onStartCommand(i, flags, startId);
	}
	int checkCookie(String cok){
		return 0;
	}
	//@Override public void 
}
