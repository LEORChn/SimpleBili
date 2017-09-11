package simplebili.lib;

import android.app.*;
import android.content.*;
import android.net.*;
import android.view.*;
import android.widget.*;
import appforms.*;
import com.LEORChn.SimpleBili.*;
import java.io.*;
import java.util.*;
import leorchn.lib.*;
import static leorchn.lib.Global.*;
public class FeedListControl implements View.OnClickListener{
	Activity Super;void tip(String s){Toast.makeText(Super,s,0).show();}
	ListView l; SimpleAdapter adapter; FeedListControl This; String cookie;
	ImageView a_head,v_head;
	List<Map<String,Object>>list= new ArrayList<Map<String, Object>>();
	List<View[]>picbox=new ArrayList<View[]>();
	int listStyle(){
		FSON j=new FSON(文本.读取(Super.getFilesDir().getPath()+"/app.setting"));
		return j.canRead()? j.get("feedliststyle",0): 0;
	}
	public FeedListControl(Activity Ths,ListView imp,String cok){
		l=imp; Super=Ths; This=this; cookie=cok;
		String[]compName=null;int[]compId=null;int layout=0;
		switch(listStyle()){
			case 2://标准列表
				layout=R.layout.listsub_video_feeds;
				compName=new String[]{"autimg","autname","vimg","vtitle","vplayed","vdan"};
				compId=new int[]{R.id.listsub_auth_img,R.id.listsub_auth_name,R.id.listsub_v_img,R.id.listsub_v_title,R.id.listsub_v_played,R.id.listsub_v_dan};
				break;
			default://简单列表
				layout=R.layout.listsub_video_feeds_basic;
				compName=new String[]{"autname","pubtime","vtitle","vplayed","vdan"};
				compId=new int[]{R.id.listsub_auth_name,R.id.listsub_submit_time,R.id.listsub_v_title,R.id.listsub_v_played,R.id.listsub_v_dan};
		}
		adapter=new SimpleAdapter(Ths,list,layout,compName,compId)
			{public View getView(final int p,View cv,ViewGroup vg){
				View v=super.getView(p,cv,vg);
				//if(v.getId()==R.id.listsub_videofeeds){
				v.setTag(list.get(p).get("vid"));
				v.setOnClickListener(This);
				/*new View.OnClickListener(){public void onClick(View p1) {
							String vid=(String)list.get(p).get("vid");//跳转到视频详情页面
							tip("av"+vid);
							Intent it=new Intent(Super,VideoDetail.class)
							.putExtra("vid",vid)
							.putExtra("cookie",cookie);
							Super.startActivity(it);
						}});*/
				ImageView cover=(ImageView)v.findViewById(R.id.listsub_v_img);
				View vib=v.findViewById(R.id.listsub_auth_img);
				if(vib==null)vib=v.findViewById(R.id.listsub_auth_name);
				vib.setTag(list.get(p).get("auth"));
				vib.setOnClickListener(This);/*new View.OnClickListener(){public void onClick(View p1) {
					String authid=(String)list.get(p).get("auth");//跳转到up主空间页面
					tip("space"+authid);
				}});*/
				if(list.size()>picbox.size())picbox.add(new View[]{cover,vib});
				return v;
		}};
		l.setAdapter(adapter);
	}
	@Override public void onClick(View v) {switch(v.getId()){
		case R.id.listsub_videofeeds:
			tip("准备加载 av"+v.getTag());
			Intent it=new Intent(Super,VideoDetail.class)
				.putExtra("vid",(String)v.getTag())
				.putExtra("cookie",cookie);
			Super.startActivity(it); break;
		case R.id.listsub_auth_name:
			tip("准备加载 space"+v.getTag());
			tip("还没有做出来读取UP主空间功能");break;
		}
	}
	public void additem(String auth,String pubtime,String video,String cover,String played,String danmakued,String auth_id,String v_id){
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("autimg", android.R.drawable.ic_menu_report_image);
        map.put("autname", auth);
		map.put("pubtime",pubtime);
        map.put("vimg", android.R.drawable.ic_menu_report_image);
        map.put("vtitle", video);
        map.put("vplayed",played);
		map.put("vdan",danmakued);
		map.put("vid",v_id);
		map.put("auth",auth_id);
		map.put("coverurl",cover);
        list.add(map);
    }
	public void refresh(){ adapter.notifyDataSetChanged(); }
	public void clear(){
		list.clear();
		refresh();
	}
	public void sethead(int pos,String path){ setpic(pos,path,1); }
	public void setcover(int pos,String path){ setpic(pos,path,0); }
	void setpic(int pos,String path,int type){
		try{
			File f=new File(path);
			if(f.exists())((ImageView)picbox.get(pos)[type]).setImageURI(Uri.fromFile(f));
		}catch(Exception e){}
	}
	public String debug(){
		String s=picbox.toString();
		return s;
	}
	public String getvid(int pos){
		return(String)list.get(pos).get("vid");
	}
	public String getuid(int pos){
		return(String)list.get(pos).get("auth");
	}
	public ListView getMainView(){return l;}
	public int size(){ return list.size(); }
	public boolean visible(){return l.getVisibility()==View.VISIBLE;}
	public void visible(final boolean visible){l.setVisibility(visible?View.VISIBLE:View.GONE);}
	
}
