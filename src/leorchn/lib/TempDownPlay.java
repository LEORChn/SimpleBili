package leorchn.lib;
import android.app.*;
import android.content.*;
import android.net.*;
import appforms.*;
import java.io.*;
import java.util.*;
import static leorchn.lib.Global.*;
public class TempDownPlay{//边下边播
	static long id=0;
	static DownloadManager dm;
	static File vf;
	public static String start(Context c,String url,Map<String,String>headers){
		try{
			File f=c.getExternalCacheDir(); f.mkdir();
			DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
			Iterator<String>itr=headers.keySet().iterator();
			while(itr.hasNext()){
				String key=itr.next();
				req.addRequestHeader(key,headers.get(key));
			}
			//req.setNotificationVisibility(req.VISIBILITY_HIDDEN);
			req.setAllowedOverRoaming(false);//指定下载路径和下载文件名
			req.setDestinationInExternalPublicDir("/Android/data/" + c.getPackageName() + "/cache/","temp.mp4");
			if(vf==null) vf=new File(f.getPath() + "/temp.mp4");
			if(vf.exists()) vf.delete();
			if(dm==null){
				dm=((DownloadManager)c.getSystemService(Context.DOWNLOAD_SERVICE));
			}else{
				if(id!=0)end();//如果不使用此代码，将会出现一个奇葩的bug
			}
			id=dm.enqueue(req);
			return vf.getPath();
		}catch(Exception e){
			//((Activity1)c).Msgbox("DownloadMgr Error",Arrays.toString(e.getStackTrace()),"ok");
		}
		return "";
	}
	public static void end(){
		if(dm!=null && id!=0){
			dm.remove(id);
			id=0;
			vf.delete();
		}
	}
}
