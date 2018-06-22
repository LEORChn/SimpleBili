package appforms;

import android.app.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.lang.reflect.*;
import java.util.*;
import leorchn.lib.*;

import leorchn.lib.CrashHandlerReg;
import static android.media.MediaPlayer.*;
import static leorchn.lib.Global.*;
import leorchn.lib.*;
import static leorchn.lib.HttpRequest.*;
import android.provider.Settings;
import java.text.*;
import simplebili.lib.*;

//This Class Only For Online Video!!!
public class VideoPlaySimple extends Activity1 implements View.OnTouchListener,MessageQueue.IdleHandler,OnErrorListener,OnPreparedListener,OnBufferingUpdateListener,OnCompletionListener,DanmakuViewControl.OnDanmakuLogListener {
	VideoPlaySimple This; public Activity getContext(){return This;}
	String path="",title="",partname="",vid="",cid="",
	referer="Referer: http://www.bilibili.com/video/\r\n",
	useragent="User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36\r\n";
	int hasinit=0; DanmakuViewControl dvc;
	boolean allowhistory=false,readyplay=false,readyinfo=false;int readydanmaku=0;//0=准备加载，1=不开启弹幕，2=弹幕错误，3=弹幕完成
	protected void onCreate(Bundle sis){
		super.onCreate(sis);
		This=this; if(sets==null)setsload();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) 
            //透明状态栏
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		retryplay();//Looper.myQueue().addIdleHandler(this);
	}
	public boolean onIdle(){
		switch(hasinit){
			case 0://初始化界面
				dvc=new DanmakuViewControl(this,layout.activity_video_play_simple,id.vplay_danmakuview,50);
				setContentView(dvc.getBackRootView());
				btnbind(id.vplay_endplay,id.vplay_backward15,id.vplay_replay,id.vplay_panelhide,id.vplay_nextpart,id.vplay_frameplay);
				vp=(VideoView)fv(id.vplay_player);
				inf=(TextView)fv(id.vplay_timeinfo);
				totaltime=(TextView)fv(id.vplay_totaltime);
				pb=(ProgressBar)fv(id.vplay_progress);
				panelfin=fv(id.vplay_panelfinish); visible(panelfin,false);
				preload=fv(id.vplay_preload);
				playdbg=(TextView)fv(id.vplay_playerdbg); 
				dandbg=(TextView)fv(id.vplay_dandbg); dvc.setOnDanmakuLogListener(this);
				scltip=(TextView)fv(id.vplay_scrolldisplay);
				itv=(ImageView)fv(id.vplay_animtv);//加载电视动画
				itv.setBackgroundResource(drawable.anim_bilitv);//设置为动态
				AnimationDrawable bg=(AnimationDrawable)itv.getBackground();
				bg.start();
				path=getIntent().getStringExtra("path");
				vid=getIntent().getStringExtra("vid");
				cid=getIntent().getStringExtra("cid");
				allowhistory=getIntent().getBooleanExtra("history",false);
				//loadStringParam(new String[]{path,title,partname,vid,cid,cookie},"path,title,partname,vid,cid,cookie".split(","));
				//clearStringNull(title,partname,vid,cid,cookie);
				break;
			case 1://加载日志
				dandbg.setText(playdbg.getText(),TextView.BufferType.EDITABLE);
				if(readydanmaku==1){
					dlog("弹幕组件被禁用。\n");
					readydanmaku=1;
				}else dvc.downAndLoadDanmaku("http://comment.bilibili.com/"+cid+".xml");
				playdbg.setText(playdbg.getText(),TextView.BufferType.EDITABLE);
				plog("正在准备播放："+path+"\n");
				plog("正在初始化【在线】播放环境...\n");
				break;
			case 2:
				try{
					int sp=Integer.parseInt(Uri.parse(path).getQueryParameter("rate"));
					if(sp>4500)sp=sp/1000;
					tip("被B站限制缓冲速度 "+sp+" kb");
				}catch(Exception e){plog("B站限速信息读取异常。\n");}
				mhead=new HashMap<String,String>();
				int sdk=android.os.Build.VERSION.SDK_INT;
				if(sdk >26){
					mhead.put("Referer",referer.split(": ")[1]);
					mhead.put("User-Agent",useragent.split(": ")[1]);
				}else if(sdk >20){
					mhead.put("Referer",referer.split(": ")[1]);
					mhead.put("USER-AGENT",useragent.split(": ")[1]);
				}else{
					mhead.put("USER-AGENT",useragent.split(": ")[1]+referer);
				}
				setVideoURI(Uri.parse(path),mhead);
				//if(false){ entertempdownplay(mhead); hasinit++; return false; }
				plog("在线播放环境初始化完成，正在缓冲...\n");
				break;
			case 3:
				vp.setOnErrorListener(this);
				vp.setOnPreparedListener(this);
				vp.setOnCompletionListener(this);
				//vp.setOnGenericMotionListener(this);
		}
		hasinit++;
		return hasinit<10;
	}
/*	void entertempdownplay(Map<String,String>head){
		tip("系统版本(高于20)："+android.os.Build.VERSION.SDK_INT);
		playpath=TempDownPlay.start(this,path,head);
		if(playpath.isEmpty())return;
		vp.setVideoPath(playpath);
	}*/
	View preload; TextView playdbg,dandbg,scltip;
	VideoView vp; MediaPlayer mp; ProgressBar pb; TextView inf; TextView totaltime; ImageView itv;
	View panelfin;
	//监听器 开始
	Map<String,String>mhead; //boolean reseturl_android5=false;
	public boolean onError(MediaPlayer p1, int what, int extra) { 
		plog("播放器错误，按菜单键重试... what="+what+",extra="+extra);
		return true;
	} final int progbarAccurate=5;//进度条精度
	public void onPrepared(MediaPlayer p1){//开始播放视频！
		mp=p1;
		p1.setScreenOnWhilePlaying(true);
		p1.setOnBufferingUpdateListener(this);
		totaltime.setText("/"+Formater.format(p1.getDuration()));
		readyplay=true;
		onRequestPlay(); //如果未禁用并且在加载中则等待弹幕加载完成或出错
	}
	public void onBufferingUpdate(MediaPlayer p1, int p2) { 
		buffprog=p2; keepUpdateThreadAlive();//progressupdate();
	}
	public void onCompletion(MediaPlayer p1){
		visible(panelfin,true);
		signHistory(vid,cid,-1);
	}
	public void onClick(View v){switch(v.getId()){
		case id.vplay_endplay: finish(); break;
		case id.vplay_backward15: seekto(vp.getDuration()-15000); visible(panelfin,false); vp.start(); break;
		case id.vplay_replay: seekto(0); visible(panelfin,false); vp.start(); break;
		case id.vplay_panelhide: visible(panelfin,false); break;
		case id.vplay_nextpart: break; //TODO
		case id.vplay_frameplay: if(vp.getCurrentPosition()>0)try{ vp.start(); Thread.sleep(45); vp.pause();}catch(Exception e){} break;
	}}
	GestureDetector ges; int scrolltype=0,sclreal=0; double scrollval=0;//0=并无滚动。1=滚动进度条。2=滚动亮度。3=滚动音量
	float volume=1;
	final int progmtp=10,brigmtp=20,volmtp=20;//滑动屏幕的作用倍数
	void init_Ges(){
		ges=new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
			@Override public boolean onScroll(MotionEvent et1,MotionEvent et2,float x,float y){
				if(scrolltype==0) //只在第一次滚动时触发判断是何种滚动
					if(Math.abs(x)>Math.abs(y)){// 左右移动，调整进度
						scrollval=0;
						if(et1.getY()<(vp.getHeight()*.8))
							scrolltype=3;
						else
							scrolltype=4;
						vp.pause();
					}else if(et1.getX()<(vp.getWidth()/2)){ //在屏幕左侧移动，调整亮度
						scrollval=brigmtp*getbright();
						scrolltype=1;
					}else{ //屏在幕右侧移动，调整音量
						scrollval=volmtp*volume*15;
						scrolltype=2;
					}
				visible(scltip,true);
				switch(scrolltype){
					case 3://微调进度
						scrollval-=x;
						sclreal=(int)Math.floor(scrollval/progmtp);
						//int curpos=...;
						sclfix(progmtp,0-(vp.getCurrentPosition()/1000),(vp.getDuration()-vp.getCurrentPosition())/1000);
						scltip.setText(//已修BUG:可倒退到0秒以下、以及显示文本不居中
							Formater.format(vp.getCurrentPosition()+(sclreal*1000))+totaltime.getText()+"\n"+
							sclreal+(Math.abs(sclreal)>4?" 秒 进度":" 秒 无效"));
						break;
					case 4://大量进度
						scrollval-=x;
						sclreal=(int)Math.floor(scrollval);
						//int curpos=...;
						sclfix(1,0-(vp.getCurrentPosition()/1000),(vp.getDuration()-vp.getCurrentPosition())/1000);
						scltip.setText(
							Formater.format(vp.getCurrentPosition()+(sclreal*1000))+totaltime.getText()+"\n"+
							sclreal+(Math.abs(sclreal)>4?" 秒 进度":" 秒 无效"));
						break;
					case 1://亮度
						scrollval+=y;
						sclfix(brigmtp,0,15);
						setbright(sclreal);
						scltip.setText("亮度 "+sclreal);
						break;
					case 2://音量
						scrollval+=y; 
						sclfix(volmtp,0,15);
						volume=sclreal/15f;
						mp.setVolume(volume,volume);//vol.setProgress(sclreal);
						scltip.setText("音量 "+sclreal);
				}
				return false;
			}
				public boolean onDoubleTap(MotionEvent et) {if(vp.isPlaying())vp.pause();else vp.start();visible(panelfin,false);return false;}
			//public boolean onDoubleTapEvent(MotionEvent p1){return false;}//双击后但并未结束时的其他手势，绑定于双击事件
			//public boolean onSingleTapConfirmed(MotionEvent et) {return false;}
		});
	}
	public boolean onTouch(View p1, MotionEvent et) {
		if(et.getAction()==et.ACTION_UP){
			if(scrolltype>2){// 1和2为拖进度条
				vp.start();
				if(Math.abs(sclreal)>4)//拖进度条的无效范围
					seekto(vp.getCurrentPosition()+sclreal*1000);//隐患：跳转到范围外的时间、播放完毕后重新跳转不隐藏面板
				visible(panelfin,false);
			}
			visible(scltip,false);
			scrolltype=0;
			////tipoff();
		}
		ges.onTouchEvent(et); return true;
	}
	//int launchtms=0;
	public void onDanmakuLog(String log) { dlog(log); }
	public void onDanmakuLoadCompleted() {
		readydanmaku=3;//弹幕完成
		onRequestPlay();
	}
	public void onDanmakuLoadError(int errCode,String errmsg){
		readydanmaku=2;//弹幕错误
		dlog(errCode+": "+errmsg);
		//onRequestPlay();
	}
	void onRequestPlay(){
		if(readyinfo){
			visible(preload,false);
			vp.setBackgroundColor(android.R.color.transparent);
			vp.start();
			vp.setOnTouchListener(this);
			init_Ges();
		}else if(readydanmaku>0 && readyplay){
			if(delayplay !=null)delayplay.execute();//因为执行完毕一次后它会自毁以防止二次执行，并防止在载入视频信息时多次重试造成错误
		}
	}
	//监听器 结束
	void setbright(float b){
		//Settings.System.putInt(this.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,b*17);
		Window w=getWindow();
		WindowManager.LayoutParams lp=w.getAttributes();
		lp.screenBrightness=b/15;
		w.setAttributes(lp);
		realbright=(int)b;
	} int realbright=-5;
	float getbright(){
		//return Settings.System.getInt(this.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,18);
		return realbright==-5?getWindow().getAttributes().screenBrightness:realbright;
	}
	
	void sclfix(int multiple,int realmin,int realmax){ 
		if(scrollval<realmin*multiple)scrollval=realmin*multiple;
		if(scrollval>realmax*multiple)scrollval=realmax*multiple;
		sclreal=(int)Math.floor(scrollval/multiple);
	}
	static class Formater{
		static String format(long i){int sec=(int)Math.floor((i % 60000)/1000);return ((int)Math.floor(i / 60000))+":"+(sec<10?"0"+sec:sec);}
	}
	int buffprog=0;
	void progressupdate(){
		int playpos=vp.getCurrentPosition();
		pb.setProgress(( playpos *100*progbarAccurate)/vp.getDuration());
		pb.setSecondaryProgress(buffprog*progbarAccurate);
		inf.setText(Formater.format( playpos ));
		if(readydanmaku==3)dvc.updateDanmaku( playpos ); //弹幕完成时才能updated
		if(pb.getProgress()/progbarAccurate<99)signHistory(vid,cid,playpos/1000);//观看进度小于99%才能写入时间，这样不至于把-1也就是“已看完”覆盖掉
	}
	Thread updatethread=new Thread(){public void run(){
		Runnable updaterun=new Runnable(){public void run(){progressupdate();}};
		while(true){
			This.runOnUiThread(updaterun);
			try{Thread.sleep(1000);}catch(Exception e){}
		}
	}};
	void keepUpdateThreadAlive(){ if(!updatethread.isAlive()) updatethread.start(); }
	void setVideoURI(Uri uri,Map<String,String>mHeaders){
		try{
			Class vpclass=Class.forName("android.widget.VideoView");
			Method[]ms=vpclass.getMethods();
			Method mTarget=null;
			for(Method m:ms){
				if(m.getName().equals("setVideoURI"))
					if(m.getParameterTypes().length==2) mTarget=m;
			}
			if(mTarget==null)
				new Msgbox("Set Headers Fail","很抱歉！\n您的系统不支持覆盖此方法，视频可能无法正常播放。","ok");
			else
				mTarget.invoke(vp,new Object[]{uri,mHeaders});
		}catch(Exception e){new Msgbox("Set Headers Fail",E.trace(e),"ok");}
	}
	long lastReqExit=0;
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode){
			case 4://返回键
				if (System.currentTimeMillis() - lastReqExit < 3000) {
					TempDownPlay.end();
					finish();
				} else {
					tip("再按一次退出播放。");
					lastReqExit = System.currentTimeMillis();
				}break;
		}
		return false;//super.onKeyDown(keyCode, event);
	}
	final int menuidx=Menu.FIRST;
	public boolean onCreateOptionsMenu(Menu menu) {
		String[]menus="重试,启动附加功能...".split(",");
		int[]menuicons={drawable.topmenu_refresh, drawable.topmenu_extmodule};
		for(int i=0;i<menus.length;i++)
			menu.add(0,menuidx+i,0,menus[i]).setIcon(menuicons[i]);
		return super.onCreateOptionsMenu(menu);
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		this.closeOptionsMenu();
		switch(item.getItemId()-menuidx){
			case 0: retryplay(); break;
			case 1: 附加功能(this); break;
			default: break;
		}return super.onOptionsItemSelected(item);
	}
	void plause(boolean to){}//play和pause的结合体，控制由用户引起的播放和暂停操作
	void seekto(int t){ t=t<1000?0:t; vp.seekTo(t); if(readydanmaku==3)dvc.onVideoSeekto(t); }
	
	
	void plog(String s){playdbg.append(s);}
	void dlog(String s){dandbg.append(s);}
	void clearStringNull(String...s){for(String s2:s)if(s2==null)s2="";}
	void loadStringParam(String[]s,String[]id){for(int i=0,sl=s.length;i<sl;i++)s[i]=getIntent().getStringExtra(id[i]);}
	AsyncTask delayplay;//这个是在等待播放器缓冲及弹幕加载完毕后执行的几秒延时
	void retryplay(){ 
		if(sets.get("danmakuengine",1)==0)readydanmaku=1;//表示被禁用
		if(readydanmaku>1)readydanmaku=0;//表示之前已启用过
		readyplay=false; readyinfo=false;
		hasinit=0; 
		delayplay=new AsyncTask<Object,Integer,Void>(){
			protected void onPreExecute(){
				pb.setMax(100*progbarAccurate);
			}
			@Override protected Void doInBackground(Object[] p1) {
				for(int i=10;i<100;i+=2){
					publishProgress(i);
					try{ Thread.sleep(25); }catch(Exception e){}
				} return null;
			}
			@Override protected void onProgressUpdate(Integer[] values) {
				pb.setProgress(progbarAccurate*values[0]);
				super.onProgressUpdate(values);
			}
			@Override protected void onPostExecute(Void r){
				pb.setProgress(0);
				readyinfo=true;
				onRequestPlay(); delayplay=null;//自毁以防止二次执行，并防止在载入视频信息时多次重试造成错误
			}
		};
		Looper.myQueue().addIdleHandler(this);
	}
	long lastSignHistory=System.currentTimeMillis();
	void signHistory(String aid,String cid,int second){//历史记录:秒数为-1即"已看完"
		if(allowhistory && (second==-1 || (lastSignHistory+30000<System.currentTimeMillis() && second>30))){//如果需要记录的时间为“已看完”，那么不需要判断上次是在什么时候记录的
			lastSignHistory = System.currentTimeMillis();
			//检查 cookie
			if (!(mcok.contains("DedeUserID=") && mcok.contains("SESSDATA="))){
				tip("历史记录失败（1）：\nCookie参数不足。");
				return;
			}
			new Http("POST", "http://api.bilibili.com/x/report/web/heartbeat", mcok, "aid="+aid+"&cid="+cid+"&played_time="+second){
				@Override protected void onload(String data){
					if(!new FSON(data).canRead()){
						tip("历史记录失败（2）：\n网络传输错误，请检查网络或更新简哔。");
					}
				}
			};
		}
	}
}
