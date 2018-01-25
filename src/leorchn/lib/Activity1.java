package leorchn.lib;
import android.os.*;
import android.app.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import com.LEORChn.SimpleBili.R;
import appforms.*;
import simplebili.App;
import android.graphics.Bitmap;
public abstract class Activity1 extends Activity implements MessageQueue.IdleHandler,Thread.UncaughtExceptionHandler,View.OnClickListener{
	protected static Icon icon;
	protected static R.id id;
	protected static R.layout layout;
	protected static R.drawable drawable,draw;//可以给 R 类定义快捷方式和多个别名而且不用额外声明，超爽
	protected static R.color color;
	
	public static final String UA_win="Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36",
	UA_android="Mozilla/5.0 (Linux; Android 4.4.4;) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2307.2 Mobile Safari/537.36",
	UA_mobilebili="Mozilla/5.0 BiliDroid/4.11.7 (bbcallen@gmail.com)";
	
	public static String DIR_cache=App.getContext().getExternalCacheDir().getPath()+"/",
	DIR_data=App.getContext().getFilesDir().getPath()+"/",
	DIR_cache_pic=DIR_cache+"pic/",
	DIR_cache_log=DIR_cache+"log/";
//-----
	public static FSON defSetting=new FSON(appforms.Settings.getDefaultSetting()),
						sets=setsload(), user=userload();//顺序不能改，必须先初始化默认配置然后再加载用户配置
	public static String setspath(){ return DIR_data+"app.setting"; }
	public static FSON setsload(){//from setting file
		String s=Text.read(setspath());
		FSON j=new FSON((s==null || s.isEmpty())?"{}":s),//载入当前配置
			ds=defSetting;//载入默认配置
		for(int i=0,len=ds.length();i<len;i++){//向后兼容、校验配置文件的完整性
			FSON each=ds.getList(i);
			if(!j.exists(each.get(1,"")))
				j.set(each.get(1,""),each.get(2,new Object()));//校验失败时添加默认配置值
		}
		return j;
	}
	public static boolean setssave(){ return Text.write(sets.toString(),setspath(),"UTF8"); }
//-----
	public static int muid;
	public static String mcok;//="Cookie: ";
	public static int useradd(String cok){//浏览器进程，有进程隔离//0=正常,-1=网络异常,-11=识别码错误,-12=识别码失效
		try{
			if(cok.contains("SESSDATA=") && cok.contains("%"));else return -11;//cookie格式无效
			//Text.write(cok,DIR_cache+"tempcok","utf8");
			Http h=new Http("get","http://api.bilibili.com/nav",cok,"");
			while(!h.isfin)try{ Thread.sleep(500); }catch(Exception e){}
			String s=h.get(),face;
			FSON j=new FSON(s),u=new FSON("{}"),us=user.getObject("users"),od=user.getList("order");
			if(!j.canRead()){
				//new Msgbox("-1",s,"ok");
				return -1;
			}//网络异常
			j=j.getObject("data");
			boolean usable=j.get("isLogin",false);
			if(!usable)return -12;//cookie不可用
			long d=Long.valueOf(cok.split("SESSDATA=")[1].split("%(.){2}")[1]);//有可能会异常跳出
			int uid=j.get("mid",-1);
			face=j.get("face","");
			u.set("l",usable);//isLogin
			//u.set("i",j.get("mid", 0));//userId
			u.set("n",j.get("uname","神秘用户"));//Name
			u.set("h",face);//userHeadpicture
			u.set("c",cok);//userCookie
			u.set("d",d);
			u.set("u",false);//userisUpdated
			
			boolean update=false;
			for(int i=0,len=od.length();i<len;i++)
				if(od.get(i,0) == uid)
					update=true; //tip(string(od));}
			if(!update) od.add(uid);
			us.set(string(uid),u);
			/*for(int i=0,len=us.length();i<len;i++){
				if(Objects.equals(us.getObject(i).get("i",0), u.get("i",0))){//比较id，更新帐号
					us.set(i,u);
					update=true;
					break;
				}
			}*/
			user.set("order",od);
			user.set("users",us);
			if(user.get("main",-1)==0) user.set("main",1);
			//icon.putusr(uid,face); // 本来是想在加载完信息时顺便下载头像图片，但是在加载完信息后浏览器进程会立刻被杀，图片就无法成功下载
			//new File(DIR_cache+"tempcok").delete();
		}catch(Exception e){
			return -11;
		}return 0;
	}
	public static boolean usersave(){ return Text.write(user.toString(),userpath(),"UTF8"); }
	public static String userpath(){ return DIR_data+"user.info"; }
	public static FSON userload(){
		if(new File(userpath()).exists()){
			FSON u,us,od;
			u=new FSON(Text.read(userpath()));
			if(!u.canRead()) u=new FSON(userdef());
			us=u.getObject("users");
			od=u.getList("order");
			for(int i=0,len=od.length();i<len;i++){
				int id=od.get(i,0);
				FSON su=us.getObject(string(id));
				//(string(id,"\n",su.get("h","")));
				icon.putusr(id,su.get("h",""));
				if(u.get("main",0)==i){
					userset(id,u);//加载主帐户配置
					//break;
				}
			}
			return u;
		}else{
			icon.putusr(0,"");
			muid=-1;//首次打开时就会这样，如果为-1则直接进入设置页面，进入设置页面后再设为隐身模式
			//首次进入时不设置默认帐户为隐身模式，直接跳到设置页面
		}
		return new FSON(userdef());
	}
	public static boolean userset(int uid){//加载主帐户配置，也可以用它切换主帐号。参数 ( uid )
		return userset(uid,user);
	}
	private static boolean userset(int touid,FSON user){//初始化时使用的加载主帐户配置方法
		FSON od=user.getList("order");
		for(int i=0,len=od.length();i<len;i++){
			int id=od.get(i,0);
			if(id == touid){
				muid=touid;
				mcok=user.getObject("users").getObject(string(id)).get("c","");
				user.set("main",i);
				return true;
			}
		}
		/*for(int i=0,len=us.length();i<len;i++){
			FSON su=us.getObject(i);
			if(su.get("i",0)==uid){
				muid=uid;
				mcok=su.get("c","");
				user.set("main",i);
				return true;
			}
		}*/
		return false;
	}
	public static String userdef(){ return "{'main':0,'order':[0],'users':{'0':{'u':true,'d':0,'c':'','n':'隐身模式','l':true,'h':''}}}";}
	/*public static void userload(String s){//想的是如果没有隐身模式才用这个
		FSON u=new FSON(s),us,dfu;
		if(!u.canRead())return;
		us=u.getList("users");
		if(us==null)return;//防止我的意外
		dfu=us.getObject(u.get("main",0));
		if(dfu==null || dfu.get("c","").equals("")) mcok="";//默认帐户无效
		if(dfu.get("c","").equals(""))return;
		user=u;
		
	}*/
//-----
	static int[]getuidexceptzero(){
		FSON od=user.getList("order");
		int[]uid =new int[od.length()-1];
		for(int i=1,len=od.length();i<len;i++) uid[i-1]=od.get(i,0);
		return uid;
	}
	static boolean[]plugcfg(String name,int[]uid){//从配置文件里获取初始设置
		FSON od=user.getList("order");
		if(uid.length==0) return new boolean[0];//没有登录任何帐号
		boolean[]b=new boolean[uid.length];

		String path=string(DIR_data,"plogin/",name,".loginusers");
		String s=Text.read(path);
		FSON j=new FSON(s);
		if(!j.canRead()){
			j=new FSON("[]");
			for(int single:uid) j.add(single);//未设定时默认全体激活
			s=j.toString();
			Text.write(s,path,"utf-8");
		}
		for(int i=0,len=uid.length;i<len;i++)//外层 遍历用户
			for(int i2=0;i2<b.length;i2++)//内层 遍历配置
				if(j.get(i2,0)==uid[i]){
					b[i]=true;
					break;
				}
		return b;
	}
	static String[]uid2name(int...ids){
		String[]s=new String[ids.length];
		FSON us=user.getObject("users");
		for(int i=0,len=ids.length;i<len;i++){
			FSON su=us.getObject(string(ids[i]));
			if(su instanceof FSON) s[i]=su.get("n","");
			else s[i]="";
		}return s;
	}
	String[]uid2cookie(int...ids){
		String[]s=new String[ids.length];
		FSON us=user.getObject("users");
		for(int i=0,len=ids.length;i<len;i++){
			FSON su=us.getObject(string(ids[i]));
			if(su instanceof FSON) s[i]=su.get("c","");
			else s[i]="";
		}return s;
	}
	public void 附加功能(final Activity1 a){
		if(! PermissionGuide.isCanNotify()){
			final File t=new File(DIR_data+"tip_notifycation");
			if(t.exists()){ extmodule(a); return; }
			new Msgbox("通知栏权限","插件需要使用通知栏权限，以便您查看和控制插件的使用情况。\n点击左侧 “显示通知” 方框打勾 \u2611 后即可使用。",
				"前往设置","不再提示","忽略"){
				@Override void onClick(int i){
					if(i==vbyes){
						PermissionGuide.tosetting(a);
					}else if(i==vbno){
						try{ t.createNewFile(); }catch(Exception e){}
					}else if(i==vbmid){
						extmodule(a);
					}
				}
			};
			return;
		}else extmodule(a);
	}
	public void extmodule(final Activity a){
		//String cook=Activity1.mcok;
		//if(cook==null)cook="";//tip("uid "+Activity1.muid+"\ncok "+cook);
		int r=Global.列表信息框(this,"启动附加功能","插件设置向导（建设中）","绿色弹幕辅助模块","社区温暖注册模块","开发调试");
		switch (r) {
			case 0:
				new Msgbox("功能建设中","这个功能在建设中！","ok");break;
				//startActivity(new Intent(this, PermissionGuide.class)); break;
			case 1: case 2:
				String[]plugname={"","LiveUpgrade","GetAllowance"};
				Class[]plugs={null,LiveUpgrade.class,GetAllowance.class};
				final int[]uid=getuidexceptzero();
				final boolean[]b=plugcfg(plugname[r],uid);
				final Class pluglaunch=plugs[r];
				if (b.length==0) {
					tip("请先添加帐号后再使用此功能");
				} else {
					new CheckMsgbox(new String[]{"选择帐号登录","登录到插件","取消"},b,uid2name(uid)){
						void onClick(int i,boolean[]stat){
							if(i==vbyes){
								int[]choice=intFilter(uid,stat);
								if(choice.length==0) tip("请选择一个帐号再试");
								else{
									String[]coks=uid2cookie(choice);
									startService(new Intent(a, pluglaunch).putExtra("cookie", coks));
								}
								//tip(Arrays.toString(choice));
							}
						}
						int[]intFilter(int[]i,boolean[]f){
							int count=0;//要准备的容器量
							for(boolean sin:f) if(sin) count++;//每选中一个帐号，增加一个容器
							int[]filted=new int[count];//新建容器
							count=0;//当前的含义为 容器填充进度
							for(int l=0;l<f.length;l++) if(f[l]){ filted[count]=i[l]; count++; }
							return filted;
						}
					};
				}break;
			case 3:
				a.startService(new Intent(a, MemMonitor.class)); break;

		}
	}
//-----
	protected String http(String method,String url,String param,String formdata){//每个activity都可用的http，需要在其他线程
		return HttpRequest.http(method,url,param,formdata);
	}
	protected static class Http extends AsyncTask<String,Void,String>{//封装型异步http，一般不用
		public boolean isfin=false;
		public Http(String method,String url,String param,String formdata){
			execute(method,url,param,formdata);
		}
		@Override protected String doInBackground(String[]p){
			p[0]=HttpRequest.http(p[0],p[1],p[2],p[3]);
			isfin=true;
			return p[0];
		}
		@Override protected void onPostExecute(String p){ fin(p); }
		void fin(String data){}
	}
//-----
	protected void btnbind(View...v){ for(View btnv:v)btnv.setOnClickListener(this); }//连续绑定多个【动态】view的点击事件到本activity
	protected void btnbind(int...id){ for(int btnid:id)fv(btnid).setOnClickListener(this); }//连续绑定多个【静态】view的点击事件到本activity
	abstract public void onClick(View v);//每个窗口应该都有按钮吧？
	protected void seticon(View v,android.graphics.Bitmap i){
		if(v instanceof ImageView){ ((ImageView)v).setImageBitmap(i); }
	}
	protected View fv(int id){return findViewById(id);}//查找当前activity唯一的
	protected View fv(ViewGroup vg,int id){return vg.findViewById(id);}//查找列表子项中唯一的
	protected static void tip(String s){Toast.makeText(App.getContext(),s,0).show();}
	private Thread.UncaughtExceptionHandler defUeh;
	private Activity1 This=this;//一个默认指向当前activity的指针，在内部类中使用
	public Activity1(){
		super();
		defUeh= Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
		
	}
	public void addIdleHandler(){ Looper.myQueue().addIdleHandler(this); }//添加一个初始化或空闲行为，大多数在Activity.onCreate使用
	public boolean queueIdle(){return onIdle();}
	//abstract protected void onCreate(Bundle sis);
	abstract protected boolean onIdle();//每个窗口应该都用这个来初始化
//-----
	protected static String string(Object...str){ return buildstring(str).toString(); }
	protected static StringBuilder buildstring(Object...str){
		StringBuilder bdr=new StringBuilder();
		return string(bdr,str);
	}
	protected static StringBuilder string(StringBuilder bdr,Object...str){
		for(Object s:str) bdr.append(s);
		return bdr;
	}
//-----
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
				.setMessage("程序发生了错误并即将退出。以下信息已自动保存，您可用反馈功能发送这些信息。\n"+rep)
				.setNeutralButton("关闭", this)
				.create();
			excad.show();
			Looper.loop();
		}
		public void onClick(DialogInterface p1, int p2) {
			System.exit(10);
		}
		void savelog(){
			try{
				File log=new File(DIR_cache_log);
				log.mkdirs();
				log=log.createTempFile(string(System.currentTimeMillis(),"_"),".log",log);
				Text.write(rep,log.getPath(),"utf-8");
			}catch(Exception e){}
		}
	}
	protected void pl(Object...o){tipl(o);}
	protected void tipl(Object...o){System.out.println(string(o));}
	HashMap<String,Bitmap>pics=new HashMap<>();
	protected class HeadLoader extends Download implements Runnable{
		int uid=0; String spath=null,pth=null;
		Bitmap cur=null; View v=null;
		public HeadLoader(int inid,String url,View imgbox){// 如果mid为负数，表示使用缓存的图片哈希表
			uid=inid;
			spath=shortpath(url);
			pth=DIR_cache_pic+spath;
			v=imgbox;
			if(new File(pth).exists()){ // 文件已存在
				process();
			}else{ // 处理文件未存在的图
				cur=icon.img_def;
				v.post(this); // 先加载默认图占位
				executeOnExecutor(THREAD_POOL_EXECUTOR,pth,"get",url,"",""); // 开始下载图片
			}
		}
		@Override void done(File f){ process(); } // 下载完成，该图已存在
		@Override void fail(Throwable t){}
		void process(){ // 处理文件已存在的图
			if(uid<0){ // 临时图
				cur=pics.get(spath);
				if(!(cur instanceof Bitmap)){ // 当前窗口未有缓存
					cur=icon.process(pth,icon.jpg,32,32);
					pics.put(spath,cur); // 添加缓存
				}
			}else{//共享图（已存在）
				cur=icon.user(uid);
				if(!(cur instanceof Bitmap)){ // 共享区没有缓存
					cur=icon.process(pth,icon.jpg,32,32);
					icon.users.put(uid,cur);
				}
			}
			v.post(this);
		}
		String shortpath(String url){
			String[]fn=url.split("/");
			return fn[fn.length-1];
		}
		public void run(){ onLoad(v,cur); }
		//public void addView(View save){v=save; tip(string("adding: \n",v,"\nvs\n",save));}
		void onLoad(View v,Bitmap b){ seticon(v,b); }//默认操作，如有需要就改这个
	}
	protected class Msgbox extends AlertDialog.Builder implements DialogInterface.OnClickListener{
		protected int vbyes=AlertDialog.BUTTON_POSITIVE,
			vbno=AlertDialog.BUTTON_NEGATIVE,
			vbmid=AlertDialog.BUTTON_NEUTRAL;
		public Msgbox(String...msgs){
			super(Activity1.this); 
			for(int i=0,len=msgs.length;i<len;i++){
				switch(i){
					case 0: setTitle(msgs[0]); break;
					case 1: setMessage(msgs[1]); break;
					case 2: setPositiveButton(msgs[2],this); setCancelable(false); break;
					case 3: setNegativeButton(msgs[3],this); break;
					case 4: setNeutralButton(msgs[4],this); break;
				}
			}show();
		}
		public void onClick(DialogInterface p1,int p2){ onClick(p2); }
		void onClick(int i){}
	}

	protected abstract class CheckMsgbox extends AlertDialog.Builder implements DialogInterface.OnClickListener,DialogInterface.OnMultiChoiceClickListener {
		protected int vbyes=AlertDialog.BUTTON_POSITIVE,
		vbno=AlertDialog.BUTTON_NEGATIVE,
		vbmid=AlertDialog.BUTTON_NEUTRAL;
		boolean stat[];
		public CheckMsgbox(String[]msgs,boolean[]status,String...options){
			super(Activity1.this); 
			for(int i=0,len=msgs.length;i<len;i++){
				switch(i){
					case 0: setTitle(msgs[0]); break;
					case 1: setPositiveButton(msgs[1],this); setCancelable(false); break;
					case 2: setNegativeButton(msgs[2],this); break;
					case 3: setNeutralButton(msgs[3],this); break;
				}
			} stat=status;
			this.setMultiChoiceItems(options,status,this);
			show();
		}
		public void onClick(DialogInterface p1,int p2){ onClick(p2,stat); }
		public void onClick(DialogInterface p1,int p2,boolean p3){ stat[p2]=p3; onChange(p2,p3); }
		abstract void onClick(int i,boolean[]status)
		void onChange(int i,boolean status){}
	}
}
