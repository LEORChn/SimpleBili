/* Created by LEOR_Chn
 * License Copyright(R) LEOR_Chn (2014-2017)
 * LEOR_Chn Soft.
 */
package leorchn.lib;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.widget.*;
import appforms.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import leorchn.App;

public class HttpRequest{
	public static String http(String method,String url,String reqHeader,String formdata){
		String v8="";//方法：GET、POST等
		try{
			InputStream s=getdataInputStream(method,url,reqHeader,formdata);
			ByteArrayOutputStream outstream = new ByteArrayOutputStream();
			byte[] readbyte = new byte[1024];
			int readcount;
			while(true){
				if((readcount=s.read(readbyte)) == -1)break;
				outstream.write(readbyte, 0, readcount);
			}
			v8 = new String(outstream.toByteArray(), "UTF8");
			outstream.close();
			s.close();
		}catch(Exception e){
			return E.trace(e);
		}
		return v8;
	}
	public static InputStream getdataInputStream(String method,String url,String reqHeader,String formdata){
		InputStream s=null;
		try{
			HttpURLConnection h=(HttpURLConnection)new URL(url).openConnection();
			h.setRequestMethod(method.toUpperCase());
			h.setDoOutput(!formdata.isEmpty());//DoOutput：主体发送开关，如果没有主体则关闭
			h.setConnectTimeout(5000);
			for(String 分条:reqHeader.split("\n"))//自动分割请求头并逐条添加。注意：请不要故意添加错误的请求头
				if(分条.contains(":")){String[]dat=分条.split(":",2);//sys.o.pl("addRequestHead:"+分条.split(": ")[0].trim()+","+分条.split(": ")[1].trim());
					h.setRequestProperty(dat[0].trim(),dat[1].trim());}

			if(!formdata.isEmpty()) h.getOutputStream().write(formdata.getBytes("UTF8"));
			switch(h.getResponseCode()){
				case HttpURLConnection.HTTP_OK:
				case HttpURLConnection.HTTP_CREATED:
				case HttpURLConnection.HTTP_ACCEPTED:
					s = h.getInputStream();
					break;
				default:
					s = h.getErrorStream();
			}//不能在此关闭输入流，会造成错误
		}catch(Exception e){ s=new ByteArrayInputStream(E.trace(e).getBytes()); }
		return s;
	}
	public static void 下载文件(String 网络文件,String 文件名,Runnable 下载完成时执行){下载文件(网络文件,文件名,下载完成时执行,true);}
	public static void 下载文件(String 网络文件,String 文件名,Runnable 下载完成时执行,boolean 覆盖文件){
		try{
			Context c=App.getContext();//创建下载任务
			java.io.File f=c.getExternalCacheDir();f.mkdir();
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(网络文件));
			request.setAllowedOverRoaming(false);//指定下载路径和下载文件名
			request.setDestinationInExternalPublicDir("/Android/data/"+c.getPackageName()+"/cache/", 文件名);
			if(覆盖文件) if(new java.io.File(f.getPath()+"/"+文件名).exists()) f.delete();
			//获取下载管理器，将下载任务加入下载队列，否则不会进行下载
			final long id=((DownloadManager)c.getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(request);
			注册下载广播(c,id,下载完成时执行);
		}catch(Exception e){}
	}
	static HashMap<Long,BroadcastReceiver>广播接收器=new HashMap<Long,BroadcastReceiver>();
	public static void 注册下载广播(final Context 广播注册者,final long id,final Runnable 下载完成时执行){
		广播接收器.put(id,new BroadcastReceiver(){
			public void onReceive(Context arg0, Intent 行为包) {
				if(行为包.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)==id){
					广播注册者.unregisterReceiver(广播接收器.remove(id));
					if (下载完成时执行 !=null)下载完成时执行.run();
					System.gc();
				}
			}
		});
		IntentFilter 行为筛选器 = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		广播注册者.registerReceiver(广播接收器.get(id), 行为筛选器);
	}
	public static String URL解码(String text,String charset){
		try {
			return URLDecoder.decode(text, charset);
		}catch(Exception e) {
			return "";
		}
	}
	public static String deflateDecoder(InputStream is){
		try{
			byte[]v0=new byte[1024],buffer=new byte[4096];
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			int v4;
			while(true){
				if((v4 = is.read(v0)) == -1)break;
				outputStream.write(v0, 0, v4);
			}
			is.close();//必须在此处才能关闭输入流
			Inflater inflater = new Inflater(true);
			inflater.setInput(outputStream.toByteArray());

			outputStream = new ByteArrayOutputStream(outputStream.toByteArray().length);
			while(!inflater.finished()){
				int count = inflater.inflate(buffer);
				outputStream.write(buffer, 0, count);
			}
			byte[] output = outputStream.toByteArray();
			outputStream.close();
			return new String(output, "UTF-8");
		}catch(Throwable e){
			return E.trace(e);
		}
    }
	static void tip(String s){Toast.makeText(App.getContext(),s,0).show();}
	abstract static class t{
		abstract void r();
		public t(){
			new Handler(Looper.getMainLooper()).post(new Runnable(){public void run(){r();}});
		}
	}
}
