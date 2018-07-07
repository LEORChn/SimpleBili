package leorchn.lib;
import android.app.*;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.os.*;
import android.view.*;
import android.widget.*;
import appforms.*;
import java.io.*;
import java.util.*;
import simplebili.App;
import simplebili.lib.User;
import com.LEORChn.SimpleBili.R;
public abstract class Activity1 extends Activity implements MessageQueue.IdleHandler,Thread.UncaughtExceptionHandler,View.OnClickListener{
	protected static Icon icon;
	protected static Bitmap ic_sys(int id){ return icon.sys(id); }
	protected static R.id id;
	protected static R.layout layout;
	protected static R.drawable drawable,draw,d;//可以给 R 类定义快捷方式和多个别名而且不用额外声明，超爽
	protected static R.color color;
	protected static R.menu menu;
	
	public static final String UA="User-Agent: ",
	UA_win="Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36",
	UA_android="Mozilla/5.0 (Linux; Android 4.4.4;) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2307.2 Mobile Safari/537.36",
	UA_mobilebili="Mozilla/5.0 BiliDroid/4.11.7 (bbcallen@gmail.com)";
	
	public static String DIR_cache=App.getContext().getExternalCacheDir().getPath()+"/",
	DIR_data=App.getContext().getFilesDir().getPath()+"/",
	DIR_cache_pic=DIR_cache+"pic/",
	DIR_cache_log=DIR_cache+"log/";
//-----
	public static int muid;
	public static String mcok="Cookie: ";
	public static FSON defSetting=new FSON(appforms.Settings.getDefaultSetting()),
						sets=setsload();
	public static User user=new User(DIR_data+"user.info");//顺序不能改，必须先初始化默认配置然后再加载用户配置
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
	
//-----
	static boolean[]plugcfg(String name,int[]uid){//从配置文件里获取初始设置
		int[]od=user.getUids();
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
	public void 附加功能(final Activity1 a){
		if(! PermissionGuide.isCanNotify()){
			final File t=new File(DIR_data+"tip_notifycation");
			if(t.exists()){ extmodule(a); return; }
			new Msgbox("通知栏权限","插件需要使用通知栏权限，以便您查看和控制插件的使用情况。\n点击左侧 “显示通知” 方框打勾 \u2611 后即可使用。",
				"前往设置","不再提示","忽略"){
				@Override protected void onClick(int i){
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
		int r=Global.列表信息框(this,"启动附加功能","插件设置向导（建设中）","绿色弹幕辅助模块","社区温暖注册模块","日常行为模拟模块","开发调试");
		switch (r) {
			case 0:
				new Msgbox("功能建设中","这个功能在建设中！","ok");break;
				//startActivity(new Intent(this, PermissionGuide.class)); break;
			case 1: case 2: case 3:
				String[]plugname={"","LiveUpgrade","GetAllowance","DailyQuest"};
				Class[]plugs={null,LiveUpgrade.class,GetAllowance.class,DailyQuest.class};
				final int[]uid=user.getUids();
				final boolean[]b=plugcfg(plugname[r],uid);
				final Class pluglaunch=plugs[r];
				if (b.length==0) {
					tip("请先添加帐号后再使用此功能");
				} else {
					new CheckMsgbox(new String[]{"选择帐号登录","登录到插件","取消"},b,user.getNames(uid)){
						protected void onClick(int i,boolean[]stat){
							if(i==vbyes){
								int[]choice=intFilter(uid,stat);
								if(choice.length==0) tip("请选择一个帐号再试");
								else{
									String[]coks=user.getCookies(choice);
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
			case 4:
				a.startService(new Intent(a, MemMonitor.class)); break;

		}
	}
//-----
	protected String http(String method,String url,String param,String formdata){//每个activity都可用的http，需要在其他线程
		return HttpRequest.http(method,url,param,formdata);
	}
	public static class Http extends AsyncTask<String,Void,String>{//封装型异步http，一般不用
		public boolean isfin=false;
		public String result="";
		public Http(String method,String url,String param,String formdata){
			execute(method,url,param,formdata);
		}
		@Override protected String doInBackground(String[]p){
			pl("<---   "+p[1]);
			result=HttpRequest.http(p[0],p[1],p[2],p[3]);
			isfin=true;
			return result;
		}
		@Override protected void onPostExecute(String p){pl("--->   "+p); onload(p); }
		//void fin(String data){pl("pass wrong way! "+data);}
		protected void onload(String d){}
		public String pending(){return waitfor();}
		public String waitfor(){
			try{while(!isfin)Thread.sleep(200);}catch(Throwable e){}
			return result;
		}
		/* if single thread recommand usage:
			Http h=new Http("get","http://",mcok,"").waitfor();
		*/
		/* if multi-thread (like needs to update ui) recommand usage:
			new Http("get","http://",mcok,""){
				protected void onload(String d){}//update ui here
			};
		*/
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
	protected View extractView(View v){
		try{
			((ViewGroup)v.getParent()).removeView(v);
			return v;
		}catch(Throwable e){ return null; }
	}
	protected ViewGroup inflateView(int id){return(ViewGroup)LayoutInflater.from(this).inflate(id,null);}
	protected void setText(View v,String s){((TextView)v).setText(s);}
	protected void startActivity(Class<?>c){startActivity(new Intent(this,c));}
	protected static boolean visible(View v){return v.getVisibility()==View.VISIBLE;}
	protected static void visible(final View v,final boolean visible){v.post(new Runnable(){public void run(){v.setVisibility(visible?View.VISIBLE:View.GONE);}});}
	protected static void tip(String s){Toast.makeText(App.getContext(),s,0).show();}
	private Thread.UncaughtExceptionHandler defUeh;
	private Activity1 This=this;//一个默认指向当前activity的指针，在内部类中使用
	public Activity1(){
		super();
		defUeh= Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
		
	}
	@Override protected void onCreate(Bundle sis){
		super.onCreate(sis);
		oncreate();
		addIdleHandler();
	}
	abstract protected void oncreate();
	public void addIdleHandler(){ Looper.myQueue().addIdleHandler(this); }//添加一个初始化或空闲行为，大多数在Activity.onCreate使用
	public boolean queueIdle(){return onIdle();}
	//abstract protected void onCreate(Bundle sis);
	abstract protected boolean onIdle();//每个窗口应该都用这个来初始化
//-----
	public static String string(Object...str){ return buildstring(str).toString(); }
	public static StringBuilder buildstring(Object...str){
		StringBuilder bdr=new StringBuilder();
		return string(bdr,str);
	}
	public static StringBuilder string(StringBuilder bdr,Object...str){
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
			android.os.Process.killProcess(android.os.Process.myPid());
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
	public static void pl(Object...o){tipl(o);}
	public static void tipl(Object...o){System.out.println(string(o));}
	protected void setUserHead(View imageView,int uid){
		icon.user(imageView,uid);
	}
	
	/*HashMap<String,Bitmap>pics=new HashMap<>();
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
				cur=ic_sys(d.img_def);
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
		protected void onLoad(View v,Bitmap b){ seticon(v,b); }//默认操作，如有需要就改这个
	}*/
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
		protected void onClick(int i){}
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
		protected abstract void onClick(int i,boolean[]status)
		protected void onChange(int i,boolean status){}
	}

	// 以下-----系统事件覆盖区
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		PopupMenuCompat.exec(menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case id.menuitem_leorchn: startActivity(new Intent(this,UpZone.class).putExtra("space","3084436")); break;
			case id.menuitem_share_this_app:
				Intent i2=new Intent(Intent.ACTION_SEND)
					.setType("text/plain")
					.putExtra(Intent.EXTRA_TEXT,"推荐一个有趣的软件【简哔】，是B站第三方安卓客户端，占用低还有各种黑科技，来试用一下呀。")
					.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i2); break;
			case id.menuitem_github: openurl("https://github.com/LEORChn/SimpleBili"); break;
			case id.menuitem_donate: openurl("https://leorchn.github.io/?about"); break;
			case id.menuitem_review: openurl("market://details?id="+getPackageName()); break;

		}return super.onOptionsItemSelected(item);
	}
	void openurl(String url){
		startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse(url)));
	}
}
