package appforms;
import android.widget.Toast;
import android.view.*;
import android.app.*;
import android.content.*;
import java.math.BigDecimal;
import leorchn.lib.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import android.os.*;
import java.util.*;
import com.LEORChn.SimpleBili.R;
import static android.os.PowerManager.*;
import static leorchn.lib.HttpRequest.*;
import java.text.*;

public class GetAllowance extends Service implements DialogInterface.OnClickListener{
	/*服务固定*/public GetAllowance(){super();}public android.os.IBinder onBind(Intent i){return null;}
	static GetAllowance This;
	boolean gotinfo=false;
	//int tvs=0,raffs=0,golds=0;
	String initStr="{\"roomid\":295460,\"uid\":7354572585223}",errmsg="";
	byte[]command,initpak,beatpak={0,0,0,16,0,16,0,1,0,0,0,2,0,0,0,1};
	final String ip="broadcastlv.chat.bilibili.com";
	final int port=2243;
	long lastheart=0;
	ArrayList<String>log=new ArrayList<>();
	Toast tipper;
	Handler init=new Handler(){
		@Override public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
				case 0: init();break;
				case 1: joinTV(msg.arg1,(String)msg.obj); break;
				case 2: joinRaff(msg.arg1); break;
				case 9: tip(""+msg.obj);break;
			}
		}
	};
	static SimpleDateFormat fmter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
	PowerManager.WakeLock wl;
	@Override public void onCreate(){ This=this;
		//if(System.currentTimeMillis()>1514401321000l) throw new RuntimeException("system api error timeout");
		PowerManager pm=(PowerManager)this.getSystemService(POWER_SERVICE);
		wl=pm.newWakeLock(PARTIAL_WAKE_LOCK|ON_AFTER_RELEASE,"LEORChnGetAllowance");
		if(wl!=null)wl.acquire();
	}
	@Override public void onDestroy() {
		if(wl!=null)wl.release(); super.onDestroy();
	} 
	public int onStartCommand(Intent i, int f, int startId) {
		tipper=Toast.makeText(this,"",1);tipper.setGravity(Gravity.TOP,0,0);
		String cookie=i.getStringExtra("cookie");if(cookie==null)cookie="";//提示参数错误
		new User(cookie);
		//uid=i.getStringExtra("uid");if(uid==null)uid="-1";
		//referer=i.getStringExtra("referer");if(referer==null)referer="295460";
		try{
			command="{\"roomid\":295460,\"uid\":7354572585445}".getBytes("utf8");
		}catch(Exception e){}
		initpak=new byte[]{0,0,0,((Integer)(command.length+16)).byteValue(),0,16,0,1,0,0,0,7,0,0,0,1};
		initpak=bytecopy(initpak,command);
		command=null;
		log("开门！社区送温暖！\n已开启锁屏领取，随时注意电量噢");
		init.obtainMessage(0).sendToTarget();
		return Service.START_NOT_STICKY;
	}
	AsyncTask task;//保持连接
	void init(){
		if(task!=null)return;//单次调用锁
		gotinfo=false; 生成通知("正在重新联网");
		task=new AsyncTask<Void,Void,Integer>(){
			@Override protected Integer doInBackground(Void[]p){
				int result=0;
				while(true){
					result=2;//verify();
					if(result!=0)break;//知道结果了，传出返回值
					try{Thread.sleep(5000);}catch(Exception e){}
				}
				//建议在验证时卡住，帐号过期时返回false，帐号验证后返回true
				return result;/*查询信息();*/
			}
			@Override protected void onPostExecute(Integer p){
				switch(p){
					case 1://帐号验证过期
						//onLogout(); break;
					case 2://帐号验证完成
						start();
						gotinfo=true; 生成通知("正在享受社区温暖"); break;
				}
				//try{ Thread.sleep(3000); }catch(Exception e){}
				task=null;//解除单次调用锁
			}
		}.execute();
	}
	int beattime=0;
	void start(){
		if(start==null||!start.isAlive()){
			start=new Thread(){public void run(){//连接的建立与保持 线程
					try{ 
						sok=SocketChannel.open(new InetSocketAddress(ip,port));
						sok.configureBlocking(false);
						Selector sel=Selector.open();
						sok.register(sel, SelectionKey.OP_READ);
						reading=new Listener(sel);//与本类相链接以防回收
						sok.write(ByteBuffer.wrap(initpak));
						while(sok.isOpen()){//连接打开时无限心跳包
							if(beattime<=0){sok.write(ByteBuffer.wrap(beatpak)); beattime=23; }
							beattime--;
							try{Thread.sleep(1000);}catch(Exception e){}
						}
						init.obtainMessage(0).sendToTarget();//sock已断开，尝试重启
						//此处需要无限判断是否有可用的网络连接，比如数据网和wifi，没网的时候就一直卡在这里
						//整个try也可以用无限循环包裹起来，可降低线程重启次数
					}catch(Exception e){ onWriteError(e); }
				}};
			start.start();
		}
	}
	SocketChannel sok;
	Listener reading;
	Thread start;
	/*int verify(){ int state=0;
		String debug=网络.获得数据("GET", "http://api.live.bilibili.com/User/getUserInfo", cookie, "");
		FSON j=new FSON(debug);
		if(j.canRead()){
			if(j.get("msg","fail").toLowerCase().equals("ok")){
				j=j.getObject("data");
				name=j.get("uname","神秘用户(=\u03c9=)");
				//lvl=j.get("user_level",0);
				state=2;
			}else state=1;
		}else state=0;
		return state;
	}*/
	class Listener extends Thread{//监听 线程//事实上这里面我自己只做了略微修改
		Selector s;
		public Listener(Selector sel){s=sel; this.start();}
		public void run() {
			try {
				while (s.select() > 0)
					for (SelectionKey sk : s.selectedKeys()) {//遍历每个有可用IO操作Channel对应的SelectionKey
						if (sk.isReadable()) {//如果该SelectionKey对应的Channel中有可读的数据
							SocketChannel sc = (SocketChannel) sk.channel();// 使用NIO读取Channel中的数据
							ByteBuffer buffer = ByteBuffer.allocate(1024);
							int len=sc.read(buffer);//len=已取出数据包的大小，如果过大则为-1
							buffer.flip();
							byte[]b=new byte[len];
							System.arraycopy(buffer.array(),0,b,0,len);
							onReceive(b,b.length);//将接收到的完整数据包发出
							sk.interestOps(SelectionKey.OP_READ);//为下一次读取作准备
						}
						s.selectedKeys().remove(sk);//删除正在处理的SelectionKey
					}
			}catch(Exception ex){ onReadError(ex); }//ReadError会使用主动关闭连接，然后使上线程走出循环并结束
		}
	}

	void onReceive(byte[]b,int len){//当前在 监听 线程中
		if(len>20){//心跳包的回调包大小是20，过滤掉心跳回调包
			byte[]inf=new byte[len-16];
			System.arraycopy(b,16,inf,0,inf.length);//丢弃所得包前16个非有效json数据
			try{
				FSON j=new FSON(new String(inf,"utf8"));
				if(j.canRead()){
					switch(j.get("cmd","").toLowerCase()){
						case "sys_msg"://电视
							String tvid=j.get("tv_id","");
							if(!tvid.isEmpty())
								init.obtainMessage(1,j.get("real_roomid",1024),0,tvid).sendToTarget();
							break;
						case "sys_gift":
							if(j.get("giftId",-1)>100)//祭典（以及以后的新活动？）发现此值可为103或102
								init.obtainMessage(2,j.get("real_roomid",1024),0).sendToTarget();
							break;
					}
					log(j.toString());//测试完的时候把这个删了
				}
			}catch(Exception e){}
		}lastheart=System.currentTimeMillis();
	}
	void onReadError(Exception e){//似乎还在 监听 线程内
		onWriteError(e);
		try{sok.close();}catch(Exception e2){}//关闭连接，等待连接自行重启
	}
	void onWriteError(Exception e){//似乎在 建立与保持 线程
		log(E.trace(e));
	}
	void log(String s){
		log.add(0,fmter.format(System.currentTimeMillis())+s);
		if(log.size()>49) log.remove(49);
		//生成通知(); 
		if(!s.startsWith("{")) //阻止调试信息
		init.obtainMessage(9,s).sendToTarget();
	}
	public void joinTV(final int real_roomid,final String tv_id){//仅限由handler进入
		new AsyncTask<Void,Integer,Void>(){
			@Override protected Void doInBackground(Void[]p){
				for(int i=0,len=signed.size();i<len;i++){
					User tmp=all.get(signed.get(i));
					String s=http("GET", "http://api.live.bilibili.com/gift/v2/smalltv/join?roomid=" + real_roomid + "&raffleId=" + tv_id, tmp.cok, "");
					FSON j=new FSON(s);
					if (j.canRead()) publishProgress(tmp.uid,j.get("code", 5001));
					else publishProgress(tmp.uid,408);
				}
				return null;
			}
			@Override protected void onProgressUpdate(Integer[]s){
				User tmp=all.get(s[0]);
				switch(s[1]){
					case 0: case -400: //0=加入成功；-400=已进入抽奖
						tmp.tvs++; onJoinSuccessful(0); break;
					case -401://登录信息过期
						tmp.onLogout();break;
					case 408://网络超时
						tip("网络超时，领电视失败啦"); break;
					case 5001://api过期
						tip("电视领取功能可能已经失效");
				}
			}
		}.execute();
	}
	public void joinRaff(final int real_roomid){//仅限由handler进入
		new AsyncTask<Void,Integer,Void>(){
			@Override protected Void doInBackground(Void[]p){
				if(signed.size()==0)return null;
				User tmp=all.get(signed.get(0));
				String s=http("GET", "http://api.live.bilibili.com/activity/v1/Raffle/check?roomid="+real_roomid, tmp.cok, "");
				FSON j=new FSON(s);
				if(j.canRead())
					switch(j.get("code",5001)){//判断第一次返回的状态码，比如登录凭证过期等
						case 0://正常
							try{//获取抽奖id时可能会报空指针？
								String raffid=j.getList("data").getObject(0).get("raffleId","-1");//RaffleId
								for(int i=0,len=signed.size();i<len;i++){
									tmp=all.get(signed.get(i));
									s = http("GET", "http://api.live.bilibili.com/activity/v1/Raffle/join?roomid=" + real_roomid + "&raffleId=" + raffid, tmp.cok+"\r\nReferer: http://live.bilibili.com/"+real_roomid, "");
									j = new FSON(s);
									if (j.canRead()) publishProgress(tmp.uid, j.get("code", 5001));
								}log("{ raff result"+s);return null;
							}catch(Exception e){}
							publishProgress(tmp.uid, 5002);return null;
						default:
							publishProgress(tmp.uid, j.get("code",5001));return null;
					}
				publishProgress(tmp.uid, 4081);return null;
			}
			@Override protected void onProgressUpdate(Integer[]s){
				User tmp=all.get(s[0]);
				switch(s[1]){
					case 0: case -400: //0=加入成功；-400=已进入抽奖
						tmp.raffs++; onJoinSuccessful(1); break;
					case -401://登录信息过期
						tmp.onLogout();break;
					case 4081://网络超时
						tip("网络超时，领祭典失败啦"); break;
					case 5001://api过期
						tip("祭典领取功能可能已经失效"); break;
					case 5002://api错误
						tip("api运行错误");
				}

			}
		}.execute();
	}
	String[]giftname={"电器x1","粮草x1"};
	void onJoinSuccessful(int what){
		生成通知("正在享受社区温暖");
		tip("正在为您取回快递："+giftname[what]);
	}
	/*void onLogout(){
		生成通知("登录凭证失效");
	}*/
	static final int NotificationID=0xf7a12b1b;//getHashCRC32("appforms.GetAllowance".getBytes());
	void 生成通知(){ 生成通知(""); }
	void 生成通知(String ext){
		int totaltv=0,totalraff=0;
		for(int i=0,len=added.size();i<len;i++){
			User tmp=all.get(added.get(i));
			totaltv+=tmp.tvs; totalraff+=tmp.raffs;
		}
		Notification n=new 通知(this)
			.提示("正在开门等快递和送温暖的")
			.标题(ext)
			.说明("总领取 电器x "+totaltv+" ,粮草x "+totalraff)
			.运行中(true).自动注销(false)
			.图标ID(gotinfo?R.drawable.gift1:R.drawable.gift1_off)
			.点击行为(new Runnable(){public void run(){showControls();}}).创建();
		this.startForeground(NotificationID,n);//
	}
	boolean alreadyShowControls=false;
	String[]m;
	void showControls(){
		if(alreadyShowControls)return;alreadyShowControls=true;
		java.util.List<String>l=new java.util.ArrayList<String>();
		l.add("退出挂机"); //l.add("注销(todo)");
		l.add("开发者运行日志");
		l.add("尝试读取多帐号");
		l.add("最后心跳:"+fmter.format(lastheart)+"\n名称 | 电视/粮草 | 状态");
		for(int i=0,len=added.size();i<len;i++){
			User tmp=all.get(added.get(i));
			l.add(tmp.name+//"\nID："+uid+
			  " "+tmp.tvs+"/"+tmp.raffs+
			  " "+(signed.contains(tmp.uid)?"有效":"已过期"));
		}
		//if(!gotinfo)l.add("手动重连(todo)");
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
			case 1:checklog();/*confirmSignOut();*/break;//todo
			case 2:
				String[]str=文本.读取(getExternalFilesDir("").getAbsolutePath()+"/coks.txt").split("\t");
				tonextcok:
				for(String s:str){//更新每个cok
					for(int i=0,len=added.size();i<len;i++){//检测每个已有的cok
						User tmp=all.get(added.get(i));
						if(s.toLowerCase().contains("dedeuserid="+tmp.uid)){
							tmp.onUpdate(s);
							continue tonextcok;
						}
					}
					addUser(s);//从已有的cok中找不到则添加之
				}
//			case 3:随机引用();break;
//			case 4:if(m.length==6){infotime=0;beattime=0;tip("将在10秒内重试。");}/*set time to zero*/
//				//alert(new String[]{"开发信息",debug},null,null,true);
//				break;//todo
		}m=null;alreadyShowControls=false;System.gc();
	}
	void addUser(String cok){ new User(cok); }
	void confirmExit(){
		alert(this,true,"确认退出？","",
			new String[]{"是","取消"},
			new Runnable(){public void run(){
				This.stopSelf();
			}});
	}
	void checklog(){
		m=log.toArray(new String[log.size()]);
		AlertDialog d=new AlertDialog.Builder(this)/*.setTitle("控制面板")*/
			.setCancelable(true).setItems(m, null).create();
		d.getWindow().setType(android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		d.show();
	}
	void tip(String s){tipper.setText(s);tipper.show();}

	static HashMap<Integer,User>all=new HashMap<>();
	static ArrayList<Integer>added=new ArrayList<>();
	static ArrayList<Integer>signed=new ArrayList<>();
	static class User{
		public User(String cookie){ onUpdate(cookie); }
		String cok,name; User this_User=this;
		int uid=0,tvs=0,raffs=0;
		public void check(){
			new AsyncTask<Void,Void,String>(){
				@Override protected String doInBackground(Void[]p){
					return http("GET","http://api.live.bilibili.com/live_user/v1/UserInfo/get_info_in_room?roomid=3",cok,"");
				}
				@Override protected void onPostExecute(String s){
					FSON j=new FSON(s);
					if (j.canRead()) {
						if (j.get("code", -1) == 0) {
							j = j.getObject("data").getObject("info");
							uid = j.get("uid", 0);
							name = j.get("uname", "");
							all.put(uid, this_User);
							if (!added.contains(uid))added.add(uid);
							if (!signed.contains(uid))signed.add(uid);This.log("更新成功" + uid);
						} else This.log("更新失败" + cok);
					}
				}
			}.execute();
		}
		public void onLogout(){ signed.remove(uid); }
		public void onUpdate(String cookie){ cok=cokfix(cookie); check(); };
		static String cokfix(String c){//<editor-fold desc="用此修复Cookies格式问题" defaultstate="collapsed">
			String tmp=c.trim().toLowerCase();
			if(!tmp.startsWith("cookie:")) c="Cookie: "+c;
			if(!tmp.endsWith(";")) c+=";";
			return c;
		}//</editor-fold>
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
	byte[]bytecopy(byte[]byte1,byte[]byte2){
		byte[]byte3=new byte[byte1.length+byte2.length];
		System.arraycopy(byte1, 0, byte3, 0, byte1.length);
		System.arraycopy(byte2, 0, byte3, byte1.length, byte2.length);
		return byte3;
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
	通知 标题(String 文本){标题=文本;return this;}
	通知 说明(String 文本){说明=文本;return this;}
	通知 提示(String 文本){提示=文本;return this;}
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
