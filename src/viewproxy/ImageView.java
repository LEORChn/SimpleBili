package viewproxy;
import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import leorchn.lib.*;

public class ImageView extends android.widget.ImageView implements Runnable{
	public ImageView(Context c){ super(c); }
	public ImageView(Context c,AttributeSet attrs){ super(c,attrs); }
	public ImageView(Context c,AttributeSet attrs,int defStyle){ super(c,attrs,defStyle); }
	//以上 保留构造器以确保兼容性
	public void loadUser(String imageUrl){ loadImage(imageUrl,0); }
	public void loadCover(String imageUrl){ loadImage(imageUrl,1); }
	public void loadImage(String imageUrl,int mode){
		url=imageUrl; mod=mode;
		loadNetworkImage();
	}
	void loadNetworkImage(){
		if(isNowLoad)
			switch(mod){
				case 0:
					Icon.user(this,url);
					break;
				case 1:
					Icon.cover(this,url);
			}
	}
	String url=null;
	int mod=0;
	boolean imageSeted=false,
		isNowLoad=false;
	Bitmap using=null;
	static int count=0;
	//以下 系统事件控制
	@Override protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
		//onVisibilityChanged(null,visibility);
	}

	@Override protected void onVisibilityChanged(View changedView, int visibility) {
		if(changedView!=null) super.onVisibilityChanged(changedView, visibility);
		isNowLoad=visibility==VISIBLE;
		if(url==null)return;
		if(isNowLoad){
			loadNetworkImage();
			count++;
		}else{
			setImageBitmap(null);
			//post(this); // po个屁，直接在这个线程写
			//using.recycle();
			//using=null;
			count--;
		}Global.multip("now "+count);
	}
	@Override public void run(){
		
	}
	//以上 系统事件控制
	//以下 劫持宿主事件
	@Override public void setImageBitmap(Bitmap bm){
		if(bm==null && using!=null) using.recycle(); // 这种代码只适合一次性使用bitmap，如果该bitmap有多个引用则会造成问题
		super.setImageBitmap(bm);
		imageSeted=bitmapUsable(bm); // 如果以后有其他的加载方式，就覆盖其他的方法并加上这个
		using=bm;
	}
	boolean bitmapUsable(Bitmap b){
		return b!=null && !b.isRecycled();
	}
	//@Override protected void 
}
