package leorchn.lib;
import android.graphics.*;
import java.util.*;
import java.io.*;
import android.net.Uri;
import simplebili.App;

public class Icon{
	static com.LEORChn.SimpleBili.R.drawable draw;
//----- bitmap factory options storage
	static BitmapFactory.Options
	icon16=icon16(),
	jpg=jpg();
//----- bitmap storage
	public static Bitmap
	img_def = x24(draw.img_def),//表示默认图片/图片占位符
	nav_back = x24(draw.topmenu_back),//表示向左箭头
	nav_cancel = x24(draw.topmenu_cancel),//表示关闭界面
	nav_setting = x24(draw.topmenu_setting),//表示进入设置界面
	nav_plugin = x24(draw.topmenu_extmodule),//表示启动附加组件
	video_play_count = x16(draw.ic_menu_slideshow),//列表中表示播放数量
	video_danmaku_count = x16(draw.ic_menu_send),//列表中表示弹幕数量
	video_play = x24(draw.vdetail_play),//表示开始播放
	video_danmaku_send = video_danmaku_count,//暂时引用同一个图片
	star = x24(draw.topmenu_star),
	stared = x24(draw.topmenu_stared);
//----- bitmap users' storage
	static HashMap<Integer,Bitmap>users=new HashMap<>();
	public static Bitmap user(int mid){ try{ return users.get(mid); }catch(Throwable e){ return img_def; } }
	public static void putusr(final int mid,String url){
		if(mid==0) users.put(mid,process(draw.ic_anyuser,jpg,48,48));//表示隐身模式
		else{
			String path=url2path(url);
			if(new File(path).exists()){
				users.put(mid,process(path,jpg,32,32));
			}else{
				users.put(mid,img_def);
				new Download(path,"get",url,"",""){
					@Override void done(File f) {
						users.put(mid, process(f.getAbsolutePath(),jpg,32,32));
					}
					@Override void fail(Throwable t){}
				};
			}
		}
	}
//----- file loads
	public static String url2path(String url){
		String[]fn=url.split("/");
		return Activity1.DIR_cache_pic+fn[fn.length-1];
	}
//----- bitmap processor area
	static Bitmap x16(int i){ return process(i,icon16,16,16); }
	static Bitmap x24(int i){ return process(i,icon16,24,24); }
	static Bitmap x32(int i){ return process(i,icon16,32,32); }
	
	static Bitmap process(int resid,BitmapFactory.Options opt,int width,int height){//使用自定配置初始化包内图标
		Bitmap origin=BitmapFactory.decodeResource(App.getContext().getResources(),resid,opt);
		return process(origin,width,height);
	}
	static Bitmap process(String path,BitmapFactory.Options opt,int width,int height){//使用自定配置初始化已下载的图片
		Bitmap origin=BitmapFactory.decodeFile(path,opt);
		return process(origin,width,height);
	}
	static Bitmap process(Bitmap orig,int width,int height){//处理为位图的缩小版本
		//if(orig.getWidth()>width && orig.getHeight()>height)orig.reconfigure(width,height,orig.getConfig());
		Bitmap b=Bitmap.createScaledBitmap(orig,width,height,true);
		orig.recycle();
		return b;
	}
//----- bitmap factory options init
	static BitmapFactory.Options icon16(){
		BitmapFactory.Options b=new BitmapFactory.Options();
		//b.inMutable=true;//设置为可降低大小
		b.inPreferredConfig=Bitmap.Config.ARGB_8888;//单像素4字节，红绿蓝透明度 均可分256级
		b.inPurgeable = true;//允许系统自动回收
		b.inInputShareable = true;//共享输入接口（需要允许系统回收）
		return b;
	}
	static BitmapFactory.Options jpg(){
		BitmapFactory.Options b=new BitmapFactory.Options();
		//b.inMutable=true;//设置为可降低大小
		b.inPreferredConfig=Bitmap.Config.RGB_565;//单像素双字节，不透明，红蓝分32级，绿色分64级
		b.inPurgeable = true;//允许系统自动回收
		b.inInputShareable = true;//共享输入接口（需要允许系统回收）
		return b;
	}
}
