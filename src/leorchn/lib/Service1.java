package leorchn.lib;
import android.app.*;
import android.os.*;
import android.content.*;
import android.view.*;
import java.io.*;
import com.LEORChn.SimpleBili.R;

public class Service1 extends Service implements Thread.UncaughtExceptionHandler{
	@Override public IBinder onBind(Intent p1){ return null; }
	
	protected static R.id id;
	protected static R.layout layout;
	protected static R.drawable drawable,draw;
	
	protected static String string(Object...str){ return buildstring(str).toString(); }
	protected static StringBuilder buildstring(Object...str){
		StringBuilder bdr=new StringBuilder();
		return string(bdr,str);
	}
	protected static StringBuilder string(StringBuilder bdr,Object...str){
		for(Object s:str) bdr.append(s);
		return bdr;
	}
	
	protected class Msgbox extends AlertDialog.Builder implements DialogInterface.OnClickListener{
		protected int vbyes=AlertDialog.BUTTON_POSITIVE,
			vbno=AlertDialog.BUTTON_NEGATIVE,
			vbmid=AlertDialog.BUTTON_NEUTRAL;
		public Msgbox(String...msgs){
			super(Service1.this); 
			for(int i=0,len=msgs.length;i<len;i++){
				switch(i){
					case 0: setTitle(msgs[0]); break;
					case 1: setMessage(msgs[1]); break;
					case 2: setPositiveButton(msgs[2],this); setCancelable(false); break;
					case 3: setNegativeButton(msgs[3],this); break;
					case 4: setNeutralButton(msgs[4],this); break;
				}
			}
			AlertDialog ad=create();
			ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			ad.show();
		}
		public void onClick(DialogInterface p1,int p2){ onClick(p2); }
		void onClick(int i){}
	}
	protected class 通知 extends BroadcastReceiver implements Runnable{
		private android.graphics.Bitmap BIC=null;
		private int SIC=0;private long 时间=System.currentTimeMillis();
		private String 标题="",说明="",提示="",唯一标记=Long.toHexString(时间);
		private boolean 运行中=false,自动取消=true;
		private Context 来源;private Runnable 行为=null;

		public 通知(){来源=Service1.this;}
		public 通知 大图标(android.graphics.Bitmap 图标){BIC=图标;return this;}
		public 通知 图标ID(int 图标){SIC=图标;return this;}
		public 通知 标题(String 文本){标题=文本;return this; }
		public 通知 说明(String 文本){说明=文本;return this; }
		public 通知 提示(String 文本){提示=文本;return this; }
		public 通知 点击行为(Runnable 操作){行为=操作;return this;}
		public 通知 创建时间(long 毫秒时间戳){时间=毫秒时间戳;return this;}
		public 通知 运行中(boolean isOngoing){运行中=isOngoing;return this;}
		public 通知 指定标识(String 自定标识){唯一标记=自定标识;return this;}
		public 通知 自动注销(boolean autoCancel){自动取消=autoCancel;return this;}

		public Notification 创建(){
			Notification n=new Notification.Builder(来源)//(BID,提示,时间);
				.setTicker(提示)
				.setContentTitle(标题)
				.setContentText(说明)
				.setContentIntent(PendingIntent.getBroadcast(来源, 0, new Intent(唯一标记), 0))
				.setWhen(时间)
				.setOngoing(运行中)//n.flags=运行中?0x22:16;
				.setAutoCancel(自动取消)
				.build();
			n.icon=SIC;
			if(BIC!=null)n.largeIcon=BIC;
			IntentFilter 行为筛选器 = new IntentFilter(唯一标记);// 行为筛选器如果用在服务上可以用随机生成的一个字符串
			来源.registerReceiver(this,行为筛选器);
			return n;
		}
		public void onReceive(Context arg0, Intent 行为包) {
			if(行为包.getAction().equals(唯一标记)){
				if(自动取消){
					来源.unregisterReceiver(this);
					System.gc();
				}
				if(行为 != null)行为.run(); //点击监听器.onClick(arg0,行为包);  // 标记被初始化为 E4ANotification
				run(); onclick();
			}
		}
		public void run(){};
		void onclick(){};
	}
	static Service1 This;
	public Service1(){
		super(); This=this;
		Thread.setDefaultUncaughtExceptionHandler(this);
	}
	@Override public void uncaughtException(final Thread thread, final Throwable ex) {
		rep = E.trace(ex);
		new AfterException();//新建线程显示消息
		//startActivity(new Intent(this,ExceptionReport.class).putExtra("info",errRep));
		//defUeh.uncaughtException(thread,ex);
	}
	private String rep; private AlertDialog excad=null;
	private class AfterException extends Thread implements DialogInterface.OnClickListener{
		public AfterException(){ this.start(); }
		public void run(){
			savelog();
			Looper.prepare();
			if(excad!=null)onClick(null,10);
			excad=new AlertDialog.Builder(This).setTitle("温和的错误提示")
				.setMessage("程序发生了错误并即将退出。以下信息已自动保存。\n"+rep)
				.setNeutralButton("关闭", this)
				.create();
			excad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			excad.show();
			Looper.loop();
		}
		public void onClick(DialogInterface p1, int p2) {
			System.exit(10);
		}
		void savelog(){
			try{
				File log=new File(Activity1.DIR_cache_log);
				log.mkdirs();
				log=log.createTempFile(string(System.currentTimeMillis(),"_"),".log",log);
				Text.write(rep,log.getPath(),"utf-8");
			}catch(Exception e){}
		}
	}
}
