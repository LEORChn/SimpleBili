package leorchn.lib;
import android.app.*;
import android.os.*;
import android.content.*;
import android.view.*;

public class Service1 extends Service {
	@Override public IBinder onBind(Intent p1){ return null; }
	
	protected static String string(Object...str){
		StringBuilder bdr=new StringBuilder();
		for(Object s:str) bdr.append(s);
		return bdr.toString();
	}
	
	protected class Msgbox extends AlertDialog.Builder implements DialogInterface.OnClickListener{
		public Msgbox(String...msgs){
			super(Service1.this); 
			for(int i=0,len=msgs.length;i<len;i++){
				switch(i){
					case 0: setTitle(msgs[0]); break;
					case 1: setMessage(msgs[1]); break;
					case 2: setPositiveButton(msgs[2],this); setCancelable(false); break;
					case 3: setNegativeButton(msgs[3],this); break;
					case 4: setNeutralButton(msgs[4],this); break;
				}
			}
			AlertDialog ad=create();
			ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			ad.show();
		}
		public void onClick(DialogInterface p1,int p2){ onClick(p2); }
		void onClick(int i){}
	}
}
