package simplebili.lib;
import android.app.*;
import android.view.*;
import android.widget.*;
import com.LEORChn.SimpleBili.*;
import java.util.*;

public abstract class PartsListControl implements View.OnClickListener{
	Activity Super;void tip(String s){Toast.makeText(Super,s,0).show();}
	PartsListControl This;
	ListView l; SimpleAdapter adapter;
	List<Map<String,Object>>list= new ArrayList<Map<String, Object>>();
	public PartsListControl(Activity Ths,ListView imp){
		l=imp; Super=Ths; This=this;
		adapter=new SimpleAdapter(Super,list,R.layout.listsub_classic,
								  new String[]{"title","desc"},
								  new int[]{R.id.listsub_title,R.id.listsub_desc})
		{public View getView(final int p,View cv,ViewGroup vg){
				View v=super.getView(p,cv,vg);
				v.setTag(p);
				v.setOnClickListener(This);/*new View.OnClickListener(){public void onClick(View p1) {
							This.onItemClick(p,(String)list.get(p).get("itemtag"));
						}});*/
				return v;
			}};
		l.setAdapter(adapter);
	}
	public void onClick(View v){
		onItemClick((int)v.getTag(),(String)list.get((int)v.getTag()).get("itemtag"));
	}
	public void additem(String title,String desc,String itemtag){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("title", title);
		map.put("desc", desc);
		map.put("itemtag", itemtag);
		list.add(map);
		adapter.notifyDataSetChanged();
	}
	public void clear(){
		list.clear();
		adapter.notifyDataSetChanged();
	}
	public String getTitle(int pos){
		try{
			return(String)list.get(pos).get("title");
		}catch(Exception e){}
		return "";
	}
	public ListView getMainView(){return l;}
	public int size(){ return list.size(); }
	abstract public void onItemClick(int idx,String itemtag);
}
