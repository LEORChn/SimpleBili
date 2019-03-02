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
	ScrollView l; /*Edit*/UpZoneListControl This; String cookie;
	ImageView a_head,v_head;
	List<RegTable>regs= new ArrayList<RegTable>();//在空间视频列表中保存的隐式信息
	List<View>picbox=new ArrayList<View>();
	LinearLayout root;//空间视频列表的底层界面
	ViewGroup infoarea;//主播基本信息的底层界面
	List<View>listsub=new ArrayList<View>();
	public UpZoneListControl/*Edit*/(Activity Ths,ScrollView imp,String cok){
		l=imp; Super=Ths; This=this; cookie=cok;
		String[]compName=null;int[]compId=null;int layout=0;
		//root=(LinearLayout)l.findViewById(R.id.ScrollViewRoot);
		infoarea=(ViewGroup) LayoutInflater.from(Super).inflate(R.layout.listsub_upzone_info,null);
		root.addView(infoarea);
//		switch(0){
//			default://简单列表
//				layout=R.layout.listsub_upzone_basic;/*Edit*/
//				compName=new String[]{"len","time","vtitle","vplayed","vdan","vcomm"};/*Edit*/
//				compId=new int[]{R.id.listsub_videolength,R.id.listsub_submit_time,R.id.listsub_v_title,R.id.listsub_v_played,R.id.listsub_v_dan,R.id.listsub_v_comment};
//		}
		
	}
//		adapter=new SimpleAdapter(Ths,list,layout,compName,compId)
//		{public View getView(final int p,View cv,ViewGroup vg){
//				View v=super.getView(p,cv,vg);
//				//if(v.getId()==R.id.listsub_videofeeds){
//				v.setTag(list.get(p).get("vid"));
//				v.setOnClickListener(This);
//				/*new View.OnClickListener(){public void onClick(View p1) {
//				 String vid=(String)list.get(p).get("vid");//跳转到视频详情页面
//				 tip("av"+vid);
//				 Intent it=new Intent(Super,VideoDetail.class)
//				 .putExtra("vid",vid)
//				 .putExtra("cookie",cookie);
//				 Super.startActivity(it);
//				 }});*/
//				ImageView cover=(ImageView)v.findViewById(R.id.listsub_v_img);
//				if(list.size()>picbox.size())picbox.add(cover);
//				return v;
//			}};
//		l.setAdapter(adapter);
//	}
	@Override public void onClick(View v) {switch(v.getId()){
			case R.id.listsub_videofeeds:
				int idx=listsub.indexOf(v);
				multip("准备加载 av"+regs.get(idx).vid);
				Intent it=new Intent(Super,VideoDetail.class)
					.putExtra("vid",regs.get(idx).vid)
					.putExtra("cookie",cookie);
				Super.startActivity(it); break;
		}
	}
	public void additem(String length,String subtime,String title,String cover,String played,String danmakued,String comment,String v_id){
        ViewGroup ni=(ViewGroup) LayoutInflater.from(Super).inflate(R.layout.listsub_upzone_basic,null);
		
		//Map<String, Object> map = new HashMap<String, Object>();
		RegTable map=new RegTable("",v_id,cover);
		//map.put("len",length);
		((TextView)fv(R.id.listsub_videolength,ni)).setText(length);
		//map.put("time",subtime);
		((TextView)fv(R.id.listsub_submit_time,ni)).setText(subtime);
        //map.put("vimg", android.R.drawable.ic_menu_report_image);
        //map.put("vtitle", title);
		((TextView)fv(R.id.listsub_v_title,ni)).setText(title);
        //map.put("vplayed",played);
		((TextView)fv(R.id.listsub_v_played,ni)).setText(played);
		//map.put("vdan",danmakued);
		((TextView)fv(R.id.listsub_v_dan,ni)).setText(danmakued);
		//map.put("vcomm",comment);
		((TextView)fv(R.id.listsub_v_comment,ni)).setText(comment);
		//map.put("vid",v_id);
		//map.put("coverurl",cover);
        regs.add(map);//
		listsub.add(ni);
		ni.setOnClickListener(this);
		root.addView(ni);
    }
	class RegTable{
		public RegTable(String...it){
			vimg=it[0];
			vid=it[1];
			coverurl=it[2];
		}
		String vimg,vid,coverurl;
	}
	/*public Object findObjByVid(String vid,String objkey){
		for(int i=0,max=regs.size();i<max;i++){
			if(regs.get(i).vid.equals(vid))
				return map.get(objkey);
		}
		return null;
	}*/
	//public void refresh(){ adapter.notifyDataSetChanged(); }
	public void clear(){
		root.removeViews(1,listsub.size());
		regs.clear();
		listsub.clear();
		//refresh();
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
	public int getCount(){ return listsub.size(); }
	public String getvid(int pos){
		return regs.get(pos).vid;
	}
	/*public String getuid(int pos){
		return(String)regs.get(pos).get("auth");
	}*/
	public View getInfoArea(){return infoarea;}
	public ScrollView getMainView(){return l;}
	
	public int size(){ return regs.size(); }
	public boolean visible(){return l.getVisibility()==View.VISIBLE;}
	public void visible(final boolean visible){l.setVisibility(visible?View.VISIBLE:View.GONE);}
	View fv(int id,ViewGroup vg){return vg.findViewById(id);}
}
