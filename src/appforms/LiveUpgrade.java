/* Created by LEOR_Chn
 * License Copyright(R) LEOR_Chn (2014-2017)
 * LEOR_Chn Soft.
 */
package appforms;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import java.math.BigDecimal;
import leorchn.lib.*;

//<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
public class LiveUpgrade extends Service implements DialogInterface.OnClickListener{
	/*服务固定*/public LiveUpgrade(){super();}public android.os.IBinder onBind(Intent i){return null;}
	Service This=this;
	boolean gotinfo=false;
	int speed=0,lvl=0,current=0,total=0,last=0;//Exp Default = k (1000x)
	String cookie="",name="",uid="0",referer="",errmsg="";
	public void onCreate(){}
	public int onStartCommand(Intent i, int f, int startId) {
		cookie=i.getStringExtra("cookie");if(cookie==null)cookie="";//提示参数错误
		uid=i.getStringExtra("uid");if(uid==null)uid="-1";
		referer=i.getStringExtra("referer");if(referer==null)随机引用();
		start();
		return Service.START_NOT_STICKY;
	}
	int infotime=0,beattime=0;//onStartTimeing
	void start(){
		if(start==null||!start.isAlive()){
			start=new Thread(){public void run(){//链接到本类以防回收
					while(true){
						if(infotime<=0){ 查询信息();infotime=30; }
						if(beattime<=0){ 心跳包();beattime=13; }
						infotime--;beattime--;
						try{ Thread.sleep(10000); }catch(Exception e){}
					}
				}};
			start.start();
		}
	}
	Thread start;//线程不能在结束后再次直接启动，已放在方法内重建
	void 查询信息(){
		String debug=网络.获得数据("GET", "http://api.live.bilibili.com/User/getUserInfo", "Cookie: "+cookie, "");
		FSON j=new FSON(debug);
		if(j.canRead()){
			if(j.get("msg","fail").toLowerCase().equals("ok")){
				j=j.getObject("data");
				name=j.get("uname","");
				speed=Math.max((j.get("vip",0)==0?36:72), (j.get("svip",0)==0?36:90));
				lvl=j.get("user_level",0);
				current=(j.get("user_intimacy",0)/1000);
				total=(j.get("user_next_intimacy",0)/1000);
				last=total-current;
				gotinfo=true;
			}else{ errmsg="登录凭证失效";gotinfo=false; }
		}else{ errmsg="网络数据错误";gotinfo=false; }
		生成通知();
	}
	void 心跳包(){
		网络.获得数据("POST", "http://api.live.bilibili.com/User/userOnlineHeart", 
			"Referer: http://live.bilibili.com/"+referer+"\nCookie: "+cookie, "");
	}
	void 生成通知(){
		Notification n=new 通知(this)
			.提示("正在后台挂机获取直播经验")
			.标题(name+(gotinfo?"："+speed+"k XP/h":errmsg))
			.说明("Lv."+lvl+" "+
				current+"k/"+(total<10000?total+"k":(total/1000)+"m")+" "+
				需求经验转时间(last,speed))
			.运行中(true).自动注销(false)
			.图标ID(gotinfo?android.R.drawable.presence_online:android.R.drawable.presence_offline)
			.点击行为(new Runnable(){public void run(){showControls();}}).创建();
		this.startForeground(1,n);//6278891
	}
	boolean alreadyShowControls=false;
	String[]m;
	void showControls(){
		if(alreadyShowControls)return;alreadyShowControls=true;
		java.util.List<String>l=new java.util.ArrayList<String>();
		l.add("退出挂机"); l.add("");//注销(todo)");
		l.add("帐户："+name+//"\nID："+uid+
			"\n等级："+lvl+"\n经验："+current+"k/"+(total<10000?total+"k":(total/1000)+"m")+
			"\n升级剩余时间："+需求经验转时间(last,speed)+
			"\n登录有效时间：从你登录开始30天内");
		l.add("刷新随机参数("+referer+")");
		if(!gotinfo)l.add("手动重连");
		l.add("关闭面板");
		m=l.toArray(new String[l.size()]);l=null;//final int menulen=m.length;
		AlertDialog d=new AlertDialog.Builder(this)/*.setTitle("控制面板")*/
			.setCancelable(false).setItems(m, this).create();
		d.getWindow().setType(android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		d.show();
	}
	public void onClick(DialogInterface di, int index) {
		switch(index){
			case 0: confirmExit();break;
			case 1:/*confirmSignOut();*/break;//todo
			case 3:随机引用();break;
			case 4:if(m.length==6){infotime=0;beattime=0;tip("将在10秒内重试。");}/*set time to zero*/
				//alert(new String[]{"开发信息",debug},null,null,true);
				break;//todo
		}m=null;alreadyShowControls=false;start();System.gc();
	}
	void confirmExit(){
		alert(this,true,"确认退出？","",
			new String[]{"是","取消"},
			new Runnable(){public void run(){
				This.stopForeground(true);
				This.stopSelf();
		}});
	}
	void tip(String s){Toast t=Toast.makeText(This,s,1);t.setGravity(48/*上*/,0,0);t.show();}
	
	void 随机引用() {
		String[] backReferer = {"295460", "945012", "1017", "1024"};
		referer = backReferer[new java.util.Random().nextInt(backReferer.length)];
		tip("当前随机参数为 " + referer);
	}
	class Alertinterface implements DialogInterface.OnClickListener{
		Runnable[]r; public Alertinterface(Runnable[]runnable){r=runnable;}
		public void onClick(DialogInterface di, int i) {
			try{switch(i){
				case DialogInterface.BUTTON_POSITIVE:r[0].run();break;
				case DialogInterface.BUTTON_NEGATIVE:r[1].run();break;
				case DialogInterface.BUTTON_NEUTRAL:r[2].run();
			}}catch(Exception e){}
		}
	}
	void alert(Service context,boolean cancelable,String title,String message, String[] buttons, final Runnable... willDo) {
		AlertDialog.Builder d = new AlertDialog.Builder(context)
			.setCancelable(cancelable).setTitle(title).setMessage(message);
		try{
			Alertinterface a=new Alertinterface(willDo);
			d = d.setPositiveButton(buttons[0],a);
			d = d.setNegativeButton(buttons[1],a);
			d = d.setNeutralButton(buttons[2],a);
		}catch(Exception e){}
		AlertDialog ad = d.create();
		ad.getWindow().setType(android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		ad.show();
		System.gc();
	}

	String 需求经验转时间(long 需求经验, int 时速) {
		if(需求经验<时速)return"不到1小时"; if(时速<=0)return"地久天长";
		long last=需求经验;
		int y=0,m=0,d=0;double h=0;
		if(last>=时速*8760){//24*365=8760
			y=(int)Math.floor(last/(时速*8760));
			last-=8760l*时速*y;//long 数值在最前面，这样能够防止所乘数值到达负数
		}
		if(last>=时速*729.6){//24*30.4=729.6
			m=(int)Math.floor(last/(时速*729.6));
			last-=Math.round(时速*729.6*m);
		}
		if(last>=时速*24){//天
			d=(int)Math.floor(last/(时速*24));
			last-=时速*24*d;
		}
		if(last>=时速)h=new BigDecimal((double)last/时速).setScale(1,BigDecimal.ROUND_HALF_UP).doubleValue();//时
		String res=(y>0?y+"年":"")+(m>0?m+"月":"")+(d>0?d+"天":"")+(h>0?h+"时":"");
		return res;
	}

}
class 通知 extends BroadcastReceiver{
	private android.graphics.Bitmap BIC=null;
	private int SIC=0;private long 时间=System.currentTimeMillis();
	private String 标题="",说明="",提示="",唯一标记=Long.toHexString(时间);
	private boolean 运行中=false,自动取消=true;
	private Context 来源;private Runnable 行为=null;

	通知(Context 创建者){来源=创建者;}
	通知 大图标(android.graphics.Bitmap 图标){BIC=图标;return this;}
	通知 图标ID(int 图标){SIC=图标;return this;}
	通知 标题(String 文本){标题=文本;return this;	}
	通知 说明(String 文本){说明=文本;return this;	}
	通知 提示(String 文本){提示=文本;return this;	}
	通知 点击行为(Runnable 操作){行为=操作;return this;}
	通知 创建时间(long 毫秒时间戳){时间=毫秒时间戳;return this;}
	通知 运行中(boolean isOngoing){运行中=isOngoing;return this;}
	通知 指定标识(String 自定标识){唯一标记=自定标识;return this;}
	通知 自动注销(boolean autoCancel){自动取消=autoCancel;return this;}

	Notification 创建(){
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
		}
	}
}
