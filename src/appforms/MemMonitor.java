package appforms;
import android.app.*;
import android.content.*;
import android.os.*;
import leorchn.lib.*;

public class MemMonitor extends Service1  {
	MemMonitor This=this;
	int TAG_INT=hashCode();
	String TAG_STR=Long.toHexString(System.currentTimeMillis());
	
	@Override public void onCreate() {
		super.onCreate();
		Notification n=new Notification.Builder(this)
			.setTicker("调试模式已启动")
			.setContentTitle("调试模式界面")
			.setContentText("简哔调试模式运行中")
			.setContentIntent(PendingIntent.getBroadcast(this, 0, new Intent(TAG_STR), 0))
			.setOngoing(true)//n.flags=运行中?0x22:16;
			.build();
		n.icon=android.R.drawable.ic_menu_preferences;
		IntentFilter ifilter = new IntentFilter(TAG_STR);// 行为筛选器如果用在服务上可以用随机生成的一个字符串
		registerReceiver(rec,ifilter);
		startForeground(TAG_INT,n);
	}
	boolean fwindow=true;
	BroadcastReceiver rec=new BroadcastReceiver(){
		public void onReceive(Context p1, Intent p2) {
			new Msgbox("",dumpmsg(),"【继续】","退出",fwindow?"隐藏悬浮窗":"显示悬浮窗"){
				void onClick(int i){
					if(i==1)This.stopSelf();
					else if(i==2) setFwindowStat(!fwindow);
				}
			};
		}
	};
	String dumpmsg(){
		/*Debug.MemoryInfo dm=new Debug.MemoryInfo();
		Debug.getMemoryInfo(dm);
		dm.*/// api 23 later
		return string(
			"已加载类 ",Debug.getLoadedClassCount(),
			"\n堆分配内存 ",Debug.getNativeHeapAllocatedSize(),
			"\n堆可用内存 ",Debug.getNativeHeapFreeSize(),
			"\n堆大小 ",Debug.getNativeHeapSize(),
			//"运行时状态表 ",Debug.getRuntimeStats(),// api 23 later
			"",
			"\nCPU用率 ",Debug.threadCpuTimeNanos()
		);
	}
	
	void setFwindowStat(boolean b){
		
		fwindow=b;
	}
}
