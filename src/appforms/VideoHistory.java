package appforms;

import android.app.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.LEORChn.SimpleBili.*;
import leorchn.lib.*;

import static leorchn.lib.Global.*;
import simplebili.lib.*;

public class VideoHistory extends Activity1 implements OnClickListener,MessageQueue.IdleHandler ,OnGenericMotionListener,AbsListView.OnScrollListener{
	Activity This; public Activity getContext(){return This;}
	String cookie="";
	protected void onCreate(Bundle sis) {
        super.onCreate(sis);
		This=this;
		addIdleHandler();
	}
	int hasinit=0;
	@Override public boolean onIdle() {
		switch(hasinit){case 0:
				setContentView(R.layout.activity_history);
				cookie=getIntent().getStringExtra("cookie"); if(cookie==null)cookie="";
				break;
			case 1:
				ListView vl=(ListView)fv(R.id.history_list);
				vl.setOnScrollListener(this); //允许用手指刷动态
				vl.setOnGenericMotionListener(this); //允许用鼠标滚轮刷动态
				hl=new HistoryListControl(this,vl,cookie);
				btnbind(R.id.history_goback,R.id.history_refreshfromfirstpage);
				getMoreHistory(1);
		}
		hasinit++;
		return hasinit<9;
	}
//监听器 开始
	@Override
	public void onClick(View v) {switch(v.getId()){
		case R.id.history_goback: finish();break;
		case R.id.history_refreshfromfirstpage: if(listupdating)return;hl.clear();getMoreHistory(1); break;
	}}
	
	public boolean onGenericMotion(View v,MotionEvent et){//响应鼠标滚轮更新列表
		if((v instanceof AbsListView) && //instanceof:确保是动态列表的操作。getAction:确保是鼠标滚轮。getAxisValue<0:确保是向下滚动
		   et.getAction()==et.ACTION_SCROLL && et.getAxisValue(et.AXIS_VSCROLL)<0f)
			onScrollStateChanged((AbsListView)v,0);
		return super.onGenericMotionEvent(et);
	}
	public void onScrollStateChanged(final AbsListView p1, int p2){//响应手指滑动更新列表
		if(p2==0 && (p1.getLastVisiblePosition()+5)>p1.getCount()){
			getMoreHistory((p1.getCount()/20)+1);
		}
	} //switch(p2){case 0:case 1:};//0=stop; 1=scrolling; 2=fastscrolling
	public void onScroll(AbsListView p1, int p2, int p3, int p4) {}
//监听器 结束
	HistoryListControl hl;
	boolean listupdating=false;
	void getMoreHistory(final int page){
		if(!listupdating){
			listupdating=true;
			new Thread(){public void run(){
					nextHistory(page);
					try{Thread.sleep(1000);}catch(Exception e){}//读取数据之后，给1秒时间更新界面，1秒之后才能读取下一页动态
					listupdating=false;//现在可以请求读取下一页数据了
				}}.start();
		}
	}
	static class Formater{
		static String format(long i){int sec=(int)Math.floor((i % 60000)/1000);return ((int)Math.floor(i / 60000))+":"+(sec<10?"0"+sec:sec);}
	}
	void nextHistory(int page){
		
		Http h1=new Http("GET","http://api.bilibili.com/x/v2/history?ps=20&pn="+page, cookie, ""){
			void fin(String data){
				FSON j=new FSON(data);
				if(!j.canRead())return;
					int runtime=0;
				try{
					int execResult=j.get("code",1002);//read the new video update
					if(execResult !=0)return;//data wrong, for example -101
					FSON j2=j.getList("data");
					if(!j2.canRead())return;
					for(int i=0,len=j2.length();i<len;i++){
						final FSON d=j2.getObject(i)
							,d2=d.getObject("stat");
						final String vid=d.get("aid","-1"),
							uid=d.getObject("owner").get("mid","-1");
						
									try{
										String progtext=(d.get("device", 3) == 2 ?"电脑 ": "");
										int watchpart=0,watchto;
										if(d.getObject("page")!=null){
											watchpart=d.getObject("page").get("page",1);
											if(watchpart != 1)progtext+="第"+watchpart+"段 "; }
										watchto=d.get("progress",0);
										progtext+=watchto>0?Formater.format(watchto*1000):watchto==-1?"已看完":"";
										hl.additem(d.getObject("owner").get("name", "UP主信息错误"),
												   时间.动态时间差(d.get("view_at", 0)),
												   d.get("title", "标题信息错误"),
												   progtext,
												   watchpart,//在HistoryListControl里面已经减一过了。//如果没获取到，就传0，获取到就传段数。传出的段数会比焦点索引多1，但是不是bug，因为列表获取焦点后正好多向下展示1格，可以让用户选择是播放当前段还是下一段
												   d.get("pic", ""),
												   d2.get("view", "0"),
												   d2.get("danmaku", "0"),
												   uid,
												   vid);
									}catch(Exception e){new Msgbox("Data Error",d.toString(),"ok");}
								
						runtime=i;
						//	列表封面(vid,d.get("pic",""));//ˉ↓
					}
					
								hl.refresh();
					
				}catch(Exception nfe){//NumberFormatException
					final String detail=runtime+"ran times:"+E.trace(nfe)+j.toString();
					//runOnUiThread(new Runnable(){public void run(){
					int chose=信息框2(This,"Error 1002",detail,"忽略错误并继续","复制错误信息");
					if(chose==1){ 复制文本(detail); tip("复制成功"); }
					//		}});
					
				}
				
				
			}
		};
	}
}
