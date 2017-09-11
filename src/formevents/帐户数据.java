/* Created by LEOR_Chn
 * License Copyright(R) LEOR_Chn (2014-2017)
 * LEOR_Chn Soft.
 */
package formevents;
import java.net.*;
import java.util.*;
import leorchn.lib.*;
import simplebili.App;
public class 帐户数据 {
	public static class Account{
		public Account(){}
		public Account(String json){
			try{
				data=json;
				FSON j=new FSON(json);
				if(!j.get("status",false))return;
				j=j.getObject("data");
				uid=j.get("mid",uid);
				username=j.get("name","");
				facepic=j.get("face","");
				coin=j.get("coins",coin);
				usable=true;
			}catch(Exception e){}
		}
		String username="",facepic="",data="",uid="0";//,place;//int male,rank,fans;long reg;
		double coin=0;
		boolean usable=false;
	}
	public static Account u=null;
	public static String cookie(){
		return 文本.读取(App.getContext().getFilesDir().getPath()+"/account.cookie");
	}
	public static void cookie(String data){
		文本.写出(data,程序事件.files+"/account.cookie","UTF8");
	}
	public static boolean 有效用户(){
		刷新用户信息();
		return u==null?false:u.usable && 验证cookie();//帐户对象如果不空，那么返回其的“可用”属性值
	}
	public static void 刷新用户信息(){
		new 程序事件.itf(){void r(){程序事件.用户名.setText("登录中...");}};
		String uid=找cookie("DedeUserID"),//此处还未获取到空间信息，因此需要从Cookies中找用户ID
			uidmd5=找cookie("DedeUserID__ckMd5"),
			SESS=找cookie("SESSDATA");
		String data=网络.获得数据("POST","http://space.bilibili.com/ajax/member/GetInfo",
			"Content-Type: application/x-www-form-urlencoded\n"+
			"Referer: http://space.bilibili.com/\n"+
			"Cookie: DedeUserID="+uid+"; DedeUserID__ckMd5="+uidmd5+"; SESSDATA="+SESS+"; ",
			"mid="+uid);//处理所返回的 JSON 数据，例如如果 UID 设为 3084436，那么返回
		//{"status":true,"data":{"mid":"3084436","name":"\u75af\u72c2\u5c0f\u745e\u745e","approve":false,"sex":"\u7537","rank":"10000","face":"http:\/\/i0.hdslb.com\/bfs\/face\/22c389e490efdd90c5840d945f44ee21666daf8c.png","coins":0,"DisplayRank":"10000","regtime":1390331604,"spacesta":0,"place":"\u5e7f\u897f\u533a \u67f3\u5dde\u5e02","birthday":"0000-03-05","sign":"\u6211\u7279\u4e48\u662f\u4e00\u4e2a\u6f0f\u6c14UP\u4e3b","description":"","article":0,"attentions":[3101867,11284967,39865998,14802662,807185,2177677,34184850,10795262,4691632,13842863,926677,13308108,26798384,70666,255468,2771237,3363113,686050,4292711,6332955,616129,423895,240163,18598829,899250,210070,562197,401642,221648,5581898,275981,777964,2019740,433351,374377,748709,546195,2728123,168598],"fans":1597,"friend":39,"attention":39,"level_info":{"next_exp":10800,"current_level":4,"current_min":4500,"current_exp":10426},"pendant":{"pid":0,"name":"","image":"","expire":0},"nameplate":{"nid":36,"name":"\u7ec8\u8eab\u5361","image":"http:\/\/i2.hdslb.com\/bfs\/face\/3599502e13044295e43a44ae44bb19cae7d0514a.png","image_small":"http:\/\/i0.hdslb.com\/bfs\/face\/6e2c3d68fcb89af046f2669cd0acfa5df55aa029.png","level":"\u7a00\u6709\u52cb\u7ae0","condition":"\u76f4\u64ad\u7528\u6237\u7b49\u7ea7>=40\u7ea7"},"official_verify":{"type":-1,"desc":""},"toutu":"bfs\/space\/dc02d22a718c1c436f1a355b3cd726b04098ef7d.jpg","toutuId":6,"theme":"default","theme_preview":"","im9_sign":"3c491113d8069eca14e5d75551faf388","playNum":173019}}
		u=new Account(data);
		if(u.coin==0)程序事件.提示("硬币数为零，可能会没有权限访问部分内容。");
	}
	public static String[] 用户名(){
		try{
			return new String[]{u.username,"硬币: "+u.coin};
		}catch(Exception e){}
		return new String[]{"",""};
	}
	public static void 下载头像(){
		网络.下载文件(u.facepic,"u"+u.uid+".png",new Runnable(){
			public void run(){帐户登录.刷新头像();程序事件.提示("头像已更新。");}
		});
	}
	static HashMap<String,String>cookiemap=new HashMap<String,String>();
	public static String 找cookie(String c){
		if(cookiemap.isEmpty())刷新cookiemap();
		if(!cookiemap.containsKey(c))return "";
		return cookiemap.get(c);
	}
	public static void 刷新cookiemap(){
		cookiemap.clear();
		for(String c:cookie().split("; "))
			cookiemap.put(c.split("=")[0], c.split("=")[1]);
	}
	public static boolean 验证cookie(){
		String[]findstr="DedeUserID,DedeUserID__ckMd5,SESSDATA".split(",");
		for(String s:findstr)
			if(!cookiemap.containsKey(s))return false;
		return true;
	}
	
	
}
