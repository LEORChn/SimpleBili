package leorchn.lib;
import android.os.AsyncTask;
import java.io.*;
import java.net.*;
public abstract class Download extends AsyncTask<String,Integer,Object>{
	protected Download(){}//如果要继承构造方法
	public Download(String file,String method,String url,String header,String formdata){
		executeOnExecutor(THREAD_POOL_EXECUTOR,file,method,url,header,formdata);
	}
	@Override protected Object doInBackground(String[]p){
		try{ pl("DLing file:",p[2]);
			File f=new File(p[0]);
			if(!f.getParentFile().exists())f.getParentFile().mkdirs();
			boolean isupdate=!f.createNewFile();//tag to prepare for update
			long usable=f.getUsableSpace();pl("isupdate? "+isupdate);
			if(usable==0)return new Exception("save path check fail:\npath: "+f.getAbsolutePath()+"\nusable space:"+usable);//fail: save path is not usable
			InputStream s;
			HttpURLConnection h=(HttpURLConnection)new URL(p[2]).openConnection();
			h.setRequestMethod(p[1].toUpperCase());
			h.setDoOutput(!p[4].isEmpty());//DoOutput：主体发送开关，如果没有主体则关闭
			h.setConnectTimeout(5000);
			for(String 分条:p[3].split("\n"))//自动分割请求头并逐条添加。注意：请不要故意添加错误的请求头
				if(分条.contains(":")){String[]dat=分条.split(":",2);//sys.o.pl("addRequestHead:"+分条.split(": ")[0].trim()+","+分条.split(": ")[1].trim());
					h.setRequestProperty(dat[0].trim(),dat[1].trim());}

			if(!p[4].isEmpty()) h.getOutputStream().write(p[4].getBytes("UTF8"));
			switch(h.getResponseCode()){
				case HttpURLConnection.HTTP_OK:
				case HttpURLConnection.HTTP_CREATED:
				case HttpURLConnection.HTTP_ACCEPTED:
					s = h.getInputStream();
					break;
				default:
					s = h.getErrorStream();
			}
			int filelen=h.getContentLength(),
				readcount,total=0;
			if(usable < filelen) return new Exception("usable space is not enough:\n"+usable+" / "+filelen);//判断可用空间。如果获取不到大小则filelen为-1，仍然继续下载
			if(isupdate && filelen > 0 && f.length()==filelen){//缓存中是否有该文件
				return f;//直接取出缓存中的文件
			}
			ByteArrayOutputStream outstream = new ByteArrayOutputStream();
			byte[] readbyte = new byte[1024];
			while(true){
				if((readcount=s.read(readbyte)) == -1)break;
				outstream.write(readbyte, 0, readcount);
				total+=readcount;
				publishProgress(total,filelen);
			}
			s.close();//关闭网络输入流
			readbyte=outstream.toByteArray();
			outstream.close();//关闭读得输出流
			FileOutputStream fos=new FileOutputStream(f);
			fos.write(readbyte);
			fos.close();//关闭文件输出流
			return f;
		}catch(Exception e){return e;}
	}
	@Override protected void onProgressUpdate(Integer[]p){ progress(p[0],p[1]); }
	@Override protected void onPostExecute(Object f){ 
		if(f instanceof Throwable) fail((Throwable)f);
		else if(f instanceof File)done((File)f);
		else pl("download.java - unknown object type: "+f);
		pl("download.java - so this is download result");
	}
	void progress(int...p){}
	abstract void done(File f)
	abstract void fail(Throwable t)
	private void pl(String...s){for(String st:s)System.out.println(st);}
}
