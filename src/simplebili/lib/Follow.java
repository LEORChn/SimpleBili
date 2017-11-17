package simplebili.lib;
import java.util.*;
import android.os.*;
import leorchn.lib.*;

public class Follow{
	//public static void accessUpzone(String uid){}
	/* OnFollow / UnFollow */
	public static boolean follow(String uid,String cookie){ return process(uid,true,cookie); }
	public static boolean unfollow(String uid,String cookie){ return process(uid,false,cookie); }
	public static void addOnFollowListener(OnFollowListener obj){ if(!fls.contains(obj))fls.add(obj); }
	public static void removeOnFollowListener(OnFollowListener obj){ if(fls.contains(obj))fls.remove(obj); }
	public static void check(final String uid,final int justnowdid,final String cookie){
		new AsyncTask<String,Void,Integer>(){//检查关注状态，并推送到所有
			protected Integer doInBackground(String[]p) {
				for(int i=0;i<2;i++){ //就查两次
					try{ Thread.sleep(2000); }catch(Exception e){}
					String data=网络.getdata("GET","http://api.bilibili.com/x/relation?fid="+uid,cookie,"");
					FSON j=new FSON(data);
					if(j.canRead() && j.get("code",-1)==0)//此步是校验所返回的数据是否具有有效性
						switch(justnowdid){
							case -1://刚刚没尝试关注或取关，只是查询一下
								return j.getObject("data").get("attribute",-1);
							case 0:case 2://刚刚尝试关注某人或取关
								if(j.getObject("data").get("attribute",-1)==justnowdid)
									return justnowdid;
						}
				}
				return -1;//-1不推送
			}
			@Override protected void onPostExecute(Integer r){
				if(r>=0 && r<=2) //0=false; 1,2=true; 得到0则推送这个uid没关注；1则表示悄悄关注，与2一同推送这个uid已关注
					for(int i=0,len=fls.size();i<len;i++)
						fls.get(i).onFollowStatChange(uid,r!=0);
			}
		}.execute(cookie);
	}
	static ArrayList<OnFollowListener>fls=new ArrayList<OnFollowListener>();
	public interface OnFollowListener{ void onFollowStatChange(String uid,boolean stat) }
	static long lastprocess=0;
	static boolean process(final String uid,final boolean tostat,final String cookie){
		if(lastprocess+15000>System.currentTimeMillis())return false;else lastprocess=System.currentTimeMillis();//15秒内只能调用一次关注和取关
		new AsyncTask<String,Void,Integer>(){
			protected Integer doInBackground(String[]p) {
				if(!cookie.contains("bili_jct="))return -2;//cookie没有这个值？别提了
				String[]c2=cookie.split(";");
				for(String c3:c2)
					if(c3.contains("bili_jct=")){
						c2=c3.split("bili_jct=");
						break;//分离完毕
					}
				String data=网络.getdata("POST","http://api.bilibili.com/x/relation/modify",cookie,"fid="+uid+"&act="+(tostat?"1":"2")+"&re_src=11&csrf="+c2[1].trim());
				return new FSON(data).canRead()?1:-1;
			}
			@Override protected void onPostExecute(Integer r){
				if(r>=0)check(uid,tostat?2:0,cookie);
			}
		}.execute(cookie);
		return true;
	}
	/* End / Follow */
}
