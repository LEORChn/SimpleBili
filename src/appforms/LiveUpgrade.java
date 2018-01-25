/* Created by LEOR_Chn
 * License Copyright(R) LEOR_Chn (2014-2017)
 * LEOR_Chn Soft.
 */
package appforms;
import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import java.math.*;
import java.util.*;
import leorchn.lib.*;

import static leorchn.lib.HttpRequest.*;
import android.view.*;
import java.security.*;

//<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
public class LiveUpgrade extends Service1 implements DialogInterface.OnClickListener{
	/*服务固定*/public LiveUpgrade(){super();}
	static LiveUpgrade This;
	//boolean gotinfo=false;
	//int speed=0,lvl=0,current=0,total=0,last=0;//Exp Default = k (1000x)
	String referer="",errmsg="";
	Toast tipper;
	PowerManager.WakeLock wl;
	@Override public void onCreate(){ This=this;
		PowerManager pm=(PowerManager)this.getSystemService(POWER_SERVICE);
		wl=pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE,"LEORChnLiveUpgrade");
		if(wl!=null)wl.acquire();
	}
	@Override public void onDestroy() {
		if(wl!=null)wl.release(); super.onDestroy();
	} 
	public int onStartCommand(Intent i, int f, int startId) {
		tipper=Toast.makeText(this,"",1); tipper.setGravity(Gravity.TOP,0,0);
		referer=i.getStringExtra("referer");if(referer==null)随机引用();
		String[]coks=i.getStringArrayExtra("cookie");
		init(coks);
		return Service.START_NOT_STICKY;
	}
	void init(final String...coks){//主线程
		new Msgbox("B站改版通知",
			"由于B站改版，在2018年1月16日后随时停止任何渠道的在线经验，并且插件所反映的数据会出现异常。在2017年开通老爷至今未过期的用户不受影响，但插件作者已无法继续更新插件的下一个版本，因此当前版本将是最后一个版本，有缘再见。",
			"继续使用");
		Notification n=new 通知()
			.提示("正在初始化")
			.标题("挂机进程正在初始化")
			.说明(string("正在验证 ",coks.length," 个帐号"))
			.运行中(true).自动注销(false)
			.图标ID(android.R.drawable.presence_away).创建();
		this.startForeground(1,n);//6278891
		start=new Thread(){public void run(){
			for(String cookie:coks){
				User tmp=new User(cookie);
				try{ while(tmp.stat==0)Thread.sleep(200); }catch(Exception e){}
			}start=null;
			This.start(); //在添加user成功之后再start，以防异步错误
			生成通知();
		}};
		start.start();
	}
	int infotime=20,beattime=0;//onStartTimeing
	void start(){
		if(start==null||!start.isAlive()){
			start=new Thread(){public void run(){//链接到本类以防回收
					while(true){
						if(infotime<=0){ 查询信息();infotime=30; }
						if(beattime<=0){ heartBeat();beattime=5; }
						infotime--;beattime--;
						try{ Thread.sleep(10000); }catch(Exception e){}
					}
				}};
			start.start();
		}
	}
	Thread start;//线程不能在结束后再次直接启动，已放在方法内重建
	void 查询信息(){
		for(int i=0;i<signed.size();i++){
			User tmp=all.get(signed.get(i));
			tmp.check();//防止第一次检查时重复检查
			try{ while(tmp.stat==0)Thread.sleep(200); }catch(Exception e){}//这块修改下stat的值
		}
		生成通知();
	}
	void heartBeat(){
		
		String prms="appkey=c1b107428d337928",checksum=md5(string(prms,"ea85624dfcf12d7cc7b2b3a94fac1f2c")).toLowerCase();
		for(int i=0,len=signed.size();i<len;i++)
			http("GET",string("http://live.bilibili.com/mobile/userOnlineHeart?",prms,"&sign=",checksum),
				 all.get(signed.get(i)).cok+"\r\nUser-Agent: Mozilla/5.0 BiliDroid/4.11.7 (bbcallen@gmail.com)","");
	}
	static String md5(String s){
		try{
			MessageDigest m=MessageDigest.getInstance("MD5");
			return bytestohex(m.digest(s.getBytes("UTF-8")));
		}catch(Exception e){}
		return "";
	}
	static String bytestohex(byte[] bytes) {
		StringBuffer md5str = new StringBuffer();
		// 把数组每一字节换成16进制连成md5字符串
		int digital;
		for(int i = 0;i < bytes.length;i++){
			digital = bytes[i];
			if(digital < 0) digital += 256;
			if(digital < 16) md5str.append("0");
			md5str.append(Integer.toHexString(digital));
		}
		return md5str.toString().toUpperCase();
	}
	
	void 生成通知(){
		User tmp=all.get(added.get(0));
		StringBuilder strb=new StringBuilder(),str2=new StringBuilder();
		if(signed.size()>1)string(strb,"(",signed.size(),"/",added.size(),") ");
		string(strb,tmp.name);
		if(signed.size()>0)string(strb,"：",tmp.speed,"k XP/h"); else string(strb,errmsg);
		//-----
		string(str2,"Lv.",tmp.lvl," ",tmp.current,"k/");
		if(tmp.total<10000) string(str2,tmp.total,"k"); else string(str2,tmp.total/1000,"m");
		string(str2," ",需求经验转时间(tmp.last,tmp.speed));
		//-----
		Notification n=new 通知()
			.提示("正在后台挂机获取直播经验")
			.标题(strb.toString())
			.说明(str2.toString())
			.运行中(true).自动注销(false)
			.图标ID(signed.size()>0?android.R.drawable.presence_online:android.R.drawable.presence_offline)
			.点击行为(new Runnable(){public void run(){showControls();}}).创建();
		this.startForeground(1,n);//6278891
	}
	boolean alreadyShowControls=false;
	String[]m;
	void showControls(){
		if(alreadyShowControls)return;alreadyShowControls=true;
		java.util.List<String>l=new java.util.ArrayList<String>();
		l.add("退出挂机"); //l.add("");//注销(todo)");
		l.add("名称\t | 等级 | 升级时间 | 经验值/进度");
		StringBuilder strb=new StringBuilder();
		for(int i=0,len=added.size();i<len;i++){
			User u=all.get(added.get(i));
			string(strb,"\n",u.name," | ",u.lvl," | ",需求经验转时间(u.last,u.speed)," | ",u.current*100/u.total,"%\n\t\t",
				u.current,"k / ",u.total,"k");
		} string(strb,"\n以上共 ",added.size()," 个帐号");
		l.add(strb.toString());
		l.add("刷新随机参数("+referer+")");
		l.add("手动重连");
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
			case 4:if(m.length==6){infotime=0;beattime=0;log("将在10秒内重试。");}/*set time to zero*/
				//alert(new String[]{"开发信息",debug},null,null,true);
				break;//todo
		}m=null;alreadyShowControls=false;start();System.gc();
	}
	void confirmExit(){
		new Msgbox("确认退出？","",
			"是","取消"){
			@Override void onClick(int i){
				if(i==vbno)return;
				This.stopForeground(true);
				This.stopSelf();
		}};
	}
	void log(String s){
		tipper.setText(s);tipper.show();
	}
	
	void 随机引用() {
		String[] backReferer = {"295460", "945012", "1017", "1024"};
		referer = backReferer[new java.util.Random().nextInt(backReferer.length)];
		log("当前随机参数为 " + referer);
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
	//static void pl(String s){This.log(s);System.out.println(s);}
	static HashMap<String,User>all=new HashMap<>();
	static ArrayList<String>added=new ArrayList<>();
	static ArrayList<String>signed=new ArrayList<>();
	static class User{
		public User(String cookie){ onUpdate(cookie); }
		String cok,name; User this_User=this;
		int stat=0, uid=0,speed=0,lvl=0,current=0,total=0,last=0;
		AsyncTask checking;
		public void update(){
			check();
		}
		public void check(){
			checking=new AsyncTask<Void,Void,String>(){
				@Override protected String doInBackground(Void[]p){
					return http("GET", "http://api.live.bilibili.com/User/getUserInfo", cok, "");
				}
				@Override protected void onPostExecute(String s){
					FSON j=new FSON(s);
					if(j.canRead()){
						if(j.get("msg","fail").toLowerCase().equals("ok")){
							j=j.getObject("data");
							name=j.get("uname","");
							
							speed=Math.max((j.get("vip",0)==0?36:72), (j.get("svip",0)==0?36:90));
							lvl=j.get("user_level",0);
							current=(j.get("user_intimacy",0)/1000);
							total=(j.get("user_next_intimacy",0)/1000);
							last=total-current;
							if(stat==0){
								all.put(name, this_User);
								if (!added.contains(name)) added.add(name);
								if (!signed.contains(name)) signed.add(name);
								This.log(string(name," 更新成功"));
							}
							stat=1;
							return;
						}else if(stat==1) onLogout();
					}else if(stat==0)This.log("更新失败" + cok);
					stat=-1;
				}
			}.execute();
		}
		public void onLogout(){ signed.remove(uid); }
		public void onUpdate(String cookie){ cok=cokfix(cookie); update(); };
		static String cokfix(String c){//<editor-fold desc="用此修复Cookies格式问题" defaultstate="collapsed">
			String tmp=c.trim().toLowerCase();
			if(!tmp.startsWith("cookie:")) c="Cookie: "+c;
			if(!tmp.endsWith(";")) c+=";";
			return c;
		}//</editor-fold>
	}
}

