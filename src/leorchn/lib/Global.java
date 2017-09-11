package leorchn.lib;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import appforms.*;
import java.io.*;
import java.lang.reflect.*;

import static simplebili.App.getContext;

public class Global{
	public static FSON sets;
	public static String setspath(){return getContext().getFilesDir().getPath()+"/app.setting";}
	public static String setsload(){sets=new FSON(文本.读取(setspath())); return sets.toString();}
//	public static <E extends Object>E setsget(String key,E def){return sets.get(key,def);}
//	public static <E extends Object>E setsget(int key,E def){return sets.get(key,def);}
	public static boolean setssave(){ return setssave(sets.toString()); }
	public static boolean setssave(String value){ return 文本.写出(value,getContext().getFilesDir().getPath()+"/app.setting","UTF8"); }
	
	static int statheight;
	static Toast pub=Toast.makeText(getContext(),"",0);
	public static void multip(String s){pub.setText(s);pub.show();}
	public static void tip(String s){Toast.makeText(getContext(),s,s.length()<10?0:1).show();}
	public static void enableMenuIcon(Menu menu){
		if(android.os.Build.VERSION.SDK_INT<14)return;
		try {
			Class clz=Class.forName("com.android.internal.view.menu.MenuBuilder");
			Method m=clz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);
			m.setAccessible(true); m.invoke(menu, true);
		} catch (Exception e) {}
	}
	public static void 复制文本(String s){
		ClipboardManager cm=(ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
		cm.setPrimaryClip(ClipData.newPlainText("Label",s));
	}
	public static int 取状态栏高度(){
		try {
			Class C=Class.forName("com.android.internal.R$dimen");
			if(statheight==0)statheight=
				Main_Feeds.getContext().getResources().getDimensionPixelSize(
					Integer.parseInt(C.getField("status_bar_height").get(C.newInstance()).toString())
				);
		} catch (Exception e) {}
		return statheight;
	}
	static int[]scr;
	public static int 取屏幕高度(){calcScr();return scr[1];}
	public static int 取屏幕宽度(){calcScr();return scr[0];}
	static void calcScr(){
		if(scr==null || scr[0]==0){
			try{
			DisplayMetrics dm=new DisplayMetrics();
			Main_Feeds.getContext().getWindowManager().getDefaultDisplay().getMetrics(dm);
			scr=new int[]{dm.widthPixels,dm.heightPixels};
			}catch(Exception e){scr=new int[]{0,0};}
		}
	}
	public static void 压缩图片2(String from,String to,int level){
		BitmapFactory.Options bfo=new BitmapFactory.Options();
		//bfo.inJustDecodeBounds=true;
		//BitmapFactory.decodeFile(from,bfo);
		bfo.inJustDecodeBounds=false;
		int parts=(int)Math.floor((double)bfo.outHeight/level);
		if(parts<1)parts=1;
		bfo.inSampleSize=parts;
		Bitmap bm=BitmapFactory.decodeFile(from,bfo);
		try{
			FileOutputStream fos=new FileOutputStream(new File(to));
			if(!bm.compress(Bitmap.CompressFormat.JPEG,100,fos))return;
			fos.flush();
			fos.close();
		}catch(Exception e){}
	}
	static ProgressDialog pd;
	public static void 显示进度对话框(String msg){
		pd=ProgressDialog.show(Main_Feeds.getContext(),"",msg,true,false);
	}
	public static void 关闭进度对话框(){if(pd !=null)pd.dismiss();pd=null;}
	static void ExitDialog(final int i){new t(){void r(){ throw new ExitDialog(i); }};}
	static class ExitDialog extends RuntimeException{static int result=0;ExitDialog(int r){result=r;}ExitDialog(){}}
	public static void 信息框(String title,String msg,String oktext){信息框(Main_Feeds.getContext(),title,msg,oktext);}
	public static void 信息框(final Activity a,final String title,final String msg,final String oktext){
		new t(){void r() {
			new AlertDialog.Builder(a).setCancelable(false)
				.setTitle(title).setMessage(msg)
				.setPositiveButton(oktext, new Dialog.OnClickListener(){
					public void onClick(DialogInterface p1,int p2){ throw new ExitDialog(); }
				}).show();
			try{
				Looper.getMainLooper(); Looper.loop();
			}catch(ExitDialog e){}
		}};
	}
	public static int 信息框2(String title,String msg,String oktext,String canceltext){return 信息框2(Main_Feeds.getContext(),title,msg,oktext,canceltext);}
	public static int 信息框2(final Activity a,final String title,final String msg,final String oktext,final String canceltext){
		new t(){void r(){
			new AlertDialog.Builder(a).setCancelable(false)
				.setTitle(title).setMessage(msg)
				.setPositiveButton(oktext, new Dialog.OnClickListener(){
					public void onClick(DialogInterface p1,int p2){ ExitDialog(0); }
				}).setNegativeButton(canceltext,new Dialog.OnClickListener(){
					public void onClick(DialogInterface p1,int p2){ ExitDialog(1); }
			}).show();
		}};
		try{
			Looper.getMainLooper(); Looper.loop();
		}catch(ExitDialog e){}
		return ExitDialog.result;
	}
	public static void 附加功能(Activity a){
		int r=列表信息框(a,"启动附加功能","直播等级挂机助手");
		switch (r) {
			case 0:
				String cook=formevents.帐户数据.cookie();
				if (cook.equals("")) {
					tip("请先进入主界面登录后再使用此功能");
				} else {
					a.startService(new Intent(a, LiveUpgrade.class).putExtra("cookie", cook)); 
				}break;
		}
	}
	public static int 列表信息框(final Activity a,final String title,final String...options){
		new t(){void r(){
			ExitDialog.result=-1;
			new AlertDialog.Builder(a).setCancelable(true).setTitle(title).setItems(options, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface p1,final int p2){ ExitDialog(p2); }
			}).setOnCancelListener(new DialogInterface.OnCancelListener(){
				public void onCancel(DialogInterface p1){ if(ExitDialog.result==-1)ExitDialog(-1); }
			}).show();
		}};
		try{
			Looper.getMainLooper().loop();
		}catch(ExitDialog e){}
		return ExitDialog.result;
	}
	
	abstract static class t{
		abstract void r();
		public t(){
			new Handler(Looper.getMainLooper()).post(new Runnable(){public void run(){r();}});
		}
	}
}
