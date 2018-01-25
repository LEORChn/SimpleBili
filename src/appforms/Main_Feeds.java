package appforms;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.webkit.*;
import android.widget.*;
import com.LEORChn.SimpleBili.*;
import formevents.*;
import leorchn.lib.*;
import simplebili.lib.*;

import static leorchn.lib.Global.*;
import static leorchn.lib.WidgetOverride.*;
//import static formevents.程序事件.*;
public class Main_Feeds extends Activity1 implements OnClickListener,OnKeyListener,OnGenericMotionListener,AbsListView.OnScrollListener{
    FeedListControl flc;
    WebView w;
	ImageView h;
	static Activity This; public static Activity getContext(){return This;}
    protected void onCreate(Bundle savedInstanceState) {
		This=this;
		addIdleHandler();
        super.onCreate(savedInstanceState);
		startActivity(new Intent(this,Settings.class).putExtra("launchfromfeed",true)); finish();
	}
	ImageView myhead;
	TextView name,coin;
	int hasinit=0;
	@Override protected boolean onIdle() {
		switch(hasinit){
			case 0: setContentView(R.layout.activity_main); break;
			case 1:
				if(mcok==null){//检测到帐户无效，打开设置界面重设帐户
					startActivity(new Intent(this,Settings.class));
					finish();
					return false;
				}
				ListView vl=(ListView)fv(id.main_feedlist);//提取动态列表组件
				vl.setOnScrollListener(this); //允许用手指刷动态
				vl.setOnGenericMotionListener(this); //允许用鼠标滚轮刷动态
				
				flc=new FeedListControl(this,vl,mcok);
				myhead=(ImageView)fv(id.main_myhead);
				name=(TextView)fv(id.main_myname);
				coin=(TextView)fv(id.main_mycoin);
				
				btnbind(id.main_search,id.main_download,id.main_gosetting,id.main_menu,id.main_refreshfromfirstpage);
				userinf=fv(id.main_userinfo);
				searchbox=(EditText)fv(id.main_searchbox);
				searchbox.setOnKeyListener(this);
				visible(searchbox,false);
				break;
			case 3:
				
		}
		hasinit++;
		return hasinit<9;
	}
	View userinf;EditText searchbox;
	//监听器 开始
	public void onClick(View v){ switch(v.getId()){
		case id.main_search:
			visible(userinf,!visible(userinf));
			if(!visible(searchbox)){
				visible(searchbox,true);//已修BUG:输入框出现时，请自动获取焦点
				searchbox.setText("");
				searchbox.setFocusableInTouchMode(true);
				new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){public void run(){
					searchbox.requestFocus();
				}},1000);
			}else visible(searchbox,false);
			break;
		case id.main_download: tip("下载功能以后开放...");break;
		case id.main_gosetting: startActivity(new Intent(This,Settings.class)); break;
		case id.main_menu: this.openOptionsMenu();break;
		case id.main_refreshfromfirstpage: if(!feedupdating){ flc.clear(); getVideoUpdates(1); } break;
	}}
	int tms=0; boolean loadover=false;
	public boolean onGenericMotion(View v,MotionEvent et){//响应鼠标滚轮更新列表
		if((v instanceof AbsListView) && //instanceof:确保是动态列表的操作。getAction:确保是鼠标滚轮。getAxisValue<0:确保是向下滚动
			et.getAction()==et.ACTION_SCROLL && et.getAxisValue(et.AXIS_VSCROLL)<0f)
				onScrollStateChanged((AbsListView)v,0);
		return super.onGenericMotionEvent(et);
	}
	public void onScrollStateChanged(final AbsListView p1, int p2){//响应手指滑动更新列表
		if(p2==0 && (p1.getLastVisiblePosition()+5)>p1.getCount()){
			getVideoUpdates((p1.getCount()/20)+1);
		}
	} //switch(p2){case 0:case 1:};//0=stop; 1=scrolling; 2=fastscrolling
	public void onScroll(AbsListView p1, int p2, int p3, int p4) {}//if((p2+p3+5)>p4){}//刷新动态((p4/20)+1);//刷出下一页动态
	public boolean onKey(View p1, int p2, KeyEvent p3) {
		if(p2==p3.KEYCODE_ENTER || p2==p3.KEYCODE_DPAD_CENTER){
			Intent it=new Intent(this,VideoDetail.class)
				.putExtra("vid",searchbox.getText().toString())
				.putExtra("cookie",mcok);
			this.startActivity(it);
		}
		return false;
	}

	
	//监听器 结束
	AsyncTask updater;
	boolean feedupdating=false;
	void getVideoUpdates(final int page){
		if(updater==null || updater.getStatus()==AsyncTask.Status.FINISHED)
			getFeeds(page);
	}
	void getFeeds(final int page){
		updater=new AsyncTask<Void,FSON,Integer>(){
			@Override protected Integer doInBackground(Void[] p1) {
				String data=http("GET","http://api.bilibili.com/x/web-feed/feed?ps=20&pn="+page,mcok,"");
				FSON j=new FSON(data);
				if(j.canRead()){
					//int runtime=0;
				
					int execResult=j.get("code",1002);//read the new video update
					if(execResult !=0)return execResult;//data wrong, for example -101
					FSON j2=j.getList("data");
					if(j2!=null){
						for(int i=0,len=j2.length();i<len;i++) {
							final FSON d=j2.getObject(i).getObject("archive"),
								d2=d.getObject("stat");
							publishProgress(d, d2);
							//runtime=i;
							//	列表封面(vid,d.get("pic",""));//ˉ↓
						}
						publishProgress(null);
					}
					//}catch(Exception nfe){//NumberFormatException
					/*final String detail=runtime+"ran times:"+j.toString()+java.util.Arrays.toString(nfe.getStackTrace());
					mainhandler.obtainMessage(2,new Runnable(){public void run(){
								int chose=信息框2("Error 1002",detail,"忽略错误并继续","复制错误信息");
								if(chose==1){ 复制文本(detail); tip("复制成功"); }
							}}).sendToTarget();
					return 1002;*/
					return 0;
				}
				return 1001;
			}
			@Override protected void onProgressUpdate(FSON[]d){
				if(d==null) flc.refresh();
				else{
					flc.additem(d[0].getObject("owner").get("name","UP主信息错误"),
								时间.动态时间差(d[0].get("pubdate",0)),
								d[0].get("title","标题信息错误"),
								d[0].get("pic",""),
								d[1].get("view","0"),
								d[1].get("danmaku","0"),
								d[0].getObject("owner").get("mid","-1"),
								d[0].get("aid","-1"));
				}
			}
			@Override protected void onPostExecute(Integer i){
				
			}
		}.execute();
		
	}
	
	long lastReqExit=0;
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==4)
			if(System.currentTimeMillis()-lastReqExit<3000){finish();}else{
				tip("再按一次退出简哔。");
				lastReqExit=System.currentTimeMillis();
			}
		return false;//super.onKeyDown(keyCode, event);
	}
	final int menuidx=Menu.FIRST;
	public boolean onCreateOptionsMenu(Menu menu) {
		String[]menus="我的收藏,历史记录,关注的人,关于简哔".split(",");
		int[]menuicons={drawable.topmenu_stared, drawable.topmenu_history, drawable.topmenu_people,drawable.topmenu_about};
		enableMenuIcon(menu);
		for(int i=0;i<menus.length;i++)
			menu.add(0,menuidx+i,0,menus[i]).setIcon(menuicons[i]);
		return super.onCreateOptionsMenu(menu);
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()-menuidx){
			//case 0: break;
			case 1: startActivity(new Intent(this,VideoHistory.class).putExtra("cookie",mcok)); break;
			case 3: this.closeOptionsMenu();
				int chose=列表信息框(this,"关于简哔","小瑞的空间","GitHub");
				switch(chose){
					case 0: startActivity(new Intent(this,UpZone.class).putExtra("space","3084436").putExtra("cookie",mcok)); break;
					case 1: startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/LEORChn/SimpleBili"))); break;
					case -1:
				}
				break;
			default: tip("此功能以后开放..."); break;
		}return super.onOptionsItemSelected(item);
	}
	static int 宽度(View 可视块){
		int ms=View.MeasureSpec.makeMeasureSpec(0,0);
		可视块.measure(ms,ms);
		return 可视块.getMeasuredWidth();
	}
	static int 高度(View 可视块){
		int ms=View.MeasureSpec.makeMeasureSpec(0,0);
		可视块.measure(ms,ms);
		return 可视块.getMeasuredHeight();
	}
}
