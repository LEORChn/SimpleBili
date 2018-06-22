package leorchn.lib;
import android.graphics.*;
import java.util.*;
import java.io.*;
import android.net.Uri;
import simplebili.*;
import simplebili.lib.*;
import android.view.*;
import android.widget.*;

public class Icon{
	static com.LEORChn.SimpleBili.R.drawable draw;
//----- bitmap factory options storage
	static BitmapFactory.Options
	icon16=icon16(),
	jpg=jpg();
//----- system icon bitmap storage
	static HashMap<Integer,Bitmap>sys=new HashMap<>();
	public static Bitmap sys(int id){
		if(!sys.containsKey(id)) sys.put(id,x24(id));
		return sys.get(id);
	}
//----- users' bitmap storage
	static HashMap<Integer,Bitmap>users=new HashMap<>();
	public static void user(final View v,final int id){
		String url=User.getHead(id),
			path=url2path(url);
		if(users.containsKey(id))
			((ImageView)v).setImageBitmap(users.get(id));
		else if(new File(path).exists()){
			users.put(id,process(path,jpg,32,32));
			((ImageView)v).setImageBitmap(users.get(id));
		}else{
			users.put(id,sys(draw.img_def));
			((ImageView)v).setImageBitmap(users.get(id));
			new Download(path,"get",url,"",""){
				@Override void done(File f) {
					users.put(id, process(f.getPath(),jpg,32,32));
					((ImageView)v).setImageBitmap(users.get(id));
				}
				@Override void fail(Throwable t){}
			};
		}// sys.put(id,x24(id));
	}
//----- temporary bitmap storage
	public static void user(View v,String url){ tempLoader(v,url,24,24); }
	public static void cover(View v,String url){ tempLoader(v,url,64,36); }
	static void tempLoader(final View v,String url,final int width,final int height){
		String path=url2path(url,"temp/");
		if(new File(path).exists()){
			((ImageView)v).setImageBitmap(process(path,jpg,width,height));
		}else{
			((ImageView)v).setImageBitmap(sys(draw.img_def));
			new Download(path,"get",url,"",""){
				@Override void done(File f) {
					((ImageView)v).setImageBitmap(process(f.getPath(),jpg,width,height));
				}
				@Override void fail(Throwable t){}
			};
		}
	}
//----- file loads
	public static String url2path(String url){ return url2path(url,null); }
	public static String url2path(String url,String secondDir){
		String[]fn=url.split("/");
		return Activity1.DIR_cache_pic+(secondDir==null?"":secondDir)+fn[fn.length-1];
	}
//----- bitmap processor area
	static Bitmap x16(int i){ return process(i,icon16,16,16); }
	static Bitmap x24(int i){ return process(i,icon16,24,24); }
	static Bitmap x32(int i){ return process(i,icon16,32,32); }
	
	static Bitmap process(int resid,BitmapFactory.Options opt,int width,int height){//使用自定配置初始化包内图标
		opt.inJustDecodeBounds=true;
		BitmapFactory.decodeResource(App.getContext().getResources(),resid,opt);
		opt.inJustDecodeBounds=false;
		process(opt,width,height);
		return BitmapFactory.decodeResource(App.getContext().getResources(),resid,opt); 
	}
	static Bitmap process(String path,BitmapFactory.Options opt,int width,int height){//使用自定配置初始化已下载的图片
		opt.inJustDecodeBounds=true;
		BitmapFactory.decodeFile(path,opt);
		opt.inJustDecodeBounds=false;
		process(opt,width,height);
		return BitmapFactory.decodeFile(path,opt);
	}
	static double process(BitmapFactory.Options opt,int width,int height){//处理为位图的缩小版本
		//if(orig.getWidth()>width && orig.getHeight()>height)orig.reconfigure(width,height,orig.getConfig());
		opt.inSampleSize=(int)Math.ceil(((double)opt.outWidth)/width);
		return ((double)opt.outWidth)/opt.outHeight;
	}
//----- bitmap factory options init
	static BitmapFactory.Options icon16(){
		BitmapFactory.Options b=new BitmapFactory.Options();
		//b.inMutable=true;//设置为可降低大小
		b.inPreferredConfig=Bitmap.Config.ARGB_4444;//单像素4字节，红绿蓝透明度 均可分256级
		b.inPurgeable = true;//允许系统自动回收
		b.inInputShareable = true;//共享输入接口（需要允许系统回收）
		//b.inScaled = true;
		return b;
	}
	static BitmapFactory.Options jpg(){
		BitmapFactory.Options b=new BitmapFactory.Options();
		//b.inMutable=true;//设置为可降低大小
		b.inPreferredConfig=Bitmap.Config.RGB_565;//单像素双字节，不透明，红蓝分32级，绿色分64级
		b.inPurgeable = true;//允许系统自动回收
		b.inInputShareable = true;//共享输入接口（需要允许系统回收）
		//b.inScaled = true;
		return b;
	}
}
