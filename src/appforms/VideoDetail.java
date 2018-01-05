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
import leorchn.lib.*;
import android.content.*;
import static formevents.帐户数据.找cookie;
import java.util.*;
import static android.R.drawable.*;
import android.view.View.*;
import android.net.*;
import java.security.*;
import java.math.*;
import java.text.*;
public class VideoDetail extends Activity1 implements OnClickListener{
	Activity This; public Activity getContext(){return This;}
	String vid="-1",uid="",cookie="";
    protected void onCreate(Bundle sis) {
		This=this;
		addIdleHandler();
        super.onCreate(sis);
	}
	@Override public boolean onIdle() {
		switch(hasinit){case 0:
				setContentView(R.layout.activity_video_detail);
				vid=getIntent().getStringExtra("vid"); if(vid==null)vid="-1";
				cookie=getIntent().getStringExtra("cookie"); if(cookie==null)cookie="";
				videoid=((TextView)fv(id.videodetail_v_id));
				videoid.setText("av"+vid);break;
			case 1:
				title=(TextView)fv(id.videodetail_v_title);
				desc=(TextView)fv(id.videodetail_v_desc);
				auth=(TextView)fv(id.videodetail_auth_name);
				partcount=(TextView)fv(id.videodetail_videoscount);
				coin=(TextView)fv(id.videodetail_likes);
				star=(TextView)fv(id.videodetail_stars);
				review=(TextView)fv(id.videodetail_reviews);
				play=(TextView)fv(id.videodetail_plays);break;
			case 2:
				init();
		}
		hasinit++;
		return hasinit<9;
	}
	int hasinit=0;
	void init(){
		btnbind(id.videodetail_goback,id.videodetail_refresh,id.videodetail_gosetting, id.videodetail_goupzone,
				id.videodetail_share,id.videodetail_like,id.videodetail_star,id.videodetail_download,id.videodetail_review,
				id.videodetail_openlist,id.videodetail_playfirst);
		refreshBtn=(ImageButton)fv(id.videodetail_refresh);
		downloadtext=(TextView)fv(id.videodetail_downstate);
		submiTime=(TextView)fv(id.videodetail_submit_time);
		danCount=(TextView)fv(id.videodetail_danmaku_count);
		ls=(ListView)fv(id.videodetail_videolist);
		applyVideoInfo();
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
				startActivity(new Intent(this,UpZone.class) .putExtra("space",uid) .putExtra("cookie",cookie)); break;
			case id.videodetail_share: tip("客户端每日分享经验功能以后开放...");break;
			case id.videodetail_download: changeDownloadState(); break;
			case id.videodetail_playfirst: 播放视频(firstcid,0,new boolean[]{false}); break;
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
	PartsListControl plc;
	String firstcid="-1";
	TextView title,desc,auth,partcount,coin,star,review,play,submiTime,danCount;
	ListView ls; ImageButton refreshBtn;
	void applyVideoInfo(){
		if(vid=="-1")return;//↓获取视频概览
		new AsyncTask<Void,FSON,FSON>(){
			@Override protected FSON doInBackground(Void[] p1) {
				String data=http("GET", "https://api.bilibili.com/view?page=1&appkey=8e9fc618fbd41e28&id="+vid,cookie,"");
				FSON j=new FSON(data);
				if(j.canRead()){
					publishProgress(j);
					if(j.get("pages",1)>1){//视频有多个分段
						//↓获取视频多段信息
						String partsdata=http("GET", "http://www.bilibili.com/widget/getPageList?aid="+vid,cookie,"");
						return new FSON(partsdata);
					}
				}return null;
			}
			@Override protected void onProgressUpdate(FSON[]f){
				FSON j=f[0];
				title.setText(j.get("title","(视频标题获取失败)"));
				desc.setText(j.get("description","(视频没有说明)"));
				auth.setText(j.get("author","(未知up主)"));
				uid=j.get("mid","");
				coin.setText(j.get("coins","-1"));
				star.setText(j.get("favorites","-1"));
				review.setText(j.get("review","-1"));
				play.setText(j.get("play","-1"));
				partcount.setText("共 "+j.get("pages",-1)+" 段视频");
				submiTime.setText(j.get("created_at","(未知时间)"));
				danCount.setText(j.get("video_review","-1"));
				plc=new PartsListControl(This, ls){
					public void onItemClick(int idx, String itemtag,boolean[]setwatch) {if(downstate)下载视频(itemtag);else 播放视频(itemtag,idx,setwatch);}
				};
				FSON j2=j.getList("list").getObject(0);
				String cid=""+j2.get("cid",-1);
				firstcid=cid;
				plc.additem("【1】"+j2.get("part",""),"Cid: "+cid,cid);
			}
			@Override protected void onPostExecute(FSON j){
				if(j!=null){
				if(j.canRead()){
					//tip(this,j.length()+"");
					for(int i=1,len=j.length();i<len;i++){
						FSON onepart=j.getObject(i);
						String cid=""+onepart.get("cid",-1);
						plc.additem("【"+(i+1)+"】"+onepart.get("pagename",""),"Cid: "+cid,cid);
					}
					//分段加载成功，开始放置焦点。至于单段的，放不放焦点都无所谓，可拉倒吧
					int partprog=getIntent().getIntExtra("partprog",0);
					if(partprog>=plc.size()) partprog=plc.size()-1;
					ls.setSelection(partprog);//重载此activity之后，自动翻页到对应位置
				}else plc.additem("更多分段加载失败","重新进入本页面可重试加载","-1");
				}else{
					//网络连接失败或权限不足
					partcount.setText("视频信息加载失败");
					tip("网络连接失败 或 帐号权限不足");
					//信息框(this,"视频信息加载失败","请检查网络连接或帐号权限"+data,"ok");
				}//refresh.setImageResource(stat_notify_sync_noanim);
			}
		}.execute();
		 /*&& j.get("title","datadenied").equals("datadenied")*/
	}
	String getVideoUrl(final String cid){
		if(cid.equals("-1"))return"";
		setsload();
		final String[]dt={""};
		//↓获取视频地址
		Looper.prepare();
		ProgressDialog pd=ProgressDialog.show(VideoDetail.this,"","解析视频地址...",true,true);
		new AsyncTask<Void,Void,Void>(){
			//@Override protected void onPreExecute(){}
			@Override protected Void doInBackground(Void[] p1) {
				String prms = "cid="+cid+"&player=1&quality="+(sets.get("quality",0)+1/*参数只接受1到3，设置值0对应参数1，设置值1对应参数3*/)+"&ts="+(System.currentTimeMillis()/1000),
					chksum = toMD5(prms+"1c15888dc316e05a15fdd0a02ed6584f"),
					url = "http://interface.bilibili.com/playurl?"+prms+"&sign="+chksum.toLowerCase();
					dt[0]=http("GET",url,cookie+"\nX-Forwarded-For: 220.181.111.81\nClient-IP: 220.181.111.81","");
				return null;
			}
			
			@Override protected void onPostExecute(Void v){
				Looper.myLooper().quit();
			}
		}.execute();
		Looper.loop();
		pd.dismiss();
		String data=dt[0];
		XML x=new XML(data);
		XML[]xl={};
		x=x.get("video"); if(x==null)return"";
		xl=x.getList("durl"); if(xl.length==0)return"";
		x=xl[0].get("url");
		xl=xl[0].getList("backup_url");
		switch(sets.get("playport",0)){
			case 0: return x==null?data:x.text();//自动选择主节点
			case 1: return xl.length==0?data:xl[0].text();//自动选择首个备用节点
			case 2: default:
				ArrayList<String>dl=new ArrayList<String>();
				if(x instanceof XML)dl.add(x.text());
				if(xl.length>0)
					for(XML xli:xl)
						dl.add(xli.text());
				String[]datapack=dl.toArray(new String[]{});
				int choose=列表信息框(this,"选择播放地址",datapack);
				if(choose==-1)return"";
				return datapack[choose];
		}
	}
	void 播放视频(String cid,int index,boolean[]settowatched){
		if(cid.equals("-1"))return;
		boolean notgeturl=false; int checksumretry=sets==null?3:sets.get("checksumretry",3);//判断null临时补救措施
		String data="";
		do{//先获取一次看看能不能有正确的数据
			data=getVideoUrl(cid);
			if(!data.startsWith("http")){
				String errmsg="视频地址解析错误，请重试。";
				FSON code=null;
				if(data.startsWith("{") && (code=new FSON(data).getObject("video"))!=null)
					switch(code.get("code",0)){
						case -5006: //checksum error: 所提交的参数校验出错
							if(checksumretry>0){
								notgeturl=true;
								checksumretry--;
								multip("Checksum重试剩余 "+checksumretry);
								continue;
							}break;
						case -5016: //no dispatch info: 该段视频没有对应清晰度
							errmsg="该视频不支持此清晰度，请切换清晰度。"; break;//此处还可制作“画质自动”“省流自动”
					}
				new Msgbox("解析错误",errmsg+"其返回的调试信息是\na"+vid+"c"+cid+data,"ok");
				return;//到达此处，说明该错误已严重到无法自动处理的地步，直接拒绝播放
			}else break;
		}while(notgeturl);
		switch(sets.get("playeronline",0)){
			case 0:
				startActivity(new Intent(This,VideoPlaySimple.class).putExtra("path",data).putExtra("vid",vid).putExtra("cid",cid).putExtra("cookie",cookie).putExtra("history",true));
				settowatched[0]=true;
				getIntent().putExtra("partprog",index);//此处写入播放进度，重载activity时使用
				break;
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
	String toMD5(String s){
		try{
			return new BigInteger(1,MessageDigest.getInstance("MD5").digest(s.getBytes("utf-8"))).toString(16);
		}catch(Exception e){}
		return"";
	}
}
