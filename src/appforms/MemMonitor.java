package appforms;
import android.app.*;
import android.content.*;
import android.os.*;
import leorchn.lib.*;
import android.view.*;
import android.widget.*;
/*	usage:
	if(com.LEORChn.SimpleBili.BuildConfig.DEBUG) startService(new Intent(this,appforms.MemMonitor.class));
	xml:
	<service android:name="appforms.MemMonitor"/>
*/
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
		setFwindowStat(true);
		u.start();
		startForeground(TAG_INT,n);
	}
	boolean fwindow=true;
	BroadcastReceiver rec=new BroadcastReceiver(){
		public void onReceive(Context p1, Intent p2) {
			new Msgbox("",dumpmsg(),"【继续】","退出"){
				void onClick(int i){
					if(i==vbno)This.stopSelf();
					else if(i==vbmid) setFwindowStat(!fwindow);
				}
			};
		}
	};
	Thread u=new Thread(){
		int switcher=0;
		@Override public void run(){
			if(switcher==0)
				for(;;){
					switcher=1;
					v.post(this);
					try{Thread.sleep(1000);}catch(Exception e){}
				}
			else
				updatefwindow();
		}
	};
	void updatefwindow(){
		Debug.MemoryInfo dm=new Debug.MemoryInfo();
		Debug.getMemoryInfo(dm);
		((TextView)v).setText(
			string(
				"私有：",(dm.getTotalPrivateClean()+dm.getTotalPrivateDirty())/1024," M\nPss：",
				dm.getTotalPss()/1024," M"
		));
	}
	String dumpmsg(){
		Debug.MemoryInfo dm=new Debug.MemoryInfo();
		Debug.getMemoryInfo(dm);
		int pvc=dm.getTotalPrivateClean(),
			pvd=dm.getTotalPrivateDirty(),
			pss=dm.getTotalPss(),
			sps=dm.getTotalSwappablePss(),
			sc=dm.getTotalSharedClean(),
			sd=dm.getTotalSharedDirty();// api 23 later
		return string(
			"已加载类 ",Debug.getLoadedClassCount(),
			//"运行时状态表 ",Debug.getRuntimeStats(),// api 23 later
			"\n私有 ",pvc,
			"\n私有脏 ",pvd,
			"\npss ",pss,
			"\n交换pss ",sps,
			"\n共享 ",sc,
			"\n共享脏 ",sd,
			"\n总计 ",pvc+pvd+pss+sps+sc+sd,
			"\n堆分配 ",Debug.getNativeHeapAllocatedSize(),
			"\n堆可用 ",Debug.getNativeHeapFreeSize(),
			"\n堆大小 ",Debug.getNativeHeapSize(),
			"\nCPU用率 ",Debug.threadCpuTimeNanos()
		);
	}
	static WindowManager.LayoutParams lp;
	static View v;
	void setFwindowStat(boolean b){
		fwindow=b;
		WindowManager m=(WindowManager)getSystemService(WINDOW_SERVICE);
		if(b){
			v=new TextView(this);
			
			int wflag=0,
			wtype=0,
			w=lp.WRAP_CONTENT,
			h=lp.WRAP_CONTENT;
			wflag|=lp.FLAG_NOT_FOCUSABLE;
			wtype|=lp.TYPE_SYSTEM_ALERT;
			lp=new WindowManager.LayoutParams(w,h,wtype,wflag,android.graphics.PixelFormat.TRANSLUCENT);
			lp.gravity=Gravity.TOP;
			try{
				m.addView(v,lp);
			}catch(Throwable e){}
		}else{
			try{
				m.removeView(v);
			}catch(Throwable e){}
		}
	}
}
