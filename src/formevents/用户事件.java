/* Created by LEOR_Chn
 * License Copyright(R) LEOR_Chn (2014-2017)
 * LEOR_Chn Soft.
 */
package formevents;
import appforms.*;
import static leorchn.lib.Global.*;
import android.app.*;
import android.content.*;
//import com.e4a.runtime.components.impl.android.面板Impl;
import static formevents.程序事件.*;
import static leorchn.lib.WidgetOverride.*;
public class 用户事件 {
	public static void 网页开始载入(String 网址){
		if(网址.contains("m.bilibili.com")){
			关闭进度对话框();
			visible(登录器,false);
			用户名.setText("加载帐户信息...");
			显示进度对话框("请稍候");
		}
	}
	public static void 网页载入完成(String 网址){
		if(网址.contains("m.bilibili.com")){
			关闭进度对话框();
			登录器.loadUrl("about:blank");
			帐户登录.更新帐户();
		}else if(网址.contains("about:blank")){
			关闭进度对话框();
		}
	}
	public static void 用户头像_点击(){
		调试("",列.debug());
	}
	/*public static void 进入详情页(String aid){
		//详情页.可视(true);
		封面.图像(cache+"/av"+aid+".png");
		视频标题.内容(aid+"测试内容 测试内容 测试内容 测试内容 测试内容 而且两行");
		//初始化界面.详情页_重绘(程序事件.详情页,程序事件.封面,程序事件.预览,程序事件.视频标题,程序事件.st);
	}*/
	public static void 列表滚动(int first,int count){
		for(int i=0;i<程序事件.列.size();i++)
			if(i<first || i>first+count){
				列.setcover(i,"");
			}else{
				列.setcover(i,cache+"/av"+列.getvid(i)+".png");
			}
	}
	public static void 用户名_点击(){
		帐户数据.Account u=帐户数据.u;
		if(u != null)
			if(信息框2("帐户信息",
				"用户ID："+u.uid+
				"\n用户名："+u.username+
				"\n硬币数量："+u.coin
				,"关闭","开发信息")==1)
				程序事件.调试("开发信息 - 取得帐户信息",u.data);
	}
	public static void 用户名_长按(){
		if(信息框2("注销","真的确定要注销帐号？\n\n注销将重启本程序。","确定注销","取消")==0){
			android.webkit.CookieManager.getInstance().removeAllCookie();
			new java.io.File(程序事件.files+"/account.cookie").delete();
			信息框("注销","注销成功。","重启程序");
			Activity app=Main_Feeds.getContext();
			重启程序1(app);重启程序2(app);
			信息框("重启失败","请手动重启本程序。","关闭程序");
			app.finish();
		}
	}
	static void 重启程序1(Activity app){
		try{
			final Intent intent = app.getPackageManager().getLaunchIntentForPackage(app.getPackageName());
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			app.startActivity(intent);
		}catch(Exception e){}
	}
	static void 重启程序2(Activity app){
		try{
			ActivityManager am=(ActivityManager)app.getSystemService(Context.ACTIVITY_SERVICE);
			am.restartPackage(app.getPackageName());
		}catch(Exception e){}
	}
}
