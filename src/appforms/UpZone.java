package appforms;

import android.app.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import java.text.*;
import leorchn.lib.*;
import simplebili.lib.*;

import static leorchn.lib.Global.*;
import static leorchn.lib.WidgetOverride.*;
import android.view.ViewTreeObserver.*;

public class UpZone extends Activity1 implements OnClickListener,MessageQueue.IdleHandler ,OnGenericMotionListener,OnScrollChangedListener,AbsListView.OnScrollListener,Follow.OnFollowListener{
	Activity This; public Activity getContext(){return This;}
	String uid="",cookie="",
		contenttype="Content-Type: application/x-www-form-urlencoded\r\n",
		referer="Referer: http://space.bilibili.com/\r\n";
	protected void onCreate(Bundle savedInstanceState) {
		This=this;
		addIdleHandler();
		super.onCreate(savedInstanceState);
	}
	protected void onStop() {
		Follow.removeOnFollowListener(this);//如果不remove可能会造成内存泄漏
		super.onStop();
	}
	public boolean onIdle(){
		switch(hasinit){
			case 0:
				setContentView(layout.activity_up_zone);
				uid=getIntent().getStringExtra("space"); if(uid==null || uid.isEmpty()){ finish();return false; }//没传进up主id还看个屁的空间？果断结束activity
				cookie=getIntent().getStringExtra("cookie"); if(cookie==null)cookie="";
				break;
			case 1:
				infoarea=fv(id.upzone_infoarea);
				sv=(ScrollView)fv(id.upzone_vlist);
				sv.getViewTreeObserver().addOnScrollChangedListener(this);
				//vl.setOnScrollListener(this); //允许用手指刷
				sv.setOnGenericMotionListener(this); //允许用鼠标滚轮刷
				l=new UpZoneListControl(this,sv,cookie);
				
				name=(TextView)fv(id.upzone_upname);
				ul=(TextView)fv(id.upzone_upul);
				exp=(TextView)fv(id.upzone_upexp);
				subs=(TextView)fv(id.upzone_subs);
				fans=(TextView)fv(id.upzone_fans);
				vcount=(TextView)fv(id.upzone_videocount);
				desc=(TextView)fv(id.upzone_desc);
				addsubs=(Button)fv(id.upzone_addsubs); //visible(addsubs,false);//主动隐藏（请在布局里隐藏）
				btnbind(id.upzone_goback,id.upzone_msgp2p, id.upzone_addsubs,id.upzone_subs,id.upzone_fans);
				break;
			case 2:
				Follow.addOnFollowListener(this);
				loadUpzone(uid);
				loadUpzoneVideo(1);
				Follow.check(uid,-1,cookie);
				break;
		}
		hasinit++;
		return hasinit<9;
	}
	int hasinit=0,followstat=0;
	ScrollView sv;
	UpZoneListControl l;
	Button addsubs;
//监听器 开始
	public void onClick(View v) {switch(v.getId()){
		case id.upzone_goback: finish(); break;
		case id.upzone_msgp2p: tip("此功能暂未开放"); return;//私信功能todo
		case id.upzone_addsubs: //加关注or取消关注
			if(followstat==0){
				if(!Follow.follow(uid,cookie))multip("先休息一下好不好？");
				else addsubs.setText("操作中");
			}else{ String[]opt=new String[50]; for(int i=0;i<50;i++)opt[i]=String.valueOf(i);
				if(列表信息框(this,"取消关注此人？请选择 32",opt)==32)
				if(!Follow.unfollow(uid,cookie))multip("先休息一下好不好？");
				else addsubs.setText("操作中");
			}
			break;
		case id.upzone_subs: //看他关注的人todo
		case id.upzone_fans: tip("此功能暂未开放");//看关注他的人todo
	}}
	public void onFollowStatChange(String fid, boolean stat) {
		if(uid.equals(fid)){
			followstat=stat?1:0;
			visible(addsubs,true);
			addsubs.setText(stat?"已关注":"+关注");
		}
	}
	public boolean onGenericMotion(View v,MotionEvent et){//响应鼠标滚轮更新列表
		if((v instanceof AbsListView) && //instanceof:确保是动态列表的操作。getAction:确保是鼠标滚轮。getAxisValue<0:确保是向下滚动
		   et.getAction()==et.ACTION_SCROLL && et.getAxisValue(et.AXIS_VSCROLL)<0f)
			onScrollStateChanged((AbsListView)v,0);
		return super.onGenericMotionEvent(et);
	}
	public void onScrollChanged(){
		if(sv.getScrollY()>sv.getChildAt(0).getHeight()-1.5*sv.getHeight())
			if(l.getCount()%20==0) loadUpzoneVideo(l.getCount()/20+1);
	}
	public void onScrollStateChanged(AbsListView p1, int p2){//响应手指滑动更新列表
		//if(p2==0)visible(infoarea,firstvisibleitem==0);//这个位置有一点不好，就是向上滚轮时即使到第一个也不会显示顶部信息，需要再向下滚轮一次才显示
		multip("last:"+p1.getLastVisiblePosition()+"\ntotal:"+p1.getCount());
		if(p2==0 && (p1.getLastVisiblePosition()+5)>p1.getCount()){
			//有些人的空间读到最后一页时就不够20个了，这会导致重复加载最后一页。此问题已修复
			if(p1.getCount()%20==0) loadUpzoneVideo(p1.getCount()/20+1);
		}
	} int firstvisibleitem=0;//switch(p2){case 0:case 1:};//0=stop; 1=scrolling; 2=fastscrolling
	public void onScroll(AbsListView p1, int p2, int p3, int p4) { firstvisibleitem = p2; }
//监听器 结束
	View infoarea;
	TextView name,ul,exp,subs,fans,vcount,desc;
	int loadUpzone(final String uid){
		if(uid==null || uid.isEmpty())return 1003;//1003=程序内部错误
		new AsyncTask<Void,FSON,Object>(){
			@Override protected Object doInBackground(Void[]v){
				String data=http("POST","http://space.bilibili.com/ajax/member/GetInfo",contenttype+referer+cookie,"mid="+uid);
				if(data.isEmpty())return 1000;
				FSON j=new FSON(data);
				if(!(j.canRead()&&j.isObject()))return 1001;
				try{
					boolean execResult=j.get("status",false);
					if(!execResult)return 10011;//data wrong, for example -101
					publishProgress(j.getObject("data"));
					//cut-cut-cut-cut-cut-
					data=http("GET","http://api.bilibili.com/x/relation/stat?vmid="+uid,"","");
					j=new FSON(data);
					if(!(j.canRead()&&j.isObject()))return 1001;
					int eres=j.get("code",10012); if(eres != 0)return eres;
					return j.getObject("data");
				}catch(Exception nfe){//NumberFormatException
					final String detail=E.trace(nfe)+j.toString();
					runOnUiThread(new Runnable(){public void run(){
							new Msgbox("Error 1002",detail,"忽略错误并继续","复制错误信息"){
								void onClick(int chose){
									if(chose==1){ 复制文本(detail); tip("复制成功"); }
								}
							};
							}});
					return 1002;
				}
			}
			@Override protected void onProgressUpdate(FSON[]f){
				FSON n=f[0];
				name.setText(n.get("name","(信息错误)"));
				FSON li=n.getObject("level_info");
				int lvl=li.get("current_level",0),cur=li.get("current_exp",0),min=li.get("current_min",0),max=li.get("next_exp",cur);//max默认值为1是为了防止除以零错误
				ul.setText("Lv."+lvl);
				exp.setText(cur+(lvl<6 && lvl>0?"/"+max+" ("+((cur-min)*100/(max-min))+"%)":""));
				int[]lvc={color.ul0,color.ul1,color.ul2,color.ul3,color.ul4,color.ul5,color.ul6};
				ul.setBackgroundResource(lvc[lvl]);
				exp.setBackgroundResource(lvc[lvl]);
				desc.setText(n.get("sign","(信息错误)"));
			}
			@Override protected void onPostExecute(Object o){
				if(o instanceof FSON){
					FSON m=(FSON)o;
					subs.setText("关注 "+m.get("following",0));
					fans.setText("粉丝 "+m.get("follower",0));
				}else if(o instanceof Integer){
					
				}
			}
		}.execute();
			
		return 0;
	}
	boolean listupdating=false;
	void loadUpzoneVideo(final int page){
		if(!listupdating){ /*debug tips*/Toast t=Toast.makeText(this,System.currentTimeMillis()+" load page "+page+"\nlist count "+l.getCount(),0);t.setGravity(3|48,0,0);t.show();
			listupdating=true;
			new Thread(){public void run(){
					nextpage(page);
					try{Thread.sleep(1000);}catch(Exception e){}//读取数据之后，给1秒时间更新界面，1秒之后才能读取下一页动态
					listupdating=false;//现在可以请求读取下一页数据了
				}}.start();
		}
	}
	int nextpage(int page){
		String data=http("GET","http://space.bilibili.com/ajax/member/getSubmitVideos?pagesize=20&page="+page+"&mid="+uid,cookie, "");
		if(data.isEmpty())return 1000;
		FSON j=new FSON(data);
		if(!(j.canRead() && j.isObject())){
			new Msgbox("1001",data,"ok");return 1001;
		} int runtime=0;
		try{
			boolean execResult=j.get("status",false);//read the new video update
			if(!execResult)return 10021;//data wrong, for example -101
			j=j.getObject("data");
			final String vcountText="视频 "+j.get("count",0);
			if(j.get("pages",0)<page)return 0;//页数超出允许范围，在此处阻止读取
			FSON j2=j.getList("vlist");
			for(int i=0,len=j2.length();i<len;i++){
				final FSON d=j2.getObject(i);
				final String vid=d.get("aid","-1");
				runOnUiThread(new Runnable(){public void run(){
					vcount.setText(vcountText);
					l.additem(
						d.get("length", ""),
						formater.format(1000l*d.get("created",0)),
						d.get("title", "标题信息错误"),
						d.get("pic", ""),
						d.get("play", "0"),
						d.get("video_review", "0"),
						d.get("comment", "0"),
						vid);
					}});
				runtime=i;
				//	列表封面(vid,d.get("pic",""));//ˉ↓
			}
			//runOnUiThread(new Runnable(){public void run(){ l.refresh(); }});
		}catch(Exception nfe){//NumberFormatException
			final String detail=runtime+"ran times:"+E.trace(nfe)+j.toString();
			runOnUiThread(new Runnable(){public void run(){
				int chose=信息框2(This,"Error 1002",detail,"忽略错误并继续","复制错误信息");
				if(chose==1){ 复制文本(detail); tip("复制成功"); }
			}});
			return 10020;
		}
		return 0;
	}
	SimpleDateFormat formater=new SimpleDateFormat("yyyy-MM-dd");
}
