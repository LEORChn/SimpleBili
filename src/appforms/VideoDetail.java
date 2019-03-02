package appforms;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import leorchn.lib.*;
import simplebili.lib.*;
import static leorchn.lib.Global.*;
import android.content.*;
import java.util.*;
import static android.R.drawable.*;
import android.view.View.*;
import android.net.*;
import java.security.*;
import java.math.*;
import java.text.*;
public class VideoDetail extends Activity1 implements OnClickListener{
	String vid="-1",uid="";
    protected void oncreate(){}
	@Override public boolean onIdle(){ switch(hasinit){
			case 0:
				setContentView(layout.activity_video_detail);
				Intent i=getIntent();
				int x=i.getIntExtra("vid",0);
				vid=x==0?
					i.getStringExtra("vid"):
					x+"";
				if(vid==null)vid="-1";
				setText(fv(id.videodetail_v_id),"av"+vid);
				setText(fv(id.videodetail_v_title),i.getStringExtra("title"));
				setText(fv(id.videodetail_v_desc),i.getStringExtra("desc"));
				setText(fv(id.videodetail_auth_name),i.getStringExtra("name"));
				x=i.getIntExtra("plays",0);
				setText(fv(id.videodetail_plays),x>0?x+"":i.getStringExtra("plays"));
				x=i.getIntExtra("danmaku",0);
				setText(fv(id.videodetail_danmaku_count),x>0?x+"":i.getStringExtra("danmaku"));
				break;
			case 1:
				btnbind(id.videodetail_goback,id.videodetail_refresh,id.videodetail_gosetting, id.videodetail_goupzone,
						id.videodetail_like,id.videodetail_coin,id.videodetail_star,id.videodetail_download,id.videodetail_share,
						id.videodetail_review,id.videodetail_playfirst);
				downloadtext=(TextView)fv(id.videodetail_downstate);
				ls=(ViewGroup)fv(id.videodetail_videolist);
				initVideoInfo();
		}
		hasinit++;
		return hasinit<9;
	}
	int hasinit=0;

	String firstcid="-1";
	ViewGroup ls;
	public void onClick(View v){switch(v.getId()){
			case id.videodetail_goback: finish(); break;
			case id.videodetail_refresh:
				if(!visible(fv(id.videodetail_loading))){
					restartloading();
					initVideoInfo();
				}
				break;
			case id.videodetail_gosetting: startActivity(new Intent(this,Settings.class)); break;
			case id.videodetail_goupzone:
				startActivity(new Intent(this,UpZone.class).putExtra("space",uid)); break;
			case id.videodetail_share: tip("客户端每日分享经验功能以后开放...");break;
			case id.videodetail_download: changeDownloadState(); break;
			case id.videodetail_playfirst: playVideo(firstcid,0); break;
			case id.listsub_bg: playVideo((String)v.getTag(),fv((ViewGroup)v,id.listsub_title).getTag());break;
				
			default: tip("这个东东还不能点...");
	}}
	boolean downstate=false; TextView downloadtext;
	void changeDownloadState(){
		downstate=!downstate;
		if(downstate){
			downloadtext.setText("播放");
		}else{
			downloadtext.setText("缓存");
		}
	}

	void initVideoInfo(){
		ls.removeAllViews();
		loadVideoInfo();
	}
	void loadVideoInfo(){
		if(vid.equals("-1"))return;//↓获取视频概览
		new Http("GET","https://api.bilibili.com/view?page=1&appkey=8e9fc618fbd41e28&id="+vid,mcok,""){
			@Override protected void onload(String d){
				FSON j=new FSON(d);
				if(j.canRead()){
					showVideoInfo(j);pl(j.get("pages",-1));
					if(j.get("pages",1)>1)//视频有多个分段
						loadMoreParts();//获取视频多段信息
					else
						finishloading();
				}
			}
		};
	}
	void showVideoInfo(FSON j){
		setText(fv(id.videodetail_v_title),j.get("title", "(视频标题获取失败)"));
		setText(fv(id.videodetail_v_desc),j.get("description", "(视频没有说明)"));
		setText(fv(id.videodetail_auth_name),j.get("author", "(未知up主)"));
		setText(fv(id.videodetail_videoscount),string("共 ",j.get("pages",-1)," 段视频"));
		setText(fv(id.videodetail_coins),j.get("coins", "-1"));
		setText(fv(id.videodetail_stars),j.get("favorites", "-1"));
		setText(fv(id.videodetail_reviews),j.get("review", "-1"));
		setText(fv(id.videodetail_plays),j.get("play", "-1"));
		setText(fv(id.videodetail_submit_time),j.get("created_at", "(未知时间)"));
		setText(fv(id.videodetail_danmaku_count),j.get("video_review", "-1"));
		uid = j.get("mid", "");

		FSON j2=j.getList("list").getObject(0);
		String cid="" + j2.get("cid", -1);
		firstcid = cid;
		showMoreParts(1, j2.get("part", ""), cid);
	}
	void loadMoreParts(){
		new Http("GET", "https://www.bilibili.com/widget/getPageList?aid="+vid,mcok,""){
			@Override protected void onload(String d){
				FSON j=new FSON(d);
				if(j.canRead()){
					for(int i=1,len=j.length();i<len;i++){
						FSON onepart=j.getObject(i);
						String cid=""+onepart.get("cid",-1);
						showMoreParts(i+1,onepart.get("pagename",""),cid);
					}
					//分段加载成功，开始放置焦点。至于单段的，放不放焦点都无所谓，可拉倒吧
					int partprog=getIntent().getIntExtra("partprog",0);
					//todo
					//if(partprog>=plc.size()) partprog=plc.size()-1;
					//ls.setSelection(partprog);//重载此activity之后，自动翻页到对应位置
				}//todo://else plc.additem("更多分段加载失败","重新进入本页面可重试加载","-1");
				finishloading();
			}
		};
	}
	void showMoreParts(int index,String title,String cid){
		ViewGroup v=inflateView(layout.listsub_classic);
		View t=fv(v,id.listsub_title);
		setText(t,string("【",index,"】",title));
		setText(fv(v,id.listsub_desc),"Cid: "+cid);
		v.setTag(cid);
		t.setTag(index);
		ls.addView(v);
		btnbind(v);
	}
	void restartloading(){//控制左上加载圈
		visible(fv(id.videodetail_loading),true);
	}
	void finishloading(){
		visible(fv(id.videodetail_loading),false);
	}
	void playVideo(String cid,int index){
		getVideoUrlProxy(cid,1);
	}
	void downVideo(String cid){
		getVideoUrlProxy(cid,2);
		//if(!data.startsWith("http"))return;//错误的数据，拒绝下载
		tip("还没有下载功能...");
	}
	void getVideoUrlProxy(String cid,int intentId){
		if(cid.equals("-1"))return;
		int checksumretry=sets==null?3:sets.get("checksumretry",3);//判断null临时补救措施
		getVideoUrl(cid,intentId,checksumretry);
	}
	void getVideoUrl(final String cid,final int intentId,final int chanceCount){//获取视频地址//请勿直接调用此函数，由proxy进入
		VideoDecryptor.start(this, vid, cid);
		if(0==0) return;
		// =============================
		final ProgressDialog pd=ProgressDialog.show(VideoDetail.this,"","解析视频地址...",true,true);
		String[]qns={"1","25","50","100"};
		int qn=sets.get("quality",0);
		String quality=qns[qn],
			prms = string("appkey=84956560bc028eb7&cid=",cid,"&otype=json&qn=",quality,"&quality=",quality,"&type=",qn==0?"mp4":"flv"),
			seckey = "94aba54af9065f71de72f5508f1cd42e",
			chksum = Crypt.md5(prms+seckey),
			url = "http://interface.bilibili.com/v2/playurl?"+prms+"&sign="+chksum.toLowerCase();
		new Http("get",url,mcok,""){
			@Override protected void onload(String data){
				pd.dismiss();
				FSON j=new FSON(data);
				if(j.canRead()){
					String url="";
					String[]bakurl={};
					if(j.get("code",0)==10002)
						if(retry()) return;
					j=j.getList("durl");
					try{
						if(j.length()>1) tip("视频切割过多，只能播放部分");
						j=j.getObject(0);
						url=j.get("url","");
						j=j.getList("backup_url");
						bakurl=new String[j.length()];
						for(int i=0,len=bakurl.length;i<len;i++)
							bakurl[i]=j.get(i,"");
					}catch(Throwable e){ if(retry()) return; }
					switch(sets.get("playport",0)){
						case 0: onGotUrl(url,cid,intentId); break;//自动选择主节点
						case 1: onGotUrl(bakurl[0],cid,intentId); break;//自动选择首个备用节点
						case 2: default:
							ArrayList<String>dl=new ArrayList<String>();
							dl.add(url);
							if(bakurl.length>0)
								for(String surl:bakurl)
									dl.add(surl);
							String[]datapack=dl.toArray(new String[]{});
							int choose=列表信息框(VideoDetail.this,"选择播放地址",datapack);
							if(choose==-1) return;//此处由用户取消，无需重试
							onGotUrl(datapack[choose],cid,intentId);
					}
				}else
					retry();
			}
			boolean retry(){
				multip("Checksum重试剩余 "+chanceCount);
				if(chanceCount>0) getVideoUrl(cid,intentId,chanceCount-1);
				return true;
			}
		};
	}
	void onGotUrl(String url,String cid,int intentId){
		switch(intentId){
			case 1://播放
				switch(sets.get("playeronline",0)){
					case 0:
						startActivity(new Intent(this,VideoPlaySimple.class).putExtra("path",url).putExtra("vid",vid).putExtra("cid",cid).putExtra("history",true));
						getIntent().putExtra("partprog",cid);//此处写入播放进度，重载activity时使用
						break;
					case 2: startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url))); break;//todo: send url
					case 3: //复制到剪贴板
						复制文本(url);
						tip("视频地址已复制");break;
					case 1:default: //选择第三方播放器
						startActivity(new Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(url),"video/*"));
				}
				break;
			case 2://下载
				
		}
	}
}
