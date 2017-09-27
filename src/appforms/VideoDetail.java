package appforms;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.LEORChn.SimpleBili.*;
import static com.LEORChn.SimpleBili.R.id;
import java.io.*;
import leorchn.lib.*;
import simplebili.lib.*;
import static leorchn.lib.Global.*;
import static leorchn.lib.WidgetOverride.*;
import leorchn.lib.CrashHandlerReg;
import android.content.*;
import static formevents.帐户数据.找cookie;
import java.util.*;
import static android.R.drawable.*;
import android.view.View.*;
import android.net.*;
import java.security.*;
import java.math.*;
public class VideoDetail extends Activity implements OnClickListener,MessageQueue.IdleHandler{
	Activity This; public Activity getContext(){return This;}
	String vid="-1",uid="",cookie="";
    protected void onCreate(Bundle sis) {
        super.onCreate(sis);
		CrashHandlerReg.reg(this);
		This=this;
		Looper.myQueue().addIdleHandler(this);
	}
	@Override public boolean queueIdle() {
		switch(hasinit){case 0:
				setContentView(R.layout.activity_video_detail);
				vid=getIntent().getStringExtra("vid"); if(vid==null)vid="-1";
				cookie=getIntent().getStringExtra("cookie"); if(cookie==null)cookie="";
				videoid=((TextView)fv(id.videodetail_v_id));
				videoid.setText("av"+vid);break;
			case 1:init();
		}
		hasinit++;
		return hasinit<2;
	}
	int hasinit=0;
	void init(){
		btnbind(id.videodetail_goback,id.videodetail_refresh,id.videodetail_gosetting, id.videodetail_goupzone,
				id.videodetail_share,id.videodetail_like,id.videodetail_star,id.videodetail_download,id.videodetail_review,
				id.videodetail_openlist,id.videodetail_playfirst);
		ImageButton refreshBtn=(ImageButton)fv(id.videodetail_refresh);
		downloadtext=(TextView)fv(id.videodetail_downstate);
		applyVideoInfo(
			(TextView)fv(id.videodetail_v_title),
			(TextView)fv(id.videodetail_v_desc),
			(TextView)fv(id.videodetail_auth_name),
			(TextView)fv(id.videodetail_videoscount),
			(TextView)fv(id.videodetail_likes),
			(TextView)fv(id.videodetail_stars),
			(TextView)fv(id.videodetail_reviews),
			(TextView)fv(id.videodetail_plays),
			(ListView)fv(id.videodetail_videolist),
			refreshBtn
		);
	}
	public void onClick(View v){switch(v.getId()){
			case id.videodetail_goback: finish(); break;
			case id.videodetail_refresh:
				if(hasinit==2) new android.os.Handler().postDelayed(new Runnable(){public void run(){init();hasinit=2;}},2000);
				hasinit=1;
				((ImageButton)v).setImageResource(ic_popup_sync); break;
			case id.videodetail_gosetting: startActivity(new Intent(This,Settings.class)); break;
			case id.videodetail_goupzone:
				tip("准备加载 space"+uid);
				Intent uz=new Intent(this,UpZone.class)
					.putExtra("space",uid)
					.putExtra("cookie",cookie);
				startActivity(uz); break;
			case id.videodetail_share: tip("客户端每日分享经验功能以后开放...");break;
			case id.videodetail_download: changeDownloadState(); break;
			case id.videodetail_playfirst: 播放视频(firstcid); break;
			case id.videodetail_openlist:
				View basicinf=fv(id.videodetail_basicinfo);
				if (basicinf != null) visible(basicinf, !visible(basicinf)); break;
			default: tip("这个东东还不能点...");
	}}
	boolean downstate=false; TextView downloadtext,videoid;
	void changeDownloadState(){
		downstate=!downstate;
		if(downstate){
			downloadtext.setText("播放");
			videoid.setText(videoid.getText()+" (缓存模式)");
		}else{
			downloadtext.setText("缓存");
			videoid.setText(((String)videoid.getText()).split(" ")[0]);
		}
	}
	String firstcid="-1";
	void applyVideoInfo(TextView t,TextView d,TextView a,TextView c,TextView likecoin,TextView favo,TextView reviews,TextView plays,ListView ls,ImageButton refresh){
		//tip("loading... vid= "+vid);
		if(vid=="-1")return;//↓获取视频概览
		String data=网络.获得数据("GET", "https://api.bilibili.com/view?page=1&appkey=8e9fc618fbd41e28&id="+vid,cookie,"");
		FSON j=new FSON(data);
		if(j.canRead() /*&& j.get("title","datadenied").equals("datadenied")*/){
			t.setText(j.get("title","(视频标题获取失败)"));
			d.setText(j.get("description","(视频没有说明)"));
			a.setText(j.get("author","(未知up主)"));
			uid=j.get("mid","");
			likecoin.setText(j.get("coins","-1"));
			favo.setText(j.get("favorites","-1"));
			reviews.setText(j.get("review","-1"));
			plays.setText(j.get("play","-1"));
			c.setText("共 "+j.get("pages",-1)+" 段视频");
			PartsListControl l=new PartsListControl(this, ls){
				public void onItemClick(int idx, String itemtag) {if(downstate)下载视频(itemtag);else 播放视频(itemtag);}
			};
			FSON j2=j.getList("list").getObject(0);
			String cid=""+j2.get("cid",-1);
			firstcid=cid;
			l.additem("【1】"+j2.get("part",""),"Cid: "+cid,cid);
			if(j.get("pages",1)>1){//视频有多个分段
				//信息框(this,"检测到多个分段","即将开始加载","ok");//↓获取视频多段信息
				String partsdata=网络.获得数据("GET", "http://www.bilibili.com/widget/getPageList?aid="+vid,cookie,"");
				j=new FSON(partsdata);
				//信息框(this,"",partsdata,"ok");
				if(j.canRead()){
					//tip(this,j.length()+"");
					for(int i=1,len=j.length();i<len;i++){
						FSON onepart=j.getObject(i);
						cid=""+onepart.get("cid",-1);
						l.additem("【"+(i+1)+"】"+onepart.get("pagename",""),"Cid: "+cid,cid);
					}
					//分段加载成功，开始放置焦点。至于单段的，放不放焦点都无所谓，可拉倒吧
					int partprog=getIntent().getIntExtra("partprog",0);
					if(partprog>=l.size()) partprog=l.size()-1;
					ls.setSelection(partprog);
				}else{
					l.additem("更多分段加载失败","重新进入本页面可重试加载","-1");
				}
			}
			
		}else{
			//网络连接失败或权限不足
			c.setText("视频信息加载失败");
			tip("网络连接失败 或 帐号权限不足");
			信息框(this,"视频信息加载失败","请检查网络连接或帐号权限"+data,"ok");
		}
		refresh.setImageResource(stat_notify_sync_noanim);
	}
	String getVideoUrl(String cid){
		if(cid.equals("-1"))return"";
		setsload();
		String prms = "cid="+cid+"&player=1&quality="+(sets.get("quality",0)*2 +1/*参数只接受1和3，设置值0对应参数1，设置值1对应参数3*/)+"&ts="+(System.currentTimeMillis()/1000),
			chksum = toMD5(prms+"1c15888dc316e05a15fdd0a02ed6584f"),
			url = "http://interface.bilibili.com/playurl?"+prms+"&sign="+chksum.toLowerCase();
		//↓获取视频地址
		String data=网络.获得数据(this,"解析视频地址...","GET",url,cookie+"\nX-Forwarded-For: 220.181.111.81\nClient-IP: 220.181.111.81","");
		FSON j=FSON.fromXML(data);
		j=j.getObject("video"); if(j==null)return"";//此时大概已经说明获取到的是错误的数据
		j=j.getList("durl")==null?j.getObject("durl"):j.getList("durl").getObject(0);
		switch(sets.get("playport",0)){
			case 0:
				try{ data=j.get("url",""); }catch(Exception e){ return FSON.fromXML(data).toString(); }
				break;//自动选择主节点//偶发BUG:空指针错误
			case 1:data=j.getList("backup_url").get(0,"");break;//自动选择首个备用节点
			case 2:default:
				ArrayList<String>dl=new ArrayList<String>();
				dl.add(j.get("url",""));
				j=j.getObject("backup_url");//try{
				if(j.getList("url")==null){
					dl.add(j.get("url",""));
				}else{
					for(int i=0,last=j.length();i<last;i++)
						dl.add(j.getList("url").get(i,""));

				}//}catch(Exception e){信息框2("error",j.toString()+"\n\nfollow origin:"+Arrays.toString(e.getStackTrace()),"ok","cancel");}
				String[]datapack=dl.toArray(new String[]{});
				int choose=列表信息框(this,"选择播放地址",datapack);
				if(choose==-1)return"";
				data=datapack[choose];
		}return data;
	}
	void 播放视频(String cid){
		if(cid.equals("-1"))return;
		String data=getVideoUrl(cid);
		if(!data.startsWith("http")){
			信息框(this,"解析错误","视频地址解析错误，请重试。其返回的调试信息是\na"+vid+"c"+cid+data,"ok");
			return;}//错误的数据，拒绝播放
		switch(sets.get("player",0)){
			case 0: startActivity(new Intent(This,VideoPlaySimple.class).putExtra("path",data).putExtra("cid",cid)); break;
			case 2: startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(data))); break;//todo: send url
			case 3: //复制到剪贴板
				复制文本(data);
				tip("视频地址已复制");break;
			case 1:default: //选择第三方播放器
				startActivity(new Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(data),"video/*"));
		}
	}
	void 下载视频(String cid){
		if(cid.equals("-1"))return;
		String data=getVideoUrl(cid);
		if(!data.startsWith("http"))return;//错误的数据，拒绝下载
		tip("还没有下载功能...");
	}
	void btnbind(int...id){for(int btnid:id)btnbind(btnid);}
	void btnbind(int id){fv(id).setOnClickListener(this);}
	View fv(int id){return findViewById(id);}
	String toMD5(String s){
		try{
			return new BigInteger(1,MessageDigest.getInstance("MD5").digest(s.getBytes("utf-8"))).toString(16);
		}catch(Exception e){}
		return"";
	}
}
