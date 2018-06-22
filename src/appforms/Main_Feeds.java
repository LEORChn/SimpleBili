package appforms;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import leorchn.lib.*;
import simplebili.lib.*;

import static leorchn.lib.Global.*;

public class Main_Feeds extends Activity1 implements OnClickListener,OnKeyListener{//},OnGenericMotionListener{//},AbsListView.OnScrollListener{
	protected void onCreate(Bundle savedInstanceState) {
		addIdleHandler();
		super.onCreate(savedInstanceState);
		//startActivity(new Intent(this,Settings.class).putExtra("launchfromfeed",true)); finish();
	}
	int hasinit=0;

	@Override protected void onResume(){
		super.onResume();
		if(hasinit==0)return;
		setText(fv(id.main_myname),user.getName());
		setUserHead(fv(id.main_myhead),user.getUid());
	}
	@Override protected boolean onIdle() {
		switch(hasinit){
			case 0: setContentView(layout.activity_main); break;
			case 1:
				onResume();
				sv=new ScrollViewV((ScrollView)fv(id.main_feedlist)){
					@Override public void onScroll(int from,int to){
						onScrolling(from,to);
					}
				};//提取动态列表组件
				
				coin=(TextView)fv(id.main_mycoin);
				
				userinf=fv(id.main_userinfo);
				searchbox=(EditText)fv(id.main_searchbox);
				searchbox.setOnKeyListener(this);
				visible(searchbox,false);
				if(false&&mcok==null){//强行阻止，检测到帐户无效，打开设置界面重设帐户
					startActivity(Settings.class);
					return false;
				}
				btnbind(id.main_search,id.main_download,id.main_gosetting,id.main_menu,id.main_refreshfromfirstpage);
				break;
			case 3:
				seticon(fv(id.main_search),ic_sys(d.topmenu_search));
				seticon(fv(id.main_download),ic_sys(d.topmenu_downloads));
				seticon(fv(id.main_gosetting),ic_sys(d.topmenu_setting));
				seticon(fv(id.main_menu),ic_sys(d.topmenu_menu));
				seticon(fv(id.main_refreshfromfirstpage),ic_sys(d.topmenu_refresh));
				getVideoUpdates(1);
		}
		hasinit++;
		return hasinit<9;
	}
	View userinf;EditText searchbox; TextView name,coin;
	ScrollViewV sv;
	//监听器 开始
	public void onClick(View v){ switch(v.getId()){
		case id.main_search:
			visible(userinf,!visible(userinf));
			if(!visible(searchbox)){
				visible(searchbox,true);//已修BUG:输入框出现时，请自动获取焦点
				searchbox.setText("");
				searchbox.setFocusableInTouchMode(true);
				searchbox.postDelayed(new Runnable(){public void run(){
					searchbox.requestFocus();
				}},1000);
			}else visible(searchbox,false);
			break;
		case id.main_download: tip("下载功能以后开放...");break;
		case id.main_gosetting: startActivity(Settings.class); break;
		case id.main_menu: this.openOptionsMenu();break;
		case id.main_refreshfromfirstpage: if(!feedupdating){ sv.clear(); getVideoUpdates(1); } break;
		case id.listsub_videofeeds:
			startActivity(new Intent(this,VideoDetail.class).putExtra("vid",(String)v.getTag())); break;
		case id.listsub_auth_name:
			startActivity(new Intent(this,UpZone.class).putExtra("space",(String)v.getTag())); break;
			
	}}
	int tms=0; boolean loadover=false;
	public boolean onKey(View p1, int p2, KeyEvent p3) {
		if(p2==p3.KEYCODE_ENTER || p2==p3.KEYCODE_DPAD_CENTER)
			startActivity(new Intent(this,VideoDetail.class).putExtra("vid",searchbox.getText().toString()));
		return false;
	}
	void onScrolling(int f,int t){
		int listheight=sv.getMaxScroll(),
			windowheight=sv.getWindowHeight();
		if(t > listheight- 1.5*windowheight)
			getVideoUpdates(sv.getInner().getChildCount()/20+1);
		//multip("from "+f+" scroll to: "+t+"\nlistheight: "+listheight+" windowheight:"+windowheight);
	}

	//监听器 结束
	Http updater;
	boolean feedupdating=false;
	void getVideoUpdates(final int page){
		if(updater!=null) return;
		updater=new Http("GET","http://api.bilibili.com/x/web-feed/feed?ps=20&pn="+page,string(UA,UA_win,"\r\n",mcok),""){
			@Override protected void onload(String data){
				FSON j=new FSON(data),j2;
				if(!j.canRead()){
					multip("网络连接失败，请再试一次"); return;
				}
				if(j.get("code",1002)!=0){
					multip("数据读取错误，请检查帐号状况"); return;
				}
				if((j2=j.getList("data"))==null){
					multip("翻到底了哟，没有更多数据"); return;
				}
				for(int i=0,len=j2.length();i<len;i++) {
					final FSON d=j2.getObject(i).getObject("archive"),
						d2=d.getObject("stat"),
						owninfo=d.getObject("owner");
					ViewGroup sub=inflateView(layout.listsub_video_feeds_basic);
					sub.setTag(d.get("aid","-1"));
					TextView owner=(TextView)fv(sub,id.listsub_auth_name);
					setText(owner,owninfo.get("name","UP主信息错误"));
					owner.setTag(owninfo.get("mid","-1"));
					setText(fv(sub,id.listsub_submit_time),时间.动态时间差(d.get("pubdate",0)));
					setText(fv(sub,id.listsub_v_title),d.get("title","标题信息错误"));
					setText(fv(sub,id.listsub_v_played),d2.get("view","0"));
					setText(fv(sub,id.listsub_v_dan),d2.get("danmaku","0"));
					//((ImageView)fv(sub,id.listsub_auth_img)).setImageBitmap(ic_sys(draw.img_def));
					//((ImageView)fv(sub,id.listsub_v_img)).setImageBitmap(ic_sys(draw.img_def));
					icon.user(fv(sub,id.listsub_auth_img),owninfo.get("face",""));
					//icon.cover(fv(sub,id.listsub_v_img),d.get("pic",""));
					((viewproxy.ImageView)fv(sub,id.listsub_v_img)).loadCover(d.get("pic",""));
					seticon(fv(sub,id.listsub_ic_slideshow),ic_sys(draw.ic_menu_slideshow));
					seticon(fv(sub,id.listsub_ic_send),ic_sys(draw.ic_menu_send));
					btnbind(sub,owner);
					sv.addView(sub);
				}
				updater=null;
			}//update ui here
		};
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
	public boolean onCreateOptionsMenu(Menu m) {
		getMenuInflater().inflate(menu.main_nav,m);
		return super.onCreateOptionsMenu(m);
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case id.menuitem_history: startActivity(VideoHistory.class); break;
			case id.menuitem_downloads: tip("下载功能以后开放..."); break;
			case id.menuitem_stars:
			case id.menuitem_follows:
				tip("此功能以后开放...");break;
			case id.menuitem_leorchn: startActivity(new Intent(this,UpZone.class).putExtra("space","3084436")); break;
			case id.menuitem_github: startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/LEORChn/SimpleBili"))); break;
				
		}return super.onOptionsItemSelected(item);
	}
}
