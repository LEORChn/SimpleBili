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

import static leorchn.lib.Activity1.*;
import static simplebili.App.getContext;

public class Global {
		
	static int statheight;
	public static void multip(String s){mainhandler.obtainMessage(1000,s).sendToTarget();}
	public static void tip(String s){mainhandler.obtainMessage(1001,s).sendToTarget();}
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
	public static Handler mainhandler=new Handler(Looper.getMainLooper()){
		Toast pub=Toast.makeText(getContext(),"",0);
		@Override public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
				case 0:tip("简哔提示：\n"+msg.obj); break;
				//case 1:信息框(((String[])msg.obj)[0],((String[])msg.obj)[1],"OK");break;
				case 2:((Runnable)msg.obj).run(); break;
				case 1000: pub.setText(msg.obj.toString());pub.show(); break; //multip
				case 1001: String s=msg.obj.toString();Toast.makeText(getContext(),s,s.length()<10?0:1).show(); break; //tip
			}
		} 
	};
}
