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
public class UpZoneListControl implements View.OnClickListener{
	Activity Super;void tip(String s){Toast.makeText(Super,s,0).show();}
	ListView l; SimpleAdapter adapter; /*Edit*/UpZoneListControl This; String cookie;
	ImageView a_head,v_head;
	List<Map<String,Object>>list= new ArrayList<Map<String, Object>>();
	List<View>picbox=new ArrayList<View>();
	public UpZoneListControl/*Edit*/(Activity Ths,ListView imp,String cok){
		l=imp; Super=Ths; This=this; cookie=cok;
		String[]compName=null;int[]compId=null;int layout=0;
		switch(0){
			default://简单列表
				layout=R.layout.listsub_upzone_basic;/*Edit*/
				compName=new String[]{"len","time","vtitle","vplayed","vdan"};/*Edit*/
				compId=new int[]{R.id.listsub_videolength,R.id.listsub_submit_time,R.id.listsub_v_title,R.id.listsub_v_played,R.id.listsub_v_dan};
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
				if(list.size()>picbox.size())picbox.add(cover);
				return v;
			}};
		l.setAdapter(adapter);
	}
	@Override public void onClick(View v) {switch(v.getId()){
			case R.id.listsub_videofeeds:
				multip("准备加载 av"+v.getTag());
				Intent it=new Intent(Super,VideoDetail.class)
					.putExtra("vid",(String)v.getTag())
					.putExtra("cookie",cookie);
				Super.startActivity(it); break;
		}
	}
	public void additem(String length,String subtime,String title,String cover,String played,String danmakued,String v_id){
        Map<String, Object> map = new HashMap<String, Object>();
		map.put("len",length);
		map.put("time",subtime);
        map.put("vimg", android.R.drawable.ic_menu_report_image);
        map.put("vtitle", title);
        map.put("vplayed",played);
		map.put("vdan",danmakued);
		map.put("vid",v_id);
		map.put("coverurl",cover);
        list.add(map);
    }
	public Object findObjByVid(String vid,String objkey){
		Map<String, Object> map;
		for(int i=0,max=list.size();i<max;i++){
			map=list.get(i);
			if(map.get("vid").equals(vid))
				return map.get(objkey);
		}
		return null;
	}
	public void refresh(){ adapter.notifyDataSetChanged(); }
	public void clear(){
		list.clear();
		refresh();
	}
	public void setcover(int pos,String path){ setpic(pos,path); }
	void setpic(int pos,String path){
		try{
			File f=new File(path);
			if(f.exists())((ImageView)picbox.get(pos)).setImageURI(Uri.fromFile(f));
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
