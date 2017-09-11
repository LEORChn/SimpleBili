package appforms;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.webkit.*;
import android.widget.*;
import com.LEORChn.SimpleBili.*;
import formevents.*;
import static leorchn.lib.Global.*;
import leorchn.lib.*;
import simplebili.lib.*;

import static com.LEORChn.SimpleBili.R.id.*;
import static leorchn.lib.WidgetOverride.*;
import static formevents.程序事件.*;
public class Main_Feeds extends Activity implements OnClickListener,OnKeyListener,OnGenericMotionListener,AbsListView.OnScrollListener{
	void tip(String s){Toast.makeText(this,s,1).show();}
    FeedListControl flc;
    WebView w;
	ImageView h;
	String cookie="";
	static Activity This; public static Activity getContext(){return This;}
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		CrashHandlerReg.reg(this);
		This=this;
		setContentView(R.layout.activity_main);
	}
	boolean hasinit=false;
	protected void onStart() {
		super.onStart();
		if(!new java.io.File(getFilesDir().getPath()+"/app.setting").exists())
			startActivity(new Intent(This,Settings.class));//检测到设置文件不存在，打开设置界面并初始化设置
		if( ! hasinit){
			ListView vl=(ListView)fv(main_feedlist);//提取动态列表组件
			cookie="Cookie: "+帐户数据.cookie();
			flc=new FeedListControl(this,vl,cookie);
				初始化主页(
				(ImageView)fv(main_myhead),
				(TextView)fv(main_myname),
				(TextView)fv(main_mycoin),
				(WebView)fv(main_login),
				(ProgressBar)fv(main_webload),
				flc
			);
			vl.setOnScrollListener(this); //允许用手指刷动态
			vl.setOnGenericMotionListener(this); //允许用鼠标滚轮刷动态
			btnbind(main_search,main_download,main_gosetting,main_menu,main_refreshfromfirstpage);
			userinf=fv(main_userinfo);
			searchbox=(EditText)fv(main_searchbox);
			searchbox.setOnKeyListener(this);
			visible(searchbox,false);
			刷新帐号信息(); hasinit=true;
		}
	}
	View userinf;EditText searchbox;
	//监听器 开始
	public void onClick(View v){ switch(v.getId()){
		case main_search:
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
		case main_download: tip("下载功能以后开放...");break;
		case main_gosetting: startActivity(new Intent(This,Settings.class)); break;
		case main_menu: this.openOptionsMenu();break;
		case main_refreshfromfirstpage: if(!feedupdating){ flc.clear(); getVideoUpdates(1); } break;
	}} int tms=0;
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
				.putExtra("cookie",cookie);
			this.startActivity(it);
		}
		return false;
	}

	
	//监听器 结束
	
	boolean feedupdating=false;
	void getVideoUpdates(final int page){
		if(!feedupdating){
			feedupdating=true;
			new Thread(){public void run(){
				程序事件.读取关注动态(page);
				try{Thread.sleep(1000);}catch(Exception e){}//读取动态数据之后，给1秒时间更新界面，1秒之后才能读取下一页动态
				feedupdating=false;//现在可以请求读取下一页动态数据了
			}}.start();
		}
	}
	
	void btnbind(int...id){for(int btnid:id)btnbind(btnid);}
	void btnbind(int id){fv(id).setOnClickListener(this);}
	View fv(int id){return findViewById(id);}
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
		String[]menus="我的收藏,历史记录,关注的人".split(",");
		int[]menuicons={R.drawable.topmenu_stared, R.drawable.topmenu_history, R.drawable.topmenu_people};
		enableMenuIcon(menu);
		for(int i=0;i<menus.length;i++)
			menu.add(0,menuidx+i,0,menus[i]).setIcon(menuicons[i]);
		return super.onCreateOptionsMenu(menu);
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()-menuidx){
			//case 0: break;
			case 1: startActivity(new Intent(this,VideoHistory.class).putExtra("cookie",cookie)); break;
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
