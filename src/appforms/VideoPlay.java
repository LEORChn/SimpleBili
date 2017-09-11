package appforms;


import android.app.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.LEORChn.SimpleBili.*;
import leorchn.lib.*;

import leorchn.lib.CrashHandlerReg;
import static leorchn.lib.Global.*;
import static leorchn.lib.WidgetOverride.*;

public class VideoPlay extends Activity implements MessageQueue.IdleHandler{
	Activity This; public Activity getContext(){return This;}
	String path="",title="",partname="",vid="",cid="",cookie="",
	referer="Referer: http://www.bilibili.com/video/\n",
	useragent="User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36\n";
	int hasinit=0; boolean onlineplay=false;
	protected void onCreate(Bundle sis){
		super.onCreate(sis);
		CrashHandlerReg.reg(this);
		This=this;
		Looper.myQueue().addIdleHandler(this);
	}
	public boolean queueIdle(){
		switch(hasinit){
			case 0://读取信息
				setContentView(R.layout.activity_video_play);
				playdbg=(TextView)fv(R.id.vplay_playerdbg);
				ImageView itv=(ImageView)fv(R.id.vplay_animtv);//加载电视动画
				ViewGroup.LayoutParams itvp=itv.getLayoutParams();
				itvp.width=itv.getHeight();
				itv.setLayoutParams(itvp);//重新调整动画大小
				vv=(VideoView)fv(R.id.vplay_player);
				visible(vv,false);//将播放器设为不可见
				path=getIntent().getStringExtra("path");
				if(path==null){ plog("播放参数错误：无参数。\n请退出播放。"); return false;}
				//root=(RelativeLayout)fv(R.id.vplay_root);
				preview=(LinearLayout)fv(R.id.vplay_preload);
				dandbg=(TextView)fv(R.id.vplay_dandbg);
				loadStringParam(new String[]{title,partname,vid,cid,cookie},"title,partname,vid,cid,cookie".split(","));
				onlineplay=getIntent().getBooleanExtra("onlineplay",false);
				clearStringNull(title,partname,vid,cid,cookie);
				
				itv.setBackgroundResource(R.drawable.anim_bilitv);//设置为动态
				AnimationDrawable bg=(AnimationDrawable)itv.getBackground();
				bg.start();
				break;
			case 1://加载
				
			case 10:
				preview.removeAllViews();//last todo
		}
		hasinit++;
		return hasinit<2;
	}
	//RelativeLayout root;
	LinearLayout preview; TextView playdbg,dandbg;
	VideoView vv;
	void plog(String s){playdbg.append(s);}
	void dlog(String s){dandbg.append(s);}
	//void handerMessage(){}
	protected void onStart(){super.onStart();}
	void go(){
		VideoView v;
		Uri.Builder i=new Uri.Builder();

	}
	View fv(int id){return findViewById(id);}
	void clearStringNull(String...s){for(String s2:s)if(s2==null)s2="";}
	void loadStringParam(String[]s,String[]id){for(int i=0,sl=s.length;i<sl;i++)s[i]=getIntent().getStringExtra(id[i]);}
}
