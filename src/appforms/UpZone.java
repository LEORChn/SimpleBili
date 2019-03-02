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
import viewproxy.ListView;
import java.util.*;

public class UpZone extends Activity1 implements ListView.OnListNeedsUpdateListener,AbsListView.OnItemClickListener,Follow.OnFollowListener{
	String uid="",
		contenttype="Content-Type: application/x-www-form-urlencoded\r\n",
		referer="Referer: http://space.bilibili.com/\r\n";
	@Override protected void oncreate(){}
	@Override protected void onStop() {
		Follow.removeOnFollowListener(this);//如果不remove可能会造成内存泄漏
		super.onStop();
	}
	@Override public boolean onIdle(){
		switch(hasinit){
			case 0:
				setContentView(layout.activity_up_zone);
				uid=getIntent().getStringExtra("space"); if(uid==null || uid.isEmpty()){ finish();return false; }//没传进up主id还看个屁的空间？果断结束activity
				break;
			case 1:
				infoarea=fv(id.upzone_infoarea);
				l2=(ListView)fv(id.upzone_vlist);
				l2.setOnListNeedsUpdateListener(this);
				l2.setAdapter(inflateView(layout.listsub_upzone_info),lc);
				l2.setOnItemClickListener(this);
				
				name=(TextView)fv(id.upzone_upname);
				ul=(TextView)fv(id.upzone_upul);
				subs=(TextView)fv(id.upzone_subs);
				fans=(TextView)fv(id.upzone_fans);
				vcount=(TextView)fv(id.upzone_videocount);
				desc=(TextView)fv(id.upzone_desc);
				addsubs=(Button)fv(id.upzone_addsubs); //visible(addsubs,false);//主动隐藏（请在布局里隐藏）
				btnbind(id.upzone_goback,id.upzone_skiptodate,id.upzone_msgp2p,
					id.upzone_addsubs,id.upzone_subs,id.upzone_fans);
				break;
			case 2:
				Follow.addOnFollowListener(this);
				loadUpzone(uid);
				loadUpzoneVideo(1);
				Follow.check(uid,-1,mcok);
				break;
		}
		hasinit++;
		return hasinit<9;
	}
	int hasinit=0,followstat=0;
	ListView l2;
	UserSpaceListControl lc=new UserSpaceListControl();
	Button addsubs;
//监听器 开始
	DateVideoSkipper ds;
	@Override public void onClick(View v) {switch(v.getId()){
		case id.upzone_goback: finish(); break;
		case id.upzone_skiptodate:
			if(ds != null && !ds.isFinished()){
				ds.cancel();
				return;
			}
			final Calendar c=Calendar.getInstance();
			new DatePickerDialog(this,
				new DatePickerDialog.OnDateSetListener(){
					@Override public void onDateSet(DatePicker p,int y,int m,int d){
						c.set(y,m,d,0,0);
						ds=new DateVideoSkipper(c.getTimeInMillis()/1000+86400);//增加一天时间，这样比较靠近当天24点的视频显示在上方，靠近0点的视频在下方
					}
				},
				c.get(c.YEAR),
				c.get(c.MONTH),
				c.get(c.DAY_OF_MONTH)
			).show();
			break;
		case id.upzone_msgp2p: tip("此功能暂未开放"); return;//私信功能todo
		case id.upzone_addsubs: //加关注or取消关注
			if(followstat==0){
				if(!Follow.follow(uid,mcok))multip("先休息一下好不好？");
				else addsubs.setText("操作中");
			}else{ String[]opt=new String[50]; for(int i=0;i<50;i++)opt[i]=String.valueOf(i);
				if(列表信息框(this,"取消关注此人？请选择 32",opt)==32)
				if(!Follow.unfollow(uid,mcok))multip("先休息一下好不好？");
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
	@Override public void onListNeedsUpdate(ListView lsv,BaseAdapter ba){
		if(ba.getCount()%20==0) loadUpzoneVideo(Math.round(ba.getCount()/20f)+1);
	}
	@Override public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4){
		Bean b=lc.get(p3-1);//因为头部也占一个索引，因此
		startActivity(new android.content.Intent(this,VideoDetail.class)
			.putExtra("vid",b.token)
			.putExtra("title",b.title)
			.putExtra("plays",b.count)
			.putExtra("danmaku",b.danmaku)
			);
	}
//监听器 结束
	View infoarea;
	TextView name,ul,subs,fans,vcount,desc;
	int loadUpzone(String uid){
		if(uid==null || uid.isEmpty())return 1003;//1003=程序内部错误
		new Http("POST","https://space.bilibili.com/ajax/member/GetInfo",string(contenttype,referer,mcok),"mid="+uid){
			@Override protected void onload(String data){
				FSON j=new FSON(data);
				if(!j.canRead())return;
				try{
					boolean execResult=j.get("status",false);
					if(!execResult)return;//data wrong, for example -101
					j=j.getObject("data");
					FSON li=j.getObject("level_info");
					int lvl=li.get("current_level",0);//max默认值为1是为了防止除以零错误
					int[]lvc={color.ul0,color.ul1,color.ul2,color.ul3,color.ul4,color.ul5,color.ul6};
					ul.setBackgroundResource(lvc[lvl]);
					setText(name,j.get("name","(信息错误)"));
					setText(ul,"Lv."+lvl);
					setText(desc,j.get("sign","(信息错误)"));
				}catch(Throwable e){//NumberFormatException
				}
			}
		};
		loadUserRelation(uid);
		return 0;
	}
	void loadUserRelation(String uid){
		new Http("get","https://api.bilibili.com/x/relation/stat?vmid="+uid,"",""){
			@Override protected void onload(String data){
				FSON m=new FSON(data);
				if(m.canRead() && m.get("code",-1)==0){
					m=m.getObject("data");
					subs.setText("关注 "+m.get("following",0));
					fans.setText("粉丝 "+m.get("follower",0));
				}else{
					subs.setText("关注数:失败");
					fans.setText("粉丝数:失败");
				}
			}
		};
	}
	Http UserSpaceVideoListLoader;
	void loadUpzoneVideo(int page){
		if(page==1){ //加载第一页时
			lc.clear(); //刷新视频列表页，但是旋转圈可见
			if(UserSpaceVideoListLoader!=null) //可能之前有未加载完的网络请求
				UserSpaceVideoListLoader.cancel(true); //添加一个取消标签防止它之后更改界面
		}else if(UserSpaceVideoListLoader != null) return;
		UserSpaceVideoListLoader=new Http("GET",string("http://space.bilibili.com/ajax/member/getSubmitVideos?pagesize=20&page=",page,"&mid=",uid),mcok, ""){
			@Override protected void onload(String data){
				FSON j=new FSON(data);
				if(!j.canRead())return;
				if(!j.get("status",false))return;//data wrong, for example -101
				j=j.getObject("data");
				setText(vcount,"视频 "+j.get("count",0));
				
				j=j.getList("vlist");
				for(int i=0,len=j.length();i<len;i++){
					FSON d=j.getObject(i);
					Bean b=new Bean();
					b.title=d.get("title", "标题信息错误");
					b.count=d.get("play", 0);
					b.danmaku=d.get("video_review", 0);
					b.reply=d.get("comment", 0);
					b.length=d.get("length", "");
					b.date_int=d.get("created",0);
					b.date=formater.format(1000l*b.date_int);
					b.imageUrl=d.get("pic", "");
					b.token=d.get("aid",-1);
					lc.add(b);
				}
				lc.refresh();
				UserSpaceVideoListLoader=null;
			}
		};
	}
	static SimpleDateFormat formater=new SimpleDateFormat("yyyy-MM-dd");
	//自动把空间里的视频 翻页到指定时间
	class DateVideoSkipper implements MessageQueue.IdleHandler,Runnable {
		String base="正在根据日期翻页中，\n如需取消请再按一次",
			cancel_sys="没有更多数据，自动取消翻页",
			cancel_user="您已取消翻页。",
			finishd="已翻到指定页面。";
		Toast t=Toast.makeText(UpZone.this,base,1);
		long s;
		int start;
		boolean found;
		public DateVideoSkipper(long second){
			s=second;
			t.setGravity(Gravity.CENTER,0,0);
			Looper.myQueue().addIdleHandler(this);
		}
		public boolean isFinished(){ return found; }
		public void cancel(){
			found=true; t.setText(cancel_user); t.show();
		}//这里有个bug，无法取消操作
		@Override public boolean queueIdle(){
			if(found)return false;
			t.setText(base);
			t.show();
			for(int len=lc.getCount();start<len;start++){
				Bean b=lc.get(start);
				if(b.date_int<s){
					found=true;
					int first=l2.getFirstVisiblePosition();
					/*	start > first  如果是向上翻就直接定位吧，向下就另说
						Count - start  总项目数减去第一个匹配的项目的索引，小于单屏时平滑滚动，大于时直接定位
						LastVisible - FirstVisible  单屏幕的可见项目数
					*/
					if(start>first && lc.getCount()-start<l2.getLastVisiblePosition()-first){
						l2.setSelection(start);
						l2.smoothScrollToPosition(start+1);
					}else
						l2.setSelection(start+1);
					t.setText(finishd);
					t.show(); //到这里时，实际上已经完成了操作
				}
				if(found)return false; //已取消操作
			}
			l2.setSelection(start);
			l2.smoothScrollToPosition(start+1);
			found |= (l2.getCount()%20)>1;
			if(found){
				t.setText(cancel_sys);
				t.show();
			}
			return !found;
		}
		@Override public void run(){//这段是用来监听滚动中时，如果用户取消滚动，则停止滚动的
			/*now=l2.getScrollY();
			if(last!=0){ //数据记录
				if(canceled){
					if(last==now) return; //取消后看起来好像不再滚动了，退出
					l2.scrollTo(
				}
			}
			last=now; //覆写上次检测结果
			l2.postDelayed(this,1000);*/
		}
	}
	class Bean{
		static final int LAYOUT=layout.listsub_upzone_basic;
		int count, //表示视频播放次数
			danmaku, //表示视频弹幕量
			reply, //视频回复数
			like, //视频赞数
			dislike, //视频踩数
			token; //表示视频的id
		long date_int;
		String imageUrl, //表示文件夹或视频的封面url
			title, //表示视频的标题
			upname, //表示视频上传者的名称
			length, //视频长度
			date; //表示视频上传日期
	}
	class UserSpaceListControl extends BaseAdapter{
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
			if(v==null){
				v=inflateView(Bean.LAYOUT);
				ViewGroup w=(ViewGroup)v;
				d=new Holder();
				d.vtitle=fv(w,id.listsub_title);
				d.vcount=fv(w,id.listsub_playcount);
				d.vdanmaku=fv(w,id.listsub_danmakucount);
				d.vlength=fv(w,id.listsub_videolength);
				d.vdate=fv(w,id.listsub_submit_time);
				d.vimage=fv(w,id.listsub_v_img);
				v.setTag(d);
				seticon(fv(w,id.listsub_ic_slideshow),ic_sys(draw.ic_menu_slideshow));
				seticon(fv(w,id.listsub_ic_send),ic_sys(draw.ic_menu_send));
			}else{
				d=(Holder)v.getTag();
			}
			setText(d.vtitle,b.title); // 视频名字
			setText(d.vcount,String.valueOf(b.count)); // 视频播放次数
			setText(d.vdanmaku,String.valueOf(b.danmaku)); // 视频弹幕数量
			setText(d.vlength,b.length);
			setText(d.vdate,b.date);

			//此处设置图片链接
			return v;
		}
	}
	class Holder{
		View vtitle,
			vdate,
			vcount,
			vdanmaku,
			vlength,
			vimage;
		View reply,
			like,
			dislike;
	}
}
