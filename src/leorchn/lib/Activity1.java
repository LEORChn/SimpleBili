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
import simplebili.App;
public abstract class Activity1 extends Activity implements MessageQueue.IdleHandler,Thread.UncaughtExceptionHandler,View.OnClickListener{
	protected static R.id id;
	protected static R.layout layout;
	protected static R.drawable drawable,draw;//可以给 R 类定义快捷方式和多个别名而且不用额外声明，超爽
	protected static R.color color;
	
	public static final String UA_win="Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36",
	UA_android="Mozilla/5.0 (Linux; Android 4.4.4;) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2307.2 Mobile Safari/537.36",
	UA_mobilebili="Mozilla/5.0 BiliDroid/4.11.7 (bbcallen@gmail.com)";
	
	public static String DIR_cache=App.getContext().getCacheDir().getPath()+"/",
	DIR_data=App.getContext().getFilesDir().getPath()+"/";
	
	public static FSON defSetting=new FSON(appforms.Settings.getDefaultSetting()),
						sets=setsload(), user=userload();//顺序不能改，必须先初始化默认配置然后再加载用户配置
	public static String setspath(){ return DIR_data+"app.setting"; }
	public static FSON setsload(){
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
	
	public static String mcok;//="Cookie: ";
	public static int useradd(String cok){//0=正常,-1=网络异常,-11=识别码错误,-12=识别码失效
		try{
			if(cok.contains("SESSDATA=") && cok.contains("%"));else return -11;//cookie格式无效
			//Text.write(cok,DIR_cache+"tempcok","utf8");
			Http h=new Http("get","http://api.bilibili.com/nav",cok,"");
			while(!h.isfin)try{ Thread.sleep(500); }catch(Exception e){}
			String s=h.get();
			FSON j=new FSON(s),u=new FSON("{}"),us=user.getList("users");
			if(!j.canRead()){
				//new Msgbox("-1",s,"ok");
				return -1;
			}//网络异常
			j=j.getObject("data");
			boolean usable=j.get("isLogin",false);
			if(!usable)return -12;//cookie不可用
			long d=Long.valueOf(cok.split("SESSDATA=")[1].split("%(.){2}")[1]);//有可能会异常跳出
			u.set("l",usable);//isLogin
			u.set("i",j.get("mid", 0));//userId
			u.set("n",j.get("uname","神秘用户"));//Name
			u.set("h",j.get("face",""));//userHeadpicture
			u.set("c",cok);//userCookie
			u.set("d",d);
			u.set("u",false);//userisUpdated
			boolean update=false;
			for(int i=0,len=us.length();i<len;i++){
				if(Objects.equals(us.getObject(i).get("i",0), u.get("i",0))){//比较id，更新帐号
					us.set(i,u);
					update=true;
					break;
				}
			}
			if(!update)us.add(u);
			user.set("users",us);
			//new File(DIR_cache+"tempcok").delete();
		}catch(Exception e){
			return -11;
		}return 0;
	}
	public static boolean userset(int i){
		return false;
	}
	public static boolean usersave(){ return Text.write(user.toString(),userpath(),"UTF8"); }
	public static String userpath(){ return DIR_data+"user.info"; }
	public static FSON userload(){
		String s=Text.read(userpath());
		return new FSON((s==null || s.isEmpty())?"{'main':0,'users':[]}":s);
	}
	
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
	protected void btnbind(View...v){ for(View btnv:v)btnv.setOnClickListener(this); }//连续绑定多个【动态】view的点击事件到本activity
	protected void btnbind(int...id){ for(int btnid:id)fv(btnid).setOnClickListener(this); }//连续绑定多个【静态】view的点击事件到本activity
	abstract public void onClick(View v);//每个窗口应该都有按钮吧？
	protected View fv(int id){return findViewById(id);}//查找当前activity唯一的
	protected View fv(ViewGroup vg,int id){return vg.findViewById(id);}//查找列表子项中唯一的
	protected void tip(String s){Toast.makeText(this,s,0).show();}
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
	
	protected static String string(Object...str){ return buildstring(str).toString(); }
	protected static StringBuilder buildstring(Object...str){
		StringBuilder bdr=new StringBuilder();
		return string(bdr,str);
	}
	protected static StringBuilder string(StringBuilder bdr,Object...str){
		for(Object s:str) bdr.append(s);
		return bdr;
	}
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
	}
	protected class Msgbox extends AlertDialog.Builder implements DialogInterface.OnClickListener{
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
}

