/* Created by LEOR_Chn
 * License Copyright(R) LEOR_Chn (2014-2017)
 * LEOR_Chn Soft.
 */
package formevents;

import android.webkit.CookieManager;
import android.webkit.WebView;
import appforms.*;
import java.io.File;
import java.util.HashMap;
import simplebili.lib.*;
import android.widget.*;
import android.view.*;
import static leorchn.lib.WidgetOverride.*;
import static leorchn.lib.Global.*;
import static formevents.程序事件.*;
import android.os.*;
public class 帐户登录 {
	//static ImageView 头像;static TextView 用户名;
	//static WebView 登录器;static FeedListControl 列;
	//public static void 读取组件(ImageView h,TextView n,WebView l,FeedListControl s){头像=h;用户名=n;登录器=l;列=s;}
	public static void 重新登录(){//由新线程的Startup及新线程的“验证帐户”执行的重新登录
		new itf(){void r(){
		visible(登录器,true);
		列.visible(false);
		//登录器.clearCache(true);登录器.clearFormData();
		HashMap<String,String>extHeader=new HashMap<String,String>();
		//extHeader.put("User-Agent","AppleWebKit/537.36 Mobile");
		extHeader.put("Referer", "http://m.bilibili.com/space.html");
		登录器.loadUrl("https://passport.bilibili.com/login", extHeader);
		}};
	}
	public static void 更新帐户(){
		new itf(){void r(){
		帐户数据.cookie(CookieManager.getInstance().getCookie("bilibili.com"));
		if(验证帐户())帐户数据.下载头像();
		登录器.clearCache(true);
		登录器.clearFormData();
		File f2=new File(Main_Feeds.getContext().getDir("webview",0).getAbsolutePath()+"/Cache/");
		if(f2.exists()&& f2.isDirectory()){
			for(File f:f2.listFiles()){
				f.delete();
			}程序事件.提示("浏览器缓存已清除。");
		}
		程序事件.刷新帐号信息();}};
	}
	public static boolean 验证帐户(){//由 Startup 启动，新线程中执行
		if(!帐户数据.有效用户()){
			程序事件.提示("错误 登录失败，帐户有效性验证失败。\n"+帐户数据.u.data);
			//Looper.getMainLooper();Looper.loop();
			帐户数据.cookie("");
			重新登录();
			return false;
		}return true;
	}
	public static void 读取帐户(){//由 Startup 启动，新线程中执行
		new 程序事件.itf(){void r(){
			visible(登录器,false);
			登录器.loadUrl("about:blank");
			登录器.freeMemory();
			列.visible(true);
			String[]userinf=帐户数据.用户名();
			用户名.setText(userinf[0]);
			用户消息.setText(userinf[1]);
		}};
		if(!new File(程序事件.cache+"/u"+帐户数据.u.uid+".png").exists()){
			帐户数据.下载头像();
		}else{
			刷新头像();程序事件.提示("清除缓存并重启程序以更新头像。");
		}
		/*new 程序事件.itf(){void r(){
			switch(信息框2("测试版本","请选择要参与的测试项目。\n选择Service之后，Activity会finish();","Service","Activity")){
				//case 0:程序事件.启动直播挂机();
				case 1:*/程序事件.刷新最新动态();
			/*}
		}};*/
	}
	public static void 刷新头像(){setImage(头像,程序事件.cache+"/u"+帐户数据.u.uid+".png");}
	//static void visible(View v,boolean b){程序事件.visible(v,b);}
	//static boolean visible(View v){return 程序事件.visible(v);}
}
