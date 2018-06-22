package simplebili.lib;
import leorchn.lib.*;
import java.io.File;

public class User extends Impl{
	public User(String path){Impl.init(path);}
	public static UserAddResult add(String cookie){return Impl.add(cookie);}//使用账户身份添加账户
	//浏览器进程，有进程隔离//0=正常,-1=网络异常,-11=识别码错误,-12=识别码失效
	public static boolean set(int afterUid){return Impl.set(afterUid);}//设置指定账户号为主账户
	public static void logout(int uid){Impl.logout(uid);}
	public static String getCookie(){return Impl.getCookie();}//获取主账户身份
	public static String getCookie(int uid){return Impl.getCookie(uid);}//根据账户号获取账户身份
	public static String[]getCookies(int...uid){return Impl.getCookies(uid);}//根据指定多个账户号获取账户身份
	public static int getUid(){return Impl.getUid();}//获取主账户号
	public static int getUid(int index){return Impl.getUid(index);}//根据排序获取主账户号
	public static int[]getUids(){return Impl.getUids();}//根据排序获取所有账户号
	public static String getName(){return Impl.getName();}//获取主账户名称
	public static String getName(int uid){return Impl.getName(uid);}//根据账户号获取账户名称
	//如果想使用 getName(index)，请用 getName(getUid(index));
	public static String[]getNames(int...uids){return Impl.getNames(uids);}//根据指定多个账户号获取账户名称
	//获取账户的名称列表： getNames(getUids());
	public static long getExpire(int uid){return Impl.getExpire(uid);}//从文件重载账户信息
	public static String getHead(int uid){return Impl.getHead(uid);}//从文件重载账户信息
	public static void load(){Impl.load();}//从文件重载账户信息
	public static void load(String s){Impl.load(s);}//从文件重载账户信息
	public static String getDefault(){return Impl.getDefault();}//载入默认账户信息
	public static boolean save(){return Impl.save();}//保存账户信息到文件
	public static String path(){return Impl.path();}//从外部获取账户信息路径
	
	public static interface OnUserChangedListener{
		void onUserChanged(int uid,String cookie,String name)
	}
	public static class UserAddResult{
		public int status;
		public FSON users;
		public UserAddResult set(int stat){
			status=stat;
			return this;
		}
		public UserAddResult set(int stat,FSON usr){
			users=usr;
			return set(stat);
		}
	}
}
class Impl{
	static User.OnUserChangedListener listener;
	static FSON user;
	static int uid=0;
	static String mcok,name,
		path="";
	public static void init(String pth){
		path=pth;
		load();
	}
	public static User.UserAddResult add(String cok){
		User.UserAddResult r=new User.UserAddResult();
		try{
			if(!cok.toLowerCase().startsWith("cookie:"))cok="Cookie: "+cok;
			if(cok.contains("SESSDATA=") && cok.contains("%"));else return r.set(-11);//cookie格式无效
			String s=new Activity1.Http("get","http://api.bilibili.com/nav",cok,"").waitfor();
			FSON j=new FSON(s),u=new FSON("{}"),us=user.getObject("users"),od=user.getList("order");
			if(!j.canRead()) return r.set(-1);//网络异常
			j=j.getObject("data");
			boolean usable=j.get("isLogin",false);
			if(!usable)return r.set(-12);//cookie不可用
			long d=Long.valueOf(cok.split("SESSDATA=")[1].split("%(.){2}")[1]);//获取身份过期时间，有可能会异常跳出
			u.set("l",usable);//isLogin
			u.set("n",j.get("uname","神秘用户"));//Name
			u.set("h",j.get("face",""));//userHeadpicture
			u.set("c",cok);//userCookie
			u.set("d",d);//过期时间
			u.set("u",false);//userisUpdated
			
			int uid=j.get("mid",0);
			boolean update=false;//检测是添加新用户，还是更新已有用户的信息
			for(int i=0,len=od.length();i<len;i++)
				if(od.get(i,0) == uid)
					update=true; //确认是更新已有信息，无需更改账户顺序
			if(!update) od.add(uid); //确认是添加新用户，顺序加到最底部
			us.set(String.valueOf(uid),u);//把用户数据复制到上层集合，也就是data中
			user.set("order",od);//更新账户顺序
			user.set("users",us);//更新账户集合
			if(user.get("main",0)==0) user.set("main",od.get(0,0));//如果第一次添加账户，设置为主账户
			//icon.putusr(uid,face); // 本来是想在加载完信息时顺便下载头像图片，但是在加载完信息后浏览器进程会立刻被杀，图片就无法成功下载
			return r.set(1,user);
		}catch(Throwable e){
			return r.set(-11);
		}
	}
	public static boolean set(int newuid){
		FSON u=user.getObject("users").getObject(String.valueOf(newuid));
		boolean valid=(u instanceof FSON);
		if(valid){
			uid=newuid;
			user.set("main",newuid);
		}
		mcok=valid?u.get("c",""):"Cookie: ";
		name=valid?u.get("n",""):"未登录";
		syncToActivity();
		return valid;
	}
	public static void logout(int inuid){
		boolean reset=uid==inuid;//如果删除主帐户
		
	}
	static void syncToActivity(){
		Activity1.mcok=mcok;
		Activity1.muid=uid;
	}
	public static String getCookie(){return mcok;}
	public static String getCookie(int uid){
		FSON us=user.getObject("users");
		FSON su=us.getObject(uid);
		return(su instanceof FSON)?su.get("c",""):"";
	}
	public static String[]getCookies(int...uids){
		String[]s=new String[uids.length];
		FSON us=user.getObject("users");
		for(int i=0,len=uids.length;i<len;i++){
			FSON su=us.getObject(String.valueOf(uids[i]));
			s[i]=(su instanceof FSON)?su.get("c",""):"";
		}return s;
	}
	public static int getUid(){return uid;}
	public static int getUid(int index){
		try{
			return user.getList("order").get(index,0);
		}catch(Throwable e){
			return 0;
		}
	}
	public static int[]getUids(){
		FSON od=user.getList("order");
		int[]uid =new int[od.length()];
		for(int i=0,len=od.length();i<len;i++) uid[i]=od.get(i,0);
		return uid;
	}
	public static String getName(){return name;}
	public static String getName(int uid){return getNames(uid)[0];}
	public static String[]getNames(int...uids){
		String[]s=new String[uids.length];
		FSON us=user.getObject("users");
		for(int i=0,len=uids.length;i<len;i++){
			FSON su=us.getObject(String.valueOf(uids[i]));
			if(su instanceof FSON) s[i]=su.get("n","");
			else s[i]="";
		}return s;
	}
	public static long getExpire(int uid){
		FSON us=user.getObject("users");
		us=us.getObject(String.valueOf(uid));
		if(us instanceof FSON) return us.get("d",0);
		return 0;
	}
	public static String getHead(int uid){
		FSON us=user.getObject("users");
		us=us.getObject(String.valueOf(uid));
		if(us instanceof FSON) return us.get("h", "");
		return "";
	}
	public static void load(){
		if(new File(path).exists()){
			load(Text.read(path));
		}else{
			//首次打开时就会这样，如果为-1则直接进入设置页面，进入设置页面后再设为隐身模式
			//首次进入时不设置默认帐户为隐身模式，直接跳到设置页面
			load(getDefault());
		}
	}
	public static void load(String d){
		FSON u,us,od;
		u=new FSON(d);//u 变为整个文件的数据
		if(!u.canRead())
			u=new FSON(getDefault());//如果整个数据不可读，载入默认数据
		us=u.getObject("users");//us 变为所有用户的账户信息，不包含排序
		od=u.getList("order");//od 变为所有用户的账户排序
		/*for(int i=0,len=od.length();i<len;i++){//加载所有可用账户
			int id=od.get(i,0);
			FSON su=us.getObject(String.valueOf(id));
			Icon.putusr(id,su.get("h",""));
		}*/
		user=u;
		set(u.get("main",0));//加载主账户
	}
	public static String getDefault(){
		return "{'main':0,'order':[],'users':{}}";
	}
	public static boolean save(){
		return Text.write(user.toString(),path,"UTF8");
	}
	public static String path(){return path;}
}
