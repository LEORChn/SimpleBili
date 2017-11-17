/* Created by LEOR_Chn
 * License Copyright(R) LEOR_Chn (2014-2017)
 * LEOR_Chn Soft.
 */
package formevents;

import android.view.View;

import appforms.*;

import android.widget.*;
import android.webkit.*;
//import com.e4a.runtime.components.impl.android.n25.水平滑块条Impl;
import simplebili.lib.*;

import android.content.*;
import android.app.ActivityManager.RunningServiceInfo;
import leorchn.lib.*;
import static leorchn.lib.Global.*;
import static leorchn.lib.WidgetOverride.*;
import android.graphics.*;
import android.os.*;
public final class 程序事件 {

	static ImageView 头像,封面;
	static TextView 用户名,用户消息,视频标题;
	static WebView 登录器; static ProgressBar 登录器进度条;
	static FeedListControl 列;
	static int st = Global.取状态栏高度(), sh = Global.取屏幕高度(), sw = Global.取屏幕宽度();
	static String cache = Main_Feeds.getContext().getExternalCacheDir().getPath(),
		files = Main_Feeds.getContext().getFilesDir().getPath();

	public static void 初始化主页(ImageView h,TextView n,TextView msg,WebView l,ProgressBar p,FeedListControl s){
		头像 = h;用户名 = n;用户消息=msg;登录器 = l;登录器进度条=p;列 = s;
		init(l,p); init_userhead(h); init(s);
		//帐户登录.读取组件(h,n,l,s);
	}
	public static void 初始化详情页(ImageView h,TextView t){
		封面=h;视频标题=t;
		
	}
	public static void 刷新帐号信息() {
		new T(){void r(){
			if (帐户数据.cookie().equals("")) {
				帐户登录.重新登录();
			} else {//已登录
				if (帐户登录.验证帐户()) {
					帐户登录.读取帐户();
				}
			}
		}};
	}
	public static void 刷新最新动态(){
		new itf(){void r(){
			列.clear();
		}};
		int refres=读取关注动态(1);
		if(refres!=0)提示("刷新动态失败，错误码 "+refres+"\n"+错误信息_读取动态(refres));
		if(refres==-101 && 信息框2("帐号验证失效","要重新登录吗？","重新登录","下次")==0)帐户登录.重新登录();
	}
	public static String 错误信息_读取动态(int code){
		String[]s={"正常",//0
			"服务器验证身份失败",//-101
			"无法从服务器获取数据，检查网络连接",//1000
			"从服务器获取的数据不可读取",//1001
			"程序无法理解服务器返回的数据，可能程序版本太旧"};//1002
		int[]i={0,-101,1000,1001,1002};
		for(int i2=0;i2<i.length;i2++) if(i[i2]==code) return s[i2]; return "";
	}
	//static java.util.HashMap<String,int>cover=new java.util.HashMap<String,int>();
	public static int 读取关注动态(int page) {
		String data=网络.获得数据("GET","http://api.bilibili.com/x/web-feed/feed?ps=20&pn="+page,"Cookie: DedeUserID="+帐户数据.找cookie("DedeUserID")+"; SESSDATA="+帐户数据.找cookie("SESSDATA")+"; ", "");
		if(data.isEmpty())return 1000;
		FSON j=new FSON(data);
		if(!(j.canRead() && j.isObject())){
			调试("1001",data);return 1001;
		} int runtime=0;
		try{
			int execResult=j.get("code",1002);//read the new video update
			if(execResult !=0)return execResult;//data wrong, for example -101
			FSON j2=j.getList("data");
			for(int i=0,len=j2.length();i<len;i++){
				final FSON d=j2.getObject(i)
					.getObject("archive"),
				d2=d.getObject("stat");
				final String vid=d.get("aid","-1"),
					uid=d.getObject("owner").get("mid","-1");
				mainhandler.obtainMessage(2,new Runnable(){public void run(){
					列.additem(d.getObject("owner").get("name","UP主信息错误"),
						时间.动态时间差(d.get("pubdate",0)),
						d.get("title","标题信息错误"),
						d.get("pic",""),
						d2.get("view","0"),
						d2.get("danmaku","0"),
						uid,
						vid);
				}}).sendToTarget();
				runtime=i;
				//	列表封面(vid,d.get("pic",""));//ˉ↓
			}
			mainhandler.obtainMessage(2,new Runnable(){public void run(){
				列.refresh();
			}}).sendToTarget();
		}catch(Exception nfe){//NumberFormatException
			final String detail=runtime+"ran times:"+j.toString()+java.util.Arrays.toString(nfe.getStackTrace());
			mainhandler.obtainMessage(2,new Runnable(){public void run(){
				int chose=信息框2("Error 1002",detail,"忽略错误并继续","复制错误信息");
				if(chose==1){ 复制文本(detail); tip("复制成功"); }
			}}).sendToTarget();
			return 1002;
		}
		return 0;
	}
	public static void 列表封面(final String aid,String url){
		if(new java.io.File(cache+"/av"+aid+".png").exists()){
			for(int i=0;i<列.size();i++)
				if(i<10 && 列.getvid(i).equals(aid)){//限制初始化时最多加载10张图片
					列.setcover(i,cache+"/av"+aid+".png");
					return;
				}
		}else if(!url.isEmpty()){
			网络.下载文件(url,"av"+aid+".png",new Runnable(){public void run(){
				压缩图片2(cache+"/av"+aid+".png", cache+"/av"+aid+".png", 1);
				列表封面(aid,"");
			}});
		}
	}
	/* 0=正常
	 * -101=服务器验证身份失败
	 * 1000=无法从服务器获取数据，检查网络连接
	 * 1001=从服务器获取的数据不可读取
	 * 1002=程序无法理解服务器返回的数据，可能程序版本太旧
	 */
	/*public static void 启动直播挂机(){
		String uid=帐户数据.找cookie("DedeUserID"),//此处还未获取到空间信息，因此需要从Cookies中找用户ID
			uidmd5=帐户数据.找cookie("DedeUserID__ckMd5"),
			SESS=帐户数据.找cookie("SESSDATA");
		Intent i=new Intent(mainActivity.getContext(),可弹窗服务.class);
		i.putExtra("cookie","DedeUserID="+uid+"; DedeUserID__ckMd5="+uidmd5+"; SESSDATA="+SESS+"; ");
		i.putExtra("uid", uid);
		if(!服务已开启(可弹窗服务.class))开启服务(i);
		mainActivity.getContext().finish();
	}
	public static boolean 服务已开启(Class 服务类){
		String 本包名=mainActivity.getContext().getPackageName();
		java.util.List<RunningServiceInfo> 运行中服务列表 =((android.app.ActivityManager)mainActivity.getContext().getSystemService("activity")).getRunningServices(300);
		boolean 服务已运行=false;
		for(RunningServiceInfo rsi : 运行中服务列表){
			ComponentName 服务组件 = rsi.service;
			if(本包名.equals(服务组件.getPackageName()))
				if(服务类.equals(服务组件.getClass()))
					return true;
		}
		return false;
	}
	public static boolean 开启服务(Intent i){
		try{
			mainActivity.getContext().startService(i);
		}catch(Exception e){
			return false;
		}
		return true;
	}*/
	public static void 调试(String title,String content){mainhandler.obtainMessage(1,new String[]{title,content}).sendToTarget();}
	public static void 提示(String s){mainhandler.obtainMessage(0,s).sendToTarget();}
	public abstract static class itf /*implements Runnable*/{
		abstract void r();
		public itf(){
			mainhandler.post(new Runnable(){public void run(){r();}});
		}
	}
	static Handler mainhandler=new Handler(Looper.getMainLooper()){
		@Override public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
				case 0:tip("简哔提示：\n"+msg.obj);break;
				case 1:信息框(((String[])msg.obj)[0],((String[])msg.obj)[1],"OK");break;
				case 2:((Runnable)msg.obj).run();
			}
		} 
	};
	public abstract static class T{
		abstract void r();
		public T(){
			new Thread(new Runnable(){public void run(){
				r();
			}}).start();
		}
	}
}
