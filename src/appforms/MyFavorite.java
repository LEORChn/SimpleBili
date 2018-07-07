package appforms;
import leorchn.lib.*;
import android.view.*;
import android.widget.AdapterView.*;
import android.widget.*;
import viewproxy.*;
import viewproxy.ListView;
import java.util.*;

public class MyFavorite extends Activity1 implements OnItemSelectedListener,OnItemClickListener,ListView.OnListNeedsUpdateListener{
	int fromSpace=0;
	String fromSpaceName="";
	int nowSpace=0,
		userUid=0;
	MyFavorite This;
	@Override protected void oncreate() {
		This=this;
		fromSpace=getIntent().getIntExtra("fromUid",0);
		if(fromSpace>0)fromSpaceName=getIntent().getStringExtra("fromSpace");
	}
/* 调用 onResume 时可能有以下 10种 情况，左侧表示帐号列表是否是需要更新
	【需要更新】未登录时首次访问别人空间
	【不需更新】未登录时访问别人空间，但是去了一下设置页面后回来，仍然未登录
	【需要更新】未登录时访问别人空间，但是去设置页面已登录后回来
	【需要更新】已登录时首次访问别人空间
	【不需更新】已登录时访问别人空间，但是去了一下设置页面后回来，帐号未变更
	【需要更新】已登录时访问别人空间，但是去设置页面更换帐号（包含注销）后回来
	【需要更新】已登录时首次访问自己空间
	【不需更新】已登录时访问自己空间，但是去了一下设置页面后回来，帐号未变更
	【需要更新】已登录时访问自己空间，但是去设置页面更换帐号后回来
	【需要更新，这会导致页面无效】已登录时访问自己空间，但是去设置页面注销帐号后回来
*/
	@Override protected void onResume(){
		super.onResume();
		if(hasinit==0)return; //界面初始化尚未完成时退出
		int myNewUid=user.getUid(); //尝试检查当前帐号或帐号变更
		if(myNewUid==0 && fromSpace==0){ //新获取的用户所用帐号和来源空间看起来似乎都不可用
			tip("未登录帐号无法访问自己的空间。");
			finish();
			return;
		}
		if(userUid==myNewUid)return; //窗口恢复时未改变帐号 时，帐号列表不做更新
		userUid=myNewUid; //帐号有变更，更新为新帐号的组件
		String[]s=fromSpace==0?
			new String[]{user.getName()}://查看自己空间时
			myNewUid==0?
				new String[]{fromSpaceName}://未登录时查看别人空间
				new String[]{fromSpaceName,user.getName()};//已登录时查看别人空间
		Spinner sp=(Spinner)fv(id.favorite_userswitch);
		sp.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, s));
		sp.setOnItemSelectedListener(this);
	}

	@Override protected boolean onIdle(){
		switch(hasinit){
			case 0:
				setContentView(layout.activity_favorite);
				break;
			case 1:
				pager=(ViewPager)fv(id.favorite_viewpager);
				ListView ls=(ListView)fv(id.favorite_folderlist);
				ls.setAdapter(fl);
				ls.setOnItemClickListener(this);
				View loadingcircle=new ProgressBar(this);
				loadingcircle.setPadding(0,50,0,50);//顶部和底部padding
				ls=(ListView)fv(id.favorite_videolist);
				ls.setAdapter(vl,loadingcircle);
				ls.setOnItemClickListener(this);
				ls.setOnListNeedsUpdateListener(this);
				btnbind(id.favorite_goback);
				break;
			case 2:
				onResume();//包含有帐号检测相关，并在之后由帐号选择器Spinner自动加载
		}
		hasinit++;
		return hasinit<9;
	}
	int hasinit=0;
	ViewPager pager;
	FavoriteListControl fl=new FavoriteListControl(),
		vl=new FavoriteListControl();
	//监听器 区域
	@Override public void onClick(View v) {
		switch(v.getId()){
			case id.favorite_goback: onKeyDown(4,null); break;
			//tip(pager.getScrollX()+"/"+pager.getMaxScroll()+"/"+pager.getWindowWidth());
		}
	}
	@Override public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4) {
		nowSpace=p3==1?
			user.getUid():
			fromSpace==0?
				user.getUid():
				fromSpace;
		getFolder();
	}
	@Override public void onNothingSelected(AdapterView<?> p1){}
	@Override public void onItemClick(AdapterView<?>l,View v,int i,long g){//当收藏夹的文件夹或视频列表项被点击时
		Holder h=(Holder)v.getTag();
		if(h==null)return;
		switch(h.type&Bean.TYPE_VIDEO){
			case 0://folder
				getVideos(fl.get(i).token,1);
				break;
			default://video
				Bean b=vl.get(i);
				startActivity(new android.content.Intent(this,VideoDetail.class)
					.putExtra("vid",b.token+"")
					.putExtra("title",b.title)
					.putExtra("name",b.upname)
					.putExtra("plays",b.count+"")
					.putExtra("danmaku",b.danmaku+""));
		}
	}
	@Override public void onListNeedsUpdate(ListView ls,BaseAdapter ba){
		
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==4)
			if(pager.getCurrentPage()==0){
				finish();
			}else{
				pager.setPage(0);
			}
		return false;//super.onKeyDown(keyCode, event);
	}
	//监听器 结束
	Http favFolderLoader;
	void getFolder(){ // 读取收藏夹目录
		if(favFolderLoader==null || favFolderLoader.isfin);else return;
		visible(pager.getPage(0),false); //隐藏收藏夹页
		fl.clear(); //重置视频列表页
		vl.clear(); //隐藏视频列表页
		visible(pager.getPage(1),true); //显示旋转圈
		favFolderLoader=new Http("get","https://api.bilibili.com/x/v2/fav/folder?vmid="+nowSpace,mcok,""){
			protected void onload(String data){
				visible(pager.getPage(0),true); //显示收藏夹页
				visible(pager.getPage(1),false); //隐藏旋转圈
				FSON j=new FSON(data);
				if(!j.canRead())return;
				if(j.get("code",-1)!=0)return;
				j=j.getList("data");
				if(j==null)return;
				for(int i=0,len=j.length();i<len;i++){
					FSON f=j.getObject(i);
					try{
						Bean b=new Bean();
						b.type=f.get("state",0);
						b.count=b.danmaku=f.get("cur_count",0);
						b.title=f.get("name","");
						b.token=f.get("fid",0);
						b.imageUrl="";//f.getList("cover").getObject(0).get("pic","");
						fl.add(b);
					}catch(Throwable e){}
				}
				fl.refresh();
			}
		};
	}
	Http favVideoLoader; //为了防止在加载中时重复加载同一页，
	void getVideos(int folderId,int page){
		pager.setPageVisible(1); //显示这个页面
		pager.setPage(1);
		if(page==1){ //加载第一页时
			vl.clear(); //刷新视频列表页，但是旋转圈可见
			if(favVideoLoader!=null){ //可能之前有未加载完的网络请求
				favVideoLoader.cancel(true); //添加一个取消标签防止它之后更改界面
				favVideoLoader=null; //重置task，允许随后创建一个新task
			}
		}
		if(favVideoLoader==null || favVideoLoader.isfin);else return;
		favVideoLoader=new Http("get",string("https://api.bilibili.com/x/v2/fav/video?vmid=",nowSpace,"&fid=",folderId,"&pn=",page),mcok,""){
			protected void onload(String data){
				if(isCancelled())return;
				FSON j=new FSON(data);
				if(!j.canRead())return;
				if(j.get("code",-1)!=0)return;
				j=j.getObject("data");
				if(j==null)return;
				j=j.getList("archives");
				if(j==null)return;
				for(int i=0,len=j.length();i<len;i++){
					FSON f=j.getObject(i);
					try{
						FSON stat=f.getObject("stat");
						Bean b=new Bean();
						b.type=Bean.TYPE_VIDEO | f.get("state",0);
						b.title=f.get("title","");
						b.upname=f.getObject("owner").get("name","");
						b.count=stat.get("view",0);
						b.danmaku=stat.get("danmaku",0);
						b.token=f.get("aid",0);
						b.imageUrl=f.get("pic","");
						vl.add(b);
					}catch(Throwable e){}
				}
				vl.refresh();
			}
		};
	}
	class Bean{
		static final int //右起，Type的第一位表示可能无法访问，第二位表示类型
			TYPE_SYSTEM=0,
			TYPE_FOLDER=0,
			TYPE_PRIVATE=1,
			TYPE_USER=2,
			TYPE_VIDEO=4,
			TYPE_VIDEO_DEAD=TYPE_VIDEO|TYPE_PRIVATE,
			LAYOUT_FOLDER=layout.listsub_favorite_folder,//表示item类型
			LAYOUT_VIDEO=layout.listsub_favorite_video;
		int type, //常量表示。决定页面item类型
			count, //表示文件夹视频数量，也表示视频播放次数
			danmaku, //表示视频弹幕量
			token; //表示文件夹的id，也表示视频的id
		int reply, //仅用于视频列表 - 回复数
			like, //仅用于视频列表 - 赞数
			dislike; //仅用于视频列表 - 踩数
		String imageUrl, //表示文件夹或视频的封面url
			title, //表示文件夹的名称，也表示视频的标题
			upname; //表示视频上传者的名称
	}
	class FavoriteListControl extends BaseAdapter{
		ArrayList<Bean>a=new ArrayList<>();
		int listItemCount;
		public void add(Bean b){ a.add(b); listItemCount++; }
		public void clear(){ a.clear(); listItemCount=0; refresh(); }
		public void refresh(){ notifyDataSetChanged(); }
		public Bean get(int p){ return a.get(p); }
		@Override public int getCount(){ return listItemCount; }
		@Override public Object getItem(int p){ return null; }
		@Override public long getItemId(int p){ return 0; }
		@Override public View getView(int p, View v, ViewGroup p3){
			Bean b=get(p);
			Holder d;
			int style=b.type;
			if(v==null){
				v=This.inflateView(
					(style&Bean.TYPE_VIDEO)==0?
						Bean.LAYOUT_FOLDER:
						Bean.LAYOUT_VIDEO);
				ViewGroup w=(ViewGroup)v;
				d=new Holder();
				d.type=b.type;
				d.vtitle=fv(w,id.listsub_title);
				d.vvisible_vupname=fv(w,id.listsub_auth_name);
				d.vcount=fv(w,id.listsub_playcount);
				d.vcount_danmaku=fv(w,id.listsub_danmakucount);
				d.vimage=fv(w,id.listsub_v_img);
				v.setTag(d);
				if((style&Bean.TYPE_VIDEO)>0){
					seticon(fv(w,id.listsub_ic_slideshow),ic_sys(draw.ic_menu_slideshow));
					seticon(fv(w,id.listsub_ic_send),ic_sys(draw.ic_menu_send));
				}
			}else{
				d=(Holder)v.getTag();
			}
			setText(d.vtitle,b.title); // 文件夹名字 或者 视频名字
			setText(d.vvisible_vupname,(style&Bean.TYPE_VIDEO)>0? // 文件夹可见模式 或者 视频主播名字
				b.upname:
				((style&Bean.TYPE_USER)==0?
					"由系统创建，":
					"")+
				((style&Bean.TYPE_PRIVATE)>0?
					"私享":
					"公开"));
			setText(d.vcount,String.valueOf(b.count)); // 文件夹视频数量 或者 视频播放次数
			setText(d.vcount_danmaku,String.valueOf(b.danmaku)); // 文件夹视频数量（覆盖在封面上） 或者 视频弹幕数量
			
			//此处设置图片链接
			if((style&Bean.TYPE_VIDEO)>0){ //以及视频列表上的更多数据
				
			}
			return v;
		}
	}
	class Holder{
		int type;
		View vtitle,
			vvisible_vupname,
			vcount,
			vcount_danmaku,
			vimage;
		View reply,
			like,
			dislike;
	}
}
